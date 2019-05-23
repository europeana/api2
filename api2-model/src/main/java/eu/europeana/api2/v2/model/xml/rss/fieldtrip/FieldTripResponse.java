package eu.europeana.api2.v2.model.xml.rss.fieldtrip;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 */
@XmlRootElement(name = "rss")
public class FieldTripResponse {

	@SuppressWarnings("unused")
	@XmlAttribute
	final String version = "2.0";

	@XmlElement(name = "channel")
	public FieldTripChannel channel = new FieldTripChannel();
}
