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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.query.Query;

import eu.europeana.api2.v2.model.json.sugarcrm.DataSet;
import eu.europeana.api2.v2.model.json.sugarcrm.Provider;
import eu.europeana.api2.v2.model.json.sugarcrm.SugarCRMSearchResults;
import eu.europeana.corelib.logging.Logger;
import eu.europeana.corelib.logging.Log;
import eu.europeana.uim.sugarcrmclient.enums.EuropeanaRetrievableField;
import eu.europeana.uim.sugarcrmclient.enums.EuropeanaUpdatableField;
import eu.europeana.uim.sugarcrmclient.internal.helpers.ClientUtils;

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

	@Resource(name = "corelib_db_morphia_datastore_sugarcrmcache")
	private Datastore datastore;

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
}
