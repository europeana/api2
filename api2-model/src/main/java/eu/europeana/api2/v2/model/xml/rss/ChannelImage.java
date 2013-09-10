package eu.europeana.api2.v2.model.xml.rss;

import javax.xml.bind.annotation.XmlElement;

public class ChannelImage {

	@XmlElement(name = "title")
	private String title = "Europeana Open Search";

	@XmlElement(name = "link")
	private String link = "http://europeana.eu";

	@XmlElement(name = "url")
	private String url = "http://www.europeana.eu/portal/sp/img/europeana-logo-en.png";

//	private int width = 206;
//
//	private int height = 123;
}
