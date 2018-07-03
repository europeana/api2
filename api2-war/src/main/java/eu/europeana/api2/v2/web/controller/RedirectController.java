/*
 * Copyright 2007-2015 The Europeana Foundation
 *
 * Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 * by the European Commission;
 * You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 * http://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 * any kind, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under
 * the Licence.
 */

package eu.europeana.api2.v2.web.controller;

import eu.europeana.api2.model.json.ApiError;
import eu.europeana.api2.utils.JsonUtils;
import eu.europeana.corelib.definitions.edm.beans.BriefBean;
import eu.europeana.corelib.definitions.solr.model.Query;
import eu.europeana.corelib.search.SearchService;
import eu.europeana.corelib.search.model.ResultSet;
import eu.europeana.corelib.web.exception.EuropeanaException;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;

/**
 * Controller for redirects urls (e.g. values of edmIsShownAt and edmIsShownBy fields)
 * Only redirecting to urls that are in the Solr index are allowed
 */
@Controller
public class RedirectController {

    private static final Logger LOG = LogManager.getLogger(RedirectController.class);

    private SearchService searchService;

    @Autowired
    public RedirectController(SearchService searchService) {
        this.searchService = searchService;
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
    @RequestMapping(value = {"/{apiKey}/redirect", "/{apiKey}/redirect.json", "/v2/{apiKey}/redirect", "/v2/{apiKey}/redirect.json"},
            method = RequestMethod.GET)
    public Object handleRedirect(@PathVariable String apiKey, @RequestParam(value = "shownAt", required = true) String isShownAt,
            HttpServletResponse response) {

        // Disabled while awaiting better implementation (ticket #1742)
        // apiLogService.logApiRequest(wskey, id, RecordType.REDIRECT, profile);
        if (StringUtils.isBlank(isShownAt)) {
            return this.generateError(response, "Empty 'shownAt' parameter", apiKey);
        }

        if (isInEuropeana(isShownAt)) {
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
    private boolean isInEuropeana(String url) {
        try {
            // it's important to call escapeQueryChars to prevent people messing up the query by adding illegal characters
            Query query = new Query("provider_aggregation_edm_isShownAt:\"" +ClientUtils.escapeQueryChars(url)+ "\"")
                    .setApiQuery(true)
                    .setFacetsAllowed(false)
                    .setSpellcheckAllowed(false)
                    .setSort(null);
            ResultSet<BriefBean> resultSet = searchService.search(BriefBean.class, query);
            LOG.debug("Redirect query = {}", query.getExecutedQuery());
            return resultSet.getResultSize() > 0;
        } catch (EuropeanaException ste) {
            LOG.error("Error checking if url is in Solr index", ste);
        }
        // we return true so we maintain redirect functionality when Solr is down
        return true;
    }

}
