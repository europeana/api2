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

import java.io.StringWriter;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;

import org.w3c.dom.Element;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.Morphia;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.UpdateOperations;

import com.google.common.collect.Lists;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

import eu.europeana.api2.v2.model.json.sugarcrm.DataSet;
import eu.europeana.api2.v2.model.json.sugarcrm.Provider;
import eu.europeana.api2.v2.model.json.sugarcrm.SugarCRMSearchResults;

import eu.europeana.corelib.logging.Logger;
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
 * Implementation of the caching mechanism 
 * 
 * @author Georgios Markakis (gwarkx@hotmail.com)
 * 
 * @since Oct 30, 2013
 */
public class SugarCRMCache {

	@Log
	private Logger log;

	@Resource
	private SugarWsClient sugarwsClient;

	@Resource(name = "api_db_mongo_cache")
	private Mongo mongo;

	private Datastore datastore;

	private final static String CACHEDB = "sugarcrmCache";

	/**
	 * Use a local instance if MongoDB version cannot be injected from Spring 
	 * from Spring Context (useful in Unit Testing)
	 */
	@PostConstruct
	public void initLocal() {
		if (mongo == null) {
			try {
				mongo = new Mongo();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (MongoException e) {
				e.printStackTrace();
			}
		}

		DB db = mongo.getDB(CACHEDB);
		Morphia morphia = new Morphia();
		morphia.map(DataSet.class).map(Provider.class);
		datastore = morphia.createDatastore(mongo, CACHEDB);

		mongo.getDB(CACHEDB).getCollection("DataSet").ensureIndex("identifier");
		mongo.getDB(CACHEDB).getCollection("DataSet").ensureIndex("savedsugarcrmFields.name");
		mongo.getDB(CACHEDB).getCollection("DataSet").ensureIndex("savedsugarcrmFields.country_c");
		mongo.getDB(CACHEDB).getCollection("DataSet").ensureIndex("savedsugarcrmFields.sales_stage");
		mongo.getDB(CACHEDB).getCollection("Provider").ensureIndex("identifier");
	}

	/*
	 * Mongo (retrieval from cache) related operations
	 */

	/**
	 * Gets all providers from the Mongo Cache.
	 * 
	 * @return the JSON/Morphia annotated provider beans wrapped in a SugarCRMSearchResults JSON object
	 */
	public SugarCRMSearchResults<Provider> getProviders() {
		return getProviders(null, 0, 0);
	}

	/**
	 * Gets all providers from the Mongo Cache.
	 * @param country the country code filter (might be null)
	 * @param offset the offset of search (might be 0)
	 * @param pagesize the page size (might be 0: 200 will be set as default in that case)
	 * 
	 * @return the JSON/Morphia annotated provider beans wrapped in a SugarCRMSearchResults JSON object
	 */
	public SugarCRMSearchResults<Provider> getProviders(String country, int offset, int pagesize) {
		SugarCRMSearchResults<Provider> results = new SugarCRMSearchResults<Provider>("", "");
		Query<Provider> query = datastore.find(Provider.class);
		if (country != null) {
			query.filter("country", country.toUpperCase());
		}
		if (offset != 0 && pagesize != 0) {
			query.offset(offset);
			query.limit(pagesize);
		}
		if (offset == 0 && pagesize != 0) {
			query.offset(0);
			query.limit(pagesize);
		}
		if (offset != 0 && pagesize == 0) {
			query.offset(offset);
		}
		List<Provider> res = query.asList();
		long count = datastore.find(Provider.class).countAll();
		results.totalResults = count;
		results.items = res;

		for (Provider pr :res) {
			inflateProvider(pr);
		}

		return results;
	}

	/**
	 * Gets all datasets from the Mongo Cache.
	 * 
	 * @return the JSON/Morphia annotated provider beans wrapped in a SugarCRMSearchResults JSON object
	 */
	public SugarCRMSearchResults<DataSet> getCollections() {
		return getCollections(0, 0);
	}

	public SugarCRMSearchResults<DataSet> getCollections(int offset, int pagesize) {
		return getCollections(0, 0, null, null, null);
	}

	/**
	 * Gets all datasets from the Mongo Cache.
	 * @param offset the offset of search (might be 0)
	 * @param pagesize the page size (might be 0: 200 will be set as default in that case)
	 * 
	 * @return the JSON/Morphia annotated provider beans wrapped in a SugarCRMSearchResults JSON object
	 */
	public SugarCRMSearchResults<DataSet> getCollections(int offset, int pagesize, String name, String country, String status) {
		SugarCRMSearchResults<DataSet> results = new SugarCRMSearchResults<DataSet>("", "");
		Query<DataSet> query = datastore.find(DataSet.class);

		if (StringUtils.isNotBlank(name)) {
			query.field("savedsugarcrmFields.name").equal(name);
		}

		if (StringUtils.isNotBlank(country)) {
			query.field("savedsugarcrmFields.country_c").equal(country);
		}

		if (StringUtils.isNotBlank(status)) {
			String sysId = ClientUtils.translateDsStatusDescription(status);
			if (StringUtils.isNotBlank(sysId)) {
				query.field("savedsugarcrmFields.sales_stage").equal(sysId);
			}
			// TODO: create an else branch because it is an invalid query!!!
		}

		if (offset != 0 && pagesize != 0) {
			query.offset(offset);
			query.limit(pagesize);
		}

		if (offset == 0 && pagesize != 0) {
			query.offset(0);
			query.limit(pagesize);
		}

		if(offset != 0 && pagesize == 0) {
			query.offset(offset);
		}

		List<DataSet> res = query.asList();
		long count = query.countAll();
		// TODO: check how offset modifies the behaviour
		// long count = ds.find(DataSet.class).countAll();
		results.totalResults = count;
		results.items = res;

		for (DataSet ds :res) {
			inflateDataset(ds);
		}

		return results;
	}

	/**
	 * Gets a provider according to the given ID
	 * 
	 * @param id the provider ID
	 * @return the JSON/Morphia annotated provider bean wrapped in a SugarCRMSearchResults JSON object
	 */
	public SugarCRMSearchResults<Provider> getProviderbyID(String id) {
		SugarCRMSearchResults<Provider> results = new SugarCRMSearchResults<Provider>("", "");
		results.items = new ArrayList<Provider>();
		Provider prov = datastore.find(Provider.class).field("_id").equal(id).get();
		if (prov != null) {
			inflateProvider(prov);
			results.items.add(prov);
			return results;
		}
		else {
			return results;
		}
	}

	/**
	 * Gets a dataset according to the given ID
	 * @param id the dataset ID
	 * @return the JSON/Morphia annotated dataset bean wrapped in a SugarCRMSearchResults JSON object
	 */
	public SugarCRMSearchResults<DataSet> getCollectionByID(String id) {
		SugarCRMSearchResults<DataSet> results = new SugarCRMSearchResults<DataSet>("", "");
		results.items = new ArrayList<DataSet>();
		DataSet dts = datastore.find(DataSet.class).field("_id").equal(id).get();
		if (dts != null) {
			inflateDataset(dts);
			results.items.add(dts);
			return results;
		}
		else {
			return results;
		}
	}

	/**
	 * Gets the set of the collections that belong to a provider given the provider ID
	 * @param id the provider ID
	 * @return the JSON/Morphia annotated dataset beans wrapped in a SugarCRMSearchResults JSON object
	 */
	public SugarCRMSearchResults<DataSet> getCollectionByProviderID(String id) {
		List<DataSet> reslist = datastore.find(DataSet.class).field("provIdentifier").equal(id).asList();
		for (DataSet dts : reslist) {
			inflateDataset(dts);
		}
		SugarCRMSearchResults<DataSet> results = new SugarCRMSearchResults<DataSet>("", "");
		results.items = new ArrayList<DataSet>();
		results.items.addAll(reslist);
		return results;
	}

	/*
	 * SugarCrm (population of cache from SugarCRM) related operations
	 */

	/**
	 * Performs a re-population/synchronization of the MongoDB cache if found that
	 * the latter is empty.
	 * 
	 * @throws JIXBQueryResultException
	 */
	public void populateRepositoryFromScratch() throws JIXBQueryResultException {
		SugarCRMSearchResults<Provider> providers = getProviders();
		if (providers.items.isEmpty()) {

			ArrayList<Element> exportedProviders = Lists.newArrayListWithExpectedSize(1000);
			GetEntryList providerRequest = new GetEntryList();

			// We want to retrieve all fields
			SelectFields fields = new SelectFields();
			providerRequest.setSelectFields(fields);
			providerRequest.setModuleName(EuropeanaDatasets.ORGANIZATIONS.getSysId());
			providerRequest.setSession(sugarwsClient.getSessionID());
			providerRequest.setOrderBy(
				EuropeanaRetrievableField.DATE_ENTERED.getFieldId());
			providerRequest.setMaxResults(100);
			providerRequest.setOffset(0);
			// D = Data Aggregator
			// P = Content Provider
			providerRequest.setQuery("(accounts_cstm.agg_status_c LIKE '%P' OR accounts_cstm.agg_status_c LIKE '%D')");
			// providerRequest.setQuery("(accounts_cstm.agg_status_c LIKE '%D')");

			int offset = 0;
			while (true) {
				GetEntryListResponse response = sugarwsClient.getEntryList(providerRequest);

				if (response.getReturn().getEntryList().getArray() != null) {
					exportedProviders.addAll((ArrayList<Element>) response.getReturn()
						.getEntryList().getArray().getAnyList());
				} else {
					break;
				}
				offset = response.getReturn().getNextOffset();
				providerRequest.setOffset(offset);
			}

			log.info(String.format("Saving %d providers", exportedProviders.size()));
			int i = 1;
			for (Element exportedProvider : exportedProviders) {
				log.info(String.format("Processing provider %d/%d", i++, exportedProviders.size()));
				Provider provider = new Provider();
				populateProviderFromDOMElement(provider, exportedProvider);
				datastore.save(provider);
				extractDatasetsFromProvider(exportedProvider);
			}
			log.info("All providers are saved.");
		}
	}

	/**
	 * Auxiliary method that saves or updates a Provider into the Cache (MongoDB)
	 * @param prov the provider object
	 */
	public void saveupdateProvider2Cache(Provider prov) {
		UpdateOperations<Provider> ops = datastore
				.createUpdateOperations(Provider.class).disableValidation()
					.set("savedsugarcrmFields", prov.savedsugarcrmFields);
		Query<Provider> query = datastore.createQuery(Provider.class)
				.field("identifier").equal(prov.identifier);
		datastore.updateFirst(query, ops, true);
	}

	/**
	 * Auxiliary method that saves or updates a Dataset into the Cache (MongoDB)
	 * @param dts the collection object
	 */
	public void saveupdateCollection2Cache(DataSet dts) {
		UpdateOperations<DataSet> ops = datastore
				.createUpdateOperations(DataSet.class).disableValidation()
				.set("savedsugarcrmFields", dts.savedsugarcrmFields);
		Query<DataSet> query = datastore.createQuery(DataSet.class)
				.field("identifier").equal(dts.identifier);
		datastore.updateFirst(query, ops, true);
	}

	/**
	 * Auxiliary method that extracts and populates the Mongo cache with all the Datasets
	 * that belong to a provider given the provider's DOM represention contained in the
	 * web service response.
	 * 
	 * @param exportedProvider The provider DOM element
	 * @throws JIXBQueryResultException
	 */
	private void extractDatasetsFromProvider(Element exportedProvider)
			throws JIXBQueryResultException {

		String sugarCRMProviderID = ClientUtils.extractFromElement("id", exportedProvider);
		String uimProviderID = ClientUtils.extractFromElement("name_id_c", exportedProvider);
		GetRelationships request = new GetRelationships();
		request.setDeleted(0);
		request.setModuleId(sugarCRMProviderID);
		request.setModuleName("Accounts");
		request.setRelatedModule("Opportunities");
		request.setRelatedModuleQuery("");
		request.setSession(sugarwsClient.getSessionID());

		GetRelationshipsResponse resp = sugarwsClient.getRelationships(request);
		if (resp.getReturn().getIds().getArray() != null) {
			List<Element> el = resp.getReturn().getIds().getArray()
					.getAnyList();

			// Iterate the retrieved related datasets
			for (Element elm : el) {
				String datasetId = extractDatasetId(elm.getTextContent());
				if (StringUtils.isNotBlank(datasetId)) {
					String query = "opportunities.id LIKE '" + datasetId + "%'";

					SugarCRMSearchResults<DataSet> datasets = retrieveDataset(query, uimProviderID); 
					if (datasets.items.size() != 0) {
						for (DataSet dts : datasets.items) {
							datastore.save(dts);
						}
					}
				}
			}
		}
	}

	/**
	 * Removes timestamp from dataset ID.
	 * For example:
	 * 7dd9673a-41e7-880c-4cb1-5101299709812014-01-15 10:13:490 --> 7dd9673a-41e7-880c-4cb1-51012997098
	 * @param rawId
	 * @return
	 */
	private String extractDatasetId(String rawId) {
		if (StringUtils.isBlank(rawId)) {
			return null;
		}
		String[] datasetIdParts = rawId.split("-");
		StringBuilder datasetId = new StringBuilder();
		for (int i = 0; i < 5; i++) {
			if (i < 4) {
				datasetId.append(datasetIdParts[i]);
				datasetId.append("-");
			} else {
				datasetId.append(datasetIdParts[i].substring(0, 11));
			}
		}
		return datasetId.toString();
	}
	/**
	 * Basic polling function for providers (sugarcrm 2 cache)
	 * @throws JIXBQueryResultException 
	 */
	public SugarCRMSearchResults<Provider> pollProviders() throws JIXBQueryResultException {
		String q1 = "accounts.date_modified > DATE_SUB(NOW(),INTERVAL 66 MINUTE)";
		String q2 = "accounts_cstm.agg_status_c LIKE '%D'";
		SugarCRMSearchResults<Provider> retrievedProviders = retrieveProviders(q1, q2);
		for (Provider provider : retrievedProviders.items) {
			log.info(String.format("Provider: %s was updated by the scheduler...", provider.identifier));
			saveupdateProvider2Cache(provider);
		}
		return retrievedProviders;
	}

	/**
	 * Basic polling function for datasets (sugarcrm 2 cache)
	 * @throws JIXBQueryResultException
	 */
	public SugarCRMSearchResults<DataSet> pollCollections() throws JIXBQueryResultException {
		SugarCRMSearchResults<DataSet> retrievedDatasets = retrieveDataset("opportunities.date_modified > DATE_SUB(NOW(),INTERVAL 66 MINUTE)", null);
		for (DataSet dataset : retrievedDatasets.items) {
			log.info(String.format("Dataset: %s was updated by the scheduler...", dataset.identifier));
			saveupdateCollection2Cache(dataset);
		}
		return retrievedDatasets;
	}

	/**
	 * Auxiliary Method used to retrieve datasets by ID (from SugarCRM)
	 * @param id 
	 * @return the JSON/Morphia annotated dataset beans wrapped in a SugarCRMSearchResults JSON object
	 * @throws JIXBQueryResultException
	 */
	private SugarCRMSearchResults<DataSet> retrieveDataset(String query, String providerID) 
			throws JIXBQueryResultException {
		GetEntryList request = new GetEntryList();
		SugarCRMSearchResults<DataSet> results = new SugarCRMSearchResults<DataSet>("", "");
		results.items = new ArrayList<DataSet>();
		// We want to retrieve all fields
		SelectFields fields = new SelectFields();
		request.setSelectFields(fields);
		request.setModuleName(EuropeanaDatasets.DATASETS.getSysId());
		request.setSession(sugarwsClient.getSessionID());
		request.setOrderBy(EuropeanaRetrievableField.DATE_ENTERED.getFieldId());
		request.setMaxResults(100);
		request.setOffset(0);
		request.setQuery(query);

		GetEntryListResponse response = sugarwsClient.getEntryList(request);
		ArrayList<Element> list = null;
		if (response.getReturn().getEntryList().getArray() != null) {
			list = (ArrayList<Element>) response.getReturn().getEntryList()
					.getArray().getAnyList();
		} else {
			list = new ArrayList<Element>();
		}

		for (Element el : list) {
			DataSet ds = new DataSet();
			populateDatasetFromDOMElement(ds, el, providerID);
			results.items.add(ds);
		}
		return results;
	}

	/**
	 * Auxiliary Method for retrieving all the providers (from SugarCRM)
	 * 
	 * @return the JSON/Morphia annotated provider beans wrapped in a SugarCRMSearchResults JSON object
	 * @throws JIXBQueryResultException
	 */
	private SugarCRMSearchResults<Provider> retrieveProviders(String... query)
			throws JIXBQueryResultException {

		GetEntryList request = new GetEntryList();
		SugarCRMSearchResults<Provider> results = new SugarCRMSearchResults<Provider>("", "");
		results.items = new ArrayList<Provider>();
		request.setSelectFields(new SelectFields());
		request.setModuleName(EuropeanaDatasets.ORGANIZATIONS.getSysId());
		request.setSession(sugarwsClient.getSessionID());
		request.setOrderBy(EuropeanaRetrievableField.DATE_ENTERED.getFieldId());
		request.setMaxResults(200);
		request.setOffset(0);

		if (query.length == 0) {
			request.setQuery("(accounts_cstm.agg_status_c LIKE '%D')");
		}
		else {
			StringWriter querywrt = new StringWriter();
			if (query.length == 1) {
				querywrt.append("(");
				querywrt.append(query[0]);
				querywrt.append(")");
				request.setQuery(querywrt.toString());
			}
			else {
				querywrt.append("(");

				for (int i=0; i<query.length; i++) {
					querywrt.append("(");
					querywrt.append(query[i]);
					querywrt.append(")");
					if (i < query.length -1) {
						querywrt.append(" and ");
					}
				}

				querywrt.append(")");
				request.setQuery(querywrt.toString());
			}
		}

		GetEntryListResponse response = sugarwsClient.getEntryList(request);
		ArrayList<Element> list = null;

		if (response.getReturn().getEntryList().getArray() != null) {
			list = (ArrayList<Element>) response.getReturn().getEntryList()
					.getArray().getAnyList();
		} else {
			list = new ArrayList<Element>();
		}

		log.info(String.format("Retrieved %d providers", list.size()));
		for (Element el : list) {
			Provider prov = new Provider();
			// Insert values in Provider Object
			populateProviderFromDOMElement(prov, el);
			results.items.add(prov);
		}

		return results;
	}

	/**
	 * Auxiliary method that populates a Morphia annotated DataSet object 
	 * given a received DOM element
	 * 
	 * @param prov a reference to the DataSet object 
	 * @param el a reference to the DOM element
	 */
	private void populateDatasetFromDOMElement(DataSet ds, Element el, String providerID) {
		String identifier = ClientUtils.extractFromElement(EuropeanaRetrievableField.NAME.getFieldId(), el).split("_")[0];
		ds.identifier = identifier;
		ds.provIdentifier = providerID;
		ds.status = ClientUtils.translateStatus(ClientUtils.extractFromElement(
				EuropeanaUpdatableField.STATUS.getFieldId(), el));
		ds.edmDatasetName = ClientUtils.extractFromElement(
				EuropeanaRetrievableField.NAME.getFieldId(), el);
		String publishedRecordsStr = ClientUtils.extractFromElement(
				EuropeanaUpdatableField.TOTAL_INGESTED.getFieldId(), el);
		if (StringUtils.isNotBlank(publishedRecordsStr)) {
			try {
				ds.publishedRecords = Long.parseLong(publishedRecordsStr);
			} catch (NumberFormatException e) {
				ds.publishedRecords = 0;
			}
		} else {
			ds.publishedRecords = 0;
		}
		String delrecordsStr = ClientUtils.extractFromElement(
				EuropeanaUpdatableField.DELETED_RECORDS.getFieldId(), el);
		if (StringUtils.isNotBlank(delrecordsStr)) {
			try {
				ds.deletedRecords = Long.parseLong(delrecordsStr);
			} catch (NumberFormatException e) {
				ds.deletedRecords = 0;
			}
		} else {
			ds.deletedRecords = 0;
		}
		ds.savedsugarcrmFields = ClientUtils.mapFromElement(el);
	}

	/**
	 * Inflate a Dataset JSON annotated object from the cache.
	 * 
	 * @param ds the dataset object 
	 */
	private void inflateDataset(DataSet ds) {
		ds.status = ClientUtils.translateStatus(ds.savedsugarcrmFields.get(EuropeanaUpdatableField.STATUS.getFieldId()));
		ds.edmDatasetName = ds.savedsugarcrmFields.get(EuropeanaRetrievableField.NAME.getFieldId());
		ds.creationDate = ds.savedsugarcrmFields.get(EuropeanaRetrievableField.DATE_ENTERED.getFieldId());
		ds.providerName = ds.savedsugarcrmFields.get(EuropeanaRetrievableField.ORGANIZATION_NAME.getFieldId());

		//ds.publicationDate = ds.savedsugarcrmFields.get(EuropeanaRetrievableField.EXPECTED_INGESTION_DATE.getFieldId());
		String precordsStr = ds.savedsugarcrmFields.get(EuropeanaUpdatableField.TOTAL_INGESTED.getFieldId());
		if (precordsStr != null) {
			try {
				ds.publishedRecords = Long.parseLong(precordsStr);
			}
			catch (Exception ex) {
				ds.publishedRecords = 0;
			}
		}
		String delrecordsStr = ds.savedsugarcrmFields.get(EuropeanaUpdatableField.DELETED_RECORDS.getFieldId());
		if (delrecordsStr != null) {
			ds.deletedRecords = Long.parseLong(delrecordsStr);
		}
	}

	/**
	 * Copy the fields needed from the DOM element to the related 
	 * (persisted) Morphia annotated fields. 
	 * 
	 * @param prov the provider object
	 * @param el the DOM element
	 */
	private void populateProviderFromDOMElement(Provider prov, Element el) {
		prov.identifier = ClientUtils.extractFromElement("name_id_c", el);
		prov.country = ClientUtils.extractFromElement("country_c", el);
		prov.savedsugarcrmFields = ClientUtils.mapFromElement(el);
	}

	/**
	 * Inflate a Provider JSON object from the cache.
	 * 
	 * @param prov the provider object
	 */
	private void inflateProvider(Provider prov) {
		prov.name = prov.savedsugarcrmFields.get("name");
		prov.country = prov.savedsugarcrmFields.get("country_c");
		prov.name = prov.savedsugarcrmFields.get("name");
		prov.altname = prov.savedsugarcrmFields.get("name_alt_c");
		prov.acronym = prov.savedsugarcrmFields.get("name_acronym_c");
		prov.domain = prov.savedsugarcrmFields.get("account_type");
		prov.geolevel = prov.savedsugarcrmFields.get("geo_level_c");
		prov.role = ClientUtils.translateType(prov.savedsugarcrmFields.get("agg_status_c"));
		prov.scope = prov.savedsugarcrmFields.get("scope_c");
		prov.sector = prov.savedsugarcrmFields.get("sector_c");
		prov.country = prov.savedsugarcrmFields.get("country_c");
		prov.website = prov.savedsugarcrmFields.get("website");
	}

	/**
	 * Setter for SugarWsClient instance
	 * @param sugarwsClient
	 */
	public void setSugarwsClient(SugarWsClient sugarwsClient) {
		this.sugarwsClient = sugarwsClient;
	}
}
