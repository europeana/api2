package eu.europeana.api2.v2.model.xml.kml;

import javax.xml.bind.annotation.XmlElement;

/**
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 * @deprecated 2018-01-09 search with coordinates functionality*
 */
@Deprecated
public class Placemark {

	@XmlElement
	public String name;

	@XmlElement
	public String description;

	@XmlElement(name = "Point")
	public Point point = new Point();

	@XmlElement
	public String address;

	@XmlElement(name = "TimeSpan")
	public TimeSpan timeSpan;
}
