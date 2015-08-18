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

@XmlSchema(namespace = SrwResponse.NS_SRW, xmlns = {
		@XmlNs(namespaceURI = SrwResponse.NS_SRW, prefix = "srw"),
		@XmlNs(namespaceURI = SrwResponse.NS_DIAG, prefix = "diag"),
		@XmlNs(namespaceURI = SrwResponse.NS_XCQL, prefix = "xcql"),
		@XmlNs(namespaceURI = SrwResponse.NS_MODS, prefix = "mods"),
		@XmlNs(namespaceURI = SrwResponse.NS_EUROPEANA, prefix = "europeana"),
		@XmlNs(namespaceURI = SrwResponse.NS_ENRICHMENT, prefix = "enrichment"),
		@XmlNs(namespaceURI = SrwResponse.NS_DCX, prefix = "dcx"),
		@XmlNs(namespaceURI = SrwResponse.NS_TEL, prefix = "tel"),
		@XmlNs(namespaceURI = SrwResponse.NS_XSI, prefix = "xsi"),
		@XmlNs(namespaceURI = SrwResponse.NS_DCTERMS, prefix = "dcterms"),
		@XmlNs(namespaceURI = SrwResponse.NS_DC, prefix = "dc") }, elementFormDefault = javax.xml.bind.annotation.XmlNsForm.QUALIFIED)
package eu.europeana.api2.v2.model.xml.srw;

import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlSchema;
import eu.europeana.api2.model.xml.srw.SrwResponse;

