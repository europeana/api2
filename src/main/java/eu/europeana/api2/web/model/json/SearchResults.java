package eu.europeana.api2.web.model.json;

import java.util.List;

import eu.europeana.api2.web.model.json.abstracts.ApiResponse;

public class SearchResults<T> extends ApiResponse {

	public long itemsCount;

	public long totalResults;
	
	public List<T> items;
	
	public SearchResults(String apikey, String action) {
		super(apikey, action);
	}
	
}
