package eu.europeana.api2.config;

import eu.europeana.api2.model.utils.Api2UrlService;
import eu.europeana.api2.v2.service.RouteDataService;
import eu.europeana.api2.v2.utils.ApiKeyUtils;
import eu.europeana.api2.v2.utils.HttpCacheUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
        "classpath:corelib-utils-context.xml",
        "classpath:corelib-web-context.xml",
})
@PropertySource("classpath:europeana.properties")
public class AppConfig {

    private static final Logger LOG = LogManager.getLogger(AppConfig.class);

    @Value("${portal.baseUrl:}")
    private String portalBaseUrl;

    @Value("${api2.baseUrl:}")
    private String api2BaseUrl;

    @Value("${apikey.validate.url:}")
    private String apikeyValidateUrl;

    @Value("${apiGateway.baseUrl:}")
    private String apiGatewayBaseUrl;

    @Autowired
    private Environment env;

    @PostConstruct
    public void logConfiguration() {
        LOG.info("CF_INSTANCE_INDEX  = {}, CF_INSTANCE_GUID = {}, CF_INSTANCE_IP  = {}",
                System.getenv("CF_INSTANCE_INDEX"), System.getenv("CF_INSTANCE_GUID"), System.getenv("CF_INSTANCE_IP"));
        LOG.info("Active Spring profiles: {}", Arrays.toString(env.getActiveProfiles()));
        LOG.info("Default Spring profiles: {}", Arrays.toString(env.getDefaultProfiles()));
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
     * Utility for validating api keys
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
     * @return Api2UrlService bean
     */
    @Bean
    public Api2UrlService api2UrlService() {
        Api2UrlService urlService = new Api2UrlService(routeConfigLoader().getRouteBaseUrlMap(), portalBaseUrl, api2BaseUrl, apikeyValidateUrl, apiGatewayBaseUrl);
        // log default baseUrls used for requests without a matching route in the config
        LogManager.getLogger(Api2UrlService.class).info("Portal base url = {}", urlService.getPortalBaseUrl(""));
        LogManager.getLogger(Api2UrlService.class).info("API2 base url = {}", urlService.getApi2BaseUrl(""));
        LogManager.getLogger(Api2UrlService.class).info("Apikey validate url = {}", urlService.getApikeyValidateUrl());
        LogManager.getLogger(Api2UrlService.class).info("Api gateway base url = {}", urlService.getApiGatewayBaseUrl(""));
        return urlService;
    }

    @Bean
    public RouteConfigLoader routeConfigLoader(){
        return new RouteConfigLoader();
    }

    @Bean
    public RouteDataService routeService(){
        return new RouteDataService();
    }
}
