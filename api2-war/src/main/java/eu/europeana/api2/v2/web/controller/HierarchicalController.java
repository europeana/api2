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
import eu.europeana.api2.v2.service.HierarchyRunner;
import eu.europeana.api2.v2.utils.ApiKeyUtils;
import eu.europeana.corelib.db.entity.enums.RecordType;
import eu.europeana.corelib.search.SearchService;
import io.swagger.annotations.ApiOperation;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.*;

/**
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 * @author Maike Dulk
 */
@Controller
//@Api(value = "hierarchical_records", description = " ")
//@SwaggerSelect
@RequestMapping(value = "/v2/record")
public class HierarchicalController {

    private static Logger log = Logger.getLogger(HierarchicalController.class);

    @Resource
    private SearchService searchService;

    private static final int DEFAULT_HIERARCHY_TIMEOUT = 8000;
    private static final int MAX_HIERARCHY_TIMEOUT = 20000;
    private static final int MIN_HIERARCHY_TIMEOUT = 400;

    @Resource
    private ApiKeyUtils apiKeyUtils;

    @Bean
    public HierarchyRunner hierarchyRunnerBean() {
        return new HierarchyRunner();
    }

    @ApiOperation(value = "returns the object itself")
    @RequestMapping(value = "/{collectionId}/{recordId}/self.json", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ModelAndView getSelf(
            @PathVariable String collectionId,
            @PathVariable String recordId,
            @RequestParam(value = "profile", required = false, defaultValue = "") String profile,
            @RequestParam(value = "wskey", required = true) String wskey,
            @RequestParam(value = "callback", required = false) String callback,
            @RequestParam(value = "hierarchytimeout", required = false, defaultValue = "0") int hierarchyTimeout,
            HttpServletRequest request,
            HttpServletResponse response,
            RedirectAttributes redirectAttrs) {
        return hierarchyTemplate(RecordType.HIERARCHY_SELF, collectionId, recordId,
                profile, wskey, -1, -1, callback, request, response, redirectAttrs, hierarchyTimeout);
    }

    @ApiOperation(value = "returns the object, its ancestors and siblings")
    @RequestMapping(value = "/{collectionId}/{recordId}/ancestor-self-siblings.json", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ModelAndView getAncestorSelfSiblings(
            @PathVariable String collectionId,
            @PathVariable String recordId,
            @RequestParam(value = "profile", required = false, defaultValue = "") String profile,
            @RequestParam(value = "wskey", required = true) String wskey,
            @RequestParam(value = "callback", required = false) String callback,
            @RequestParam(value = "hierarchytimeout", required = false, defaultValue = "0") int hierarchyTimeout,
            HttpServletRequest request,
            HttpServletResponse response,
            RedirectAttributes redirectAttrs) {
        return hierarchyTemplate(RecordType.HIERARCHY_ANCESTOR_SELF_SIBLINGS, collectionId, recordId,
                profile, wskey, -1, -1, callback, request, response, redirectAttrs, hierarchyTimeout);
    }

    @ApiOperation(value = "returns the object's children")
    @RequestMapping(value = "/{collectionId}/{recordId}/children.json", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ModelAndView getChildren(
            @PathVariable String collectionId,
            @PathVariable String recordId,
            @RequestParam(value = "profile", required = false, defaultValue = "") String profile,
            @RequestParam(value = "wskey", required = true) String wskey,
            @RequestParam(value = "limit", required = true, defaultValue = "10") int limit,
            @RequestParam(value = "offset", required = true, defaultValue = "0") int offset,
            @RequestParam(value = "callback", required = false) String callback,
            @RequestParam(value = "hierarchytimeout", required = false, defaultValue = "0") int hierarchyTimeout,
            HttpServletRequest request,
            HttpServletResponse response,
            RedirectAttributes redirectAttrs) {
        return hierarchyTemplate(RecordType.HIERARCHY_CHILDREN, collectionId, recordId,
                profile, wskey, limit, offset, callback, request, response, redirectAttrs, hierarchyTimeout);
    }

    @ApiOperation(value = "returns the object's parent")
    @RequestMapping(value = "/{collectionId}/{recordId}/parent.json", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ModelAndView getParent(
            @PathVariable String collectionId,
            @PathVariable String recordId,
            @RequestParam(value = "profile", required = false, defaultValue = "") String profile,
            @RequestParam(value = "wskey", required = true) String wskey,
            @RequestParam(value = "limit", required = true, defaultValue = "10") int limit,
            @RequestParam(value = "offset", required = true, defaultValue = "0") int offset,
            @RequestParam(value = "callback", required = false) String callback,
            @RequestParam(value = "hierarchytimeout", required = false, defaultValue = "0") int hierarchyTimeout,
            HttpServletRequest request,
            HttpServletResponse response,
            RedirectAttributes redirectAttrs) {
        return hierarchyTemplate(RecordType.HIERARCHY_PARENT, collectionId, recordId,
                profile, wskey, limit, offset, callback, request, response, redirectAttrs, hierarchyTimeout);
    }

    // maintain backwards compatibility with previous spelling of "preceeding"
    @ApiOperation(value = "returns the object's preceding siblings")
    @RequestMapping(value = {"/{collectionId}/{recordId}/preceding-siblings.json",
            "/{collectionId}/{recordId}/preceeding-siblings.json"}, method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ModelAndView getPrecedingSiblings(
            @PathVariable String collectionId,
            @PathVariable String recordId,
            @RequestParam(value = "profile", required = false, defaultValue = "") String profile,
            @RequestParam(value = "wskey", required = true) String wskey,
            @RequestParam(value = "limit", required = true, defaultValue = "10") int limit,
            @RequestParam(value = "offset", required = true, defaultValue = "0") int offset,
            @RequestParam(value = "callback", required = false) String callback,
            @RequestParam(value = "hierarchytimeout", required = false, defaultValue = "0") int hierarchyTimeout,
            HttpServletRequest request,
            HttpServletResponse response,
            RedirectAttributes redirectAttrs) {
        return hierarchyTemplate(RecordType.HIERARCHY_PRECEDING_SIBLINGS, collectionId, recordId,
                profile, wskey, limit, offset, callback, request, response, redirectAttrs, hierarchyTimeout);
    }

    @ApiOperation(value = "returns the object's following siblings")
    @RequestMapping(value = "/{collectionId}/{recordId}/following-siblings.json", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ModelAndView getFollowingSiblings(
            @PathVariable String collectionId,
            @PathVariable String recordId,
            @RequestParam(value = "profile", required = false, defaultValue = "") String profile,
            @RequestParam(value = "wskey", required = true) String wskey,
            @RequestParam(value = "limit", required = true, defaultValue = "10") int limit,
            @RequestParam(value = "offset", required = true, defaultValue = "0") int offset,
            @RequestParam(value = "callback", required = false) String callback,
            @RequestParam(value = "hierarchytimeout", required = false, defaultValue = "0") int hierarchyTimeout,
            HttpServletRequest request,
            HttpServletResponse response,
            RedirectAttributes redirectAttrs) {
        return hierarchyTemplate(RecordType.HIERARCHY_FOLLOWING_SIBLINGS, collectionId, recordId,
                profile, wskey, limit, offset, callback, request, response, redirectAttrs, hierarchyTimeout);
    }

    public ModelAndView hierarchyTemplate(RecordType recordType, String collectionId, String recordId,
                                          String profile, String wskey, int limit, int offset, String callback,
                                          HttpServletRequest request, HttpServletResponse response,
                                          RedirectAttributes redirectAttrs, int hierarchyTimeout) {

        String                  rdfAbout = "/" + collectionId + "/" + recordId;
        HierarchyRunner mrBean = hierarchyRunnerBean();
        hierarchyTimeout = (hierarchyTimeout == 0 ? DEFAULT_HIERARCHY_TIMEOUT :
                            (hierarchyTimeout < MIN_HIERARCHY_TIMEOUT ? MIN_HIERARCHY_TIMEOUT :
                             (hierarchyTimeout > MAX_HIERARCHY_TIMEOUT ? MAX_HIERARCHY_TIMEOUT : hierarchyTimeout)));
        try {
            final ExecutorService timeoutExecutorService = Executors.newSingleThreadExecutor();
            Future<ModelAndView> myFlexibleFriend = timeoutExecutorService.submit(()
                    -> mrBean.call(recordType, rdfAbout, profile, wskey, limit, offset, callback, request,
                    response, log, apiKeyUtils, searchService));
            return myFlexibleFriend.get(hierarchyTimeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            response.setStatus(HttpServletResponse.SC_GATEWAY_TIMEOUT);
            log.error("InterruptedException thrown: " + e.getMessage());
            if (null != e.getCause()) log.error("Cause: " + e.getCause().toString());
            return generateErrorHierarchy(rdfAbout, wskey, callback, "InterruptedException thrown when processing");
        } catch (TimeoutException e) {
            response.setStatus(HttpServletResponse.SC_GATEWAY_TIMEOUT);
            log.error("TimeoutException thrown: " + e.getMessage());
            if (null != e.getCause()) log.error("Cause: " + e.getCause().toString());
            return generateErrorHierarchy(rdfAbout, wskey, callback, "TimeoutException thrown when processing");
        } catch (ExecutionException e) {
            log.error("ExecutionExeption thrown: " + e.getMessage());
            if (null != e.getCause()) log.error("Cause: " + e.getCause().toString());
            ModelAndView gimmeJustTheRecordThen = new ModelAndView("redirect:/v2/record" + rdfAbout + ".json");
            redirectAttrs.addAttribute("profile", profile);
            redirectAttrs.addAttribute("wskey", wskey);
            redirectAttrs.addAttribute("callback", callback);
            return gimmeJustTheRecordThen;
        }
    }

    private ModelAndView generateErrorHierarchy(String rdfAbout, String wskey, String callback,
                                                String message) {
        return JsonUtils.toJson(new ApiError(wskey, String.format(message + " record %s",
                rdfAbout), 999L), callback);

    }

}
