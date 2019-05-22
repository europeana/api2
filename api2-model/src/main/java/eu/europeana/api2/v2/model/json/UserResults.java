package eu.europeana.api2.v2.model.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import eu.europeana.api2.v2.model.json.abstracts.AbstractSearchResults;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

/**
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 * @deprecated 2018-01-09 old MyEuropeana functionality
 */
@JsonInclude(NON_EMPTY)
@Deprecated
public class UserResults<T> extends AbstractSearchResults<T> {

    public String username;

    public UserResults() {
        // used by Jackson
        super();
    }

    public UserResults(String apikey) {
        super(apikey);
    }

    public String getUsername() {
        return username;
    }
}
