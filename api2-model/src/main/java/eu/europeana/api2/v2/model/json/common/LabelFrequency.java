package eu.europeana.api2.v2.model.json.common;

import com.fasterxml.jackson.annotation.JsonInclude;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

/**
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 */
@JsonInclude(NON_EMPTY)
public class LabelFrequency {

    /**
     * The term
     */
    public String label;

    /**
     * Frequency of term in the index
     */
    public long count;
}
