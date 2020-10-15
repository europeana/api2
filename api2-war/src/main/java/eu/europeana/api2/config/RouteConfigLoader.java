package eu.europeana.api2.config;

import eu.europeana.corelib.record.BaseUrlWrapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static eu.europeana.corelib.utils.ConfigUtils.containsKeyPrefix;

/**
 * Loads configuration settings mapping request routes to data sources.
 * Data source must correspond to a valid data source ID in the config.
 */
@Configuration
public class RouteConfigLoader {

    private final Map<String, String> routeDataSourceMap = new HashMap<>();
    private final Map<String, String> routeSolrMap = new HashMap<>();
    private final Map<String, BaseUrlWrapper> routeBaseUrlMap = new HashMap<>();

    private static final String SEPARATOR = ".";

    private static final String PORTAL_BASEURL_PROP = "portal.baseUrl";
    private static final String GATEWAY_BASEURL_PROP = "apiGateway.baseUrl";
    private static final String API_BASEURL_PROP = "api2.baseUrl";


    /**
     * defined in corelib-definitions-context.xml
     */
    @Autowired
    @Qualifier("europeanaProperties")
    private Properties properties;

    public RouteConfigLoader() {
    }

    public RouteConfigLoader(@Autowired @Qualifier("europeanaProperties") Properties properties) {
        this.properties = properties;
    }

    @PostConstruct
    void loadRouteConfig() {
        String defaultApiBaseUrl = properties.getProperty(API_BASEURL_PROP, "");
        String defaultGatewayBaseUrl = properties.getProperty(GATEWAY_BASEURL_PROP, "");
        String defaultPortalBaseUrl = properties.getProperty(PORTAL_BASEURL_PROP, "");

        int routeNo = 1;
        while (containsKeyPrefix(properties, "route" + routeNo)) {
            String basePath = "route" + routeNo + SEPARATOR;

            String routePath = properties.getProperty(basePath + "path");
            String dataSourceId = properties.getProperty(basePath + "data-source");
            String solrId = properties.getProperty(basePath + "solr");

            if (StringUtils.isAnyBlank(routePath, dataSourceId, solrId)) {
                throw new IllegalStateException(
                        String.format("Empty route mapping found in config - route:%s, data-source:%s, solr:%s, config prop:%s",
                                routePath, dataSourceId, solrId, basePath));
            }

            // use default baseUrl values if no overrides were configured for this route
            String routeApiBaseUrl = properties.getProperty(basePath + API_BASEURL_PROP, defaultApiBaseUrl);
            String routeGatewayBaseUrl = properties.getProperty(basePath + GATEWAY_BASEURL_PROP, defaultGatewayBaseUrl);
            String routePortalBaseUrl = properties.getProperty(basePath + PORTAL_BASEURL_PROP, defaultPortalBaseUrl);

            routeDataSourceMap.put(routePath, dataSourceId);
            routeSolrMap.put(routePath, solrId);
            routeBaseUrlMap.put(routePath, new BaseUrlWrapper(routeApiBaseUrl, routeGatewayBaseUrl, routePortalBaseUrl));

            routeNo++;
        }
    }

    /**
     * Gets route-to-dataSource map
     */
    public Map<String, String> getRouteDataSourceMap() {
        return Collections.unmodifiableMap(routeDataSourceMap);
    }

    /**
     * Gets route-to-solr map
     */
    public Map<String, String> getRouteSolrMap() {
        return Collections.unmodifiableMap(routeSolrMap);
    }

    /**
     * Gets route-to-baseUrl map
     */
    public Map<String, BaseUrlWrapper> getRouteBaseUrlMap() {
        return Collections.unmodifiableMap(routeBaseUrlMap);
    }
}
