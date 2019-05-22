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

