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

import eu.europeana.api2.ApiLimitException;
import eu.europeana.api2.model.json.ApiError;
import eu.europeana.api2.utils.JsonUtils;
import eu.europeana.api2.v2.model.LimitResponse;
import eu.europeana.api2.v2.model.json.sugarcrm.DataSet;
import eu.europeana.api2.v2.model.json.sugarcrm.Provider;
import eu.europeana.api2.v2.model.json.sugarcrm.SugarCRMSearchResults;
import eu.europeana.api2.v2.service.SugarCRMCache;
import eu.europeana.api2.v2.utils.ApiKeyUtils;
import eu.europeana.api2.v2.utils.ControllerUtils;
import eu.europeana.api2.v2.web.swagger.SwaggerSelect;
import eu.europeana.corelib.db.entity.enums.RecordType;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import java.util.Date;

/**
 * Controller for providing Provider/DataSet specific information contained into
 * SugarCRM
 *
 * @author Georgios Markakis (gwarkx@hotmail.com)
 * @since Sep 24, 2013
 * @deprecated July 2018 replaced by Metis
 */
//@Controller
//@Api(tags = {"Providers and Datasets"})
//@SwaggerSelect
@Deprecated
public class SugarCRMController {

	private static final Logger LOG = LogManager.getLogger(SugarCRMController.class);

	@Resource
	private SugarCRMCache sugarCRMCache;

	@Resource
	private ApiKeyUtils apiKeyUtils;

    /**
     * Returns the list of Europeana providers. The response is an Array of JSON objects, each one containing the identifier
     * and the name of a provider.
     *
     * @param wskey
     * @param callback
     * @param countryCode
     * @param offset
     * @param pagesize
     * @param request
     * @param httpResponse
     * @return JSON response
     */
//	@ApiOperation(value = "get the list of Europeana data providers", nickname = "listProviders")
//	@RequestMapping(value = "/v2/providers.json", method = {RequestMethod.GET}, produces = MediaType.APPLICATION_JSON_VALUE)
	public ModelAndView findproviders(
			@RequestParam(value = "wskey", required = false) String wskey,
			@RequestParam(value = "callback", required = false) String callback,
			@RequestParam(value = "countryCode", required = false) String countryCode,
			@RequestParam(value = "offset", required = false) String offset,
			@RequestParam(value = "pagesize", required = false) String pagesize,
			HttpServletRequest request,
			HttpServletResponse httpResponse) throws ApiLimitException {

		LimitResponse apiKeyCheck = apiKeyUtils.checkLimit(wskey, request.getRequestURL().toString(), RecordType.PROVIDERS, null);
		ControllerUtils.addResponseHeaders(httpResponse);


		Date starttime = new Date();
		SugarCRMSearchResults<Provider> response;
		try {
			int intOffset = offset == null ? 0 : Integer.parseInt(offset);
			int intPagesize = pagesize == null ? 0 : Integer.parseInt(pagesize);
			response = sugarCRMCache.getProviders(countryCode, intOffset, intPagesize);
            response = fillBasicResponse(response, wskey, apiKeyCheck, starttime);
		} catch (Exception e) {
			String error = "Error fetching all providers";
			LOG.error(error, e);
			return JsonUtils.toJson(new ApiError(wskey, error + " " + e.getMessage(), apiKeyCheck.getRequestNumber()), callback);
		}

		return JsonUtils.toJson(response, callback);
	}

	private SugarCRMSearchResults fillBasicResponse(SugarCRMSearchResults<?> response, String wskey, LimitResponse apiKeyCheck, Date startTime) {
        response.apikey = wskey;
        response.requestNumber = apiKeyCheck.getRequestNumber();
        response.itemsCount = response.items.size();
        response.statsStartTime = startTime;
        response.statsDuration = new Date().getTime() - startTime.getTime();
        response.success = true;
        return response;
    }

    /**
     * Returns information about a single Europeana provider identified by
     * provider_id. The response contains the following fields: identifier,
     * name, description, website, country.
     * @param id
     * @param wskey
     * @param callback
     * @param request
     * @param httpResponse
     * @return JSON response
     */
//	@ApiOperation(value = "get information about a specific Europeana provider", nickname = "getProvider")
//	@RequestMapping(value = "/v2/provider/{id}.json", method = {RequestMethod.GET}, produces = MediaType.APPLICATION_JSON_VALUE)
	public ModelAndView findprovidersByID(
			@PathVariable String id,
			@RequestParam(value = "wskey", required = false) String wskey,
			@RequestParam(value = "callback", required = false) String callback,
			HttpServletRequest request,
			HttpServletResponse httpResponse) throws ApiLimitException {

		LimitResponse apiKeyCheck = apiKeyUtils.checkLimit(wskey, request.getRequestURL().toString(), RecordType.PROVIDER, null);
		ControllerUtils.addResponseHeaders(httpResponse);

		Date starttime = new Date();
		SugarCRMSearchResults<Provider> response;
		try {
			response = sugarCRMCache.getProviderbyID(id);
            response = fillBasicResponse(response, wskey, apiKeyCheck, starttime);
			response.totalResults = Long.valueOf(response.items.size());
		} catch (Exception e) {
			String error = "Error fetching all providers";
			LOG.error(error, e);
			return JsonUtils.toJson(new ApiError(wskey, error + " " + e.getMessage(), apiKeyCheck.getRequestNumber()), callback);
		}

		return JsonUtils.toJson(response, callback);
	}

