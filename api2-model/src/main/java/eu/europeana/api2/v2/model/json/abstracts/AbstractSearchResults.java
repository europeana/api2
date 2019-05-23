package eu.europeana.api2.v2.model.json.abstracts;

import com.fasterxml.jackson.annotation.JsonInclude;
import eu.europeana.api2.model.json.abstracts.ApiResponse;

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

/**
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 */
@JsonInclude(NON_EMPTY)
public class AbstractSearchResults<T> extends ApiResponse {

    public long itemsCount;

    @JsonInclude(NON_NULL)
    public Long totalResults = 0L;

    public String nextCursor;

    @JsonInclude(NON_NULL)
    public List<T> items;

    public AbstractSearchResults(String apikey) {
        super(apikey);
    }

    public AbstractSearchResults() {
        // used by Jackson
        super();
    }

    public Long getTotalResults() {
        if (totalResults < itemsCount) {
            return itemsCount;
        }
        return totalResults;
    }

}
