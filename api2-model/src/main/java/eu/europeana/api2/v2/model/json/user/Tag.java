package eu.europeana.api2.v2.model.json.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import eu.europeana.api2.v2.model.json.abstracts.UserObject;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

/**
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 * @deprecated 2018-01-09 old MyEuropeana functionality
 */
@JsonInclude(NON_EMPTY)
@Deprecated
public class Tag extends UserObject {

    public String tag;

    public String getTag() {
        return tag;
    }
}
