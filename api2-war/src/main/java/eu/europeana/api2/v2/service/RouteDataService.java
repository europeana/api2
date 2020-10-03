package eu.europeana.api2.v2.service;

import eu.europeana.api2.config.RouteConfigLoader;
import eu.europeana.corelib.record.DataSourceWrapper;
import eu.europeana.corelib.record.config.RecordServerConfig;
import eu.europeana.corelib.search.config.SearchServerConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
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

    @Autowired
    private SearchServerConfig searchServerConfig;

    private static final Logger LOG = LogManager.getLogger(RouteDataService.class);

    public RouteDataService() {
    }

    @Autowired
    public RouteDataService(RouteConfigLoader routeConfig, RecordServerConfig recordServerConfig, SearchServerConfig searchServerConfig) {
        this.routeConfig = routeConfig;
        this.recordServerConfig = recordServerConfig;
        this.searchServerConfig = searchServerConfig;
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

        String solrId;
        for (Map.Entry<String, String> solrMap : routeConfig.getRouteSolrMap().entrySet()) {
            route = solrMap.getKey();
            solrId = solrMap.getValue();

            if (searchServerConfig.getSolrClientById(solrId).isEmpty()) {
                LOG.error("Invalid solr config for route {}: no solr client with id {}", route, solrId);
                isConfigValid = false;
            }
        }
        ;


        if (!isConfigValid) {
            throw new IllegalStateException("Invalid route configuration");
        }
    }


    /**
     * Gets data source to be used in handling request, based on the top-level request route
     *
     * @param requestRoute FQDN for request route
     * @return Optional containing data source
     */
    public Optional<DataSourceWrapper> getRecordServerForRequest(String requestRoute) {
        Optional<String> dataSourceId = getDataSourceIdForRoute(requestRoute);

        if (dataSourceId.isEmpty()) {
            return Optional.empty();
        }

        return recordServerConfig.getDataSourceById(dataSourceId.get());
    }

    /**
     * Gets Solr client to be used in performing query
     *
     * @param requestRoute FQDN for request route
     * @return Optional containing Solr client
     */
    public Optional<SolrClient> getSolrClientForRequest(String requestRoute) {
        Optional<String> dataSourceId = getSolrClientForRoute(requestRoute);

        if (dataSourceId.isEmpty()) {
            return Optional.empty();
        }

        return searchServerConfig.getSolrClientById(dataSourceId.get());
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
     * //TODO: similar to {@link #getDataSourceIdForRoute(String)}. Refactor
     */
    private Optional<String> getSolrClientForRoute(String route) {
        // make sure we use only the highest level part for matching and not the FQDN
        String topLevelName = getTopLevelName(route);

        // exact matching
        String result = routeConfig.getRouteSolrMap().get(topLevelName);
        if (result != null) {
            LOG.debug("Route {} - found exact Solr match", topLevelName);
            return Optional.of(result);
        }

        // fallback 1: try to match with "contains"
        for (Map.Entry<String, String> entry : routeConfig.getRouteSolrMap().entrySet()) {
            if (topLevelName.contains(entry.getKey())) {
                LOG.debug("Route {} - matched with {}", topLevelName, entry.getKey());
                return Optional.ofNullable(entry.getValue());
            }
        }

        LOG.warn("Route {} - no configured Solr client found", topLevelName);
        return Optional.empty();
    }

    /**
     * Finds dataSource ID for route.
     * Code reproduced from Thumbnail API
     * TODO: Similar to {@link #getSolrClientForRoute(String)}. Refactor
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
