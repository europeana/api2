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

package eu.europeana.api2.v2.service;

import eu.europeana.api2.ApiLimitException;
import eu.europeana.api2.model.json.ApiError;
import eu.europeana.api2.utils.JsonUtils;
import eu.europeana.api2.v2.model.LimitResponse;
import eu.europeana.api2.v2.model.json.HierarchicalResult;
import eu.europeana.api2.v2.utils.ControllerUtils;
import eu.europeana.corelib.db.entity.enums.RecordType;
import eu.europeana.corelib.definitions.exception.Neo4JException;
import eu.europeana.corelib.neo4j.entity.Neo4jBean;
import eu.europeana.corelib.neo4j.entity.Neo4jStructBean;
import eu.europeana.corelib.search.SearchService;
import eu.europeana.corelib.web.utils.RequestUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Im Luthien hain echant 30/06/15
 * multithreaded version to remedy errors generated
 * when being called multiple times in a short time span
 *
 */

public class HierarchyRunner {
    private static final int MAX_LIMIT = 100;
    private String rdfAbout;
    private RecordType recordType;
    private String profile;
    private String wskey;
    private int limit;
    private int offset;
    private String callback;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private Logger log;
    private ControllerUtils controllerUtils;
    private SearchService searchService;

    @Async
    public Future<ModelAndView> call(RecordType recordType,
                                     String rdfAbout, String profile,
                                     String wskey, int limit, int offset, String callback,
                                     HttpServletRequest request, HttpServletResponse response,
                                     Logger log, ControllerUtils controllerUtils, SearchService searchService) throws Neo4JException {
        this.recordType = recordType;
        this.rdfAbout = rdfAbout;
        this.profile = profile;
        this.wskey = wskey;
        this.limit = limit;
        this.offset = offset;
        this.callback = callback;
        this.request = request;
        this.response = response;
        this.log = log;
        this.controllerUtils = controllerUtils;
        this.searchService = searchService;
        log.info("Running thread for " + rdfAbout);

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
            return new AsyncResult<>(JsonUtils.toJson(new ApiError(e), callback));
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
        return new AsyncResult <> (json);
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
}
