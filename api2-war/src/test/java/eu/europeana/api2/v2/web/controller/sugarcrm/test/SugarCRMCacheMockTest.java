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
package eu.europeana.api2.v2.web.controller.sugarcrm.test;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import org.jibx.runtime.JiBXException;
import org.junit.BeforeClass;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import eu.europeana.api2.v2.model.json.sugarcrm.DataSet;
import eu.europeana.api2.v2.model.json.sugarcrm.Provider;
import eu.europeana.api2.v2.model.json.sugarcrm.SugarCRMSearchResults;
import eu.europeana.api2.v2.service.SugarCRMCache;
import eu.europeana.uim.sugarcrmclient.ws.SugarWsClient;
import eu.europeana.uim.sugarcrmclient.ws.exceptions.JIXBQueryResultException;

/**
 * Stand alone Mock UnitTests for API Caching mechanism
 * 
 * @author Georgios Markakis (gwarkx@hotmail.com)
 *
 * @since Nov 12, 2013
 */
public class SugarCRMCacheMockTest extends AbstractSugarCRMCacheTest {

	/**
	 * Initializes the Mock objects for the unit test
	 * 
	 * @throws IOException
	 * @throws JiBXException
	 * @throws JIXBQueryResultException
	 */
	@BeforeClass
	public static void init() throws IOException, JiBXException, JIXBQueryResultException{

		sugarwsClient = mock(SugarWsClient.class);
		caheinstance = mock(SugarCRMCache.class);	
		final ArgumentCaptor<String> providercaptor = ArgumentCaptor.forClass(String.class);
		final SugarCRMSearchResults<Provider> providerres = new SugarCRMSearchResults<Provider>(null, null);
		Provider provider = new Provider();
		provider.identifier = "mockid";
		provider.name = "mockname";
		provider.savedsugarcrmFields = new HashMap<String,String>();
		providerres.items = new ArrayList<Provider>();
		providerres.items.add(provider);		
		final SugarCRMSearchResults<DataSet> collectionres = new SugarCRMSearchResults<DataSet>(null, null);
		DataSet ds = new DataSet();
		ds.identifier = "mockid";
		ds.name = "mockname";
		ds.deletedRecords = 0;
		ds.publishedRecords = 0;
		ds.provIdentifier = "mockprovID";
		ds.status = "mockstatus";
		ds.savedsugarcrmFields = new HashMap<String,String>();
		collectionres.items = new ArrayList<DataSet>();
		collectionres.items.add(ds);
		
		when(caheinstance.getProviders()).thenAnswer(new Answer<SugarCRMSearchResults<Provider>>(){
			@Override
			public SugarCRMSearchResults<Provider> answer(InvocationOnMock invocation) throws Throwable {
				return providerres;
			}
		});
		
		when(caheinstance.getProviderbyID(providercaptor.capture())).thenAnswer(new Answer<SugarCRMSearchResults<Provider>>(){
			@Override
			public SugarCRMSearchResults<Provider> answer(InvocationOnMock invocation) throws Throwable {
			    SugarCRMSearchResults<Provider> providerres = new SugarCRMSearchResults<Provider>(null, null);
				Provider provider = new Provider();
				provider.identifier = providercaptor.getValue();
				provider.name = "mockname";
				provider.savedsugarcrmFields = new HashMap<String,String>();
				providerres.items = new ArrayList<Provider>();
				providerres.items.add(provider);		
				return providerres;
			}
		});
		
		when(caheinstance.getProviders(anyString(), anyInt(), anyInt())).thenAnswer(new Answer<SugarCRMSearchResults<Provider>>(){
			@Override
			public SugarCRMSearchResults<Provider> answer(InvocationOnMock invocation) throws Throwable {
				return providerres;
			}
		});
		
		when(caheinstance.pollProviders()).thenReturn(providerres);

		when(caheinstance.pollCollections()).thenReturn(collectionres);

		when(caheinstance.getCollectionByID(anyString())).thenAnswer(new Answer<SugarCRMSearchResults<DataSet>>(){
			@Override
			public SugarCRMSearchResults<DataSet> answer(InvocationOnMock invocation) throws Throwable {
				return collectionres;
			}
		});
		
		when(caheinstance.getCollectionByProviderID(anyString())).thenAnswer(new Answer<SugarCRMSearchResults<DataSet>>(){
			@Override
			public SugarCRMSearchResults<DataSet> answer(InvocationOnMock invocation) throws Throwable {
				return collectionres;
			}
		});
		
		caheinstance.setSugarwsClient(sugarwsClient);
		caheinstance.initLocal();
	}
}
