package eu.europeana.api2.web.model.xml.srw;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="searchRetrieveResponse")
public class SRWResponse {

	public static final String NS_SRW = "http://www.loc.gov/zing/srw/";
	public static final String NS_DIAG = "http://www.loc.gov/zing/srw/diagnostic/";
	public static final String NS_XCQL = "http://www.loc.gov/zing/cql/xcql/";
	public static final String NS_MODS = "http://www.loc.gov/mods/v3";
	public static final String NS_EUROPEANA = "http://www.europeana.eu";
	public static final String NS_ENRICHMENT = "http://www.europeana.eu/schemas/ese/enrichment/";
	public static final String NS_DCTERMS = "http://purl.org/dc/terms/";
	public static final String NS_DC = "http://purl.org/dc/elements/1.1/";
	public static final String NS_DCX = "http://purl.org/dc/elements/1.1/";
	public static final String NS_TEL = "http://krait.kb.nl/coop/tel/handbook/telterms.html";
	public static final String NS_XSI = "http://www.w3.org/2001/XMLSchema-instance";

	@XmlElement
	public String version = "1.1";

	@XmlElement(name = "records")
	public Records records = new Records();

	@XmlElement
	public EchoedSearchRetrieveRequest echoedSearchRetrieveRequest = new EchoedSearchRetrieveRequest();
}
