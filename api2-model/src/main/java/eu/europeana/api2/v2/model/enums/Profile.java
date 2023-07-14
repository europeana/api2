package eu.europeana.api2.v2.model.enums;

/**
 *	The Profile parameter controls the format and richness of the response
 *  Supported values for the profile parameter used in serach and record requests
 * @author srishti singh (refractored @since 14 July 2023)
 *
 */
public enum Profile {
    MINIMAL("minimal"),
    STANDARD("standard"),
    RICH("rich"),
    DEBUG("debug"),
    TRANSLATE("translate"), // translates title and description in specified language (if not already available)
    HITS("hits"),
    SCHEMAORG("schemaOrg"), // adds schema.org data in record response
    PARAMS("params"),
    PORTAL("portal"),  // used in Search
    FACETS("facets"), // used in Search
    SPELLING("spelling"); // @Deprecated(since = "May 2021")

    private String name;

    Profile(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /**
     * Check if the profile is a valid profile
     * @param profile
     * @return
     */
    public static boolean isValid(String profile) {
        for (Profile field : Profile.values()) {
            if (field.getName().equalsIgnoreCase(profile)) {
                return true;
            }
        }
        return false;
    }

    /**
     * return the profile matching the value
     * @param profile
     * @return
     */
    public static Profile getValue(String profile) {
        for (Profile field : Profile.values()) {
            if (field.getName().equalsIgnoreCase(profile)) {
                return field;
            }
        }
        return null;
    }
}
