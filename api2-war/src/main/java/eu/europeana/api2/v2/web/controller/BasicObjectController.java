package eu.europeana.api2.v2.web.controller;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import eu.europeana.api2.model.json.ApiError;
import eu.europeana.api2.utils.JsonUtils;
import eu.europeana.api2.v2.model.json.BasicObjectResult;
import eu.europeana.corelib.db.entity.enums.RecordType;
import eu.europeana.corelib.db.exception.DatabaseException;
import eu.europeana.corelib.db.exception.LimitReachedException;
import eu.europeana.corelib.db.service.ApiKeyService;
import eu.europeana.corelib.db.service.ApiLogService;
import eu.europeana.corelib.definitions.db.entity.relational.ApiKey;
import eu.europeana.corelib.definitions.solr.beans.FullBean;
import eu.europeana.corelib.logging.Log;
import eu.europeana.corelib.logging.Logger;
import eu.europeana.corelib.solr.exceptions.MongoDBException;
import eu.europeana.corelib.solr.exceptions.SolrTypeException;
import eu.europeana.corelib.solr.service.SearchService;

/**
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 */
@Controller
@RequestMapping(value = "/v2/basicrecord")
public class BasicObjectController {

	@Log
	private Logger log;

	@Resource
	private ApiKeyService apiKeyService;

	@Resource
	private ApiLogService apiLogService;

	@Resource
	private SearchService searchService;

	@RequestMapping(value = "/{collectionId}/{recordId}.json", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ModelAndView record(
			@PathVariable String collectionId,
			@PathVariable String recordId,
			@RequestParam(value = "profile", required = false, defaultValue = "full") String profile,
			@RequestParam(value = "wskey", required = true) String wskey,
			@RequestParam(value = "callback", required = false) String callback,
			HttpServletRequest request, HttpServletResponse response) {
		response.setCharacterEncoding("UTF-8");

		String europeanaObjectId = "/" + collectionId + "/" + recordId;
		String requestUri = europeanaObjectId + ".json";
		ApiKey apiKey;
		long requestNumber = 0;

		try {
			apiKey = apiKeyService.findByID(wskey);
			if (apiKey == null) {
				return JsonUtils.toJson(new ApiError(wskey, "record.json", "Unregistered user"), callback);
			}
			apiKey.getUsageLimit();
			requestNumber = apiKeyService.checkReachedLimit(apiKey);
		} catch (DatabaseException e) {
		  // Disabled while awaiting better implementation (ticket #1742)
		  //			apiLogService.logApiRequest(wskey, requestUri, RecordType.OBJECT, profile);
			return JsonUtils.toJson(new ApiError(wskey, "record.json", e.getMessage(),
					requestNumber), callback);
		} catch (LimitReachedException e) {
          // Disabled while awaiting better implementation (ticket #1742)
		  //			apiLogService.logApiRequest(wskey, requestUri, RecordType.LIMIT, profile);
			return JsonUtils.toJson(new ApiError(wskey, "record.json", e.getMessage(), e.getRequested()), callback);
		}
		
		BasicObjectResult objectResult = new BasicObjectResult(wskey, "record.json",
				requestNumber);
		
		objectResult.object = new HashMap<String, Object>();

		try {
			FullBean bean = searchService.findById(europeanaObjectId,true);
			if (bean == null) {
				bean = searchService
						.resolve(europeanaObjectId,true);
			}

			if (bean == null) {
	          // Disabled while awaiting better implementation (ticket #1742)
			  // apiLogService.logApiRequest(wskey, requestUri, RecordType.LIMIT, profile);
				return JsonUtils.toJson(new ApiError(wskey, "record.json",
						"Invalid record identifier: " + europeanaObjectId,
						requestNumber), callback);
			}
			
			flattenBean(bean, objectResult.object);

		} catch (SolrTypeException e) {
			return JsonUtils.toJson(new ApiError(wskey, "record.json", e.getMessage(),
					requestNumber), callback);
		} catch (MongoDBException e) {
			return JsonUtils.toJson(new ApiError(wskey, "record.json", e.getMessage(),
					requestNumber), callback);
		}
		
		return JsonUtils.toJson(objectResult, callback);
	}
	
	private void flattenBean(FullBean bean, Map<String, ?> values) {
//		Map<String,?> props = PropertyUtils.describe(bean);
//		for (Entry<String, ?> entry: props.entrySet()) {
//			if (StringUtils.startsWith(m.getName(), "get")) {
//				if (m.getReturnType() instanceof Map) {
//					
//				}
//			}
//		}
	}
	
}
