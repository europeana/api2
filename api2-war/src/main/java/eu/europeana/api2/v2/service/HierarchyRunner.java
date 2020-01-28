package eu.europeana.api2.v2.service;

import eu.europeana.api2.ApiKeyException;
import eu.europeana.api2.model.json.ApiError;
import eu.europeana.api2.utils.JsonUtils;
import eu.europeana.api2.v2.model.LimitResponse;
import eu.europeana.api2.v2.model.json.HierarchicalResult;
import eu.europeana.api2.v2.utils.ApiKeyUtils;
import eu.europeana.api2.v2.utils.ControllerUtils;
import eu.europeana.corelib.db.entity.enums.RecordType;
import eu.europeana.corelib.neo4j.Neo4jSearchService;
import eu.europeana.corelib.neo4j.entity.Neo4jBean;
import eu.europeana.corelib.neo4j.entity.Neo4jStructBean;
import eu.europeana.corelib.neo4j.exception.Neo4JException;
import eu.europeana.corelib.web.exception.ProblemType;
import eu.europeana.corelib.web.utils.RequestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.servlet.ModelAndView;

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

    @Async
    public ModelAndView call(RecordType recordType,
                             String rdfAbout,
                             String profile,
                             String wskey,
                             int limit,
                             int offset,
                             String callback,
                             HttpServletRequest request,
                             HttpServletResponse response,
                             ApiKeyUtils apiKeyUtils,
                             Neo4jSearchService searchService) {

        LOG.debug("Running thread for {}", rdfAbout);

        long selfIndex = 0L;
        long t0        = System.currentTimeMillis();
        ControllerUtils.addResponseHeaders(response);

        limit = Math.min(limit, MAX_LIMIT);

        long          t1 = System.currentTimeMillis();
        LimitResponse limitResponse;

        try {
            limitResponse = apiKeyUtils.checkLimit(wskey, request.getRequestURL().toString(), recordType);
        } catch (ApiKeyException e) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return JsonUtils.toJson(new ApiError(wskey, e), callback);
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
        try {
            hierarchicalResult.self = searchService.getSingle(rdfAbout);
            if (hierarchicalResult.self != null) {
                if (hierarchicalResult.self.hasChildren() && hierarchicalResult.self.getChildrenCount() == 0) {
                    throw new Neo4JException(ProblemType.NEO4J_502_BAD_DATA);
                }
                selfIndex = hierarchicalResult.self.getIndex();
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
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    }
                } else {
                    hierarchicalResult.message = "This record has no children";
                    hierarchicalResult.success = false;
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                }
            } else if (recordType.equals(RecordType.HIERARCHY_PARENT)) {
                if (hierarchicalResult.self == null || StringUtils.isBlank(hierarchicalResult.self.getParent())) {
                    hierarchicalResult.message = "This record has no parent";
                    hierarchicalResult.success = false;
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                } else {
                    hierarchicalResult.parent = searchService.getSingle(hierarchicalResult.self.getParent());
                }
            } else if (recordType.equals(RecordType.HIERARCHY_FOLLOWING_SIBLINGS)) {
                hierarchicalResult.followingSiblings = searchService.getFollowingSiblings(rdfAbout,
                                                                                          offset,
                                                                                          limit,
                                                                                          selfIndex);
                if (hierarchicalResult.followingSiblings == null || hierarchicalResult.followingSiblings.isEmpty()) {
                    hierarchicalResult.message = "This record has no following siblings";
                    hierarchicalResult.success = false;
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                }
            } else if (recordType.equals(RecordType.HIERARCHY_PRECEDING_SIBLINGS)) {
                hierarchicalResult.precedingSiblings = searchService.getPrecedingSiblings(rdfAbout,
                                                                                          offset,
                                                                                          limit,
                                                                                          selfIndex);
                if (hierarchicalResult.precedingSiblings == null || hierarchicalResult.precedingSiblings.isEmpty()) {
                    hierarchicalResult.message = "This record has no preceding siblings";
                    hierarchicalResult.success = false;
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                }
            } else if (recordType.equals(RecordType.HIERARCHY_ANCESTOR_SELF_SIBLINGS)) {
                Neo4jStructBean struct = searchService.getInitialStruct(rdfAbout, selfIndex);
                if (struct == null) {
                    hierarchicalResult.message = "No hierarchical information found for record " + rdfAbout;
                    hierarchicalResult.success = false;
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                } else {
                    String  partialErrorMsg = "This record has no";
                    boolean hasParent       = true;
                    boolean hasFollowing    = true;
                    boolean hasPreceding    = true;
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
                    if (!hasParent) {
                        partialErrorMsg += " parent";
                        if (!hasFollowing || !hasPreceding) {
                            partialErrorMsg += " or";
                        }
                    }
                    if (!hasFollowing && !hasPreceding) {
                        partialErrorMsg += " siblings";
                    } else if (hasFollowing && !hasPreceding) {
                        partialErrorMsg += " preceding siblings";
                    } else if (!hasFollowing) {
                        partialErrorMsg += " following siblings";
                    }
                    if (!hasParent || !hasFollowing || !hasPreceding) {
                        hierarchicalResult.message = partialErrorMsg;
                    }

                    if (struct.getPrecedingSiblingChildren() != null &&
                        !struct.getPrecedingSiblingChildren().isEmpty()) {
                        hierarchicalResult.precedingSiblingChildren = struct.getPrecedingSiblingChildren();
                    }
                    if (struct.getFollowingSiblingChildren() != null &&
                        !struct.getFollowingSiblingChildren().isEmpty()) {
                        hierarchicalResult.followingSiblingChildren = struct.getFollowingSiblingChildren();
                    }
                }
            }
        } catch (Neo4JException e) {
            hierarchicalResult.success = false;
            String message;

            if (e.getProblem().equals(ProblemType.NEO4J_404)) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                message = "Requested hierarchical information not found for record " + rdfAbout;
                LOG.debug(message);
            } else if (e.getProblem().equals(ProblemType.NEO4J_502_BAD_DATA)) {
                response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
                message = "Inconsistency found in hierarchical data for record " + rdfAbout;
                LOG.warn(message);
            } else if (e.getProblem().equals(ProblemType.NEO4J_503_CONNECTION)) {
                response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                message = "Couldn't connect to Neo4j hierarchical database";
                if (null != e.getCause()) LOG.error(message + ". Cause: {}", e.getCause());
                else LOG.error(message);
            } else if (e.getProblem().equals(ProblemType.NEO4J_500)) {
                response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
                message = "Could not process request because Neo4j hierarchical database returned an error";
                LOG.error(message);
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                message = "Error processing hierarchical request";
                LOG.error(message);
            }
            return JsonUtils.toJson(new ApiError(wskey, message), callback);
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

}
