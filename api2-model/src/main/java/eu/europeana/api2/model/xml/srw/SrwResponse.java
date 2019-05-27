package eu.europeana.api2.model.xml.srw;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import eu.europeana.api2.v2.model.xml.srw.EchoedSearchRetrieveRequest;
import eu.europeana.api2.v2.model.xml.srw.Records;
import eu.europeana.corelib.definitions.EuropeanaStaticUrl;

@XmlRootElement(name = "searchRetrieveResponse")
public class SrwResponse {

	public static final String NS_SRW = "http://www.loc.gov/zing/srw/";
	public static final String NS_DIAG = "http://www.loc.gov/zing/srw/diagnostic/";
	public static final String NS_XCQL = "http://www.loc.gov/zing/cql/xcql/";
	public static final String NS_MODS = "http://www.loc.gov/mods/v3";
	public static final String NS_EUROPEANA = EuropeanaStaticUrl.EUROPEANA_NAMESPACE_URL;
	public static final String NS_ENRICHMENT = EuropeanaStaticUrl.EUROPEANA_NAMESPACE_URL + "/schemas/ese/enrichment/";
	public static final String NS_DCTERMS = "http://purl.org/dc/terms/";
	public static final String NS_DC = "http://purl.org/dc/elements/1.1/";
	public static final String NS_DCX = "http://purl.org/dc/elements/1.1/";
	public static final String NS_TEL = "http://krait.kb.nl/coop/tel/handbook/telterms.html";
	public static final String NS_XSI = "http://www.w3.org/2001/XMLSchema-instance";

	@XmlElement
	public String version = "1.1";

	@XmlElement(name = "records")
	public Records records = new Records();

	@SuppressWarnings("unused")
	@XmlElement
	public EchoedSearchRetrieveRequest echoedSearchRetrieveRequest = new EchoedSearchRetrieveRequest();
}
