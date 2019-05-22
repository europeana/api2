package eu.europeana.api2.v2.model.xml.rss.atom;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import eu.europeana.api2.v2.model.xml.definitions.Namespaces;

/**
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 */
@SuppressWarnings("unused")
@XmlType(namespace = Namespaces.NS_ATOM)
public class AtomLink {

	@XmlAttribute
	public String href = "";

	@XmlAttribute
	final String rel = "search";

	@XmlAttribute
	final String type = "application/rss+xml";
}
