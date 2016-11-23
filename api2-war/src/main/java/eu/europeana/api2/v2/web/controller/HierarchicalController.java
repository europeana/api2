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

import eu.europeana.api2.ApiLimitException;
import eu.europeana.api2.model.json.ApiError;
import eu.europeana.api2.utils.JsonUtils;
import eu.europeana.api2.v2.model.LimitResponse;
import eu.europeana.api2.v2.model.json.HierarchicalResult;
import eu.europeana.api2.v2.utils.ControllerUtils;
import eu.europeana.api2.v2.web.swagger.SwaggerIgnore;
import eu.europeana.corelib.db.entity.enums.RecordType;
import eu.europeana.corelib.definitions.exception.Neo4JException;
import eu.europeana.corelib.neo4j.entity.Neo4jBean;
import eu.europeana.corelib.neo4j.entity.Neo4jStructBean;
import eu.europeana.corelib.search.SearchService;
import eu.europeana.corelib.web.utils.RequestUtils;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
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
import java.util.Collections;
import java.util.List;
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
    private static final int MAX_LIMIT = 100;

    @Resource
    private SearchService searchService;

    @Resource
    private ControllerUtils controllerUtils;

    @Resource
    private ObjectController objectController;

    @ApiOperation(value = "returns the object itself")
    @RequestMapping(value = "/{collectionId}/{recordId}/self.json", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ModelAndView getSelf(
            @PathVariable String collectionId,
            @PathVariable String recordId,
            @RequestParam(value = "profile", required = false, defaultValue = "") String profile,
            @RequestParam(value = "wskey", required = true) String wskey,
            @RequestParam(value = "callback", required = false) String callback,
            HttpServletRequest request,
            HttpServletResponse response,
            RedirectAttributes redirectAttrs) {
        return hierarchyTemplate(RecordType.HIERARCHY_SELF, collectionId, recordId,
                profile, wskey, -1, -1, callback, request, response, redirectAttrs);
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
            HttpServletRequest request,
            HttpServletResponse response,
            RedirectAttributes redirectAttrs) {
        return hierarchyTemplate(RecordType.HIERARCHY_ANCESTOR_SELF_SIBLINGS, collectionId, recordId,
                profile, wskey, -1, -1, callback, request, response, redirectAttrs);
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
            HttpServletRequest request,
            HttpServletResponse response,
            RedirectAttributes redirectAttrs) {
        return hierarchyTemplate(RecordType.HIERARCHY_CHILDREN, collectionId, recordId,
                profile, wskey, limit, offset, callback, request, response, redirectAttrs);
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
            HttpServletRequest request,
            HttpServletResponse response,
            RedirectAttributes redirectAttrs) {
        return hierarchyTemplate(RecordType.HIERARCHY_PARENT, collectionId, recordId,
                profile, wskey, limit, offset, callback, request, response, redirectAttrs);
    }

    @ApiOperation(value = "returns the object's preceding siblings")
    @RequestMapping(value = "/{collectionId}/{recordId}/preceding-siblings.json", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ModelAndView getPrecedingSiblings(
            @PathVariable String collectionId,
            @PathVariable String recordId,
            @RequestParam(value = "profile", required = false, defaultValue = "") String profile,
            @RequestParam(value = "wskey", required = true) String wskey,
            @RequestParam(value = "limit", required = true, defaultValue = "10") int limit,
            @RequestParam(value = "offset", required = true, defaultValue = "0") int offset,
            @RequestParam(value = "callback", required = false) String callback,
            HttpServletRequest request,
            HttpServletResponse response,
            RedirectAttributes redirectAttrs) {
        return hierarchyTemplate(RecordType.HIERARCHY_PRECEDING_SIBLINGS, collectionId, recordId,
                profile, wskey, limit, offset, callback, request, response, redirectAttrs);
    }

    @SwaggerIgnore
//    @ApiOperation(value = "returns the object's preceeeeeding siblings (backwards compatibility")
    @RequestMapping(value = "/{collectionId}/{recordId}/preceeding-siblings.json", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ModelAndView getPreceedingSiblings(
            @PathVariable String collectionId,
            @PathVariable String recordId,
            @RequestParam(value = "profile", required = false, defaultValue = "") String profile,
            @RequestParam(value = "wskey", required = true) String wskey,
            @RequestParam(value = "limit", required = true, defaultValue = "10") int limit,
            @RequestParam(value = "offset", required = true, defaultValue = "0") int offset,
            @RequestParam(value = "callback", required = false) String callback,
            HttpServletRequest request,
            HttpServletResponse response,
            RedirectAttributes redirectAttrs) {
        return hierarchyTemplate(RecordType.HIERARCHY_PRECEDING_SIBLINGS, collectionId, recordId,
                profile, wskey, limit, offset, callback, request, response, redirectAttrs);
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
            HttpServletRequest request,
            HttpServletResponse response,
            RedirectAttributes redirectAttrs) {
        return hierarchyTemplate(RecordType.HIERARCHY_FOLLOWING_SIBLINGS, collectionId, recordId,
                profile, wskey, limit, offset, callback, request, response, redirectAttrs);
    }

    public HierarchicalController() {
    }

//    private ModelAndView hierarchyTemplate(RecordType recordType, String collectionId, String recordId,
//                                           String profile, String wskey, int limit, int offset, String callback,
//                                           HttpServletRequest request, HttpServletResponse response,
//                                           RedirectAttributes redirectAttrs) {
////        ExecutorService         executorService  = Executors.newSingleThreadExecutor();
//        ExecutorService         executorService  = Executors.newCachedThreadPool();
//
//        String                  rdfAbout = "/" + collectionId + "/" + recordId;
//        HierarchyTemplateRunner runner = new HierarchyTemplateRunner(recordType, rdfAbout, profile, wskey, limit, offset, callback, request, response, log, controllerUtils, searchService);
//        Future<ModelAndView>    future = executorService.submit(runner);
//
//        long t9 = System.currentTimeMillis();
//
//        ModelAndView result;
//        try {
//            result = future.get();
//            executorService.shutdown();
//            executorService.awaitTermination(5, TimeUnit.SECONDS);
//            return result;
//        } catch (InterruptedException e) {
//            log.error("InterruptedException thrown: " + e.getMessage());
//            log.error("Cause: " + e.getCause());
//            return generateErrorHierarchy(recordType, rdfAbout, wskey, callback, "InterruptedException");
//        } catch (ExecutionException e) {
//            log.error("ExecutionExeption thrown: " + e.getMessage());
//            log.error("Cause: " + e.getCause());
//            ModelAndView gimmeJustTheRecordThen = new ModelAndView("redirect:/v2/record" + rdfAbout + ".json");
//            redirectAttrs.addAttribute("profile", profile);
//            redirectAttrs.addAttribute("wskey", wskey);
//            redirectAttrs.addAttribute("callback", callback);
//            return gimmeJustTheRecordThen;
//        } finally {
//            if (!executorService.isTerminated()) {
//                log.error("Neo4J query thread didn't terminate in time");
//            }
//            executorService.shutdownNow();
//            log.error("Neo4J query thread shut down");
//        }
//    }


    public ModelAndView hierarchyTemplate(RecordType recordType, String collectionId, String recordId,
                                          String profile, String wskey, int limit, int offset, String callback,
                                          HttpServletRequest request, HttpServletResponse response,
                                          RedirectAttributes redirectAttrs) {

        String                  rdfAbout = "/" + collectionId + "/" + recordId;
        try {
            Future<ModelAndView> future = hierarchyRunner(recordType, rdfAbout, profile, wskey, limit,
                                                          offset, callback, request, response);
            while (true){
                if (future.isDone()) {
                    log.info("Neo4j query completed");
                    return future.get();
                }
            }
        } catch (Neo4JException e) {
            log.error("Neo4JException thrown: " + e.getMessage());
            log.error("Cause: " + e.getCause());
            return generateErrorHierarchy(recordType, rdfAbout, wskey, callback, "Neo4JException");
        } catch (InterruptedException e) {
            log.error("InterruptedException thrown: " + e.getMessage());
            log.error("Cause: " + e.getCause());
            return generateErrorHierarchy(recordType, rdfAbout, wskey, callback, "InterruptedException");
        } catch (ExecutionException e) {
            log.error("ExecutionExeption thrown: " + e.getMessage());
            log.error("Cause: " + e.getCause());
            ModelAndView gimmeJustTheRecordThen = new ModelAndView("redirect:/v2/record" + rdfAbout + ".json");
            redirectAttrs.addAttribute("profile", profile);
            redirectAttrs.addAttribute("wskey", wskey);
            redirectAttrs.addAttribute("callback", callback);
            return gimmeJustTheRecordThen;
        }
    }





    @Async
    public Future<ModelAndView> hierarchyRunner(RecordType recordType, String rdfAbout,
                                           String profile, String wskey, int limit, int offset, String callback,
                                           HttpServletRequest request, HttpServletResponse response) throws Neo4JException {

        long t0 = System.currentTimeMillis();
        controllerUtils.addResponseHeaders(response);

        limit = Math.min(limit, MAX_LIMIT);

        long          t1 = System.currentTimeMillis();
        LimitResponse limitResponse;

        try {
            limitResponse = controllerUtils.checkLimit(wskey, request.getRequestURL().toString(),
                    recordType, profile);
        } catch (ApiLimitException e) {
            response.setStatus(e.getHttpStatus());
            return new AsyncResult <> (JsonUtils.toJson(new ApiError(e), callback));
        }

        log.info("Limit: " + (System.currentTimeMillis() - t1));
        t1 = System.currentTimeMillis();

        HierarchicalResult hierarchicalResult = new HierarchicalResult(wskey, limitResponse.getRequestNumber());
        if (StringUtils.containsIgnoreCase(profile, "params")) {
            hierarchicalResult.addParams(RequestUtils.getParameterMap(request), "wskey");
            hierarchicalResult.addParam("profile", profile);
        }

        log.info("Init object: " + (System.currentTimeMillis() - t1));
        t1 = System.currentTimeMillis();

        hierarchicalResult.self = searchService.getHierarchicalBean(rdfAbout);

        if (hierarchicalResult.self != null) {
            hierarchicalResult.self.setChildrenCount(
                    searchService.getChildrenCount(rdfAbout));
        } else {
            hierarchicalResult.success = false;
            response.setStatus(404);
            return new AsyncResult <> (JsonUtils.toJson(new ApiError(wskey,
                    String.format("Invalid record identifier: %s", rdfAbout),
                    limitResponse.getRequestNumber()), callback));
        }

        log.info("get self: " + (System.currentTimeMillis() - t1));
        t1 = System.currentTimeMillis();

        if (recordType.equals(RecordType.HIERARCHY_CHILDREN)) {
            if (hierarchicalResult.self.getChildrenCount() > 0) {
                hierarchicalResult.children = searchService.getChildren(rdfAbout, offset, limit);
                if (hierarchicalResult.children == null || hierarchicalResult.children.isEmpty()) {
                    hierarchicalResult.message = "This record has no children";
                    hierarchicalResult.success = false;
                    response.setStatus(404);
                } else {
                    addChildrenCount(hierarchicalResult.children);
                }
            } else {
                hierarchicalResult.message = "This record has no children";
                hierarchicalResult.success = false;
                response.setStatus(404);
            }
        } else if (recordType.equals(RecordType.HIERARCHY_PARENT)) {
            if (hierarchicalResult.self == null || StringUtils.isBlank(hierarchicalResult.self.getParent())) {
                hierarchicalResult.message = "This record has no parent";
                hierarchicalResult.success = false;
                response.setStatus(404);
            } else {
                hierarchicalResult.parent = searchService.getHierarchicalBean(hierarchicalResult.self.getParent());
                if (hierarchicalResult.parent != null) {
                    hierarchicalResult.parent.setChildrenCount(
                            searchService.getChildrenCount(hierarchicalResult.parent.getId()));
                }
            }
        } else if (recordType.equals(RecordType.HIERARCHY_FOLLOWING_SIBLINGS)) {
            long tgetsiblings = System.currentTimeMillis();
            hierarchicalResult.followingSiblings = searchService.getFollowingSiblings(rdfAbout, limit);
            log.info("Get siblings: " + (System.currentTimeMillis() - tgetsiblings));
            if (hierarchicalResult.followingSiblings == null || hierarchicalResult.followingSiblings.isEmpty()) {
                hierarchicalResult.message = "This record has no following siblings";
                hierarchicalResult.success = false;
                response.setStatus(404);
            } else {
                long tgetsiblingsCount = System.currentTimeMillis();
                addChildrenCount(hierarchicalResult.followingSiblings);
                log.info("Get siblingsCount: " + (System.currentTimeMillis() - tgetsiblingsCount));
            }
        } else if (recordType.equals(RecordType.HIERARCHY_PRECEDING_SIBLINGS)) {
            hierarchicalResult.precedingSiblings = searchService.getPrecedingSiblings(rdfAbout, limit);
            if (hierarchicalResult.precedingSiblings == null || hierarchicalResult.precedingSiblings.isEmpty()) {
                hierarchicalResult.message = "This record has no preceding siblings";
                hierarchicalResult.success = false;
                response.setStatus(404);
            } else {
                addChildrenCount(hierarchicalResult.precedingSiblings);
            }
        } else if (recordType.equals(RecordType.HIERARCHY_ANCESTOR_SELF_SIBLINGS)) {
            Neo4jStructBean struct = searchService.getInitialStruct(rdfAbout);
            if (struct == null) {
                hierarchicalResult.message = String.format("This record has no hierarchical structure %s", rdfAbout);
                hierarchicalResult.success = false;
                response.setStatus(404);
            } else {
                String partialErrorMsg = "This record has no";
                boolean hasParent = true;
                boolean hasFollowing = true;
                boolean hasPreceding = true;
                // reversed order
                if (struct.getParents() != null && !struct.getParents().isEmpty()) {
                    List<Neo4jBean> tempParents = struct.getParents();
                    Collections.reverse(tempParents);
                    hierarchicalResult.ancestors = tempParents;
                } else {
                    hasParent = false;
                }
                if (struct.getFollowingSiblings() != null && !struct.getFollowingSiblings().isEmpty()) {
                    hierarchicalResult.followingSiblings = struct.getFollowingSiblings();
                } else {
                    hasFollowing = false;
                }
                if (struct.getPrecedingSiblings() != null && !struct.getPrecedingSiblings().isEmpty()) {
                    hierarchicalResult.precedingSiblings = struct.getPrecedingSiblings();
                } else {
                    hasPreceding = false;
                }
                if (!hasParent){
                    partialErrorMsg += " parent";
                    if (!hasFollowing || !hasPreceding){
                        partialErrorMsg += " or";
                    }
                }
                if (!hasFollowing && !hasPreceding){
                    partialErrorMsg += " siblings";
                } else if (hasFollowing && !hasPreceding){
                    partialErrorMsg += " preceding siblings";
                } else if (!hasFollowing){
                    partialErrorMsg += " following siblings";
                }
                if (!hasParent || !hasFollowing || !hasPreceding) {
                    hierarchicalResult.message = partialErrorMsg;
                }


                if (struct.getPrecedingSiblingChildren() != null && !struct.getPrecedingSiblingChildren().isEmpty()) {
                    hierarchicalResult.precedingSiblingChildren = struct.getPrecedingSiblingChildren();
                }
                if (struct.getFollowingSiblingChildren() != null && !struct.getFollowingSiblingChildren().isEmpty()) {
                    hierarchicalResult.followingSiblingChildren = struct.getFollowingSiblingChildren();
                }
            }
        }
        log.info("get main: " + (System.currentTimeMillis() - t1));
        t1 = System.currentTimeMillis();

        hierarchicalResult.statsDuration = (System.currentTimeMillis() - t0);
        ModelAndView json = JsonUtils.toJson(hierarchicalResult, callback);
        log.info("toJson: " + (System.currentTimeMillis() - t1));
        return new AsyncResult <ModelAndView> (json);
    }

    private void addChildrenCount(List<Neo4jBean> beans) throws Neo4JException {
        if (beans != null && beans.size() > 0) {
            for (Neo4jBean bean : beans) {
                if (bean.hasChildren()) {
                    bean.setChildrenCount(searchService.getChildrenCount(bean.getId()));
                }
            }
        }
    }

    private ModelAndView generateErrorHierarchy(RecordType recordType, String rdfAbout, String wskey, String callback,
                                                String exceptionType) {
        return JsonUtils.toJson(new ApiError(wskey, String.format(exceptionType + " thrown when processing record %s",
                rdfAbout), -1L), callback);

    }

    protected static String getAction(RecordType recordType) {
        String action = "";
        if (recordType.equals(RecordType.HIERARCHY_CHILDREN)) {
            action = "children.json";
        } else if (recordType.equals(RecordType.HIERARCHY_SELF)) {
            action = "self.json";
        } else if (recordType.equals(RecordType.HIERARCHY_PARENT)) {
            action = "parent.json";
        } else if (recordType.equals(RecordType.HIERARCHY_FOLLOWING_SIBLINGS)) {
            action = "following-siblings.json";
        } else if (recordType.equals(RecordType.HIERARCHY_PRECEDING_SIBLINGS)) {
            action = "preceding-siblings.json";
        } else if (recordType.equals(RecordType.HIERARCHY_ANCESTOR_SELF_SIBLINGS)) {
            action = "ancestor-self-siblings.json";
        }
        return action;
    }

}