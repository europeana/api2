package eu.europeana.api2.v2.web.controller;

import eu.europeana.api2.ApiKeyException;
import eu.europeana.api2.model.json.ApiError;
import eu.europeana.api2.model.utils.Api2UrlService;
import eu.europeana.api2.utils.JsonUtils;
import eu.europeana.api2.v2.model.LimitResponse;
import eu.europeana.api2.v2.service.HierarchyRunner;
import eu.europeana.api2.v2.utils.ApiKeyUtils;
import eu.europeana.corelib.db.entity.enums.RecordType;
import eu.europeana.corelib.neo4j.Neo4jSearchService;
import eu.europeana.corelib.web.exception.EuropeanaException;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static eu.europeana.api2.model.utils.Api2UrlService.NOTHING;

/**
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 * @author Maike Dulk
 */

@Lazy
@Controller
//@Api(value = "hierarchical_records", description = " ")
//@SwaggerSelect
@RequestMapping(value = "/v2/record")
public class HierarchicalController {

    private static final Logger LOG = LogManager.getLogger(HierarchicalController.class);

    private static final int DEFAULT_HIERARCHY_TIMEOUT = 8_000;
    private static final int MAX_HIERARCHY_TIMEOUT = 20_000;
    private static final int MIN_HIERARCHY_TIMEOUT = 400;

    // we allow only 20 requests at a time (per server instance), more are automatically placed in a queue
    private final ExecutorService timeoutExecutorService = Executors.newFixedThreadPool(20);

    @Resource(name = "corelib_neo4j_searchService" )
    private Neo4jSearchService searchService;

    @Resource
    private ApiKeyUtils    apiKeyUtils;

    @Resource
    private Api2UrlService urlService;

    @Bean
    public HierarchyRunner hierarchyRunnerBean() {
        return new HierarchyRunner();
    }


    @ApiOperation(value = "returns the object itself")
    @GetMapping(value = "/{collectionId}/{recordId}/self.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public ModelAndView getSelf(
            @PathVariable String collectionId,
            @PathVariable String recordId,
            @RequestParam(value = "profile", required = false, defaultValue = "") String profile,
            @RequestParam(value = "wskey") String apikey,
            @RequestParam(value = "callback", required = false) String callback,
            @RequestParam(value = "hierarchytimeout", required = false, defaultValue = "0") int hierarchyTimeout,
            HttpServletRequest request,
            HttpServletResponse response) throws EuropeanaException {
        return hierarchyTemplate(RecordType.HIERARCHY_SELF, collectionId, recordId,
                profile, apikey, -1, -1, callback, request, response, hierarchyTimeout);
    }

    @ApiOperation(value = "returns the object, its ancestors and siblings")
    @GetMapping(value = "/{collectionId}/{recordId}/ancestor-self-siblings.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public ModelAndView getAncestorSelfSiblings(
            @PathVariable String collectionId,
            @PathVariable String recordId,
            @RequestParam(value = "profile", required = false, defaultValue = "") String profile,
            @RequestParam(value = "wskey") String apikey,
            @RequestParam(value = "callback", required = false) String callback,
            @RequestParam(value = "hierarchytimeout", required = false, defaultValue = "0") int hierarchyTimeout,
            HttpServletRequest request,
            HttpServletResponse response) throws EuropeanaException {
        return hierarchyTemplate(RecordType.HIERARCHY_ANCESTOR_SELF_SIBLINGS, collectionId, recordId,
                profile, apikey, -1, -1, callback, request, response, hierarchyTimeout);
    }

