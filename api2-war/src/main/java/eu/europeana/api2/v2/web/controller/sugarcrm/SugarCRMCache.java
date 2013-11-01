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
package eu.europeana.api2.v2.web.controller.sugarcrm;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.w3c.dom.Element;
import com.google.code.morphia.Datastore;
import com.google.code.morphia.Morphia;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.UpdateOperations;
import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import eu.europeana.api2.v2.model.json.sugarcrm.DataSet;
import eu.europeana.api2.v2.model.json.sugarcrm.Provider;
import eu.europeana.api2.v2.model.json.sugarcrm.SugarCRMSearchResults;
import eu.europeana.corelib.logging.Log;
import eu.europeana.uim.sugarcrmclient.enums.EuropeanaDatasets;
import eu.europeana.uim.sugarcrmclient.enums.EuropeanaRetrievableField;
import eu.europeana.uim.sugarcrmclient.enums.EuropeanaUpdatableField;
import eu.europeana.uim.sugarcrmclient.internal.helpers.ClientUtils;
import eu.europeana.uim.sugarcrmclient.jibxbindings.GetEntryList;
import eu.europeana.uim.sugarcrmclient.jibxbindings.GetEntryListResponse;
import eu.europeana.uim.sugarcrmclient.jibxbindings.GetRelationships;
import eu.europeana.uim.sugarcrmclient.jibxbindings.GetRelationshipsResponse;
import eu.europeana.uim.sugarcrmclient.jibxbindings.SelectFields;
import eu.europeana.uim.sugarcrmclient.ws.SugarWsClient;
import eu.europeana.uim.sugarcrmclient.ws.exceptions.JIXBQueryResultException;

/**
 * @author Georgios Markakis (gwarkx@hotmail.com)
 * 
 * @since Oct 30, 2013
 */
public class SugarCRMCache {

	@Log
	private Logger log;

	@Resource
	private SugarWsClient sugarwsClient;

	private final static String CACHEDB = "sugarcrmCache";
	private Mongo mongo;
	private DB db;
	private Datastore ds;

