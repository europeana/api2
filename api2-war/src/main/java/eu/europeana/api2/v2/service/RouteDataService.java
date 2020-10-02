package eu.europeana.api2.v2.service;

import eu.europeana.api2.config.RouteConfigLoader;
import eu.europeana.corelib.record.DataSourceWrapper;
import eu.europeana.corelib.record.config.RecordServerConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Optional;

/**
 * Maps request routes to the configured data sources.
 */
@Service
public class RouteDataService {
    @Autowired
    private RouteConfigLoader routeConfig;

    @Autowired
    private RecordServerConfig recordServerConfig;

    private static final Logger LOG = LogManager.getLogger(RouteDataService.class);

    public RouteDataService() {
    }

    @Autowired
    public RouteDataService(RouteConfigLoader routeConfig, RecordServerConfig recordServerConfig) {
        this.routeConfig = routeConfig;
        this.recordServerConfig = recordServerConfig;
    }

    @PostConstruct
    void validateRouteConfig() {
        String route, dsId;
        boolean isConfigValid = true;
        for (Map.Entry<String, String> dsMap : routeConfig.getRouteDataSourceMap().entrySet()) {
            route = dsMap.getKey();
            dsId = dsMap.getValue();
            if (recordServerConfig.getDataSourceById(dsId).isEmpty()) {
                LOG.error("Invalid data source configured for route {}: no data source found with id {}", route, dsId);
                isConfigValid = false;
            }
        }

        if (!isConfigValid) {
            throw new IllegalStateException("Invalid route configuration");
        }
    }


    /**
     * Gets data source to be used in handling request, based on the top-level request route
     *
     * @param requestServerName FQDN for request route
     * @return Optional containing data source
     */
    public Optional<DataSourceWrapper> getRecordServerForRequest(String requestServerName) {
        Optional<String> dataSourceId = getDataSourceIdForRoute(requestServerName);

        if (dataSourceId.isEmpty()) {
            return Optional.empty();
        }

        return recordServerConfig.getDataSourceById(dataSourceId.get());
    }


    private String getTopLevelName(String route) {
        int i = route.indexOf('.');
        if (i >= 0) {
            return route.substring(0, i);
        }
        return route;
    }


    /**
     * Finds dataSource ID for route.
     * Code reproduced from Thumbnail API
     */
    private Optional<String> getDataSourceIdForRoute(String route) {
        // make sure we use only the highest level part for matching and not the FQDN
        String topLevelName = getTopLevelName(route);

        // exact matching
        String result = routeConfig.getRouteDataSourceMap().get(topLevelName);
        if (result != null) {
            LOG.debug("Route {} - found exact data source match", topLevelName);
            return Optional.of(result);
        }

        // fallback 1: try to match with "contains"
        for (Map.Entry<String, String> entry : routeConfig.getRouteDataSourceMap().entrySet()) {
            if (topLevelName.contains(entry.getKey())) {
                LOG.debug("Route {} - matched with {}", topLevelName, entry.getKey());
                return Optional.ofNullable(entry.getValue());
            }
        }

        LOG.warn("Route {} - no configured data source found", topLevelName);
        return Optional.empty();
    }
}
