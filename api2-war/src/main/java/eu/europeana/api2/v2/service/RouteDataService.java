package eu.europeana.api2.v2.service;

import eu.europeana.api2.config.RouteConfigLoader;
import eu.europeana.corelib.record.BaseUrlWrapper;
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

import static eu.europeana.api2.model.utils.RouteMatcher.getEntryForRoute;

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

    private static final BaseUrlWrapper EMPTY_BASE_URL = new BaseUrlWrapper("", "", "");

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
        String route;
        String dsId;
        boolean isConfigValid = true;
        for (Map.Entry<String, String> entry : routeConfig.getRouteDataSourceMap().entrySet()) {
            route = entry.getKey().trim();
            dsId = entry.getValue().trim();
            if (recordServerConfig.getDataSourceById(dsId).isPresent()) {
                LOG.info("Route {} - data source id {} configured", route, dsId);
            } else {
                LOG.error("Invalid data source configured for route {}: no data source found with id {}", route, dsId);
                isConfigValid = false;
            }
        }

        String solrId;
        for (Map.Entry<String, String> entry : routeConfig.getRouteSolrMap().entrySet()) {
            route = entry.getKey().trim();
            solrId = entry.getValue().trim();

            if (searchServerConfig.getSolrClientById(solrId).isPresent()) {
                LOG.info("Route {} - solr client id {} configured", route, solrId);
            } else {
                LOG.error("Invalid solr config for route {}: no solr client with id {}", route, solrId);
                isConfigValid = false;
            }
        }

        // log baseUrl mapping
        BaseUrlWrapper urls;
        for (Map.Entry<String, BaseUrlWrapper> entry : routeConfig.getRouteBaseUrlMap().entrySet()) {
            route = entry.getKey().trim();
            urls = entry.getValue();
            LOG.info("Route {} - configured baseUrls {}", route, urls);
        }

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
        Optional<String> dataSourceId = getEntryForRoute(requestRoute, routeConfig.getRouteDataSourceMap());

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
        Optional<String> solrId = getEntryForRoute(requestRoute, routeConfig.getRouteSolrMap());

        if (solrId.isEmpty()) {
            return Optional.empty();
        }

        return searchServerConfig.getSolrClientById(solrId.get());
    }

    /**
     * Gets Base URLs to use for request
     *
     * @param requestRoute request route
     * @return Optional containing Base URL wrapper instance
     */
    public BaseUrlWrapper getBaseUrlsForRequest(String requestRoute) {
        Optional<BaseUrlWrapper> baseUrl = getEntryForRoute(requestRoute, routeConfig.getRouteBaseUrlMap());
        if(baseUrl.isEmpty()) {
            // empty baseUrl values handled by corelib
            LOG.debug("No baseUrl configured. Using empty strings as baseUrl values");
            return EMPTY_BASE_URL;
        }
        return baseUrl.get();
    }
}

