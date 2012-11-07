package eu.europeana.api2.web.model.xml.srw;

import javax.xml.bind.annotation.XmlElement;

public class EchoedSearchRetrieveRequest {

	@XmlElement
	private String version = "1.1";

	@XmlElement
	public String query = "";

	@XmlElement
	private String recordPacking = "xml";
}
