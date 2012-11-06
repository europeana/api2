package eu.europeana.api2.web.model.xml.rss;

import javax.xml.bind.annotation.XmlAttribute;

public class Enclosure {

	@XmlAttribute
	public String url;

	public Enclosure() {}

	public Enclosure(String url) {
		this.url = url;
	}
}
