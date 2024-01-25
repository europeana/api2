package eu.europeana.api2.v2.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.api.commons.service.authorization.AuthorizationService;
import eu.europeana.api.commons.web.controller.BaseRestController;
import eu.europeana.api2.v2.exceptions.InvalidConfigurationException;
import eu.europeana.api2.v2.exceptions.JsonSerializationException;
import eu.europeana.api2.v2.service.ApiAuthorizationService;
import eu.europeana.api2.v2.service.RouteDataService;
import eu.europeana.corelib.search.SearchService;
import eu.europeana.corelib.web.exception.ProblemType;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Optional;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;

public abstract class BaseController extends BaseRestController {

    private static final Logger LOG  = LogManager.getLogger(BaseController.class);
    private static final String DATE_FORMAT  = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    @Resource
    protected SearchService searchService;

    @Resource
    ApiAuthorizationService authorizationService;

    protected RouteDataService routeService;

    protected ObjectMapper mapper = new ObjectMapper();

    @Autowired
    protected BaseController(RouteDataService routeService) {
        this.routeService = routeService;
    }

    /**
     * Gets Solr client to use for request
     *
     * @param route request route
     * @return Solr client
     * @throws InvalidConfigurationException if no SolrClient is configured for route
     */
    protected SolrClient getSolrClient(String route) throws InvalidConfigurationException {
        Optional<SolrClient> solrClient = routeService.getSolrClientForRequest(route);
        if (solrClient.isEmpty()) {
            LOG.error("No Solr client configured for route {}", route);
            throw new InvalidConfigurationException(ProblemType.CONFIG_ERROR,
                    "No search engine configured for request route");
        }
        return solrClient.get();
    }

    /**
     * Serialises the object to json
     * @param object to be serialized
     * @return serialized string
     * @throws JsonSerializationException if serialization failed
     */
    protected String serializeToJson(Object object) throws JsonSerializationException {
        SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH);
        mapper.setDateFormat(df);
        try {
            return mapper.writer().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new JsonSerializationException(e);
        }
    }

    protected String getAuthorizationHeader(HttpServletRequest request) {
        return request.getHeader(HttpHeaders.AUTHORIZATION);
    }

    @Override
    protected AuthorizationService getAuthorizationService() {
        return authorizationService;
    }

}
