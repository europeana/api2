package eu.europeana.api2.web.model.xml.kml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class Data {
	
	@XmlAttribute
	public String name;
	
	@XmlElement
	public String value;

	public Data() {
	}
	
	protected Data(String name) {
		this.name = name;
	}

}
