package eu.europeana.api2.v2.model.enums;

/**
 *	The Profile parameter controls the format and richness of the response
 * (see also http://labs.europeana.eu/api/search#profile-parameter)
 * N.B. similar profile is deprecated
 */
public enum Profile {
	MINIMAL("minimal"), STANDARD("standard"), RICH("rich");
	//, SIMILAR("similar");

	private String name;

	Profile(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
