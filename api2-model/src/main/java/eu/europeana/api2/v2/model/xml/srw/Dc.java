package eu.europeana.api2.v2.model.xml.srw;

import javax.xml.bind.annotation.XmlElement;

import eu.europeana.api2.model.xml.srw.SrwResponse;
import eu.europeana.api2.v1.model.json.view.FullDoc;

public class Dc {

	@XmlElement(name = "dc", namespace = SrwResponse.NS_DC)
	public FullDoc dc;
}
