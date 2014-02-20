/*
 * Copyright 2007 EDL FOUNDATION
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * you may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package eu.europeana.api2.v2.web.controller;

import java.util.Date;

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

import eu.europeana.api2.model.enums.ApiLimitException;
import eu.europeana.api2.model.json.ApiError;
import eu.europeana.api2.utils.JsonUtils;
import eu.europeana.api2.v2.model.LimitResponse;
import eu.europeana.api2.v2.model.json.sugarcrm.DataSet;
import eu.europeana.api2.v2.model.json.sugarcrm.Provider;
import eu.europeana.api2.v2.model.json.sugarcrm.SugarCRMSearchResults;
import eu.europeana.api2.v2.service.SugarCRMCache;
import eu.europeana.api2.v2.utils.ControllerUtils;
import eu.europeana.corelib.db.entity.enums.RecordType;
import eu.europeana.corelib.db.service.ApiKeyService;
import eu.europeana.corelib.db.service.ApiLogService;
import eu.europeana.corelib.logging.Log;
import eu.europeana.corelib.logging.Logger;

/**
 * Controller for providing Provider/DataSet specific information contained into
 * SugarCRM
 * 
 * @author Georgios Markakis (gwarkx@hotmail.com)
 * 
 * @since Sep 24, 2013
 */
@Controller
public class SugarCRMController {

	@Log
	private Logger log;

	@Resource
	private SugarCRMCache sugarCRMCache;

	@Resource
	private ApiKeyService apiService;

	@Resource
	private ApiLogService apiLogService;

	@Resource
	private ControllerUtils controllerUtils;

	/**
	 * Returns the list of Europeana providers. The response is an Array of JSON
	 * objects, each one containing the identifier and the name of a provider.
	 * 
	 * @param wskey
	 * @param callback
	 * @param countryCode
	 * @param offset
	 * @param pagesize
	 * @param principal
	 * 
	 * @return the JSON response
	 */
	@RequestMapping(value = "/v2/providers.json", produces = MediaType.APPLICATION_JSON_VALUE, 
			method = {RequestMethod.POST, RequestMethod.GET })
	public ModelAndView findproviders(
			@RequestParam(value = "wskey", required = false) String wskey,
			@RequestParam(value = "callback", required = false) String callback,
			@RequestParam(value = "countryCode", required = false) String countryCode,
			@RequestParam(value = "offset", required = false) String offset,
			@RequestParam(value = "pagesize", required = false) String pagesize,
			HttpServletRequest request,
			HttpServletResponse httpResponse) {
		httpResponse.setCharacterEncoding("UTF-8");

		Date starttime = new Date();
		SugarCRMSearchResults<Provider> response = null;

		LimitResponse limitResponse = null;
		try {
			limitResponse = controllerUtils.checkLimit(wskey, request.getRequestURL().toString(),
					"providers.json", RecordType.PROVIDERS, null);
		} catch (ApiLimitException e) {
			httpResponse.setStatus(e.getHttpStatus());
			return JsonUtils.toJson(new ApiError(e), callback);
		}

		String action = "/v2/providers.json";
		try {
			int intOffset = offset == null ? 0 : Integer.parseInt(offset);
			int intPagesize = pagesize == null ? 0 : Integer.parseInt(pagesize);

			response = sugarCRMCache.getProviders(countryCode, intOffset, intPagesize);
			response.action = action;
			response.apikey = wskey;
			response.requestNumber = limitResponse.getRequestNumber();
			response.itemsCount = response.items.size();
			response.statsStartTime = starttime;
			Date endtime = new Date();
			response.statsDuration = endtime.getTime() - starttime.getTime();
		} catch (Exception e) {
			String error = "Error fetching all providers";
			log.error(error, e);
			return JsonUtils.toJson(new ApiError(wskey, action, error + " " + e.getMessage(), limitResponse.getRequestNumber()), callback);
		}

		return JsonUtils.toJson(response, callback);
	}

