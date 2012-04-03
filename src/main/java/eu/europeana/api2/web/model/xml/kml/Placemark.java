package eu.europeana.api2.web.model.xml.kml;

import javax.xml.bind.annotation.XmlElement;

public class Placemark {
	
	@XmlElement
	public String name;
	
	@XmlElement
	public String description;
	
	@XmlElement(name="Point")
	public Point point = new Point();

	@XmlElement
	public String address;
	
	@XmlElement(name="TimeSpan")
	public TimeSpan timeSpan;
}
