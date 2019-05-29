package eu.europeana.api2.v2.model.xml.rss;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 */
@XmlRootElement(name = "rss")
public class RssResponse {

	@SuppressWarnings("unused")
	@XmlAttribute
	final String version = "2.0";

	@XmlElement(name = "channel")
	public Channel channel = new Channel();
}