    @ApiOperation(value = "returns the object's children")
    @GetMapping(value = "/{collectionId}/{recordId}/children.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public ModelAndView getChildren(
            @PathVariable String collectionId,
            @PathVariable String recordId,
            @RequestParam(value = "profile", required = false, defaultValue = "") String profile,
            @RequestParam(value = "wskey") String apikey,
            @RequestParam(value = "limit", defaultValue = "10") int limit,
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "callback", required = false) String callback,
            @RequestParam(value = "hierarchytimeout", required = false, defaultValue = "0") int hierarchyTimeout,
            HttpServletRequest request,
            HttpServletResponse response) throws EuropeanaException {
        return hierarchyTemplate(RecordType.HIERARCHY_CHILDREN, collectionId, recordId,
                profile, apikey, limit, offset, callback, request, response, hierarchyTimeout);
    }

    @ApiOperation(value = "returns the object's parent")
    @GetMapping(value = "/{collectionId}/{recordId}/parent.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public ModelAndView getParent(
            @PathVariable String collectionId,
            @PathVariable String recordId,
            @RequestParam(value = "profile", required = false, defaultValue = "") String profile,
            @RequestParam(value = "wskey") String apikey,
            @RequestParam(value = "limit", defaultValue = "10") int limit,
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "callback", required = false) String callback,
            @RequestParam(value = "hierarchytimeout", required = false, defaultValue = "0") int hierarchyTimeout,
            HttpServletRequest request,
            HttpServletResponse response) throws EuropeanaException {
        return hierarchyTemplate(RecordType.HIERARCHY_PARENT, collectionId, recordId,
                profile, apikey, limit, offset, callback, request, response, hierarchyTimeout);
    }

    // maintain backwards compatibility with previous spelling of "preceeding"
    @ApiOperation(value = "returns the object's preceding siblings")
    @GetMapping(value = {"/{collectionId}/{recordId}/preceding-siblings.json",
                         "/{collectionId}/{recordId}/preceeding-siblings.json"}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ModelAndView getPrecedingSiblings(
            @PathVariable String collectionId,
            @PathVariable String recordId,
            @RequestParam(value = "profile", required = false, defaultValue = "") String profile,
            @RequestParam(value = "wskey") String apikey,
            @RequestParam(value = "limit", defaultValue = "10") int limit,
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "callback", required = false) String callback,
            @RequestParam(value = "hierarchytimeout", required = false, defaultValue = "0") int hierarchyTimeout,
            HttpServletRequest request,
            HttpServletResponse response) throws EuropeanaException {
        return hierarchyTemplate(RecordType.HIERARCHY_PRECEDING_SIBLINGS, collectionId, recordId,
                profile, apikey, limit, offset, callback, request, response, hierarchyTimeout);
    }

    @ApiOperation(value = "returns the object's following siblings")
    @GetMapping(value = "/{collectionId}/{recordId}/following-siblings.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public ModelAndView getFollowingSiblings(
            @PathVariable String collectionId,
            @PathVariable String recordId,
            @RequestParam(value = "profile", required = false, defaultValue = "") String profile,
            @RequestParam(value = "wskey") String apikey,
            @RequestParam(value = "limit", defaultValue = "10") int limit,
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "callback", required = false) String callback,
            @RequestParam(value = "hierarchytimeout", required = false, defaultValue = "0") int hierarchyTimeout,
            HttpServletRequest request,
            HttpServletResponse response) throws EuropeanaException {
        return hierarchyTemplate(RecordType.HIERARCHY_FOLLOWING_SIBLINGS, collectionId, recordId,
                profile, apikey, limit, offset, callback, request, response, hierarchyTimeout);
    }

    public ModelAndView hierarchyTemplate(RecordType recordType, String collectionId, String recordId,
                                          String profile, String apikey, int limit, int offset, String callback,
                                          HttpServletRequest request, HttpServletResponse response,
                                          int hierarchyTimeout) throws EuropeanaException {

        String                  rdfAbout = "/" + collectionId + "/" + recordId;
        HierarchyRunner mrBean = hierarchyRunnerBean();
        hierarchyTimeout = (hierarchyTimeout == 0 ? DEFAULT_HIERARCHY_TIMEOUT :
                           (hierarchyTimeout < MIN_HIERARCHY_TIMEOUT ? MIN_HIERARCHY_TIMEOUT :
                           (hierarchyTimeout > MAX_HIERARCHY_TIMEOUT ? MAX_HIERARCHY_TIMEOUT : hierarchyTimeout)));
        try {
            // EA-1826
            LimitResponse limitResponse;
            if (StringUtils.equalsIgnoreCase(urlService.getApikeyValidateUrl(), NOTHING)){
                limitResponse = apiKeyUtils.checkLimit(apikey);
            } else {
                limitResponse = apiKeyUtils.validateApiKey(apikey);
            }
            Future<ModelAndView> myFlexibleFriend = timeoutExecutorService.submit(()
                    -> mrBean.call(recordType, rdfAbout, profile, limit, offset, callback, request,
                    response, searchService, apikey));
            return myFlexibleFriend.get(hierarchyTimeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            response.setStatus(HttpServletResponse.SC_GATEWAY_TIMEOUT);
            return generateErrorHierarchy(rdfAbout, apikey, callback, "InterruptedException thrown when processing", e);
        } catch (TimeoutException e) {
            response.setStatus(HttpServletResponse.SC_GATEWAY_TIMEOUT);
            return generateErrorHierarchy(rdfAbout, apikey, callback, "TimeoutException thrown when processing", e);
        } catch (ExecutionException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return generateErrorHierarchy(rdfAbout, apikey, callback, "ExecutionExeption thrown when processing", e);
        }
    }

    private ModelAndView generateErrorHierarchy(String rdfAbout, String wskey, String callback, String message, Exception e) {
        StringBuilder logMsg = new StringBuilder(message);
        if (e.getMessage() != null) {
            logMsg.append(" Message: ");
            logMsg.append(e.getMessage());
        }
        if (e.getCause() != null) {
            logMsg.append(" Cause: ");
            logMsg.append(e.getCause());
        }
        LOG.error(logMsg.toString());

        return JsonUtils.toJson(new ApiError(wskey, message + " record " + rdfAbout), callback);
    }

}
