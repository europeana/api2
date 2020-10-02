package eu.europeana.api2.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
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
    private static final String SEPARATOR = ".";

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
            routeDataSourceMap.put(routePath, dataSourceId);
            routeSolrMap.put(routePath, solrId);
            routeNo++;
        }


    }

    /**
     * Gets route-to-dataSource map
     */
    public Map<String, String> getRouteDataSourceMap() {
        return routeDataSourceMap;
    }

    /**
     * Gets route-to-solr map
     */
    public Map<String, String> getRouteSolrMap() {
        return routeSolrMap;
    }
}
