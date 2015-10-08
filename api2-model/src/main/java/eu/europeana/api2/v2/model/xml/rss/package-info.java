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

@XmlSchema(namespace = "", xmlns = {
		@XmlNs(namespaceURI = "http://a9.com/-/spec/opensearch/1.1/", prefix = "opensearch"),
		@XmlNs(namespaceURI = "http://www.w3.org/2005/Atom", prefix = "atom"),
		@XmlNs(namespaceURI = "http://purl.org/dc/elements/1.1/", prefix = "dc"),
		@XmlNs(namespaceURI = "http://purl.org/dc/terms/", prefix = "dcterms"),
		@XmlNs(namespaceURI = "http://www.fieldtripper.com/fieldtrip_rss", prefix = "fieldtrip"),
		@XmlNs(namespaceURI = "http://www.georss.org/georss", prefix = "georss"),
		@XmlNs(namespaceURI = EuropeanaUrlService.URL_EUROPEANA, prefix = "europeana"),
		@XmlNs(namespaceURI = EuropeanaUrlService.URL_EUROPEANA+"/schemas/ese/enrichment/", prefix = "enrichment") }, elementFormDefault = javax.xml.bind.annotation.XmlNsForm.QUALIFIED)
package eu.europeana.api2.v2.model.xml.rss;

import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlSchema;
import eu.europeana.corelib.web.service.EuropeanaUrlService;
