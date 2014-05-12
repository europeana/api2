package eu.europeana.api2.v2.model.xml.rss.fieldtrip;

public class FieldTripImage {

	public String url;

	public String title;

	public String attribution;

	public FieldTripImage() {
		super();
	}

	public FieldTripImage(String url) {
		this.url = url;
	}

	public FieldTripImage(String url, String title) {
		this(url);
		this.title = title;
	}
}
