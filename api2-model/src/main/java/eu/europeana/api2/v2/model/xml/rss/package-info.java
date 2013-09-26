@XmlSchema(namespace = "", xmlns = {
		@XmlNs(namespaceURI = "http://a9.com/-/spec/opensearch/1.1/", prefix = "opensearch"),
		@XmlNs(namespaceURI = "http://www.w3.org/2005/Atom", prefix = "atom"),
		@XmlNs(namespaceURI = "http://purl.org/dc/elements/1.1/", prefix = "dc"),
		@XmlNs(namespaceURI = "http://purl.org/dc/terms/", prefix = "dcterms"),
		@XmlNs(namespaceURI = EuropeanaUrlService.URL_EUROPEANA, prefix = "europeana"),
		@XmlNs(namespaceURI = EuropeanaUrlService.URL_EUROPEANA+"/schemas/ese/enrichment/", prefix = "enrichment") }, elementFormDefault = javax.xml.bind.annotation.XmlNsForm.QUALIFIED)
package eu.europeana.api2.v2.model.xml.rss;

import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlSchema;
import eu.europeana.corelib.web.service.EuropeanaUrlService;
