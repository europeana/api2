package eu.europeana.api2.v2.model;

/**
 * Data object for a name - integer value pair. It is used for storing Solr parameters
 *
 * @author Peter.Kiraly@kb.nl
 */

// TODO THIS IS A DUPLICATE CLASS - also in api-war

public class NumericFacetParameter {

    /**
     * Name of parameter key
     */
    private String name;

    /**
     * The value of the parameter
     */
    private Integer value;

    /**
     * Construct with name and integer value
     *
     * @param name  The parameter name
     * @param value The parameter value as integer
     */
    public NumericFacetParameter(String name, Integer value) {
        this.name = name;
        this.value = value;
    }

    /**
     * Construct with name and alphanumeric value
     *
     * @param name  The parameter name
     * @param value The parameter value as string
     */
    public NumericFacetParameter(String name, String value) {
        Integer intValue = 0;
        try {
            intValue = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            //
        }
        this.name = name;
        this.value = intValue;
    }

    /**
     * Gets the parameter name
     *
     * @return The parameter name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the parameter value
     *
     * @return The parameter value
     */
    public Integer getValue() {
        return value;
    }

}
