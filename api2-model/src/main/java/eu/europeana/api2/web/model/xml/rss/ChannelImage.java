package eu.europeana.api2.web.model.xml.rss;

import javax.xml.bind.annotation.XmlElement;

public class ChannelImage {

	@XmlElement(name = "title")
	private String title = "europeana.eu";

	@XmlElement(name = "link")
	private String link = "http://www.europeana.eu/portal/favicon.ico";
}
