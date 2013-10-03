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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.w3c.dom.Element;
import eu.europeana.api2.utils.JsonUtils;
import eu.europeana.api2.v2.model.json.sugarcrm.DataSet;
import eu.europeana.api2.v2.model.json.sugarcrm.Provider;
import eu.europeana.api2.v2.model.json.sugarcrm.SugarCRMSearchResults;
import eu.europeana.uim.sugarcrmclient.enums.EuropeanaRetrievableField;
import eu.europeana.uim.sugarcrmclient.enums.EuropeanaDatasets;
import eu.europeana.uim.sugarcrmclient.enums.EuropeanaUpdatableField;
import eu.europeana.uim.sugarcrmclient.jibxbindings.GetEntryList;
import eu.europeana.uim.sugarcrmclient.jibxbindings.GetEntryListResponse;
import eu.europeana.uim.sugarcrmclient.jibxbindings.GetRelationships;
import eu.europeana.uim.sugarcrmclient.jibxbindings.GetRelationshipsResponse;
import eu.europeana.uim.sugarcrmclient.jibxbindings.SelectFields;
import eu.europeana.uim.sugarcrmclient.ws.SugarWsClient;
import eu.europeana.uim.sugarcrmclient.ws.exceptions.JIXBQueryResultException;
import eu.europeana.uim.sugarcrmclient.internal.helpers.ClientUtils;

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

	@Resource
	private SugarWsClient sugarwsClient;

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
			Principal principal) {

		Date starttime = new Date();
		SugarCRMSearchResults<Provider> response = null;

		try {
			response = retrieveproviders();
			response.action = "/v2/providers.json";
			response.apikey = wskey;
			response.itemsCount = response.items.size();
			response.totalResults = response.items.size();
			response.statsStartTime = starttime;
			Date endtime = new Date();
			response.statsDuration = endtime.getTime() - starttime.getTime();

		} catch (Exception e) {
			response = new SugarCRMSearchResults<Provider>(wskey,
					"/v2/providers.json");
			response.error = "Error querying CRM knowledgebase: "
					+ e.getMessage();
			response.success = false;
			e.printStackTrace();
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
	@RequestMapping(value = "/v2/providers/provider_id.json", produces = MediaType.APPLICATION_JSON_VALUE, method = {
			RequestMethod.POST, RequestMethod.GET })
	public ModelAndView findprovidersByID(
			@RequestParam(value = "wskey", required = false) String wskey,
			@RequestParam(value = "id", required = true) String id,
			@RequestParam(value = "callback", required = false) String callback,
			Principal principal) {

		Date starttime = new Date();
		SugarCRMSearchResults<Provider> response = null;

		try {
			response = retrieveprovider("name_id_c", id);
			response.action = "/v2/providers/provider_id.json";
			response.apikey = wskey;
			response.itemsCount = response.items.size();
			response.totalResults = response.items.size();
			response.statsStartTime = starttime;
			Date endtime = new Date();
			response.statsDuration = endtime.getTime() - starttime.getTime();
			response.success = true;
		} catch (Exception e) {
			response = new SugarCRMSearchResults<Provider>(wskey,
					"/v2/providers/provider_id.json");
			response.error = "Error querying CRM knowledgebase"
					+ e.getMessage();
			response.success = false;
			e.printStackTrace();
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
	@RequestMapping(value = "/v2/datasets/provider_id.json", produces = MediaType.APPLICATION_JSON_VALUE, method = {
			RequestMethod.POST, RequestMethod.GET })
	public ModelAndView findDatasetsPerProvider(
			@RequestParam(value = "wskey", required = false) String wskey,
			@RequestParam(value = "id", required = true) String id,
			@RequestParam(value = "callback", required = false) String callback,
			Principal principal) {

		Date starttime = new Date();
		SugarCRMSearchResults<DataSet> response = null;
		try {
			response = retrieveDatasetByProvider(id);
			response.action = "/v2/datasets/provider_id.json";
			response.apikey = wskey;
			response.itemsCount = response.items.size();
			response.totalResults = response.items.size();
			response.statsStartTime = starttime;
			Date endtime = new Date();
			response.statsDuration = endtime.getTime() - starttime.getTime();
			response.success = true;
		} catch (Exception e) {
			response = new SugarCRMSearchResults<DataSet>(wskey,
					"/v2/datasets/provider_id.json");
			response.error = "Error querying CRM knowledgebase"
					+ e.getMessage();
			response.success = false;
			e.printStackTrace();
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
	@RequestMapping(value = "/v2/datasets/dataset_id.json", produces = MediaType.APPLICATION_JSON_VALUE, method = {
			RequestMethod.POST, RequestMethod.GET })
	public ModelAndView findDatasetsById(
			@RequestParam(value = "wskey", required = false) String wskey,
			@RequestParam(value = "id", required = true) String id,
			@RequestParam(value = "callback", required = false) String callback,
			Principal principal) {

		Date starttime = new Date();
		SugarCRMSearchResults<DataSet> response = null;

		try {
			response = retrieveDataset("name", id);
			response.action = "/v2/datasets/dataset_id.json";
			response.apikey = wskey;
			response.itemsCount = response.items.size();
			response.totalResults = response.items.size();
			response.statsStartTime = starttime;
			Date endtime = new Date();
			response.statsDuration = endtime.getTime() - starttime.getTime();
			response.success = true;
		} catch (Exception e) {
			response = new SugarCRMSearchResults<DataSet>("key", "action");
			response.success = false;
			response.error = "Error querying CRM knowledgebase"
					+ e.getMessage();

			e.printStackTrace();
		}

		return JsonUtils.toJson(response, callback);
	}

	
	/**
	 * Method for retrieving all the providers
	 * 
	 * @return  A Jackson object containing a complete list of the providers
	 * 
	 * @throws JIXBQueryResultException
	 */
	private SugarCRMSearchResults<Provider> retrieveproviders()
			throws JIXBQueryResultException {

		GetEntryList request = new GetEntryList();
		SugarCRMSearchResults<Provider> results = new SugarCRMSearchResults<Provider>("","");
		results.items = new ArrayList<Provider>();
		
		List<String> fields2beretrieved = new ArrayList<String>();
		
		fields2beretrieved.add("description");
		fields2beretrieved.add("name");
		fields2beretrieved.add("name_id_c");
		fields2beretrieved.add("country_c");
		
		SelectFields fields = ClientUtils.generatePopulatedSelectFields(fields2beretrieved);
				
		request.setSelectFields(fields);
		request.setModuleName(EuropeanaDatasets.ORGANIZATIONS.getSysId());
		request.setSession(sugarwsClient.getSessionID());
		request.setOrderBy(EuropeanaRetrievableField.DATE_ENTERED.getFieldId());
		request.setMaxResults(200);
		request.setOffset(0);
		
		request.setQuery("(accounts_cstm.agg_status_c LIKE '%P' OR accounts_cstm.agg_status_c LIKE '%D')");
		GetEntryListResponse response = sugarwsClient.getentrylist(request);
		ArrayList<Element> list = (ArrayList<Element>) response.getReturn()
				.getEntryList().getArray().getAnyList();

		for (Element el : list) {
			Provider prov = new Provider();
			String identifier = ClientUtils.extractFromElement("name_id_c", el);
			//String description = ClientUtils.extractFromElement("description", el);
			String name = ClientUtils.extractFromElement("name", el);
			String country = ClientUtils.extractFromElement("country_c", el);
			// Insert values in Provider Object
			prov.identifier = identifier;
			//prov.description = description;
			prov.description = "Hidden";
			prov.name = name;
			prov.country = country;
			results.items.add(prov);
		}

		return results;
	}
	
	
	
	
	/**
	 * Method used to retrieve providers by ID
	 * 
	 * @param providerID the ID
	 * @return A Jackson object containing results
	 * @throws JIXBQueryResultException
	 */
	private SugarCRMSearchResults<Provider> retrieveprovider(String field,
			String value) throws JIXBQueryResultException {

		GetEntryList request = new GetEntryList();
		SugarCRMSearchResults<Provider> results = new SugarCRMSearchResults<Provider>(
				"", "");
		results.items = new ArrayList<Provider>();

		SelectFields fields = new SelectFields(); // We want to retrieve all
													// fields
		request.setSelectFields(fields);
		request.setModuleName(EuropeanaDatasets.ORGANIZATIONS.getSysId());
		request.setSession(sugarwsClient.getSessionID());
		request.setOrderBy(EuropeanaRetrievableField.DATE_ENTERED.getFieldId());
		request.setMaxResults(100);
		request.setOffset(0);
		request.setQuery("(accounts_cstm." + field + " LIKE '" + value + "%')");
		GetEntryListResponse response = sugarwsClient.getentrylist(request);

		ArrayList<Element> list = null;

		if (response.getReturn().getEntryList().getArray() != null) {
			list = (ArrayList<Element>) response.getReturn().getEntryList()
					.getArray().getAnyList();
		} else {
			list = new ArrayList<Element>();
		}

		for (Element el : list) {

			if (response.getReturn().getEntryList().getArray() != null) {
				Provider prov = new Provider();
				String identifier = ClientUtils.extractFromElement("name_id_c", el);
				//String description = ClientUtils.extractFromElement("description", el);
				String name = ClientUtils.extractFromElement("name", el);
				String country = ClientUtils.extractFromElement("country_c", el);

				// Insert values in Provider Object
				prov.identifier = identifier;
				// prov.description = description;
				prov.description = "Hidden";
				prov.name = name;
				prov.country = country;
				results.items.add(prov);
			}

		}
		return results;

	}



	/**
	 * Method used to retrieve datasets by ID
	 * @param id
	 * @return A Jackson object containing results
	 * @throws JIXBQueryResultException
	 */
	public SugarCRMSearchResults<DataSet> retrieveDataset(String field,
			String value) throws JIXBQueryResultException {

		GetEntryList request = new GetEntryList();
		SugarCRMSearchResults<DataSet> results = new SugarCRMSearchResults<DataSet>(
				"", "");
		results.items = new ArrayList<DataSet>();
		// We want to retrieve all fields
		SelectFields fields = new SelectFields(); 													
		request.setSelectFields(fields);
		request.setModuleName(EuropeanaDatasets.DATASETS.getSysId());
		request.setSession(sugarwsClient.getSessionID());
		request.setOrderBy(EuropeanaRetrievableField.DATE_ENTERED.getFieldId());
		request.setMaxResults(100);
		request.setOffset(0);
		request.setQuery("(opportunities." + field + " LIKE '" + value + "%')");

		GetEntryListResponse response = sugarwsClient.getentrylist(request);

		ArrayList<Element> list = null;

		if (response.getReturn().getEntryList().getArray() != null) {
			list = (ArrayList<Element>) response.getReturn().getEntryList()
					.getArray().getAnyList();
		} else {
			list = new ArrayList<Element>();
		}

		for (Element el : list) {
			DataSet ds = new DataSet();
			
			String identifier = ClientUtils.extractFromElement(EuropeanaRetrievableField.NAME.getFieldId(), el).split("_")[0];			
			ds.identifier = identifier;			
			ds.status = ClientUtils.translateStatus(ClientUtils.extractFromElement(
					EuropeanaUpdatableField.STATUS.getFieldId(), el));			
			ds.name = ClientUtils.extractFromElement(
					EuropeanaRetrievableField.NAME.getFieldId(), el);
			ds.description = "Hidden";
			// ds.description =
			// ClientUtils.extractFromElement(EuropeanaRetrievableField.DESCRIPTION.getFieldId(),
			// el);
			ds.publishedRecords = ClientUtils.extractFromElement(
					EuropeanaUpdatableField.TOTAL_INGESTED.getFieldId(), el);
			ds.deletedRecords = "Not implemented yet";
			// ds.deletedRecords = ClientUtils.extractFromElement("name", el);
			results.items.add(ds);
		}

		return results;

	}

	/**
	 * Method used to retrieve datasets by provider ID
	 * 
	 * @param id
	 * @return A Jackson object containing results
	 * @throws JIXBQueryResultException
	 */
	public SugarCRMSearchResults<DataSet> retrieveDatasetByProvider(String id)
			throws JIXBQueryResultException {

		String sugarCRMID = null;
		GetEntryList prrequest = new GetEntryList();
		// We want to retrieve all fields
		SelectFields fields = new SelectFields(); 

		prrequest.setSelectFields(fields);
		prrequest.setModuleName(EuropeanaDatasets.ORGANIZATIONS.getSysId());
		prrequest.setSession(sugarwsClient.getSessionID());
		prrequest.setOrderBy(EuropeanaRetrievableField.DATE_ENTERED
				.getFieldId());
		prrequest.setMaxResults(100);
		prrequest.setOffset(0);
		prrequest.setQuery("(accounts_cstm." + "name_id_c" + " LIKE '" + id
				+ "%')");
		// prrequest.setQuery("(accounts_cstm.name_id_c = '" + id +"')");
		GetEntryListResponse response = sugarwsClient.getentrylist(prrequest);

		ArrayList<Element> list = null;

		if (response.getReturn().getEntryList().getArray() != null) {
			list = (ArrayList<Element>) response.getReturn().getEntryList()
					.getArray().getAnyList();
		} else {
			list = new ArrayList<Element>();
		}

		for (Element el : list) {

			if (response.getReturn().getEntryList().getArray() != null) {
				sugarCRMID = ClientUtils.extractFromElement("id", el);
				break;
			}

		}

		SugarCRMSearchResults<DataSet> results = new SugarCRMSearchResults<DataSet>(
				"key", "action");
		results.items = new ArrayList<DataSet>();

		if (sugarCRMID != null) {

			GetRelationships request = new GetRelationships();
			request.setDeleted(0);
			request.setModuleId(sugarCRMID);
			request.setModuleName("Accounts");
			request.setRelatedModule("Opportunities");
			request.setRelatedModuleQuery("");
			request.setSession(sugarwsClient.getSessionID());

			GetRelationshipsResponse resp = sugarwsClient
					.getrelationships(request);
			if (resp.getReturn().getIds().getArray() == null) {
				throw new JIXBQueryResultException(
						"Could not retrieve related provider information from 'Accounts module' ");
			}

			List<Element> el = resp.getReturn().getIds().getArray()
					.getAnyList();

			for (Element elm : el) {
				String[] datasetiD = elm.getTextContent().split("-");
				StringBuilder datasetiDSb = new StringBuilder();
				for (int i = 0; i < 5; i++) {
					if (i < 4) {
						datasetiDSb.append(datasetiD[i]);
						datasetiDSb.append("-");
					} else {
						datasetiDSb.append(datasetiD[i].substring(0, 11));
					}
				}

				if (datasetiD != null) {
					SugarCRMSearchResults<DataSet> datasets = retrieveDataset(
							"id", datasetiDSb.toString());
					if (datasets.items.size() != 0) {
						System.out.println(datasets.items.size());
						DataSet dataset = datasets.items.get(0);
						results.items.add(dataset);
					}
				}

			}
		}
		return results;

	}


	
	


}
