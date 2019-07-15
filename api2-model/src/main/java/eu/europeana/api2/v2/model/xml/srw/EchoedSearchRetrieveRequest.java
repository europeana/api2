package eu.europeana.api2.v2.model.xml.srw;

import javax.xml.bind.annotation.XmlElement;

/**
 * @deprecated Part of SRW responses which officially isn't supported any more
 */
@SuppressWarnings("unused")
@Deprecated
public class EchoedSearchRetrieveRequest {

	@XmlElement
	private String version = "1.1";

	@XmlElement
	public String query = "";

	@XmlElement
	private String recordPacking = "xml";
}
