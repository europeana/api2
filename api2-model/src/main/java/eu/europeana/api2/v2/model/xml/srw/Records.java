package eu.europeana.api2.v2.model.xml.srw;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

public class Records {

	@XmlElement(name = "record")
	public List<Record> record = new ArrayList<>();
}
