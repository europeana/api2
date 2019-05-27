package eu.europeana.api2.v2.model.xml.rss.fieldtrip;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

public class FieldTripChannel {

	@XmlElement
	public String title = "Europeana FieldTrip";

	@XmlElement
	public String link;

	@XmlElement
	public String description = "Europeana FieldTrip";

	@XmlElement
	public String language;

	@XmlElement
	public String url;

	@XmlElement
	public FieldTripImage image;

	@XmlElement(name = "item")
	public List<FieldTripItem> items = new ArrayList<>();
}
