package eu.europeana.api2.model.enums;

public enum Profile {
	MINIMAL("minimal"), STANDARD("standard"), RICH("rich"), SIMILAR("similar");

	private String name;

	Profile(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
