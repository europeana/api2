package eu.europeana.api2.config;


import eu.europeana.api.commons.oauth2.service.impl.EuropeanaClientDetailsService;
import eu.europeana.api.translation.client.TranslationApiClient;
import eu.europeana.api.translation.client.config.TranslationClientConfiguration;
import eu.europeana.api.translation.client.exception.TranslationApiException;
import eu.europeana.api2.model.utils.Api2UrlService;
import eu.europeana.api2.v2.exceptions.InvalidConfigurationException;
import eu.europeana.api2.v2.model.translate.MultilingualQueryGenerator;
import eu.europeana.api2.v2.model.translate.QueryTranslator;
import eu.europeana.api2.v2.service.ApiAuthorizationService;
import eu.europeana.api2.v2.service.RouteDataService;
import eu.europeana.api2.v2.service.translate.*;
import eu.europeana.api2.v2.utils.ApiKeyUtils;
import eu.europeana.api2.v2.utils.HttpCacheUtils;
import java.util.Arrays;
import java.util.Properties;
import javax.annotation.PostConstruct;

import eu.europeana.corelib.web.exception.ProblemType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import static eu.europeana.api2.v2.utils.ApiConstants.API_KEY_SERVICE_CLIENT_DETAILS;

/**
 * @author Willem-Jan Boogerd (www.eledge.net/contact).
 */
@Configuration
@ImportResource({
        "classpath:corelib-definitions-context.xml",
        "classpath:corelib-mongo-context.xml",
        "classpath:corelib-solr-context.xml",
        "classpath:corelib-web-context.xml",
})
@PropertySource("classpath:europeana.properties")
public class AppConfig {

    private static final Logger LOG = LogManager.getLogger(AppConfig.class);

    @Value("${portal.baseUrl:}")
    private String portalBaseUrl;

    @Value("${api2.baseUrl:}")
    private String api2BaseUrl;

    @Value("${apikey.service.url:}")
    private String apikeyServiceUrl;

    @Value("${apiGateway.baseUrl:}")
    private String apiGatewayBaseUrl;

    @Value("${translation.char.limit}")
    private Integer translationCharLimit;

    @Value("${translation.char.tolerance}")
    private Integer translationCharTolerance;

    @Autowired
    private Environment env;

    @Value("${translation.api.endpoint}")
    private String translationApiEndpoint;

    @PostConstruct
    public void logConfiguration() {
        if (LOG.isInfoEnabled()) {
            LOG.info("CF_INSTANCE_INDEX  = {}, CF_INSTANCE_GUID = {}, CF_INSTANCE_IP  = {}",
                    System.getenv("CF_INSTANCE_INDEX"), System.getenv("CF_INSTANCE_GUID"), System.getenv("CF_INSTANCE_IP"));
            LOG.info("Active Spring profiles: {}", Arrays.toString(env.getActiveProfiles()));
            LOG.info("Default Spring profiles: {}", Arrays.toString(env.getDefaultProfiles()));
        }

        // Make sure apikey url is okay
        if (apikeyServiceUrl != null) {
            this.apikeyServiceUrl = apikeyServiceUrl.trim();
            if (apikeyServiceUrl.isEmpty()) {
                LOG.warn("No API key service host defined!");
            } else if (!apikeyServiceUrl.startsWith("http")) {
                LOG.warn("No protocol defined for API key service host! Using http://");
                this.apikeyServiceUrl = apikeyServiceUrl + "http://";
            }
        }
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
        Api2UrlService urlService = new Api2UrlService(routeConfigLoader().getRouteBaseUrlMap(), portalBaseUrl,
                api2BaseUrl, apikeyServiceUrl, apiGatewayBaseUrl);
        // log default baseUrls used for requests without a matching route in the config
        LogManager.getLogger(Api2UrlService.class).info("Portal base url = {}", urlService.getPortalBaseUrl(""));
        LogManager.getLogger(Api2UrlService.class).info("API2 base url = {}", urlService.getApi2BaseUrl(""));
        LogManager.getLogger(Api2UrlService.class).info("Apikey service url = {}", urlService.getApikeyServiceUrl());
        LogManager.getLogger(Api2UrlService.class).info("Api gateway base url = {}", urlService.getApiGatewayBaseUrl(""));
        return urlService;
    }

    /**
     * Initialize the multil lingual search query generator if the option is enabled and there's a translation engein
     * configured
     * @return query generator bean or null
     */
    @Bean
    public MultilingualQueryGenerator multilingualQueryGenerator() throws InvalidConfigurationException {
        return new MultilingualQueryGenerator(new QueryTranslator(getTranslationApiClient()));

    }

    @Bean
    public RouteConfigLoader routeConfigLoader(){
        return new RouteConfigLoader();
    }

    @Bean
    public RouteDataService routeService(){
        return new RouteDataService();
    }

    @Bean(name = API_KEY_SERVICE_CLIENT_DETAILS)
    public EuropeanaClientDetailsService getApiKeyClientDetailsService(){
        EuropeanaClientDetailsService clientDetails = new EuropeanaClientDetailsService();
        clientDetails.setApiKeyServiceUrl(apikeyServiceUrl);
        return clientDetails;
    }

    @Bean
    public ApiAuthorizationService getAuthorizarionService(){
        return new ApiAuthorizationService();
    }

    @Bean
    public TranslationApiClient getTranslationApiClient() throws InvalidConfigurationException {
        TranslationClientConfiguration configuration = new TranslationClientConfiguration(loadProperties());
        try {
            return new TranslationApiClient(configuration);
        } catch (TranslationApiException e) {
            throw new InvalidConfigurationException(ProblemType.TRANSLATION_API_URL_ERROR);
        }
    }

    @Bean
    public TranslationService translationService() throws InvalidConfigurationException {
        return new TranslationService(
                new MetadataTranslationService(getTranslationApiClient(), new MetadataChosenLanguageService(),
                        translationCharLimit, translationCharTolerance, false),
                new MetadataLangDetectionService(getTranslationApiClient()));
    }

    private Properties loadProperties() {
        Properties properties = new Properties();
        properties.put(TranslationClientConfiguration.TRANSLATION_API_URL, translationApiEndpoint);
        return properties;
    }
}
