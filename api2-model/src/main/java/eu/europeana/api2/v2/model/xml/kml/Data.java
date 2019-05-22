package eu.europeana.api2.v2.model.xml.kml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 * @deprecated 2018-01-09 search with coordinates functionality*
 */
@Deprecated
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
