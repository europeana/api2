package eu.europeana.api2.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Loads configuration settings mapping request routes to data sources.
 * Data source must correspond to a valid data source ID in the config.
 */
@Configuration
public class RouteConfigLoader {

    private final Logger log = LogManager.getLogger(RouteConfigLoader.class);

    private final Map<String, String> routeDataSourceMap = new HashMap<>();
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

            if (StringUtils.isEmpty(routePath) || StringUtils.isEmpty(dataSourceId)) {
                log.warn("Empty route mapping found. Check that all routes in .properties file are mapped to a data source. Property prefix = {}", basePath);
            }
            routeDataSourceMap.put(routePath, dataSourceId);
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
     * Checks if a key with the given prefix is contained within a Properties object.
     *
     * @param properties Properties object
     * @param keyPrefix  key prefix to check for
     * @return true if prefix is contained within properties object, false otherwise.
     */
    private boolean containsKeyPrefix(Properties properties, String keyPrefix) {
        return properties.keySet().stream().anyMatch(k
                -> k.toString().startsWith(keyPrefix)
        );
    }
}
