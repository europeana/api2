package eu.europeana.api2.config;

import eu.europeana.api2.model.utils.Api2UrlService;
import eu.europeana.api2.v2.utils.ApiKeyUtils;
import eu.europeana.api2.v2.utils.HttpCacheUtils;
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
import java.util.Arrays;

/**
 * @author Willem-Jan Boogerd (www.eledge.net/contact).
 */
@Configuration
@ImportResource({
                        "classpath:corelib-db-context.xml",
                        "classpath:corelib-solr-context.xml",
                        "classpath:corelib-utils-context.xml",
                        "classpath:corelib-web-context.xml"
                })
@EnableScheduling
@PropertySource("classpath:europeana.properties")
public class AppConfig {

    private static final Logger LOG = LogManager.getLogger(AppConfig.class);

    @Value("${portal.baseUrl:}")
    private String portalBaseUrl;
    @Value("${api2.baseUrl:}")
    private String api2BaseUrl;

    @Deprecated
    @Value("${sitemap.s3.key}")
    private String key;
    @Deprecated
    @Value("${sitemap.s3.secret}")
    private String secret;
    @Deprecated
    @Value("${sitemap.s3.region}")
    private String region;
    @Deprecated
    @Value("${sitemap.s3.bucket}")
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
     * Utility methods to help HTTP caching processing
     * @return HttpCacheUtils bean
     */
    @Bean
    public HttpCacheUtils httpCacheUtils() {
        return new HttpCacheUtils();
    }


    /**
     * Setup service for generating API and Portal urls
     * @return
     */
    @Bean
    public Api2UrlService api2UrlService() {
        Api2UrlService urlService = new Api2UrlService(portalBaseUrl, api2BaseUrl);
        LogManager.getLogger(Api2UrlService.class).info("Portal base url = {}", urlService.getPortalBaseUrl());
        LogManager.getLogger(Api2UrlService.class).info("API2 base url = {}", urlService.getApi2BaseUrl());
        return urlService;
    }

    /**
     * The ObjectStorageClient allows access to our Storage Provider where thumbnails and sitemap files are stored
     * At the moment we use Amazon S3
     * @return ObjectStorageClient bean
     */
//    @Deprecated
//    @Bean(name = "api_sitemap_object_storage")
//    public ObjectStorageClient objectStorageClient(){
//        LOG.info("Creating new sitemap objectStorage client");
//        return new S3ObjectStorageClient(key,secret,region,bucket);
//    }

}
