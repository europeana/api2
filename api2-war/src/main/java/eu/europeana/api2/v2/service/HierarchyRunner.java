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
import eu.europeana.api2.v2.utils.ApiKeyUtils;
import eu.europeana.api2.v2.utils.ControllerUtils;
import eu.europeana.corelib.db.entity.enums.RecordType;
import eu.europeana.corelib.neo4j.entity.Neo4jBean;
import eu.europeana.corelib.neo4j.entity.Neo4jStructBean;
import eu.europeana.corelib.neo4j.exception.Neo4JException;
import eu.europeana.corelib.neo4j.Neo4jSearchService;
import eu.europeana.corelib.web.exception.EmailServiceException;
import eu.europeana.corelib.web.exception.EuropeanaException;
import eu.europeana.corelib.web.exception.ProblemType;
import eu.europeana.corelib.web.service.EmailService;
import eu.europeana.corelib.web.utils.RequestUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;

/**
 * Im Luthien hain echant 30/06/15
 * multithreaded version to remedy errors generated
 * when being called multiple times in a short time span
 *
 */

public class HierarchyRunner {

    private static final Logger LOG = LogManager.getLogger(HierarchyRunner.class);

    private static final int MAX_LIMIT = 100;
    //TODO factor exception email handling out to generic functionality
    private static final String SUBJECTPREFIX = "Europeana exception email handler: ";

    @Resource(name = "corelib_web_emailService")
    private EmailService emailService;

    private Neo4jSearchService searchService;

    @Async
    public ModelAndView call(RecordType recordType,
                                     String rdfAbout, String profile,
                                     String wskey, int limit, int offset, String callback,
                                     HttpServletRequest request, HttpServletResponse response,
                                     ApiKeyUtils apiKeyUtils, Neo4jSearchService searchService) {

        this.searchService = searchService;
        LOG.debug("Running thread for {}", rdfAbout);

        long selfIndex = 0L;
        long t0 = System.currentTimeMillis();
        ControllerUtils.addResponseHeaders(response);

        limit = Math.min(limit, MAX_LIMIT);

        long          t1 = System.currentTimeMillis();
        LimitResponse limitResponse;

        try {
            limitResponse = apiKeyUtils.checkLimit(wskey, request.getRequestURL().toString(), recordType, profile);
        } catch (ApiLimitException e) {
            response.setStatus(e.getHttpStatus());
            return JsonUtils.toJson(new ApiError(e), callback);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Limit: {}", (System.currentTimeMillis() - t1));
        }
        t1 = System.currentTimeMillis();

        HierarchicalResult hierarchicalResult = new HierarchicalResult(wskey, limitResponse.getRequestNumber());
        if (StringUtils.containsIgnoreCase(profile, "params")) {
            hierarchicalResult.addParams(RequestUtils.getParameterMap(request), "wskey");
            hierarchicalResult.addParam("profile", profile);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Init object: {}", (System.currentTimeMillis() - t1));
        }
        t1 = System.currentTimeMillis();
        try{
            hierarchicalResult.self = searchService.getSingle(rdfAbout);
            if (hierarchicalResult.self != null) {
                if (hierarchicalResult.self.hasChildren() &&
                        hierarchicalResult.self.getChildrenCount() == 0){
                    throw new Neo4JException(ProblemType.NEO4J_502, " for record " + rdfAbout);
                }
                selfIndex = hierarchicalResult.self.getIndex();
            } else {
                hierarchicalResult.success = false;
                response.setStatus(404);
                return JsonUtils.toJson(new ApiError(wskey, "Invalid record identifier: "+ rdfAbout,
                        limitResponse.getRequestNumber()), callback);
            }

        if (LOG.isDebugEnabled()) {
            LOG.debug("get self: {} ", (System.currentTimeMillis() - t1));
        }
        t1 = System.currentTimeMillis();

            if (recordType.equals(RecordType.HIERARCHY_CHILDREN)) {
                if (hierarchicalResult.self.getChildrenCount() > 0) {
                    hierarchicalResult.children = searchService.getChildren(rdfAbout, offset, limit);
                    if (hierarchicalResult.children == null || hierarchicalResult.children.isEmpty()) {
                        hierarchicalResult.message = "This record has no children";
                        hierarchicalResult.success = false;
                        response.setStatus(404);
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
                    hierarchicalResult.parent = searchService.getSingle(hierarchicalResult.self.getParent());
                }
            } else if (recordType.equals(RecordType.HIERARCHY_FOLLOWING_SIBLINGS)) {
                hierarchicalResult.followingSiblings = searchService.getFollowingSiblings(rdfAbout, offset, limit, selfIndex);
                if (hierarchicalResult.followingSiblings == null || hierarchicalResult.followingSiblings.isEmpty()) {
                    hierarchicalResult.message = "This record has no following siblings";
                    hierarchicalResult.success = false;
                    response.setStatus(404);
                }
            } else if (recordType.equals(RecordType.HIERARCHY_PRECEDING_SIBLINGS)) {
                hierarchicalResult.precedingSiblings = searchService.getPrecedingSiblings(rdfAbout, offset, limit, selfIndex);
                if (hierarchicalResult.precedingSiblings == null || hierarchicalResult.precedingSiblings.isEmpty()) {
                    hierarchicalResult.message = "This record has no preceding siblings";
                    hierarchicalResult.success = false;
                    response.setStatus(404);
                }
            } else if (recordType.equals(RecordType.HIERARCHY_ANCESTOR_SELF_SIBLINGS)) {
                Neo4jStructBean struct = searchService.getInitialStruct(rdfAbout, selfIndex);
                if (struct == null) {
                    hierarchicalResult.message = "This record has no hierarchical structure "+ rdfAbout;
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
        } catch (Neo4JException e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            LOG.error("Neo4JException thrown: {}", e.getMessage());
            if (null != e.getCause()) LOG.error("Cause: {}", e.getCause());
            // TODO re-enable mail sending at some time?
//            if (e.getProblem().getAction().equals(ProblemResponseAction.MAIL)) sendExceptionEmail(e);
            return JsonUtils.toJson(new ApiError(wskey, e.getMessage(), -1L), callback);

        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("get main: {}", (System.currentTimeMillis() - t1));
        }
        t1 = System.currentTimeMillis();


        hierarchicalResult.statsDuration = (System.currentTimeMillis() - t0);
        ModelAndView json = JsonUtils.toJson(hierarchicalResult, callback);
        if (LOG.isDebugEnabled()) {
            LOG.debug("toJson: {}", (System.currentTimeMillis() - t1));
        }
        return json;
    }

    private void sendExceptionEmail(EuropeanaException e){
        String newline = System.getProperty("line.separator");

        String header = SUBJECTPREFIX + e.getProblem().getMessage();
        String body = (e.getMessage() + newline + newline +
                ExceptionUtils.getStackTrace(e));
//                + e.getStackTrace().toString());
        try {
            emailService.sendException(header, body);
        } catch (EmailServiceException es) {
            LOG.error("Error sending email", e);
        }
    }
}
