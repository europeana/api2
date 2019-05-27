package eu.europeana.api2.v2.model.xml.rss;

import javax.xml.bind.annotation.XmlAttribute;

public class Enclosure {

	@XmlAttribute
	public String url;

	public Enclosure() {
	}

	public Enclosure(String url) {
		this.url = url;
	}
}
