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
import eu.europeana.api2.v2.model.json.BasicObjectResult;
import eu.europeana.corelib.db.exception.DatabaseException;
import eu.europeana.corelib.db.exception.LimitReachedException;
import eu.europeana.corelib.db.service.ApiKeyService;
import eu.europeana.corelib.definitions.db.entity.relational.ApiKey;
import eu.europeana.corelib.definitions.edm.beans.FullBean;
import eu.europeana.corelib.definitions.exception.Neo4JException;
import eu.europeana.corelib.edm.exceptions.MongoDBException;
import eu.europeana.corelib.edm.exceptions.MongoRuntimeException;
import eu.europeana.corelib.edm.exceptions.SolrTypeException;
import eu.europeana.corelib.search.SearchService;
import io.swagger.annotations.ApiOperation;
import org.apache.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;

/**
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 */
@Controller
@RequestMapping(value = "/v2/basicrecord")
public class BasicObjectController {

    @Resource
    private ApiKeyService apiKeyService;

    @Resource
    private SearchService searchService;

    private Logger log = Logger.getLogger(BasicObjectController.class);

    @ApiOperation(value = "returns single item")
    @RequestMapping(value = "/{collectionId}/{recordId}.json", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ModelAndView record(
            @PathVariable String collectionId,
            @PathVariable String recordId,
            @RequestParam(value = "profile", required = false, defaultValue = "full") String profile,
            @RequestParam(value = "wskey", required = true) String wskey,
            @RequestParam(value = "callback", required = false) String callback,
            HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");

        String europeanaObjectId = "/" + collectionId + "/" + recordId;
        ApiKey apiKey;
        long requestNumber = 0;

        try {
            apiKey = apiKeyService.findByID(wskey);
            if (apiKey == null) {
                return JsonUtils.toJson(new ApiError(wskey, "Unregistered user"), callback);
            }
            apiKey.getUsageLimit();
            requestNumber = apiKeyService.checkReachedLimit(apiKey);
        } catch (DatabaseException e) {
            // Disabled while awaiting better implementation (ticket #1742)
            //			apiLogService.logApiRequest(wskey, requestUri, RecordType.OBJECT, profile);
            return JsonUtils.toJson(new ApiError(wskey, e.getMessage(), requestNumber), callback);
        } catch (LimitReachedException e) {
            // Disabled while awaiting better implementation (ticket #1742)
            //			apiLogService.logApiRequest(wskey, requestUri, RecordType.LIMIT, profile);
            return JsonUtils.toJson(new ApiError(wskey, e.getMessage(), e.getRequested()), callback);
        }

        BasicObjectResult objectResult = new BasicObjectResult(wskey, requestNumber);

        objectResult.object = new HashMap<>();

        try {
            FullBean bean = searchService.findById(europeanaObjectId, true);
            if (bean == null) {
                bean = searchService .resolve(europeanaObjectId, true);
            }

            if (bean == null) {
                // Disabled while awaiting better implementation (ticket #1742)
                // apiLogService.logApiRequest(wskey, requestUri, RecordType.LIMIT, profile);
                response.setStatus(404);
                return JsonUtils.toJson(new ApiError(wskey,
                        "Invalid record identifier: " + europeanaObjectId, requestNumber), callback);
            }

        } catch (SolrTypeException e) {
            return JsonUtils.toJson(new ApiError(wskey, e.getMessage(), requestNumber), callback);
        } catch (MongoDBException e) {
            return JsonUtils.toJson(new ApiError(wskey, e.getMessage(), requestNumber), callback);
        } catch (MongoRuntimeException re) {
            return JsonUtils.toJson(new ApiError(wskey, re.getMessage(), requestNumber), callback);
        } catch (Neo4JException e) {
            log.error("Neo4JException thrown: " + e.getMessage());
            log.error("Cause: " + e.getCause());
        }

        return JsonUtils.toJson(objectResult, callback);
    }

}
