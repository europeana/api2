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

	@Resource(name = "corelib_db_morphia_datastore_sugarcrmcache")
	private Datastore datastore;

	private final static String CACHEDB = "sugarcrmCache";

	private final static String DATA_AGGREGATOR_QUERY = "(accounts_cstm.agg_status_c LIKE '%D')";
	private final static String CONTENT_PROVIDER_QUERY = "(accounts_cstm.agg_status_c LIKE '%P')";
	private final static String ALL_PROVIDER_QUERY = String.format("(%s OR %s)",
			DATA_AGGREGATOR_QUERY, CONTENT_PROVIDER_QUERY);

	/**
	 * Use a local instance if MongoDB version cannot be injected from Spring 
	 * from Spring Context (useful in Unit Testing)
	 */
	@PostConstruct
	public void initLocal() {
		if (datastore == null) {
			log.info("SugarCRMCache datasource is null");
			if (mongo == null) {
				log.info("SugarCRMCache mongo is null");
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
		}

		datastore.getDB().getCollection("DataSet").ensureIndex("identifier");
		datastore.getDB().getCollection("DataSet").ensureIndex("savedsugarcrmFields.name");
		datastore.getDB().getCollection("DataSet").ensureIndex("savedsugarcrmFields.country_c");
		datastore.getDB().getCollection("DataSet").ensureIndex("savedsugarcrmFields.sales_stage");
		datastore.getDB().getCollection("Provider").ensureIndex("identifier");
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
		List<Provider> providers = query.asList();
		long count = datastore.find(Provider.class).countAll();
		results.totalResults = count;
		results.items = providers;

		for (Provider provider : providers) {
			inflateProvider(provider);
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

		List<DataSet> dataSets = query.asList();
		long count = query.countAll();
		// TODO: check how offset modifies the behaviour
		// long count = ds.find(DataSet.class).countAll();
		results.totalResults = count;
		results.items = dataSets;

		for (DataSet dataSet : dataSets) {
			inflateDataset(dataSet);
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
		Provider provider = datastore.find(Provider.class).field("_id").equal(id).get();

		if (provider != null) {
			inflateProvider(provider);
			results.items.add(provider);
		}

		return results;
	}

	/**
	 * Gets a dataset according to the given ID
	 * @param id the dataset ID
	 * @return the JSON/Morphia annotated dataset bean wrapped in a SugarCRMSearchResults JSON object
	 */
	public SugarCRMSearchResults<DataSet> getCollectionByID(String id) {
		SugarCRMSearchResults<DataSet> results = new SugarCRMSearchResults<DataSet>("", "");
		results.items = new ArrayList<DataSet>();
		DataSet dataSet = datastore.find(DataSet.class).field("_id").equal(id).get();
		if (dataSet != null) {
			inflateDataset(dataSet);
			results.items.add(dataSet);
		}
		return results;
	}

	/**
	 * Gets the set of the collections that belong to a provider given the provider ID
	 * @param id the provider ID
	 * @return the JSON/Morphia annotated dataset beans wrapped in a SugarCRMSearchResults JSON object
	 */
	public SugarCRMSearchResults<DataSet> getCollectionByProviderID(String id) {
		List<DataSet> dataSets = datastore.find(DataSet.class).field("provIdentifier").equal(id).asList();
		for (DataSet dataSet : dataSets) {
			inflateDataset(dataSet);
		}
		SugarCRMSearchResults<DataSet> results = new SugarCRMSearchResults<DataSet>("", "");
		results.items = new ArrayList<DataSet>();
		results.items.addAll(dataSets);
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
			providerRequest.setQuery(DATA_AGGREGATOR_QUERY);

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
	 * @param provider the provider object
	 */
	public void saveupdateProvider2Cache(Provider provider) {
		UpdateOperations<Provider> operations = datastore
				.createUpdateOperations(Provider.class).disableValidation()
					.set("savedsugarcrmFields", provider.savedsugarcrmFields);
		Query<Provider> query = datastore.createQuery(Provider.class)
				.field("identifier").equal(provider.identifier);
		datastore.updateFirst(query, operations, true);
	}

	/**
	 * Auxiliary method that saves or updates a Dataset into the Cache (MongoDB)
	 * @param dataSet the collection object
	 */
	public void saveupdateCollection2Cache(DataSet dataSet) {
		UpdateOperations<DataSet> operations = datastore
				.createUpdateOperations(DataSet.class).disableValidation()
				.set("savedsugarcrmFields", dataSet.savedsugarcrmFields);
		Query<DataSet> query = datastore.createQuery(DataSet.class)
				.field("identifier").equal(dataSet.identifier);
		datastore.updateFirst(query, operations, true);
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
						for (DataSet dataSet : datasets.items) {
							datastore.save(dataSet);
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
		log.info("pollProviders()");
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
		log.info("pollCollections()");
		SugarCRMSearchResults<DataSet> retrievedDatasets = retrieveDataset(
			"opportunities.date_modified > DATE_SUB(NOW(),INTERVAL 66 MINUTE)", null);
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
		ArrayList<Element> xmlElements = null;
		if (response.getReturn().getEntryList().getArray() != null) {
			xmlElements = (ArrayList<Element>) response.getReturn().getEntryList().getArray().getAnyList();
		} else {
			xmlElements = new ArrayList<Element>();
		}

		log.info(String.format("Query %s retrieved %d datasets", request.getQuery(), xmlElements.size()));
		for (Element xmlElement : xmlElements) {
			DataSet dataSet = new DataSet();
			populateDatasetFromDOMElement(dataSet, xmlElement, providerID);
			results.items.add(dataSet);
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
			request.setQuery(DATA_AGGREGATOR_QUERY);
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
		ArrayList<Element> xmlElements = null;

		if (response.getReturn().getEntryList().getArray() != null) {
			xmlElements = (ArrayList<Element>) response.getReturn().getEntryList().getArray().getAnyList();
		} else {
			xmlElements = new ArrayList<Element>();
		}

		log.info(String.format("Query %s retrieved %d providers", request.getQuery(), xmlElements.size()));
		for (Element xmlElement : xmlElements) {
			Provider provider = new Provider();
			// Insert values in Provider Object
			populateProviderFromDOMElement(provider, xmlElement);
			results.items.add(provider);
		}

		return results;
	}

	/**
	 * Auxiliary method that populates a Morphia annotated DataSet object 
	 * given a received DOM element
	 * 
	 * @param prov a reference to the DataSet object 
	 * @param xmlElement a reference to the DOM element
	 */
	private void populateDatasetFromDOMElement(DataSet dataSet, Element xmlElement, String providerID) {
		String identifier = ClientUtils.extractFromElement(
				EuropeanaRetrievableField.NAME.getFieldId(), xmlElement).split("_")[0];
		dataSet.identifier = identifier;
		dataSet.provIdentifier = providerID;
		dataSet.status = ClientUtils.translateStatus(ClientUtils.extractFromElement(
				EuropeanaUpdatableField.STATUS.getFieldId(), xmlElement));
		dataSet.edmDatasetName = ClientUtils.extractFromElement(
				EuropeanaRetrievableField.NAME.getFieldId(), xmlElement);
		String publishedRecordsStr = ClientUtils.extractFromElement(
				EuropeanaUpdatableField.TOTAL_INGESTED.getFieldId(), xmlElement);
		if (StringUtils.isNotBlank(publishedRecordsStr)) {
			try {
				dataSet.publishedRecords = Long.parseLong(publishedRecordsStr);
			} catch (NumberFormatException e) {
				dataSet.publishedRecords = 0;
			}
		} else {
			dataSet.publishedRecords = 0;
		}
		String numberOfDeletedRecords = ClientUtils.extractFromElement(
				EuropeanaUpdatableField.DELETED_RECORDS.getFieldId(), xmlElement);
		if (StringUtils.isNotBlank(numberOfDeletedRecords)) {
			try {
				dataSet.deletedRecords = Long.parseLong(numberOfDeletedRecords);
			} catch (NumberFormatException e) {
				dataSet.deletedRecords = 0;
			}
		} else {
			dataSet.deletedRecords = 0;
		}
		dataSet.savedsugarcrmFields = ClientUtils.mapFromElement(xmlElement);
	}

	/**
	 * Inflate a Dataset JSON annotated object from the cache.
	 * 
	 * @param dataSet the dataset object 
	 */
	private void inflateDataset(DataSet dataSet) {
		dataSet.status = ClientUtils.translateStatus(
				dataSet.savedsugarcrmFields.get(
						EuropeanaUpdatableField.STATUS.getFieldId()));
		dataSet.edmDatasetName = dataSet.savedsugarcrmFields.get(
				EuropeanaRetrievableField.NAME.getFieldId());
		dataSet.creationDate = dataSet.savedsugarcrmFields.get(
				EuropeanaRetrievableField.DATE_ENTERED.getFieldId());
		dataSet.providerName = dataSet.savedsugarcrmFields.get(
				EuropeanaRetrievableField.ORGANIZATION_NAME.getFieldId());

		//ds.publicationDate = ds.savedsugarcrmFields.get(EuropeanaRetrievableField.EXPECTED_INGESTION_DATE.getFieldId());
		String precordsStr = dataSet.savedsugarcrmFields.get(EuropeanaUpdatableField.TOTAL_INGESTED.getFieldId());
		if (precordsStr != null) {
			try {
				dataSet.publishedRecords = Long.parseLong(precordsStr);
			}
			catch (Exception ex) {
				dataSet.publishedRecords = 0;
			}
		}
		String delrecordsStr = dataSet.savedsugarcrmFields.get(EuropeanaUpdatableField.DELETED_RECORDS.getFieldId());
		if (delrecordsStr != null) {
			dataSet.deletedRecords = Long.parseLong(delrecordsStr);
		}
	}

	/**
	 * Copy the fields needed from the DOM element to the related 
	 * (persisted) Morphia annotated fields. 
	 * 
	 * @param provider the provider object
	 * @param xmlElement the DOM element
	 */
	private void populateProviderFromDOMElement(Provider provider, Element xmlElement) {
		provider.identifier = ClientUtils.extractFromElement("name_id_c", xmlElement);
		provider.country = ClientUtils.extractFromElement("country_c", xmlElement);
		provider.savedsugarcrmFields = ClientUtils.mapFromElement(xmlElement);
	}

	/**
	 * Inflate a Provider JSON object from the cache.
	 * 
	 * @param provider the provider object
	 */
	private void inflateProvider(Provider provider) {
		provider.name = provider.savedsugarcrmFields.get("name");
		provider.country = provider.savedsugarcrmFields.get("country_c");
		provider.name = provider.savedsugarcrmFields.get("name");
		provider.altname = provider.savedsugarcrmFields.get("name_alt_c");
		provider.acronym = provider.savedsugarcrmFields.get("name_acronym_c");
		provider.domain = provider.savedsugarcrmFields.get("account_type");
		provider.geolevel = provider.savedsugarcrmFields.get("geo_level_c");
		provider.role = ClientUtils.translateType(provider.savedsugarcrmFields.get("agg_status_c"));
		provider.scope = provider.savedsugarcrmFields.get("scope_c");
		provider.sector = provider.savedsugarcrmFields.get("sector_c");
		provider.website = provider.savedsugarcrmFields.get("website");
	}

	/**
	 * Setter for SugarWsClient instance
	 * @param sugarwsClient
	 */
	public void setSugarwsClient(SugarWsClient sugarwsClient) {
		this.sugarwsClient = sugarwsClient;
	}
}
