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
 */
package eu.europeana.api2.v2.web.controller.sugarcrm.test;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;

import org.jibx.runtime.JiBXException;
import org.junit.BeforeClass;
import org.springframework.oxm.jibx.JibxMarshaller;
import org.springframework.ws.client.core.WebServiceTemplate;

import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.distribution.Version;
import eu.europeana.api2.v2.service.SugarCRMCache;
import eu.europeana.api2.v2.service.SugarCRMImporter;
import eu.europeana.uim.sugarcrmclient.internal.ExtendedSaajSoapMessageFactory;
import eu.europeana.uim.sugarcrmclient.internal.helpers.ClientUtils;
import eu.europeana.uim.sugarcrmclient.ws.ClientFactory;
import eu.europeana.uim.sugarcrmclient.ws.exceptions.JIXBLoginFailureException;

/**
 * Integration Tests for API caching mechanism.
 * 
 * @author Georgios Markakis (gwarkx@hotmail.com)
 *
 *         Nov 12, 2013
 * @deprecated July 2018 replaced by Metis
 */
@Deprecated
public abstract class SugarCRMCacheIntegrationTest extends
		AbstractSugarCRMCacheTest {

	/**
	 * Inititlaize the datatasources for Integration tests here
	 * 
	 * @throws Exception
	 */
	@BeforeClass
	public static void init() throws Exception {

		int port = 10000;
		MongodConfig conf = new MongodConfig(Version.V2_0_7, port, false);
		MongodStarter runtime = MongodStarter.getDefaultInstance();

		mongodExe = runtime.prepare(conf);
		mongod = mongodExe.start();

		ClientFactory clientFactory = new ClientFactory();
		MessageFactory messageFactory = null;
		try {
			messageFactory = MessageFactory.newInstance();
		} catch (SOAPException e1) {
			e1.printStackTrace();
		}

		ExtendedSaajSoapMessageFactory soapMessageFactory = new ExtendedSaajSoapMessageFactory(messageFactory);
		WebServiceTemplate webServiceTemplate = new WebServiceTemplate(soapMessageFactory);
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
		clientFactory.setWebServiceTemplate(webServiceTemplate);
		sugarwsClient = clientFactory.createInstance(
				"http://sip-manager.isti.cnr.it/sugarcrm/soap.php", 
				"user",
				"pass"
		);

		try {
			sugarwsClient.login(ClientUtils.createStandardLoginObject("user", "pass"));
		} catch (Exception e) {
			e.printStackTrace();
		}

		importerInstance = new SugarCRMImporter();
		importerInstance.setSugarwsClient(sugarwsClient);
	}
}
