package eu.europeana.api2.v2.model.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRawValue;
import eu.europeana.api2.model.json.abstracts.ApiResponse;
import eu.europeana.corelib.definitions.edm.beans.FullBean;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

/**
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 */
@JsonInclude(NON_EMPTY)
public class ObjectResult extends ApiResponse {

    public FullBean object;

    @JsonRawValue
    public String schemaOrg;

    public ObjectResult(String apikey) {
        super(apikey);
    }

    @Deprecated
    public ObjectResult(String apikey, long requestNumber) {
        super(apikey);
        this.requestNumber = requestNumber;
    }
}
