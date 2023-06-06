package eu.europeana.api2.v2.web.controller;

import org.apache.commons.lang3.StringUtils;

/**
 * Supported values for the profile parameter used in record requests
 */
public enum RecordProfile {

    SCHEMAORG("schemaOrg"), // adds schema.org data in record response
    TRANSLATE("translate"), // translates title and description in specified language (if not already available)
    PARAMS("params");       // adds the used parameters to the record response

    private String paramValue;

    RecordProfile(String paramValue) {
        this.paramValue = paramValue;
    }

    /**
     * Check if this profile is in the list of requested profiles
     * @param profiles the profile parameter value supplied in the request
     * @return true if the profile is in the request, otherwise false
     */
    public boolean isActive(String profiles) {
        return StringUtils.containsIgnoreCase(profiles, paramValue);
    }

    /**
     * @return the String value of this profile
     */
    public String getParamValue() {
        return this.paramValue;
    }
}
