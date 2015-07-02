package eu.europeana.api2.v2.model.xml.rss;

import javax.xml.bind.annotation.XmlElement;

import eu.europeana.corelib.web.service.EuropeanaUrlService;

@SuppressWarnings("unused")
public class ChannelImage {

	@XmlElement(name = "title")
	private String title = "Europeana Open Search";

	@XmlElement(name = "link")
	private String link = EuropeanaUrlService.URL_EUROPEANA;

	@XmlElement(name = "url")
	private String url = EuropeanaUrlService.URL_EUROPEANA + "/portal/sp/img/europeana-logo-en.png";

//	private int width = 206;
//
//	private int height = 123;
}
