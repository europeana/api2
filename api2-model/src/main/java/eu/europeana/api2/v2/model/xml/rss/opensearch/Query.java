package eu.europeana.api2.v2.model.xml.rss.opensearch;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 */
public class Query {

	@SuppressWarnings("unused")
	@XmlAttribute
	final String role = "request";

	@XmlAttribute
	public String searchTerms;

	@XmlAttribute
	public int startPage = 1;
}
