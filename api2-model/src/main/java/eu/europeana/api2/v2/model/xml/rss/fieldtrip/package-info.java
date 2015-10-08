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

@XmlSchema(
	namespace = "",
	xmlns = {
		@XmlNs(namespaceURI = "http://www.fieldtripper.com/fieldtrip_rss", prefix = "fieldtrip"),
		@XmlNs(namespaceURI = "http://www.georss.org/georss", prefix = "georss"),
	},
	elementFormDefault = javax.xml.bind.annotation.XmlNsForm.QUALIFIED
)
package eu.europeana.api2.v2.model.xml.rss.fieldtrip;

import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlSchema;
