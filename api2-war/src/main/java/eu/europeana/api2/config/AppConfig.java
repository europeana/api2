package eu.europeana.api2.config;

import eu.europeana.api2.v2.schedule.SugarCRMPollingScheduler;
import eu.europeana.api2.v2.service.SugarCRMCache;
import eu.europeana.api2.v2.service.SugarCRMImporter;
import eu.europeana.api2.v2.utils.ApiKeyUtils;
import eu.europeana.features.ObjectStorageClient;
import eu.europeana.features.S3ObjectStorageClient;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
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

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.sql.DataSource;
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

    @Value("${s3.key}")
    private String key;
    @Value("${s3.secret}")
    private String secret;
    @Value("${s3.region}")
    private String region;
    @Value("${s3.bucket}")
    private String bucket;

    @Resource(name = "corelib_db_dataSource")
    private BasicDataSource postgres;

    @Autowired
    private Environment env;

    @PostConstruct
    public void logSpringProfiles() {


        LOG.info("Active Spring profiles:" + Arrays.toString(env.getActiveProfiles()));
        LOG.info("Default Spring profiles:" + Arrays.toString(env.getDefaultProfiles()));

        LOG.info("Postgres Datasource: minIdle = {}, maxIdle = {}, maxActive = {} ", postgres.getMinIdle(),
                postgres.getMaxIdle(), postgres.getMaxActive());
        LOG.info("Postgres Datasource: getInitialSize = {}", postgres.getInitialSize());
        LOG.info("Postgres Datasource: getMaxWait = {}", postgres.getMaxWait());
        LOG.info("Postgres Datasource!: getMinEvictableIdleTimeMillis() = {}", postgres.getMinEvictableIdleTimeMillis());
        LOG.info("Postgres Datasource!: getTimeBetweenEvictionRunsMillis = {}", postgres.getTimeBetweenEvictionRunsMillis());
        LOG.info("Postgres Datasource: getNumActive = {}, getNumIdle = {}, getNumTestsPerEvictionRun = {}", postgres.getNumActive()
                , postgres.getNumIdle(), postgres.getNumTestsPerEvictionRun());
        LOG.info("Postgres Datasource: getValidationQuery = {}", postgres.getValidationQuery());
        LOG.info("Postgres Datasource: getValidationQueryTimeout = {}", postgres.getValidationQueryTimeout());
        LOG.info("Postgres Datasource: getDefaultReadOnly = {}", postgres.getDefaultReadOnly());
        LOG.info("Postgres Datasource: getDefaultAutoCommit = {}", postgres.getDefaultAutoCommit());

        LOG.info("Postgres Datasource: getMaxOpenPreparedStatements = {}", postgres.getMaxOpenPreparedStatements());

        LOG.info("Postgres Datasource: getRemoveAbandoned = {}", postgres.getRemoveAbandoned());
        LOG.info("Postgres Datasource: getRemoveAbandonedTimeout = {}", postgres.getRemoveAbandonedTimeout());
        LOG.info("Postgres Datasource: getLogAbandoned = {}", postgres.getLogAbandoned());

        postgres.setRemoveAbandoned(true);
        postgres.setLogAbandoned(true);

        LOG.info("Changing remove abandoned");
        LOG.info("Postgres Datasource: getRemoveAbandoned = {}", postgres.getRemoveAbandoned());
        LOG.info("Postgres Datasource: getRemoveAbandonedTimeout = {}", postgres.getRemoveAbandonedTimeout());
        LOG.info("Postgres Datasource: getLogAbandoned = {}", postgres.getLogAbandoned());


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
