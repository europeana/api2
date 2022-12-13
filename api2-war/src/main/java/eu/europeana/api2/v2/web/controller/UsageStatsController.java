package eu.europeana.api2.v2.web.controller;

import eu.europeana.api.commons.definitions.statistics.UsageStatsFields;
import eu.europeana.api.commons.definitions.statistics.search.LinkedItemMetric;
import eu.europeana.api.commons.definitions.statistics.search.SearchMetric;
import eu.europeana.api2.v2.service.RouteDataService;
import eu.europeana.corelib.definitions.solr.model.Query;
import eu.europeana.corelib.web.exception.EuropeanaException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.solr.client.solrj.SolrClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import eu.europeana.api2.v2.web.swagger.SwaggerSelect;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@Controller
@Api(tags = "Usage Statistics API")
@SwaggerSelect
public class UsageStatsController extends BaseController {

    private static final String URL_PREFIX = "\"http://data.europeana.eu/";

    public UsageStatsController(RouteDataService routeService) {
        super(routeService);
    }

    /**
     * Method to generate metric for search api
     *
     * @param wskey
     * @param request
     * @return
     */
    @ApiOperation(value = "Generate Stats", nickname = "generateStats", response = java.lang.Void.class)
    @GetMapping(value = "/record/stats", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> generateUsageStats(
            @RequestParam(value = "wskey") String wskey,
            HttpServletRequest request) throws EuropeanaException {
        apiKeyUtils.validateApiKey(wskey);
        return getEntityLinkedItem(request);
    }

    /**
     * Get the usage statistics for entity api
     *
     * @return
     */
    private ResponseEntity<String> getEntityLinkedItem(HttpServletRequest request) throws EuropeanaException {
        SolrClient solrClient = getSolrClient(request.getServerName());
        SearchMetric metric = new SearchMetric();
        metric.setType(UsageStatsFields.OVERALL_TOTAL_TYPE);

        LinkedItemMetric linkedItemMetric = new LinkedItemMetric();
        linkedItemMetric.setPlaces(getLinkedItems(solrClient, "place"));
        linkedItemMetric.setAgents(getLinkedItems(solrClient,"agent"));
        linkedItemMetric.setConcepts(getLinkedItems(solrClient, "concept"));
        linkedItemMetric.setOrganisations(getLinkedItems(solrClient, "organization"));
        linkedItemMetric.setTimespans(getLinkedItems(solrClient, "timespan"));
        linkedItemMetric.setTotal(linkedItemMetric.getOverallTotal());
        metric.setItemsLinkedToEntities(linkedItemMetric);

        metric.setTimestamp(new Date());
        return new ResponseEntity<>(serializeToJson(metric), HttpStatus.OK);
    }

    /**
     * Returns the total number of items linked for the requested entity Type
     * Example : https://api.europeana.eu/record/search.json?wskey=APIKEY&query="http://data.europeana.eu/place/"&rows=0
     * @param entityType
     * @return
     */
    private Long getLinkedItems(SolrClient solrClient, String entityType) {
        return searchService.getItemsLinkedToEntity(solrClient, createQueryForStats(entityType));
    }

    /**
     * creates query for solr
     * @param entityType
     * @return
     */
    public static Query createQueryForStats(String entityType) {
        StringBuilder q = new StringBuilder(URL_PREFIX);
        q.append(entityType).append("/\"");
        Query query = new Query(q.toString());
        query.setPageSize(0);
        return query;
    }
}
