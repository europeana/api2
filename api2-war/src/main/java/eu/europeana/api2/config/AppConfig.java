package eu.europeana.api2.config;

import eu.europeana.api2.v2.schedule.SugarCRMPollingScheduler;
import eu.europeana.api2.v2.service.SugarCRMCache;
import eu.europeana.api2.v2.service.SugarCRMImporter;
import eu.europeana.api2.v2.utils.ApiKeyUtils;
import eu.europeana.features.ObjectStorageClient;
import eu.europeana.features.S3ObjectStorageClient;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

/**
 * @author Willem-Jan Boogerd (www.eledge.net/contact).
 */
@Configuration
@ImportResource({
        "classpath:corelib-db-context.xml",
        "classpath:corelib-solr-context.xml",
        "classpath:corelib-utils-context.xml",
        "classpath:corelib-web-context.xml",
        "classpath:spring-sugarcrmclient.xml"
})
@EnableScheduling
@PropertySource("classpath:europeana.properties")
public class AppConfig {

    private static final Logger LOG = LogManager.getLogger(AppConfig.class);

    private static final String APP_NAME_IN_POSTGRES = "PostgreSQL JDBC Driver";
    private static final String QUERY_FILTER_STALE_SESSION =
                    " AND state in ('idle', 'idle in transaction', 'idle in transaction (aborted)', 'disabled')" +
                    " AND current_timestamp - state_change > interval '5 minutes'";

    @Value("${s3.key}")
    private String key;
    @Value("${s3.secret}")
    private String secret;
    @Value("${s3.region}")
    private String region;
    @Value("${s3.bucket}")
    private String bucket;

    @Value("${postgres.max.stale.sessions:}")
    private Integer pgMaxStaleSessions;

    @Resource(name = "corelib_db_dataSource")
    private DataSource postgres;

    @Autowired
    private Environment env;

    @PostConstruct
    public void logConfiguration() {
        LOG.info("Active Spring profiles:" + Arrays.toString(env.getActiveProfiles()));
        LOG.info("Default Spring profiles:" + Arrays.toString(env.getDefaultProfiles()));

        // log to which db we are connected
        try (Connection con = postgres.getConnection()) {
            String dbUrl = con.getMetaData().getURL();
            if (dbUrl.contains("password")) {
                dbUrl = dbUrl.substring(0, dbUrl.indexOf("password"));
            }
            LOG.info("Connected to " + dbUrl);
        } catch (SQLException e) {
            LOG.error("Error checking database connection", e);
        }

        LOG.info("Default Postgres Datasource settings (or settings from CF environment):");
        LOG.info("  getAbandonWhenPercentageFull() = {}", this.postgres.getAbandonWhenPercentageFull());
        LOG.info("  getDefaultReadOnly = {}", this.postgres.getDefaultReadOnly());
        LOG.info("  getDefaultAutoCommit = {}", this.postgres.getDefaultAutoCommit());
        LOG.info("  getMaxAge = {}", this.postgres.getMaxAge());
        LOG.info("  getMaxWait = {}", this.postgres.getMaxWait());
        LOG.info("  getMinEvictableIdleTimeMillis() = {}", this.postgres.getMinEvictableIdleTimeMillis());
        LOG.info("  getNumTestsPerEvictionRun = {}", this.postgres.getNumTestsPerEvictionRun());
        LOG.info("  getTimeBetweenEvictionRunsMillis = {}", this.postgres.getTimeBetweenEvictionRunsMillis());
        LOG.info("  getValidationQuery = {}", this.postgres.getValidationQuery());
        LOG.info("  getValidationQueryTimeout = {}", this.postgres.getValidationQueryTimeout());
        LOG.info("  getValidationInterval = {}", this.postgres.getValidationInterval());
        LOG.info("  getLogValidationErrors = {}", this.postgres.getLogValidationErrors());

        // When deploying on CF, the Spring Auto-reconfiguration framework will ignore all original datasource properties
        // and reset maxIdle and maxActive to 4 (see also the warning in the logs). We need to override these properties.
        // We are setting to 10, so we can scale up to 10 instances (postgres has threshold of 100 connections)
        LOG.info("Programmatically overriding settings:");
        this.postgres.setDefaultReadOnly(true);
        LOG.info("  defaultReadOnly = {}", this.postgres.getDefaultReadOnly());
        this.postgres.setMinIdle(1);
        this.postgres.setMaxIdle(5);
        this.postgres.setMaxActive(16);
        LOG.info("  minIdle = {}, maxIdle = {}, maxActive = {} ", this.postgres.getMinIdle(),
                this.postgres.getMaxIdle(), this.postgres.getMaxActive());

        // enable clean-up of threads that run longer than 120 secs -> this can leave sessions hanging on the postgresql
        // database side!!
        this.postgres.setTestOnBorrow(true);
        //    this.postgres.setRemoveAbandoned(true);
        //    this.postgres.setRemoveAbandonedTimeout(120); // sec
        //    this.postgres.setLogAbandoned(true);
        LOG.info("  isTestOnBorrow = {}, isRemoveAbandoned = {}, removeAbandonedTimeout = {}, logAbandoned = {} ",
                this.postgres.isTestOnBorrow(), this.postgres.isRemoveAbandoned(), this.postgres.getRemoveAbandonedTimeout(),
                this.postgres.isLogAbandoned());

    }

