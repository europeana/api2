package eu.europeana.api2.web.model.xml.rss.atom;

import javax.xml.bind.annotation.XmlAttribute;

public class AtomLink {

	@XmlAttribute
	String href = "";
	
	@XmlAttribute
	String rel = "self";
	
	@XmlAttribute
	String type = "application/rss+xml";
	
}
