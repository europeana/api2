package eu.europeana.api2.web.model.xml.rss;

import javax.xml.bind.annotation.XmlElement;

public class Item {
	
	@XmlElement
	public String guid;
	
	@XmlElement
	public String title;
	
	@XmlElement
	public String link;

	@XmlElement
	public String description;
	
}
