package eu.europeana.api2.v2.web.controller;

import eu.europeana.api2.model.json.ApiError;
import eu.europeana.api2.utils.JsonUtils;
import eu.europeana.api2.v2.service.RouteDataService;
import eu.europeana.corelib.definitions.edm.beans.BriefBean;
import eu.europeana.corelib.definitions.solr.model.Query;
import eu.europeana.corelib.edm.exceptions.SolrQueryException;
import eu.europeana.corelib.search.SearchService;
import eu.europeana.corelib.search.model.ResultSet;
import eu.europeana.corelib.web.exception.EuropeanaException;
import eu.europeana.corelib.web.exception.ProblemType;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

/**
 * Controller for redirects urls (e.g. values of edmIsShownAt and edmIsShownBy fields)
 * Only redirecting to urls that are in the Solr index are allowed
 *
 * @deprecated June 2019 we no longer generate redirect-urls for edmIsShownAt values. However since saved old record data
 * with redirect urls may still be around we will keep this class for the time being.
 */
@Deprecated
@Controller
public class RedirectController {

    private static final Logger LOG = LogManager.getLogger(RedirectController.class);

    private SearchService searchService;

    private RouteDataService routeService;


    @Autowired
    public RedirectController(SearchService searchService, RouteDataService routeService) {
        this.searchService = searchService;
        this.routeService = routeService;
    }

    /**
     * Handles redirects urls (e.g. values of edmIsShownAt and edmIsShownBy fields)
     *
     * @param apiKey    optional is not checked at the moment
     * @param isShownAt required, the url where to redirect
     * @param response
     * @return
     * @throws IllegalArgumentException
     */
    @GetMapping(value = {"/{apiKey}/redirect", "/{apiKey}/redirect.json", "/v2/{apiKey}/redirect", "/v2/{apiKey}/redirect.json"})
    public Object handleRedirect(
            @PathVariable String apiKey,
            @RequestParam(value = "shownAt") String isShownAt,
            HttpServletRequest request,
            HttpServletResponse response) {

        if (StringUtils.isBlank(isShownAt)) {
            return this.generateError(response, "Empty 'shownAt' parameter", apiKey);
        }

        if (isInEuropeana(isShownAt, request.getServerName())) {
            return "redirect:" + isShownAt;
        } else {
            return this.generateError(response, "Can't redirect: '"+isShownAt+"' is not a known 'shownAt' url", apiKey);
        }
    }

    private ModelAndView generateError(HttpServletResponse response, String msg, String apiKey) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        LOG.error(msg);
        return JsonUtils.toJson(new ApiError(apiKey, msg));
    }

    /**
     * We don't allow redirecting to just any url. The url has to be in the Europeana Solr index as a value of the
     * provider_aggregation_edm_isShownAt field
     * @param url
     * @return true if url is in the Europeana Solr index, otherwise false
     */
    private boolean isInEuropeana(String url, String requestRoute) {
        try {
            // it's important to call escapeQueryChars to prevent people messing up the query by adding illegal characters
            Query query = new Query("provider_aggregation_edm_isShownAt:\"" +ClientUtils.escapeQueryChars(url)+ "\"")
                    .setApiQuery(true)
                    .setFacetsAllowed(false)
                    .setSpellcheckAllowed(false)
                    .setSort(null);
            Optional<SolrClient> solrClient = routeService.getSolrClientForRequest(requestRoute);
            if (solrClient.isEmpty()) {
                LOG.warn("Error: no Solr client configured for route {}", requestRoute);
                throw new SolrQueryException(ProblemType.CANT_CONNECT_SOLR);
            }

            ResultSet<BriefBean> resultSet = searchService.search(solrClient.get(),BriefBean.class, query);

            LOG.debug("Redirect query = {}", query.getExecutedQuery());
            return resultSet.getResultSize() > 0;
        } catch (EuropeanaException ste) {
            LOG.error("Error checking if url is in Solr index", ste);
        }
        // we return true so we maintain redirect functionality when Solr is down
        return true;
    }

}
