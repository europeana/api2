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

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Date;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;

import org.jibx.runtime.JiBXException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.oxm.jibx.JibxMarshaller;
import org.springframework.ws.client.core.WebServiceTemplate;

import com.mongodb.Mongo;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.distribution.Version;
import eu.europeana.api2.v2.model.json.sugarcrm.DataSet;
import eu.europeana.api2.v2.model.json.sugarcrm.Provider;
import eu.europeana.api2.v2.model.json.sugarcrm.SugarCRMSearchResults;
import eu.europeana.api2.v2.web.controller.sugarcrm.SugarCRMCache;
import eu.europeana.uim.sugarcrmclient.internal.ExtendedSaajSoapMessageFactory;
import eu.europeana.uim.sugarcrmclient.internal.helpers.ClientUtils;
import eu.europeana.uim.sugarcrmclient.internal.helpers.PropertyReader;
import eu.europeana.uim.sugarcrmclient.internal.helpers.UimConfigurationProperty;
import eu.europeana.uim.sugarcrmclient.ws.ClientFactory;
import eu.europeana.uim.sugarcrmclient.ws.SugarWsClient;
import eu.europeana.uim.sugarcrmclient.ws.SugarWsClientImpl;
import eu.europeana.uim.sugarcrmclient.ws.exceptions.JIXBLoginFailureException;
import eu.europeana.uim.sugarcrmclient.ws.exceptions.JIXBQueryResultException;

/**
 * @author Georgios Markakis (gwarkx@hotmail.com)
 *
 * Nov 4, 2013
 */
public abstract class SugarCRMCacheTest {

	private static MongodExecutable mongodExe;
	private static MongodProcess mongod;
	private static SugarCRMCache caheinstance;
	private static SugarWsClient sugarwsClient;
	
	@BeforeClass
	public static void init() throws IOException, JiBXException{
		/*
		int port = 10000;
		MongodConfig conf = new MongodConfig(Version.V2_0_7, port, false);
		MongodStarter runtime = MongodStarter.getDefaultInstance();
		mongodExe = runtime.prepare(conf);
		mongod = mongodExe.start();
		*/
		
		
		ClientFactory clfact = new ClientFactory();
		
		MessageFactory mf = null;
		try {
		   mf = MessageFactory.newInstance();
		} catch (SOAPException e1) {
			e1.printStackTrace();
		}
		
		ExtendedSaajSoapMessageFactory mfactory = new ExtendedSaajSoapMessageFactory(mf);
		WebServiceTemplate webServiceTemplate = new WebServiceTemplate(mfactory);

		JibxMarshaller marshaller = new JibxMarshaller();

		marshaller.setStandalone(true);
		marshaller.setTargetClass(eu.europeana.uim.sugarcrmclient.jibxbindings.Login.class);
		marshaller.setTargetClass(eu.europeana.uim.sugarcrmclient.jibxbindings.GetEntries.class);
		marshaller.setEncoding("UTF-8");
		marshaller.setTargetPackage("eu.europeana.uim.sugarcrmclient.jibxbindings");

		try {
			marshaller.afterPropertiesSet();
		} catch (JiBXException e1) {
			e1.printStackTrace();
		}				
		
		webServiceTemplate.setMarshaller(marshaller);
		webServiceTemplate.setUnmarshaller(marshaller);
		
		clfact.setWebServiceTemplate(webServiceTemplate);
		
		sugarwsClient = clfact.createInstance("http://sip-manager.isti.cnr.it/sugarcrm/soap.php",
				"wsaccount", "***REMOVED***");
		
		
		try {
			sugarwsClient.login(ClientUtils.createStandardLoginObject("wsaccount","***REMOVED***"));
		} catch (JIXBLoginFailureException e) {

		} catch (Exception e){
			e.printStackTrace();
		}
		
		caheinstance = new SugarCRMCache();
		caheinstance.setSugarwsClient(sugarwsClient);
	}
	
	
	@Test
	public void getProviderbyIDTest(){
		String id = "001";
		SugarCRMSearchResults<Provider> provres = caheinstance.getProviderbyID(id);
		Provider prov = provres.items.get(0);
		assertNotNull(prov);
		assertNotNull(prov.savedsugarcrmFields);
		assertEquals(id,prov.identifier);
	}
	
	
	@Test
	public void getProvidersTest(){
	  SugarCRMSearchResults<Provider> provs = caheinstance.getProviders();
	  assertNotNull(provs.items);
	  assertTrue(!provs.items.isEmpty());

	  for(Provider prov: provs.items){
		  System.out.println(prov.identifier);
		  assertNotNull(prov.identifier);
		  assertNotNull(prov.savedsugarcrmFields);
	  }
	}
	
	
	@Test
	public void getProvidersPagingTest(){
	  SugarCRMSearchResults<Provider> provs = caheinstance.getProviders(null,0,0);
	  assertNotNull(provs.items);
	  assertTrue(!provs.items.isEmpty());

	  for(Provider prov: provs.items){
		  System.out.println(prov.identifier);
		  assertNotNull(prov.identifier);
		  assertNotNull(prov.savedsugarcrmFields);
	  }
	}
	
	@Test
	public void getCollectionByProviderIDTest(){
		SugarCRMSearchResults<DataSet> collres = caheinstance.getCollectionByProviderID("001");
		  assertNotNull(collres.items);
		  assertTrue(!collres.items.isEmpty());
		  
		  for(DataSet ds: collres.items){
			  assertNotNull(ds.identifier);
			  assertNotNull(ds.name);
			  assertNotNull(ds.status);
			  assertNotNull(ds.provIdentifier);
			  assertNotNull(ds.savedsugarcrmFields);
		  }
		  
	}
	
	@Test
	public void getCollectionByIDTest(){

		SugarCRMSearchResults<DataSet> collres = caheinstance.getCollectionByID("00101");
		assertNotNull(collres.items);
		DataSet ds = collres.items.get(0);
		assertNotNull(ds);
		
		  assertNotNull(ds.identifier);
		  assertNotNull(ds.name);
		  assertNotNull(ds.status);
		  assertNotNull(ds.provIdentifier);
		  assertNotNull(ds.savedsugarcrmFields);
		
	}
	
	@Test
	public void populationTest() throws JIXBQueryResultException{
		caheinstance.populateRepositoryFromScratch();
	}

	@Test
	public void collectionPollingTest() throws JIXBQueryResultException{
		caheinstance.pollCollections();
	}
	
	@Test
	public void providerPollingTest() throws JIXBQueryResultException{
		caheinstance.pollProviders();
	}
	
	
}
