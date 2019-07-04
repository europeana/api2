package eu.europeana.api2.v2.model.enums;

/**
 *	The Profile parameter controls the format and richness of the response
 * (see also http://labs.europeana.eu/api/search#profile-parameter)
 */
public enum Profile {
    MINIMAL("minimal"), STANDARD("standard"), RICH("rich"), DEBUG("debug");

    private String name;

    Profile(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
