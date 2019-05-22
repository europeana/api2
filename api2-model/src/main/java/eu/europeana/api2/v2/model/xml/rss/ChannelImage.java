package eu.europeana.api2.v2.model.xml.rss;

import javax.xml.bind.annotation.XmlElement;

import eu.europeana.corelib.definitions.EuropeanaStaticUrl;

@SuppressWarnings("unused")
public class ChannelImage {

	@XmlElement(name = "title")
	private String title = "Europeana Open Search";

	@XmlElement(name = "link")
	private String link = EuropeanaStaticUrl.EUROPEANA_PORTAL_URL;

	@XmlElement(name = "url")
	private String url = "https://style.europeana.eu/images/europeana-logo-default.png";

}
