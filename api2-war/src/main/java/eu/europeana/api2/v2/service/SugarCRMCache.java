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
import eu.europeana.corelib.logging.Logger;
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

	@Resource(name = "corelib_db_mongo")
	private Mongo mongo;

	private Datastore ds;
	
	private final static String CACHEDB = "sugarcrmCache";
	
	/**
	 * Use a local instance if MongoDB version cannot be injected from Spring 
     * from Spring Context (useful in Unit Testing)
	 */
	@PostConstruct
	public void initLocal(){
		if(mongo == null){
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
		ds = morphia.createDatastore(mongo, CACHEDB);

		mongo.getDB(CACHEDB).getCollection("DataSet")
				.ensureIndex("identifier");
		mongo.getDB(CACHEDB).getCollection("Provider")
				.ensureIndex("identifier");
	}
	
	/*
	 * Mongo (retrieval from cache) related operations
	 */
	
	/**
	 * Gets all providers from the Mongo Cache.
	 * 
	 * @return the JSON/Morphia annotated provider beans wrapped in a SugarCRMSearchResults JSON object
	 */
	public SugarCRMSearchResults<Provider> getProviders(){
		return getProviders(null,0,0);
	}
	
	/**
	 * Gets all providers from the Mongo Cache.
	 * @param country the country code filter (might be null)
	 * @param offset the offset of search (might be 0)
	 * @param pagesize the page size (might be 0: 200 will be set as default in that case)
	 * 
	 * @return the JSON/Morphia annotated provider beans wrapped in a SugarCRMSearchResults JSON object
	 */
	public SugarCRMSearchResults<Provider> getProviders(String country,int offset,int pagesize) {
		SugarCRMSearchResults<Provider> results = new SugarCRMSearchResults<Provider>("","");	
		Query<Provider> query = ds.find(Provider.class);
		if(country != null){
			query.filter("country", country);
		}
		if(offset != 0){
			query.offset(offset);
		}
		if(offset != 0 && pagesize!=0){
			query.limit(pagesize);
		}
		if(offset != 0 && pagesize == 0){
			query.limit(200);
		}
		List<Provider> res =  query.asList();
		long count = ds.find(Provider.class).countAll();
		results.totalResults = count;
		results.items = res;
		
		for(Provider pr :res){			
			pr.description = pr.savedsugarcrmFields.get("description");
			pr.name = pr.savedsugarcrmFields.get("name");
			pr.country = pr.savedsugarcrmFields.get("country_c");
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
		SugarCRMSearchResults<Provider> results = new SugarCRMSearchResults<Provider>("","");
		results.items = new ArrayList<Provider>();
		Provider prov = ds.find(Provider.class).field("_id").equal(id).get();
		if(prov != null){
			prov.description = prov.savedsugarcrmFields.get("description");
			prov.name = prov.savedsugarcrmFields.get("name");
			prov.country = prov.savedsugarcrmFields.get("country_c");
			results.items.add(prov);
			return results;
		}
		else{	
			return results;
		}
	}

	/**
	 * Gets a dataset according to the given ID
	 * @param id the dataset ID
	 * @return the JSON/Morphia annotated dataset bean wrapped in a SugarCRMSearchResults JSON object
	 */
	public SugarCRMSearchResults<DataSet> getCollectionByID(String id) {
		SugarCRMSearchResults<DataSet> results = new SugarCRMSearchResults<DataSet>("","");
		results.items = new ArrayList<DataSet>();
		DataSet dts = ds.find(DataSet.class).field("_id").equal(id).get();
		if(dts!= null){
			dts.status = dts.savedsugarcrmFields.get(EuropeanaUpdatableField.STATUS.getFieldId()); 
			dts.name = dts.savedsugarcrmFields.get(EuropeanaRetrievableField.NAME.getFieldId()); 
			dts.description = "Hidden";
			//dts.description = dts.savedsugarcrmFields.get(EuropeanaRetrievableField.DESCRIPTION.getFieldId()); 
			dts.publishedRecords = dts.savedsugarcrmFields.get(EuropeanaUpdatableField.TOTAL_INGESTED.getFieldId()); 
			dts.deletedRecords = "Not implemented yet";
			results.items.add(dts);
			return results;
		}
		else{
			return results;
		}

	}

	/**
	 * Gets the set of the collections that belong to a provider given the provider ID
	 * @param id the provider ID
	 * @return the JSON/Morphia annotated dataset beans wrapped in a SugarCRMSearchResults JSON object
	 */
	public SugarCRMSearchResults<DataSet> getCollectionByProviderID(String id) {
		List<DataSet> reslist = ds.find(DataSet.class).field("provIdentifier").equal(id).asList();
		
		for(DataSet dts : reslist){
			dts.status = dts.savedsugarcrmFields.get(EuropeanaUpdatableField.STATUS.getFieldId()); 
			dts.name = dts.savedsugarcrmFields.get(EuropeanaRetrievableField.NAME.getFieldId()); 
			//dts.description = "Hidden";
			dts.description = dts.savedsugarcrmFields.get(EuropeanaRetrievableField.DESCRIPTION.getFieldId()); 
			dts.publishedRecords = dts.savedsugarcrmFields.get(EuropeanaUpdatableField.TOTAL_INGESTED.getFieldId()); 
			dts.deletedRecords = "Not implemented yet";
		}
		SugarCRMSearchResults<DataSet> results = new SugarCRMSearchResults<DataSet>("","");
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
		SugarCRMSearchResults<Provider> provs = getProviders();
		if(provs.items.isEmpty()){
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
		int offset = 0;		
		while (true) {

			GetEntryListResponse response = sugarwsClient
					.getentrylist(prrequest);

			if (response.getReturn().getEntryList().getArray() != null) {
				list.addAll((ArrayList<Element>) response.getReturn()
						.getEntryList().getArray().getAnyList());
			} else {
				break;
			}
			offset = response.getReturn().getNextOffset();
			prrequest.setOffset(offset);
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
			ds.save(prov);
			extractDatasetsFromProvider(el);
		}
		
	   }
	}

	/**
	 * Auxiliary method that saves or updates a Provider into the Cache (MongoDB)
	 * @param prov the provider object
	 */
	public void saveupdateProvider2Cache(Provider prov) {
			UpdateOperations<Provider> ops = ds
					.createUpdateOperations(Provider.class).disableValidation()
					.set("savedsugarcrmFields", prov.savedsugarcrmFields);
			Query<Provider> query = ds.createQuery(Provider.class)
					.field("identifier").equal(prov.identifier);
			ds.updateFirst(query, ops, true);

	}

	/**
	 * Auxiliary method that saves or updates a Dataset into the Cache (MongoDB)
	 * @param dts the collection object
	 */
	public void saveupdateCollection2Cache(DataSet dts) {
			UpdateOperations<DataSet> ops = ds
					.createUpdateOperations(DataSet.class).disableValidation()
					.set("savedsugarcrmFields", dts.savedsugarcrmFields);
			Query<DataSet> query = ds.createQuery(DataSet.class)
					.field("identifier").equal(dts.identifier);
			ds.updateFirst(query, ops, true);
	}
	
	
	/**
	 * Auxiliary method that extracts and populates the Mongo cache with  all the Datasets
	 * that belong to a provider given the provider's DOM represention contained in the
	 * web service response.
	 * 
	 * @param prel The provider DOM element
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
					 String query = "opportunities.id LIKE '" + datasetiDSb.toString() + "%'";
					
					  SugarCRMSearchResults<DataSet> datasets =  retrieveDataset(query,uimprovID); 
					  if   (datasets.items.size() != 0) {					  
						  for (DataSet dts : datasets.items){
								ds.save(dts);
						  }
					  }
				}
			}
		}
	}
	
	
	/**
	 * Basic polling function for providers (sugarcrm 2 cache)
	 * @throws JIXBQueryResultException 
	 */
	public SugarCRMSearchResults<Provider> pollProviders() throws JIXBQueryResultException{
		String q1 ="accounts.date_modified between (NOW() - INTERVAL 2 HOUR) and NOW()";
		String q2 = "accounts_cstm.agg_status_c LIKE '%P' OR accounts_cstm.agg_status_c LIKE '%D'";
	    SugarCRMSearchResults<Provider> provres = retrieveproviders(q1,q2);    
	    for(Provider prov : provres.items){
	    	log.info("Provider:"+prov.identifier+" was updated by the scheduler...");
	    	saveupdateProvider2Cache(prov);
	    }
	    return provres;
	}
	
	
	/**
	 * Basic polling function for datasets (sugarcrm 2 cache)
	 * @throws JIXBQueryResultException
	 */
	public SugarCRMSearchResults<DataSet> pollCollections() throws JIXBQueryResultException{
		SugarCRMSearchResults<DataSet> retrdatasets = retrieveDataset("opportunities.date_modified between (NOW() - INTERVAL 2 HOUR) and NOW()",null);
		for(DataSet ds : retrdatasets.items){
	    	log.info("Dataset:"+ds.identifier+" was updated by the scheduler...");
			saveupdateCollection2Cache(ds);
		}
		return retrdatasets;
	}
	


	/**
	 * Auxiliary Method used to retrieve datasets by ID (from SugarCRM)
	 * @param id 
	 * @return the JSON/Morphia annotated dataset beans wrapped in a SugarCRMSearchResults JSON object
	 * @throws JIXBQueryResultException
	 */
	private SugarCRMSearchResults<DataSet> retrieveDataset(String query,String providerID) throws JIXBQueryResultException {

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
		//request.setQuery("(opportunities." + field + " LIKE '" + value + "%')");
		request.setQuery(query);
		
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
	 * Auxiliary Method for retrieving all the providers (from SugarCRM)
	 * 
	 * @return the JSON/Morphia annotated provider beans wrapped in a SugarCRMSearchResults JSON object
	 * @throws JIXBQueryResultException
	 */
	private SugarCRMSearchResults<Provider> retrieveproviders(String... query)
			throws JIXBQueryResultException {

		GetEntryList request = new GetEntryList();
		SugarCRMSearchResults<Provider> results = new SugarCRMSearchResults<Provider>("","");
		results.items = new ArrayList<Provider>();
		request.setSelectFields(new SelectFields());
		request.setModuleName(EuropeanaDatasets.ORGANIZATIONS.getSysId());
		request.setSession(sugarwsClient.getSessionID());
		request.setOrderBy(EuropeanaRetrievableField.DATE_ENTERED.getFieldId());
		request.setMaxResults(200);
		request.setOffset(0);
		
		if(query.length == 0){
			request.setQuery("(accounts_cstm.agg_status_c LIKE '%P' OR accounts_cstm.agg_status_c LIKE '%D')");
		}
		else{
			StringWriter querywrt = new StringWriter();
			if(query.length == 1){
				querywrt.append("(");
				querywrt.append(query[0]);
				querywrt.append(")");
				request.setQuery(querywrt.toString());
			}
			else{
				querywrt.append("(");
				
				for(int i=0; i<query.length; i++){
					querywrt.append("(");
					querywrt.append(query[i]);
					querywrt.append(")");
					if(i < query.length -1){
						querywrt.append(" and ");
					}
				}

				querywrt.append(")");
				request.setQuery(querywrt.toString());
			}
		}
		
		GetEntryListResponse response = sugarwsClient.getentrylist(request);
		ArrayList<Element> list = null;

		if (response.getReturn().getEntryList().getArray() != null) {
			list = (ArrayList<Element>) response.getReturn().getEntryList()
					.getArray().getAnyList();
		} else {
			list = new ArrayList<Element>();
		}

		for (Element el : list) {
			Provider prov = new Provider();
			// Insert values in Provider Object
			prov.identifier = ClientUtils.extractFromElement("name_id_c", el);
			//prov.description =ClientUtils.extractFromElement("description", el);
			prov.description = "Hidden";
			prov.name = ClientUtils.extractFromElement("name", el);
			prov.country = ClientUtils.extractFromElement("country_c", el);
			prov.website = ClientUtils.extractFromElement("website", el);
			prov.savedsugarcrmFields = ClientUtils.mapFromElement(el);
			results.items.add(prov);
		}

		return results;
	}
	

	
	
	
	/**
	 * Getter for SugarWsClient instance
	 * @return
	 */
	public SugarWsClient getSugarwsClient() {
		return sugarwsClient;
	}

	/**
	 * Setter for SugarWsClient instance
	 * @param sugarwsClient
	 */
	public void setSugarwsClient(SugarWsClient sugarwsClient) {
		this.sugarwsClient = sugarwsClient;
	}
	
	
	public Mongo getMongo() {
		return mongo;
	}

	public void setMongo(Mongo mongo) {
		this.mongo = mongo;
	}
	
}