	/**
	 * Returns information about a single Europeana provider identified by
	 * provider_id. The response contains the following fields: identifier,
	 * name, description, website, country.
	 * 
	 * @param query
	 * @param callback
	 * @param principal
	 * @return the JSON response
	 */
	@RequestMapping(value = "/v2/provider/{id}.json", produces = MediaType.APPLICATION_JSON_VALUE, 
			method = {RequestMethod.POST, RequestMethod.GET })
	public ModelAndView findprovidersByID(
			@PathVariable String id,
			@RequestParam(value = "wskey", required = false) String wskey,
			@RequestParam(value = "callback", required = false) String callback,
			HttpServletRequest request,
			HttpServletResponse httpResponse) {
		httpResponse.setCharacterEncoding("UTF-8");

		Date starttime = new Date();
		SugarCRMSearchResults<Provider> response = null;

		LimitResponse limitResponse = null;
		try {
			limitResponse = controllerUtils.checkLimit(wskey, request.getRequestURL().toString(),
					"provider.json", RecordType.PROVIDER, null);
		} catch (ApiLimitException e) {
			httpResponse.setStatus(e.getHttpStatus());
			return JsonUtils.toJson(new ApiError(e), callback);
		}

		String action = "/v2/provider/" + id + ".json";
		try {
			response = sugarCRMCache.getProviderbyID(id);
			response.action = action;
			response.apikey = wskey;
			response.requestNumber = limitResponse.getRequestNumber();
			response.itemsCount = response.items.size();
			response.totalResults = response.items.size();
			response.statsStartTime = starttime;
			Date endtime = new Date();
			response.statsDuration = endtime.getTime() - starttime.getTime();
			response.success = true;
		} catch (Exception e) {
			String error = "Error fetching all providers";
			log.error(error, e);
			return JsonUtils.toJson(new ApiError(wskey, action, error + " " + e.getMessage(), limitResponse.getRequestNumber()), callback);
		}

		return JsonUtils.toJson(response, callback);
	}

	/**
	 * Returns the list of datasets provided by the provider. The response is an
	 * Array of JSON objects, each one containing the identifier, the name, and
	 * the full id (composed of the identifier and the name) of a dataset.
	 * 
	 * @param query
	 * @param callback
	 * @param principal
	 * @return the JSON response
	 */
	@RequestMapping(value = "/v2/provider/{id}/datasets.json", produces = MediaType.APPLICATION_JSON_VALUE, method = {
			RequestMethod.POST, RequestMethod.GET })
	public ModelAndView findDatasetsPerProvider(
			@PathVariable String id,
			@RequestParam(value = "wskey", required = false) String wskey,
			@RequestParam(value = "callback", required = false) String callback,
			HttpServletRequest request,
			HttpServletResponse httpResponse) {
		httpResponse.setCharacterEncoding("UTF-8");

		Date starttime = new Date();
		SugarCRMSearchResults<DataSet> response = null;

		LimitResponse limitResponse = null;
		try {
			limitResponse = controllerUtils.checkLimit(wskey, request.getRequestURL().toString(),
					"provider/datasets.json", RecordType.PROVIDER_DATASETS, null);
		} catch (ApiLimitException e) {
			httpResponse.setStatus(e.getHttpStatus());
			return JsonUtils.toJson(new ApiError(e), callback);
		}

		String action = "/v2/provider/" + id + "/datasets.json";
		try {
			response = sugarCRMCache.getCollectionByProviderID(id);
			response.action = action;
			response.apikey = wskey;
			response.requestNumber = limitResponse.getRequestNumber();
			response.itemsCount = response.items.size();
			response.totalResults = response.items.size();
			response.statsStartTime = starttime;
			Date endtime = new Date();
			response.statsDuration = endtime.getTime() - starttime.getTime();
			response.success = true;
		} catch (Exception e) {
			String error = "Error fetching datasets by provider id";
			log.error(error, e);
			return JsonUtils.toJson(new ApiError(wskey, action, error + " " + e.getMessage(), limitResponse.getRequestNumber()), callback);
		}

		return JsonUtils.toJson(response, callback);
	}


