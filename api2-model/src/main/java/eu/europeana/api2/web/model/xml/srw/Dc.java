package eu.europeana.api2.web.model.xml.srw;

import javax.xml.bind.annotation.XmlElement;

import eu.europeana.api2.web.model.json.api1.FullDoc;

public class Dc {

	@XmlElement(name = "dc", namespace = SrwResponse.NS_DC)
	public FullDoc dc;
}
