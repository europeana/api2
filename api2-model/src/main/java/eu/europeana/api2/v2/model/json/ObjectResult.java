package eu.europeana.api2.v2.model.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import eu.europeana.api2.model.json.abstracts.ApiResponse;
import eu.europeana.corelib.definitions.edm.beans.BriefBean;
import eu.europeana.corelib.definitions.edm.beans.FullBean;

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

/**
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 */
@JsonInclude(NON_EMPTY)
public class ObjectResult extends ApiResponse {

    public FullBean object;

    /**
     * @deprecated since January 2018 - similarItems is not being used anymore
     */
    @Deprecated
    public List<? extends BriefBean> similarItems;

    public ObjectResult(String apikey, long requestNumber) {
        super(apikey);
        this.requestNumber = requestNumber;
    }
}