    @Scheduled(fixedRate = 60_000)
    public void debugJdbcThreadUsage() {
        long nrAbandoned = postgres.getRemoveAbandonedCount();
        long nrActive = postgres.getNumActive();
        long nrIdle = postgres.getIdle();
        Integer dbTotalSessions = getNrSessionsOnPostgresDb(false);
        Integer dbStaleSession = getNrSessionsOnPostgresDb(true);
        if (nrAbandoned == 0 && nrActive < 4 && dbStaleSession == 0) {
            LOG.info("Postgres threads: API idle = {}, active = {}, removeAbandoned = {} - PostgresDb totalSessions = {}, staleSessions = {})",
                    nrIdle, nrActive, nrAbandoned, dbTotalSessions, dbStaleSession);
        } else {
            // normally nrActive never goes above 2 in production, so nrActive > 4 means very likely hanging threads
            // removeAbanondedCount > 0 means hanging threads were removed
            LOG.error("Postgres threads: API idle = {}, active = {}, removeAbandoned = {} - PostgresDb totalSessions = {}, staleSessions = {})",
                    nrIdle, nrActive, nrAbandoned, dbTotalSessions, dbStaleSession);
        }

        if (pgMaxStaleSessions != null && dbStaleSession > pgMaxStaleSessions) {
            LOG.warn("{} stale postgres sessions terminated", removeHangingSessionOnPostgresDb());
        }
    }

    private Integer getNrSessionsOnPostgresDb(boolean staleOnly) {
        Integer result = null;
        String query = "SELECT count(pid) FROM pg_stat_activity WHERE application_name = ?";
        if (staleOnly) {
            query = query + QUERY_FILTER_STALE_SESSION;
        }
        try (Connection con = postgres.getConnection();
            PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, APP_NAME_IN_POSTGRES);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                result = rs.getInt(1);
            } else {
                LOG.error("Postgres database didn't return session data");
            }
        } catch (SQLException e) {
            LOG.error("Error checking number of sessions in postgres database", e);
        }
        return result;
    }

    private Integer removeHangingSessionOnPostgresDb() {
        Integer result = null;
        try (Connection con = postgres.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT pg_terminate_backend(pid) FROM pg_stat_activity " +
                     "WHERE application_name = ? " + QUERY_FILTER_STALE_SESSION)) {
            ps.setString(1, APP_NAME_IN_POSTGRES);
            ResultSet rs = ps.executeQuery();
            result = 0;
            while (rs.next()) {
                result++;
            }
        } catch (SQLException e) {
            LOG.error("Error removing stale sessions in postgres database", e);
        }
        return result;
    }

    /**
     * Read and setup europeana.properties files.
     * The main properties are in the europeana.properties file, but since this is committed on GitHub this must not
     * hold any usernames and passwords. These can be placed in the europeana.user.properties file which is never
     * committed
     * @return PropertSourcePlaceholderConfigurere bean
     */
    @Bean
    public static PropertySourcesPlaceholderConfigurer properties() {
        PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();
        propertySourcesPlaceholderConfigurer.setIgnoreResourceNotFound(true);
        propertySourcesPlaceholderConfigurer.setLocalOverride(true);
        propertySourcesPlaceholderConfigurer.setLocations(new ClassPathResource("europeana.properties"), new ClassPathResource("europeana.user.properties"));
        return propertySourcesPlaceholderConfigurer;
    }

    /**
     * Setup utility for checking api key limits (connects to the PostgreSql database)
     * @return ApiKeyUtils bean
     */
    @Bean
    public ApiKeyUtils apiKeyUtils() {
        return new ApiKeyUtils();
    }

    /**
     * The SugarCRMPollingScheduler regularly invokes the SugarCRMImporter
     * @return SugarCRMPollingScheduler bean
     */
    @Bean
    public SugarCRMPollingScheduler sugarCRMPollingScheduler() {
        return new SugarCRMPollingScheduler();
    }

    /**
     *  The SugarCRMIMporter connects to sugarCRM to check if data on providers or datasets is changed
     *  @return  SugarCRMImporter bean
     */
    @Bean
    public SugarCRMImporter sugarCRMImporter() {
        return new SugarCRMImporter();
    }

    @Bean
    public SugarCRMCache sugarCRMCache() {
        return new SugarCRMCache();
    }

    /**
     * The ObjectStorageClient allows access to our Storage Provider where thumbnails and sitemap files are stored
     * At the moment we use Amazon S3
     * @return ObjectStorageClient bean
     */
    @Bean(name = "api_object_storage_client")
    public ObjectStorageClient objectStorageClient(){
        LOG.info("Creating new objectStorage client");
        return new S3ObjectStorageClient(key,secret,region,bucket);
    }
}
