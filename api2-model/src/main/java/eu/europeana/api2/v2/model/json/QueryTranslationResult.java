package eu.europeana.api2.v2.model.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import eu.europeana.api2.model.json.abstracts.ApiResponse;
import eu.europeana.corelib.utils.model.LanguageVersion;

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

@JsonInclude(NON_EMPTY)
public class QueryTranslationResult extends ApiResponse {

    public List<LanguageVersion> translations;

    public String translatedQuery;

    public QueryTranslationResult(String apikey) {
        super(apikey);
    }

    public QueryTranslationResult(String apikey, long requestNumber) {
        this(apikey);
        this.requestNumber = requestNumber;
    }
}
