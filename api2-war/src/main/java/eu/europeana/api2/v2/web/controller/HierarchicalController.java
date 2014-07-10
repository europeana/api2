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

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import eu.europeana.api2.model.enums.ApiLimitException;
import eu.europeana.api2.model.json.ApiError;
import eu.europeana.api2.utils.JsonUtils;
import eu.europeana.api2.v2.model.LimitResponse;
import eu.europeana.api2.v2.model.json.HierarchicalResult;
import eu.europeana.api2.v2.utils.ControllerUtils;
import eu.europeana.corelib.db.entity.enums.RecordType;
import eu.europeana.corelib.db.service.ApiKeyService;
import eu.europeana.corelib.db.service.ApiLogService;
import eu.europeana.corelib.logging.Log;
import eu.europeana.corelib.logging.Logger;
import eu.europeana.corelib.neo4j.entity.Neo4jBean;
import eu.europeana.corelib.solr.service.SearchService;
import eu.europeana.corelib.utils.service.OptOutService;
import eu.europeana.corelib.web.service.EuropeanaUrlService;
import eu.europeana.corelib.web.utils.RequestUtils;

/**
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 */
@Controller
@RequestMapping(value = "/v2/record")
public class HierarchicalController {

	@Log
	private Logger log;

	@Resource
	private SearchService searchService;

	@Resource
	private ApiLogService apiLogService;

	@Resource
	private ApiKeyService apiService;

	@Resource
	private OptOutService optOutService;
	
	@Resource
	private EuropeanaUrlService urlService;

	@Resource
	private ControllerUtils controllerUtils;

	@RequestMapping(value = "/{collectionId}/{recordId}/hierarchy.json", method = RequestMethod.GET, 
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ModelAndView record(
			@PathVariable String collectionId,
			@PathVariable String recordId,
			@RequestParam(value = "profile", required = false, defaultValue = "") String profile,
			@RequestParam(value = "wskey", required = true) String wskey,
			@RequestParam(value = "callback", required = false) String callback,
			HttpServletRequest request,
			HttpServletResponse response) {
		controllerUtils.addResponseHeaders(response);

		LimitResponse limitResponse = null;
		try {
			limitResponse = controllerUtils.checkLimit(wskey, request.getRequestURL().toString(),
					"record.json", RecordType.OBJECT, profile);
		} catch (ApiLimitException e) {
			response.setStatus(e.getHttpStatus());
			return JsonUtils.toJson(new ApiError(e), callback);
		}

		HierarchicalResult objectResult = new HierarchicalResult(wskey, "record.json", limitResponse.getRequestNumber());
		if (StringUtils.containsIgnoreCase(profile, "params")) {
			objectResult.addParams(RequestUtils.getParameterMap(request), "wskey");
			objectResult.addParam("profile", profile);
		}

		String europeanaObjectId = "/" + collectionId + "/" + recordId;
		long t0 = (new Date()).getTime();
		Neo4jBean bean = searchService.getHierarchicalBean(europeanaObjectId);

		if (bean == null) {
			return JsonUtils.toJson(new ApiError(wskey, "record.json", "Invalid record identifier: "
					+ europeanaObjectId, limitResponse.getRequestNumber()), callback);
		}

		objectResult.object = bean;
		long t1 = (new Date()).getTime();
		objectResult.statsDuration = (t1 - t0);

		return JsonUtils.toJson(objectResult, callback);
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
		controllerUtils.addResponseHeaders(response);

		LimitResponse limitResponse = null;
		try {
			limitResponse = controllerUtils.checkLimit(wskey, request.getRequestURL().toString(),
					"record.json", RecordType.HIERARCHY_CHILDREN, profile);
		} catch (ApiLimitException e) {
			response.setStatus(e.getHttpStatus());
			return JsonUtils.toJson(new ApiError(e), callback);
		}

		HierarchicalResult objectResult = new HierarchicalResult(wskey, "children.json", limitResponse.getRequestNumber());
		if (StringUtils.containsIgnoreCase(profile, "params")) {
			objectResult.addParams(RequestUtils.getParameterMap(request), "wskey");
			objectResult.addParam("profile", profile);
		}

		String europeanaObjectId = "/" + collectionId + "/" + recordId;
		long t0 = (new Date()).getTime();
		List<Neo4jBean> beans = searchService.getChildren(europeanaObjectId, offset, limit);

		if (beans == null) {
			return JsonUtils.toJson(new ApiError(wskey, "children.json", "Invalid record identifier: "
					+ europeanaObjectId, limitResponse.getRequestNumber()), callback);
		}

		objectResult.children = beans;
		long t1 = (new Date()).getTime();
		objectResult.statsDuration = (t1 - t0);

		return JsonUtils.toJson(objectResult, callback);
	}
}