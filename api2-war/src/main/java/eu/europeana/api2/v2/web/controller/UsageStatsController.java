package eu.europeana.api2.v2.web.controller;

import eu.europeana.api.commons.definitions.statistics.UsageStatsFields;
import eu.europeana.api.commons.definitions.statistics.search.HighQualityMetric;
import eu.europeana.api.commons.definitions.statistics.search.LinkedItemMetric;
import eu.europeana.api.commons.definitions.statistics.search.SearchMetric;
import eu.europeana.api2.v2.service.RouteDataService;
import eu.europeana.api2.v2.utils.UsageStatsUtils;
import eu.europeana.corelib.definitions.solr.SolrFacetType;
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
        return getSearchMetric(request);
    }


    private ResponseEntity<String> getSearchMetric(HttpServletRequest request) throws EuropeanaException {
        SolrClient solrClient = getSolrClient(request.getServerName());
        SearchMetric metric = new SearchMetric();
        metric.setType(UsageStatsFields.OVERALL_TOTAL_TYPE);
        // linked item
        getEntityLinkedItem(solrClient, metric);
        // high quality metrics
        getHighQualityMetric(solrClient, metric);

        metric.setTimestamp(new Date());
        return new ResponseEntity<>(serializeToJson(metric), HttpStatus.OK);
    }

    /**
     * Get the usage statistics for linked items of entities
     *
     * @return
     */
    private void getEntityLinkedItem(SolrClient solrClient, SearchMetric searchMetric) {
        LinkedItemMetric linkedItemMetric = new LinkedItemMetric();
        linkedItemMetric.setPlaces(getLinkedItems(solrClient, UsageStatsFields.PLACE));
        linkedItemMetric.setAgents(getLinkedItems(solrClient,UsageStatsFields.AGENT));
        linkedItemMetric.setConcepts(getLinkedItems(solrClient, UsageStatsFields.CONCEPT));
        linkedItemMetric.setOrganisations(getLinkedItems(solrClient, UsageStatsFields.ORGANISATION));
        linkedItemMetric.setTimespans(getLinkedItems(solrClient, UsageStatsFields.TIMESPAN));
        linkedItemMetric.setTotal(linkedItemMetric.getOverallTotal());
        searchMetric.setItemsLinkedToEntities(linkedItemMetric);
    }

    /**
     * Get the stats for high quality metrics
     * @param solrClient
     * @param searchMetric
     */
    private void getHighQualityMetric(SolrClient solrClient, SearchMetric searchMetric) {
        searchMetric.setAllRecords(getMetric(solrClient, new Query(UsageStatsUtils.QUERY_ALL)));
        searchMetric.setNonCompliantRecord(getMetric(solrClient, new Query(UsageStatsUtils.QUERY_T0)));
        searchMetric.setAllCompliantRecords(getMetric(solrClient, new Query(UsageStatsUtils.QUERY_ALL_MINUS_0)));
        searchMetric.setHighQualityData(getMetric(solrClient, new Query(UsageStatsUtils.QUERY_T2_TA)));
        searchMetric.setHighQualityContent(getMetric(solrClient, new Query(UsageStatsUtils.QUERY_T2)));
        searchMetric.setHighQualityReusableContent(getMetric(solrClient, new Query(UsageStatsUtils.QUERY_T3)));
        searchMetric.setHighQualityMetadata(getMetric(solrClient, new Query(UsageStatsUtils.QUERY_TA)));
    }

    /**
     * Returns the total number of items linked for the requested entity Type
     * Example : https://api.europeana.eu/record/search.json?wskey=APIKEY&query="http://data.europeana.eu/place/"&rows=0
     * @param entityType
     * @return
     */
    private Long getLinkedItems(SolrClient solrClient, String entityType) {
        return searchService.getItemsLinkedToEntity(solrClient, UsageStatsUtils.createQueryForLinkedItems(entityType));
    }

    private HighQualityMetric getMetric(SolrClient solrClient, Query query) {
        return UsageStatsUtils.processFacetMap(searchService.getFacet(solrClient, query, SolrFacetType.TYPE));
    }
}
