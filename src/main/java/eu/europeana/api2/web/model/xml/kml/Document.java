package eu.europeana.api2.web.model.xml.kml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

public class Document {

	@XmlElement(name="ExtendedData")
	public ExtendedData extendedData = new ExtendedData();
	
	@XmlElement(name="Placemark")
	public List<Placemark> placemarks = new ArrayList<Placemark>();
}
