@XmlSchema(namespace = SRWResponse.NS_SRW,
	xmlns = {
		@XmlNs(namespaceURI = SRWResponse.NS_SRW, prefix = "srw"),
		@XmlNs(namespaceURI = SRWResponse.NS_DIAG, prefix = "diag"),
		@XmlNs(namespaceURI = SRWResponse.NS_XCQL, prefix = "xcql"),
		@XmlNs(namespaceURI = SRWResponse.NS_MODS, prefix = "mods"),
		@XmlNs(namespaceURI = SRWResponse.NS_EUROPEANA, prefix = "europeana"),
		@XmlNs(namespaceURI = SRWResponse.NS_ENRICHMENT, prefix = "enrichment"),
		@XmlNs(namespaceURI = SRWResponse.NS_DCX, prefix = "dcx"),
		@XmlNs(namespaceURI = SRWResponse.NS_TEL, prefix = "tel"),
		@XmlNs(namespaceURI = SRWResponse.NS_XSI, prefix = "xsi"),
		@XmlNs(namespaceURI = SRWResponse.NS_DCTERMS, prefix = "dcterms"),
		@XmlNs(namespaceURI = SRWResponse.NS_DC, prefix = "dc")
	},
	elementFormDefault = javax.xml.bind.annotation.XmlNsForm.QUALIFIED
)
package eu.europeana.api2.web.model.xml.srw;

import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlSchema;