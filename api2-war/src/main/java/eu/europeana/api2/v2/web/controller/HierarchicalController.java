/*
 * Copyright 2007-2012 The Europeana Foundation
 *
 *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved 
 *  by the European Commission;
 *  You may not use this work except in compliance with the Licence.
 *  
 *  You may obtain a copy of the Licence at:
 *  http://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under 
 *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of 
 *  any kind, either express or implied.
 *  See the Licence for the specific language governing permissions and limitations under 
 *  the Licence.
 */

package eu.europeana.api2.v2.web.controller;

import eu.europeana.api2.ApiLimitException;
import eu.europeana.api2.model.json.ApiError;
import eu.europeana.api2.utils.JsonUtils;
import eu.europeana.api2.v2.model.LimitResponse;
import eu.europeana.api2.v2.model.json.HierarchicalResult;
import eu.europeana.api2.v2.utils.ControllerUtils;
import eu.europeana.corelib.db.entity.enums.RecordType;
import eu.europeana.corelib.logging.Log;
import eu.europeana.corelib.logging.Logger;
import eu.europeana.corelib.neo4j.entity.Neo4jBean;
import eu.europeana.corelib.neo4j.entity.Neo4jStructBean;
import eu.europeana.corelib.search.SearchService;
import eu.europeana.corelib.web.utils.RequestUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 */
@Controller
@RequestMapping(value = "/v2/record")
public class HierarchicalController {

    private static final int MAX_LIMIT = 100;

    @Log
    private Logger log;

    @Resource
    private SearchService searchService;

    @Resource
    private ControllerUtils controllerUtils;

