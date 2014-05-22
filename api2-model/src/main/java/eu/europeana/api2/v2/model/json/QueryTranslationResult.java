package eu.europeana.api2.v2.model.json;

import java.util.List;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

import eu.europeana.api2.model.json.abstracts.ApiResponse;
import eu.europeana.corelib.utils.model.LanguageVersion;

@JsonSerialize(include = Inclusion.NON_EMPTY)
public class QueryTranslationResult extends ApiResponse {

	public List<LanguageVersion> translations;

	public String translatedQuery;

	public QueryTranslationResult(String apikey, String action) {
		super(apikey, action);
	}

	public QueryTranslationResult(String apikey, String action, long requestNumber) {
		this(apikey, action);
		this.requestNumber = requestNumber;
	}
}
