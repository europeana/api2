package eu.europeana.api2.v2.model.xml.kml;

import javax.xml.bind.annotation.XmlElement;

/**
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 * @deprecated 2018-01-09 search with coordinates functionality
 */
public class Point {

	@XmlElement
	public String coordinates;
}
