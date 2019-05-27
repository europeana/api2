package eu.europeana.api2.v2.model.json.view.submodel;

import com.fasterxml.jackson.annotation.JsonInclude;
import eu.europeana.api2.v2.model.json.common.LabelFrequency;

import java.util.ArrayList;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

/**
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 */
@JsonInclude(NON_EMPTY)
public class Facet {
    public String name;

    public List<LabelFrequency> fields = new ArrayList<>();
}
