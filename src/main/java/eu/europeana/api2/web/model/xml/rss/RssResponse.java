package eu.europeana.api2.web.model.xml.rss;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="rss")
public class RssResponse {
	
	@XmlElement(name="channel")
	Channel channel = new Channel();

}