	/**
	 * 
	 */
	public SugarCRMCache() {
		try {
			mongo = new Mongo();
			db = mongo.getDB(CACHEDB);
			Morphia morphia = new Morphia();
			morphia.map(DataSet.class).map(Provider.class);

			ds = morphia.createDatastore(mongo, CACHEDB);

			mongo.getDB(CACHEDB).getCollection("DataSet")
					.ensureIndex("identifier");
			mongo.getDB(CACHEDB).getCollection("Provider")
					.ensureIndex("identifier");

		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MongoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * @return
	 */
	public SugarCRMSearchResults<Provider> getProviders() {
		List<Provider> res = ds.find(Provider.class).asList();
		return null;
	}

	/**
	 * @param id
	 * @return
	 */
	public SugarCRMSearchResults<Provider> getProviderbyID(String id) {
		ds.find(Provider.class).filter("identifier", id).get();
		return null;
	}

	/**
	 * @param id
	 * @return
	 */
	public SugarCRMSearchResults<DataSet> getCollectionByID(String id) {
		ds.find(DataSet.class).filter("identifier", id).get();
		return null;
	}

	/**
	 * @param id
	 * @return
	 */
	public SugarCRMSearchResults<DataSet> getCollectionByProviderID(String id) {
		ds.find(DataSet.class).filter("identifier", id).get();
		return null;
	}

	/**
	 * Saves or updates a Provider into the Cache
	 */
	public void saveupdateProvider(GetEntryListResponse response) {
		ArrayList<Element> list = null;

		if (response.getReturn().getEntryList().getArray() != null) {
			list = (ArrayList<Element>) response.getReturn().getEntryList()
					.getArray().getAnyList();
		} else {
			list = new ArrayList<Element>();
		}

		for (Element el : list) {
			Provider dts = new Provider();
			String identifier = ClientUtils.extractFromElement(
					EuropeanaRetrievableField.NAME.getFieldId(), el).split("_")[0];
			dts.identifier = identifier;
			dts.savedsugarcrmFields = ClientUtils.mapFromElement(el);

			UpdateOperations<Provider> ops = ds
					.createUpdateOperations(Provider.class).disableValidation()
					.set("savedsugarcrmFields", dts.savedsugarcrmFields);
			Query<Provider> query = ds.createQuery(Provider.class)
					.field("identifier").equal(identifier);
			ds.updateFirst(query, ops, true);

		}

	}

	/**
	 * Saves or updates a Dataset into the Cache
	 */
	public void saveupdateCollection(GetEntryListResponse response) {
		ArrayList<Element> list = null;

		if (response.getReturn().getEntryList().getArray() != null) {
			list = (ArrayList<Element>) response.getReturn().getEntryList()
					.getArray().getAnyList();
		} else {
			list = new ArrayList<Element>();
		}

		for (Element el : list) {
			DataSet dts = new DataSet();
			String identifier = ClientUtils.extractFromElement(
					EuropeanaRetrievableField.NAME.getFieldId(), el).split("_")[0];
			dts.identifier = identifier;
			dts.savedsugarcrmFields = ClientUtils.mapFromElement(el);

			UpdateOperations<DataSet> ops = ds
					.createUpdateOperations(DataSet.class).disableValidation()
					.set("savedsugarcrmFields", dts.savedsugarcrmFields);
			Query<DataSet> query = ds.createQuery(DataSet.class)
					.field("identifier").equal(identifier);
			ds.updateFirst(query, ops, true);

		}
	}

	
	
	/**
	 * @throws JIXBQueryResultException
	 * 
	 */
	public void populateRepositoryFromScratch() throws JIXBQueryResultException {

		ArrayList<Element> list = new ArrayList<Element>();
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
		prrequest
				.setQuery("(accounts_cstm.agg_status_c LIKE '%P' OR accounts_cstm.agg_status_c LIKE '%D')");

		while (true) {
			int total = 0;
			int offset = 0;
			GetEntryListResponse response = sugarwsClient
					.getentrylist(prrequest);

			if (response.getReturn().getEntryList().getArray() != null) {
				list.addAll((ArrayList<Element>) response.getReturn()
						.getEntryList().getArray().getAnyList());
			} else {
				break;
			}

			total = response.getReturn().getResultCount();
			offset = response.getReturn().getNextOffset();

			if (total == offset) {
				break;
			} else {
				prrequest.setOffset(offset);
			}
		}
		
		for(Element el : list){
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
			prov.savedsugarcrmFields = ClientUtils.mapFromElement(el);
			ds.merge(prov);
			extractDatasetsFromProvider(el);
		}
	}

	/**
	 * @param prel
	 * @throws JIXBQueryResultException
	 */
	private void extractDatasetsFromProvider(Element prel)
			throws JIXBQueryResultException {

		String sugarCRMProvID = ClientUtils.extractFromElement("id", prel);
		String uimprovID = ClientUtils.extractFromElement("name_id_c", prel);

		GetRelationships request = new GetRelationships();
		request.setDeleted(0);
		request.setModuleId(sugarCRMProvID);
		request.setModuleName("Accounts");
		request.setRelatedModule("Opportunities");
		request.setRelatedModuleQuery("");
		request.setSession(sugarwsClient.getSessionID());

		GetRelationshipsResponse resp = sugarwsClient.getrelationships(request);

		if (resp.getReturn().getIds().getArray() != null) {
			List<Element> el = resp.getReturn().getIds().getArray()
					.getAnyList();

			// Iterate the retrieved related datasets
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

					  SugarCRMSearchResults<DataSet> datasets =  retrieveDataset( "id", datasetiDSb.toString(),uimprovID); 
					  
					  if   (datasets.items.size() != 0) {
					  
						  for (DataSet dts : datasets.items){
								ds.merge(dts);
						  }

					  }
				}

			}

		}

	}
	


	/**
	 * Method used to retrieve datasets by ID
	 * @param id
	 * @return A Jackson object containing results
	 * @throws JIXBQueryResultException
	 */
	public SugarCRMSearchResults<DataSet> retrieveDataset(String field,
			String value,String providerID) throws JIXBQueryResultException {

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
			ds.provIdentifier = providerID;
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
			
			ds.savedsugarcrmFields = ClientUtils.mapFromElement(el);
			
			// ds.deletedRecords = ClientUtils.extractFromElement("name", el);
			results.items.add(ds);
		}

		return results;

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
