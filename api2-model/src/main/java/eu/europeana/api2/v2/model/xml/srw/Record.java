package eu.europeana.api2.v2.model.xml.srw;

import javax.xml.bind.annotation.XmlElement;

import eu.europeana.api2.v1.model.json.view.FullDoc;

public class Record {

	@XmlElement
	public String recordSchema = "info:srw/schema/1/dc-v1.1";

	@XmlElement
	public String recordPacking = "xml";

	@XmlElement(name = "recordData")
	public Dc recordData = new Dc();
}
