package eu.europeana.api2.web.model.xml.kml;

import javax.xml.bind.annotation.XmlElement;

public class ExtendedData {
	
	@XmlElement(name="Data")
	public Data totalResults = new Data("totalResults");
	
	@XmlElement(name="Data")
	public Data startIndex = new Data("startIndex");

}
