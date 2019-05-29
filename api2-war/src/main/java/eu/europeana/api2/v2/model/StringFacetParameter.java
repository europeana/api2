package eu.europeana.api2.v2.model;

/**
 * Data object for a String - String value pair for the date range facet
 * @author luthien
 */


public class StringFacetParameter {

	private String name;
	private String value;

	public StringFacetParameter(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

}
