package eu.europeana.api2.v2.model.xml.kml;

import javax.xml.bind.annotation.XmlElement;

/**
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 * @deprecated 2018-01-09 search with coordinates functionality
 */
@Deprecated
public class ExtendedData {

	@XmlElement(name = "Data")
	public Data totalResults = new Data("totalResults");

	@XmlElement(name = "Data")
	public Data startIndex = new Data("startIndex");
}