	/**
	 * Returns the list of Europeana datasets. The response is an Array of JSON
	 * objects, each one containing the identifier and the name of a dataset.
	 * 
	 * @param wskey
	 * @param callback
	 * @param offset
	 * @param pagesize
	 * @param principal
	 * 
	 * @return the JSON response
	 */
	@RequestMapping(value = "/v2/datasets.json", produces = MediaType.APPLICATION_JSON_VALUE, method = {
			RequestMethod.POST, RequestMethod.GET })
	public ModelAndView findDatasets(
			@RequestParam(value = "wskey", required = false) String wskey,
			@RequestParam(value = "callback", required = false) String callback,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "countryCode", required = false) String country,
			@RequestParam(value = "status", required = false) String status,
			@RequestParam(value = "offset", required = false) String offset,
			@RequestParam(value = "pagesize", required = false) String pagesize,
			HttpServletRequest request,
			HttpServletResponse httpResponse) {
		httpResponse.setCharacterEncoding("UTF-8");

		Date starttime = new Date();
		SugarCRMSearchResults<DataSet> response = null;

		LimitResponse limitResponse = null;
		try {
			limitResponse = controllerUtils.checkLimit(wskey, request.getRequestURL().toString(),
					"datasets.json", RecordType.DATASETS, null);
		} catch (ApiLimitException e) {
			httpResponse.setStatus(e.getHttpStatus());
			return JsonUtils.toJson(new ApiError(e), callback);
		}

		String action = "/v2/datasets.json";
		try {
			int intOffset = offset == null ? 0 : Integer.parseInt(offset);
			int intPagesize = pagesize == null ? 0 : Integer.parseInt(pagesize);

			response = sugarCRMCache.getCollections(intOffset, intPagesize, name, country, status);
			response.action = action;
			response.apikey = wskey;
			response.requestNumber = limitResponse.getRequestNumber();
			response.itemsCount = response.items.size();
			response.statsStartTime = starttime;
			Date endtime = new Date();
			response.statsDuration = endtime.getTime() - starttime.getTime();

		} catch (Exception e) {
			String error = "Error fetching all datasets";
			log.error(error, e);
			return JsonUtils.toJson(new ApiError(wskey, action, error + " " + e.getMessage(), limitResponse.getRequestNumber()), callback);
		}

		return JsonUtils.toJson(response, callback);
	}

	/**
	 * Returns information about a dataset identified by dataset_id. The
	 * response contains the following fields: identifier, name, description,
	 * status,
	 * 
	 * @param wskey
	 * @param id
	 * @param callback
	 * @param principal
	 * @return the JSON response
	 */
	@RequestMapping(value = "/v2/dataset/{id}.json", produces = MediaType.APPLICATION_JSON_VALUE, method = {
			RequestMethod.POST, RequestMethod.GET })
	public ModelAndView findDatasetsById(
			@PathVariable String id,
			@RequestParam(value = "wskey", required = false) String wskey,
			@RequestParam(value = "callback", required = false) String callback,
			HttpServletRequest request,
			HttpServletResponse httpResponse) {
		httpResponse.setCharacterEncoding("UTF-8");

		Date starttime = new Date();
		SugarCRMSearchResults<DataSet> response = null;

		LimitResponse limitResponse = null;
		try {
			limitResponse = controllerUtils.checkLimit(wskey, request.getRequestURL().toString(),
					"datasets.json", RecordType.DATASETS, null);
		} catch (ApiLimitException e) {
			httpResponse.setStatus(e.getHttpStatus());
			return JsonUtils.toJson(new ApiError(e), callback);
		}

		String action = "/v2/dataset/" + id + ".json";
		try {
			response = sugarCRMCache.getCollectionByID(id);
			response.action = action;
			response.apikey = wskey;
			response.requestNumber = limitResponse.getRequestNumber();
			response.itemsCount = response.items.size();
			response.totalResults = response.items.size();
			response.statsStartTime = starttime;
			Date endtime = new Date();
			response.statsDuration = endtime.getTime() - starttime.getTime();
			response.success = true;
		} catch (Exception e) {
			String error = "Error fetching datasets by dataset id";
			log.error(error, e);
			return JsonUtils.toJson(new ApiError(wskey, action, error + " " + e.getMessage(), limitResponse.getRequestNumber()), callback);
		}

		return JsonUtils.toJson(response, callback);
	}
}