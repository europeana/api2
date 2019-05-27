package eu.europeana.api2.v2.model.xml.kml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

/**
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 * @deprecated 2018-01-09 search with coordinates functionality
 */
@Deprecated
public class Document {

	@XmlElement(name = "ExtendedData")
	public ExtendedData extendedData = new ExtendedData();

	@XmlElement(name = "Placemark")
	public List<Placemark> placemarks = new ArrayList<>();
}