    @RequestMapping(value = "/{collectionId}/{recordId}/self.json", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ModelAndView getSelf(
            @PathVariable String collectionId,
            @PathVariable String recordId,
            @RequestParam(value = "profile", required = false, defaultValue = "") String profile,
            @RequestParam(value = "wskey", required = true) String wskey,
            @RequestParam(value = "callback", required = false) String callback,
            HttpServletRequest request,
            HttpServletResponse response) {
        return hierarchyTemplate(RecordType.HIERARCHY_SELF, collectionId, recordId,
                profile, wskey, -1, -1, callback, request, response);
    }

    @RequestMapping(value = "/{collectionId}/{recordId}/ancestor-self-siblings.json", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ModelAndView getAncestorSelfSiblings(
            @PathVariable String collectionId,
            @PathVariable String recordId,
            @RequestParam(value = "profile", required = false, defaultValue = "") String profile,
            @RequestParam(value = "wskey", required = true) String wskey,
            @RequestParam(value = "callback", required = false) String callback,
            HttpServletRequest request,
            HttpServletResponse response) {
        return hierarchyTemplate(RecordType.HIERARCHY_ANCESTOR_SELF_SIBLINGS, collectionId, recordId,
                profile, wskey, -1, -1, callback, request, response);
    }

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
            HttpServletResponse response) {
        return hierarchyTemplate(RecordType.HIERARCHY_CHILDREN, collectionId, recordId,
                profile, wskey, limit, offset, callback, request, response);
    }

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
            HttpServletResponse response) {
        return hierarchyTemplate(RecordType.HIERARCHY_PARENT, collectionId, recordId,
                profile, wskey, limit, offset, callback, request, response);
    }

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
            HttpServletResponse response) {
        return hierarchyTemplate(RecordType.HIERARCHY_PRECEEDING_SIBLINGS, collectionId, recordId,
                profile, wskey, limit, offset, callback, request, response);
    }

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
            HttpServletResponse response) {
        return hierarchyTemplate(RecordType.HIERARCHY_FOLLOWING_SIBLINGS, collectionId, recordId,
                profile, wskey, limit, offset, callback, request, response);
    }

    private ModelAndView hierarchyTemplate(RecordType recordType,
                                           String collectionId, String recordId, String profile,
                                           String wskey, int limit, int offset, String callback,
                                           HttpServletRequest request, HttpServletResponse response) {
        long t0 = System.currentTimeMillis();
        controllerUtils.addResponseHeaders(response);

        limit = Math.min(limit, MAX_LIMIT);

        long t1 = System.currentTimeMillis();
        LimitResponse limitResponse;
        try {
            limitResponse = controllerUtils.checkLimit(wskey, request.getRequestURL().toString(),
                    getAction(recordType), recordType, profile);
        } catch (ApiLimitException e) {
            response.setStatus(e.getHttpStatus());
            return JsonUtils.toJson(new ApiError(e), callback);
        }
        log.info("Limit: " + (System.currentTimeMillis() - t1));

        t1 = System.currentTimeMillis();
        HierarchicalResult objectResult = new HierarchicalResult(wskey, getAction(recordType), limitResponse.getRequestNumber());
        log.info("Init object: " + (System.currentTimeMillis() - t1));

        if (StringUtils.containsIgnoreCase(profile, "params")) {
            objectResult.addParams(RequestUtils.getParameterMap(request), "wskey");
            objectResult.addParam("profile", profile);
        }

        String nodeId = "/" + collectionId + "/" + recordId;

        t1 = System.currentTimeMillis();
        if (recordType.equals(RecordType.HIERARCHY_CHILDREN)) {
            objectResult.children = searchService.getChildren(nodeId, offset, limit);
            if (objectResult.children == null) {
                objectResult.message = "This record has no children!";
                objectResult.success = false;
            } else {
                addChildrenCount(objectResult.children);
            }
        } else if (recordType.equals(RecordType.HIERARCHY_SELF)) {
            objectResult.self = searchService.getHierarchicalBean(nodeId);
            if (objectResult.self == null) {
                return JsonUtils.toJson(new ApiError(wskey, getAction(recordType),
                        String.format("Invalid record identifier: %s!", nodeId),
                        limitResponse.getRequestNumber()), callback);
            } else {
                objectResult.self.setChildrenCount(
                        searchService.getChildrenCount(nodeId));
            }
        } else if (recordType.equals(RecordType.HIERARCHY_PARENT)) {
            objectResult.self = searchService.getHierarchicalBean(nodeId);
            if (objectResult.self != null && StringUtils.isNotBlank(objectResult.self.getParent())) {
                objectResult.parent = searchService.getHierarchicalBean(objectResult.self.getParent());
                if (objectResult.parent == null) {
                    objectResult.message = "This record has no parent!";
                    objectResult.success = false;
                } else {
                    objectResult.parent.setChildrenCount(
                            searchService.getChildrenCount(objectResult.parent.getId()));
                }
            }
        } else if (recordType.equals(RecordType.HIERARCHY_FOLLOWING_SIBLINGS)) {
            long tgetsiblings = System.currentTimeMillis();
            objectResult.followingSiblings = searchService.getFollowingSiblings(nodeId, limit);
            log.info("Get siblings: " + (System.currentTimeMillis() - tgetsiblings));
            if (objectResult.followingSiblings == null) {
                objectResult.message = "This record has no following siblings!";
                objectResult.success = false;
            } else {
                long tgetsiblingsCount = System.currentTimeMillis();
                addChildrenCount(objectResult.followingSiblings);
                log.info("Get siblingsCount: " + (System.currentTimeMillis() - tgetsiblingsCount));
            }
        } else if (recordType.equals(RecordType.HIERARCHY_PRECEEDING_SIBLINGS)) {
            objectResult.preceedingSiblings = searchService.getPreceedingSiblings(nodeId, limit);
            if (objectResult.preceedingSiblings == null) {
                objectResult.message = "This record has no preceeding siblings!";
                objectResult.success = false;
            } else {
                addChildrenCount(objectResult.preceedingSiblings);
            }
        } else if (recordType.equals(RecordType.HIERARCHY_ANCESTOR_SELF_SIBLINGS)) {
            Neo4jStructBean struct = searchService.getInitialStruct(nodeId);
            if (struct == null) {
                objectResult.message = "This record has no hierarchical structure!";
                objectResult.success = false;
            } else {
                if (struct.getSelf() != null) {
                    objectResult.self = struct.getSelf();
                }
                if (struct.getParents() != null) {
                    objectResult.ancestors = struct.getParents();
                }
                if (struct.getFollowingSiblings() != null) {
                    objectResult.followingSiblings = struct.getFollowingSiblings();
                }
                if (struct.getPreceedingSiblings() != null) {
                    objectResult.preceedingSiblings = struct.getPreceedingSiblings();
                }
            }
        }
        log.info("get main: " + (System.currentTimeMillis() - t1));

        t1 = System.currentTimeMillis();
        if (!recordType.equals(RecordType.HIERARCHY_SELF)) {
            objectResult.self = searchService.getHierarchicalBean(nodeId);
            if (objectResult.self != null) {
                objectResult.self.setChildrenCount(
                        searchService.getChildrenCount(objectResult.self.getId()));
            }
        }
        log.info("get self: " + (System.currentTimeMillis() - t1));

        objectResult.statsDuration = (System.currentTimeMillis() - t0);

        t1 = System.currentTimeMillis();
        ModelAndView json = JsonUtils.toJson(objectResult, callback);
        log.info("toJson: " + (System.currentTimeMillis() - t1));

        return json;
    }

    private void addChildrenCount(List<Neo4jBean> beans) {
        if (beans != null && beans.size() > 0) {
            for (Neo4jBean bean : beans) {
                if (bean.hasChildren()) {
                    bean.setChildrenCount(searchService.getChildrenCount(bean.getId()));
                }
            }
        }
    }

    private String getAction(RecordType recordType) {
        String action = "";
        if (recordType.equals(RecordType.HIERARCHY_CHILDREN)) {
            action = "children.json";
        } else if (recordType.equals(RecordType.HIERARCHY_SELF)) {
            action = "self.json";
        } else if (recordType.equals(RecordType.HIERARCHY_PARENT)) {
            action = "parent.json";
        } else if (recordType.equals(RecordType.HIERARCHY_FOLLOWING_SIBLINGS)) {
            action = "following-siblings.json";
        } else if (recordType.equals(RecordType.HIERARCHY_PRECEEDING_SIBLINGS)) {
            action = "preceeding-siblings.json";
        } else if (recordType.equals(RecordType.HIERARCHY_ANCESTOR_SELF_SIBLINGS)) {
            action = "ancestor-self-siblings.json";
        }
        return action;
    }
}