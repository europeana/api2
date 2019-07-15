package eu.europeana.api2.v2.model.xml.srw;

import javax.xml.bind.annotation.XmlElement;

import eu.europeana.api2.model.xml.srw.SrwResponse;
import eu.europeana.api2.v2.model.json.view.FullDoc;

/**
 * @deprecated Part of SRW responses which officially isn't supported any more
 */
@Deprecated
public class Dc {

	@XmlElement(name = "dc", namespace = SrwResponse.NS_DC)
	public FullDoc dc;
}