    /**
     * Returns the list of datasets provided by the provider. The response is an
     * Array of JSON objects, each one containing the identifier, the name, and
     * the full id (composed of the identifier and the name) of a dataset.
     * @param id
     * @param wskey
     * @param callback
     * @param request
     * @param httpResponse
     * @return JSON response
     */
//	@ApiOperation(value = "get the list of datasets provided by a specific provider", nickname = "listProviderDatasets")
//	@RequestMapping(value = "/v2/provider/{id}/datasets.json", method = {RequestMethod.GET}, produces = MediaType.APPLICATION_JSON_VALUE)
	public ModelAndView findDatasetsPerProvider(
			@PathVariable String id,
			@RequestParam(value = "wskey", required = false) String wskey,
			@RequestParam(value = "callback", required = false) String callback,
			HttpServletRequest request,
			HttpServletResponse httpResponse) throws ApiLimitException {

		LimitResponse apiKeyCheck = apiKeyUtils.checkLimit(wskey, request.getRequestURL().toString(), RecordType.PROVIDER_DATASETS, null);
        ControllerUtils.addResponseHeaders(httpResponse);

		Date starttime = new Date();
		SugarCRMSearchResults<DataSet> response;
		try {
			response = sugarCRMCache.getCollectionByProviderID(id);
            response = fillBasicResponse(response, wskey, apiKeyCheck, starttime);
			response.totalResults = Long.valueOf(response.items.size());
		} catch (Exception e) {
			String error = "Error fetching datasets by provider id";
			LOG.error(error, e);
			return JsonUtils.toJson(new ApiError(wskey, error + " " + e.getMessage(), apiKeyCheck.getRequestNumber()), callback);
		}

		return JsonUtils.toJson(response, callback);
	}


    /**
     * Returns the list of Europeana datasets. The response is an Array of JSON
     * objects, each one containing the identifier and the name of a dataset.
     * @param wskey
     * @param callback
     * @param name
     * @param country
     * @param status
     * @param offset
     * @param pagesize
     * @param request
     * @param httpResponse
     * @return JSON response
     */
//	@ApiOperation(value = "get the list of Europeana datasets", nickname = "listDatasets")
//	@RequestMapping(value = "/v2/datasets.json", method = {RequestMethod.GET}, produces = MediaType.APPLICATION_JSON_VALUE)
	public ModelAndView findDatasets(
			@RequestParam(value = "wskey", required = false) String wskey,
			@RequestParam(value = "callback", required = false) String callback,
			@RequestParam(value = "edmDatasetName", required = false) String name,
			@RequestParam(value = "countryCode", required = false) String country,
			@RequestParam(value = "status", required = false) String status,
			@RequestParam(value = "offset", required = false) String offset,
			@RequestParam(value = "pagesize", required = false) String pagesize,
			HttpServletRequest request,
			HttpServletResponse httpResponse) throws ApiLimitException {

		LimitResponse apiKeyCheck = apiKeyUtils.checkLimit(wskey, request.getRequestURL().toString(), RecordType.DATASETS, null);
        ControllerUtils.addResponseHeaders(httpResponse);

		Date starttime = new Date();
		SugarCRMSearchResults<DataSet> response;
		try {
			int intOffset = offset == null ? 0 : Integer.parseInt(offset);
			int intPagesize = pagesize == null ? 0 : Integer.parseInt(pagesize);

			response = sugarCRMCache.getCollections(intOffset, intPagesize, name, country, status);
            response = fillBasicResponse(response, wskey, apiKeyCheck, starttime);
		} catch (Exception e) {
			String error = "Error fetching all datasets";
			LOG.error(error, e);
			return JsonUtils.toJson(new ApiError(wskey, error + " " + e.getMessage(), apiKeyCheck.getRequestNumber()), callback);
		}

		return JsonUtils.toJson(response, callback);
	}

    /**
     * Returns information about a dataset identified by dataset_id. The
     * response contains the following fields: identifier, name, description,
     * status,
     * @param id
     * @param wskey
     * @param callback
     * @param request
     * @param httpResponse
     * @return JSON response
     */
//	@ApiOperation(value = "get information about a specific dataset", nickname = "getDataset")
//	@RequestMapping(value = "/v2/dataset/{id}.json", method = {RequestMethod.GET}, produces = MediaType.APPLICATION_JSON_VALUE)
	public ModelAndView findDatasetsById(
			@PathVariable String id,
			@RequestParam(value = "wskey", required = false) String wskey,
			@RequestParam(value = "callback", required = false) String callback,
			HttpServletRequest request,
			HttpServletResponse httpResponse) throws ApiLimitException {

		LimitResponse apiKeyCheck = apiKeyUtils.checkLimit(wskey, request.getRequestURL().toString(), RecordType.DATASETS, null);
        ControllerUtils.addResponseHeaders(httpResponse);

		Date starttime = new Date();
		SugarCRMSearchResults<DataSet> response;
		try {
			response = sugarCRMCache.getCollectionByID(id);
            response = fillBasicResponse(response, wskey, apiKeyCheck, starttime);
			response.totalResults = Long.valueOf(response.items.size());
		} catch (Exception e) {
			String error = "Error fetching datasets by dataset id";
			LOG.error(error, e);
			return JsonUtils.toJson(new ApiError(wskey, error + " " + e.getMessage(), apiKeyCheck.getRequestNumber()), callback);
		}

		return JsonUtils.toJson(response, callback);
	}
}
