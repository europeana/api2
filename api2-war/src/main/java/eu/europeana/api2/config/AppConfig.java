package eu.europeana.api2.config;


import eu.europeana.api2.model.utils.Api2UrlService;
import eu.europeana.api2.v2.model.translate.MultilingualQueryGenerator;
import eu.europeana.api2.v2.model.translate.QueryTranslator;
import eu.europeana.api2.v2.service.RouteDataService;
import eu.europeana.api2.v2.service.translate.*;
import eu.europeana.api2.v2.utils.ApiKeyUtils;
import eu.europeana.api2.v2.utils.HttpCacheUtils;
import java.util.Arrays;
import javax.annotation.PostConstruct;
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

    @Value("${apikey.validate.url:}")
    private String apikeyValidateUrl;

    @Value("${apiGateway.baseUrl:}")
    private String apiGatewayBaseUrl;

    @Value("${translation.engine:NONE}") // should be either PANGEANIC, GOOGLE or NONE
    private String translationEngineString;
    private TranslationService translationService;

    @Value("${translation.search.query:false}")
    private Boolean translationSearchQuery;

    @Value("${translation.search.results:false}")
    private Boolean translationSearchResults;

    @Autowired
    private Environment env;

    @PostConstruct
    public void logConfiguration() {
        if (LOG.isInfoEnabled()) {
            LOG.info("CF_INSTANCE_INDEX  = {}, CF_INSTANCE_GUID = {}, CF_INSTANCE_IP  = {}",
                    System.getenv("CF_INSTANCE_INDEX"), System.getenv("CF_INSTANCE_GUID"), System.getenv("CF_INSTANCE_IP"));
            LOG.info("Active Spring profiles: {}", Arrays.toString(env.getActiveProfiles()));
            LOG.info("Default Spring profiles: {}", Arrays.toString(env.getDefaultProfiles()));
        }

        // Make sure apikey url is okay
        if (apikeyValidateUrl != null) {
            this.apikeyValidateUrl = apikeyValidateUrl.trim();
            if (apikeyValidateUrl.isEmpty()) {
                LOG.warn("No API key service host defined!");
            } else if (!apikeyValidateUrl.startsWith("http")) {
                LOG.warn("No protocol defined for API key service host! Using http://");
                this.apikeyValidateUrl = apikeyValidateUrl + "http://";
            }
        }

        // Make sure the correct translation service is initialized and available for components that need it
        TranslationEngine engine = TranslationEngine.fromString(translationEngineString);
        if (TranslationEngine.PANGEANIC.equals(engine)) {
            this.translationService = new PangeanicTranslationService();
        } else if (TranslationEngine.GOOGLE.equals(engine)) {
            this.translationService = new GoogleTranslationService();
        } else if (TranslationEngine.PANGEANIC2.equals(engine)) {
            this.translationService = new PangeanicV2TranslationService();
        }
        LOG.info("No translation engine available.");
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
        Api2UrlService urlService = new Api2UrlService(routeConfigLoader().getRouteBaseUrlMap(), portalBaseUrl,
                api2BaseUrl, apikeyValidateUrl, apiGatewayBaseUrl);
        // log default baseUrls used for requests without a matching route in the config
        LogManager.getLogger(Api2UrlService.class).info("Portal base url = {}", urlService.getPortalBaseUrl(""));
        LogManager.getLogger(Api2UrlService.class).info("API2 base url = {}", urlService.getApi2BaseUrl(""));
        LogManager.getLogger(Api2UrlService.class).info("Apikey validate url = {}", urlService.getApikeyValidateUrl());
        LogManager.getLogger(Api2UrlService.class).info("Api gateway base url = {}", urlService.getApiGatewayBaseUrl(""));
        return urlService;
    }

    /**
     * Make sure the correct translation service is initialized and available for components that need it
     * @return translation service or null if none is configured.
     */
    @Bean
    public TranslationService translationService() {
        return this.translationService;
    }

    /**
     * Initialize the multil lingual search query generator if the option is enabled and there's a translation engein
     * configured
     * @return query generator bean or null
     */
    @Bean
    MultilingualQueryGenerator multilingualQueryGenerator() {
        if (translationSearchQuery && this.translationService != null) {
            return new MultilingualQueryGenerator(new QueryTranslator(this.translationService));
        }
        return null;
    }

    /**
     * Initialize the search result translation service if the option is enabled and there's a translation engine
     * configured
     * @return search result translation service bean or null
     */
    @Bean
    SearchResultTranslateService searchResultTranslationService() {
        if (translationSearchResults && this.translationService != null) {
            return new SearchResultTranslateService(this.translationService);
        }
        return null;
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
