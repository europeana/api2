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

package eu.europeana.api2.v2.web.controller.sugarcrm;

import java.security.Principal;
import java.util.Date;
import javax.annotation.Resource;
import eu.europeana.corelib.logging.Logger;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import eu.europeana.api2.utils.JsonUtils;
import eu.europeana.api2.v2.model.json.sugarcrm.DataSet;
import eu.europeana.api2.v2.model.json.sugarcrm.Provider;
import eu.europeana.api2.v2.model.json.sugarcrm.SugarCRMSearchResults;
import eu.europeana.corelib.db.service.ApiKeyService;
import eu.europeana.corelib.db.service.ApiLogService;
import eu.europeana.corelib.definitions.db.entity.relational.ApiKey;
import eu.europeana.corelib.logging.Log;

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
	
	
	/**
	 * Returns the list of Europeana providers. The response is an Array of JSON
	 * objects, each one containing the identifier and the name of a provider.
	 * 
	 * @param callback
	 * @param principal
	 * @return
	 */
	@RequestMapping(value = "/v2/providers.json", produces = MediaType.APPLICATION_JSON_VALUE, method = {
			RequestMethod.POST, RequestMethod.GET })
	public ModelAndView findproviders(
			@RequestParam(value = "wskey", required = false) String wskey,
			@RequestParam(value = "callback", required = false) String callback,
			@RequestParam(value = "countryCode", required = false) String countryCode,
			@RequestParam(value = "offset", required = false) String offset,
			@RequestParam(value = "pagesize", required = false) String pagesize,
			Principal principal) {

		Date starttime = new Date();
		SugarCRMSearchResults<Provider> response = null;
		
		try {
			ApiKey apiKey = apiService.findByID(wskey);
			if (apiKey == null) {
				throw new Exception("API key not found");
			}
			apiService.checkReachedLimit(apiKey);
			
			int intOffset = offset== null ?0 :Integer.parseInt(offset);
			int intPagesize = offset== null ?0 :Integer.parseInt(offset);
			
			response = sugarCRMCache.getProviders(countryCode,intOffset,intPagesize);
			response.action = "/v2/providers.json";
			response.apikey = wskey;
			response.itemsCount = response.items.size();
			//response.totalResults = response.items.size();
			response.statsStartTime = starttime;
			Date endtime = new Date();
			response.statsDuration = endtime.getTime() - starttime.getTime();

		} catch (Exception e) {
			response = new SugarCRMSearchResults<Provider>(wskey,
					"/v2/providers.json");
			response.error = "Error fetching all providers "
					+ e.getMessage();
			response.success = false;
			log.error("Error fetching all providers ", e);
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
	 * @return
	 */
	@RequestMapping(value = "/v2/provider/{id}/providers.json", produces = MediaType.APPLICATION_JSON_VALUE, method = {
			RequestMethod.POST, RequestMethod.GET })
	public ModelAndView findprovidersByID(
			@PathVariable  String id,
			@RequestParam(value = "wskey", required = false) String wskey,
			@RequestParam(value = "callback", required = false) String callback,
			Principal principal) {

		Date starttime = new Date();
		SugarCRMSearchResults<Provider> response = null;

		try {
			ApiKey apiKey = apiService.findByID(wskey);
			if (apiKey == null) {
				throw new Exception("API key not found");
			}
			apiService.checkReachedLimit(apiKey);
			response = sugarCRMCache.getProviderbyID(id);
			response.action = "/v2/provider/"+id+"/providers.json";
			response.apikey = wskey;
			response.itemsCount = response.items.size();
			response.totalResults = response.items.size();
			response.statsStartTime = starttime;
			Date endtime = new Date();
			response.statsDuration = endtime.getTime() - starttime.getTime();
			response.success = true;
		} catch (Exception e) {
			response = new SugarCRMSearchResults<Provider>(wskey,
					"/v2/provider/"+id+"/providers.json");
			response.error = "Error fetching provider by id"
					+ e.getMessage();
			response.success = false;
			
			log.error("Error fetching provider by id", e);
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
	 * @return
	 */
	@RequestMapping(value = "/v2/provider/{id}/datasets.json", produces = MediaType.APPLICATION_JSON_VALUE, method = {
			RequestMethod.POST, RequestMethod.GET })
	public ModelAndView findDatasetsPerProvider(
			@PathVariable  String id,
			@RequestParam(value = "wskey", required = false) String wskey,
			@RequestParam(value = "callback", required = false) String callback,
			Principal principal) {

		Date starttime = new Date();
		SugarCRMSearchResults<DataSet> response = null;
		try {
			ApiKey apiKey = apiService.findByID(wskey);
			if (apiKey == null) {
				throw new Exception("API key not found");
			}
			apiService.checkReachedLimit(apiKey);
			response = sugarCRMCache.getCollectionByProviderID(id);
			response.action = "/v2/provider/"+id+"/datasets.json";
			response.apikey = wskey;
			response.itemsCount = response.items.size();
			response.totalResults = response.items.size();
			response.statsStartTime = starttime;
			Date endtime = new Date();
			response.statsDuration = endtime.getTime() - starttime.getTime();
			response.success = true;
		} catch (Exception e) {
			response = new SugarCRMSearchResults<DataSet>(wskey,
					"/v2/provider/"+id+"/datasets.json");
			response.error = "Error fetching datasets by provider id"
					+ e.getMessage();
			response.success = false;
			log.error("Error fetching datasets by provider id", e);
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
	 * @return
	 */
	@RequestMapping(value = "/v2/dataset/{id}.json", produces = MediaType.APPLICATION_JSON_VALUE, method = {
			RequestMethod.POST, RequestMethod.GET })
	public ModelAndView findDatasetsById(
			@PathVariable  String id,
			@RequestParam(value = "wskey", required = false) String wskey,
			@RequestParam(value = "callback", required = false) String callback,
			Principal principal) {

		Date starttime = new Date();
		SugarCRMSearchResults<DataSet> response = null;

		try {
			ApiKey apiKey = apiService.findByID(wskey);
			if (apiKey == null) {
				throw new Exception("API key not found");
			}
			apiService.checkReachedLimit(apiKey);
			response = sugarCRMCache.getCollectionByID(id);
			response.action = "/v2/dataset/"+id+".json";
			response.apikey = wskey;
			response.itemsCount = response.items.size();
			response.totalResults = response.items.size();
			response.statsStartTime = starttime;
			Date endtime = new Date();
			response.statsDuration = endtime.getTime() - starttime.getTime();
			response.success = true;
		} catch (Exception e) {
			response = new SugarCRMSearchResults<DataSet>(wskey, "/v2/dataset/"+id+".json");
			response.success = false;
			response.error = "Error fetching datasets by dataset id"
					+ e.getMessage();
			log.error("Error fetching datasets by dataset id", e);
		}

		return JsonUtils.toJson(response, callback);
	}

	



	
	


}
