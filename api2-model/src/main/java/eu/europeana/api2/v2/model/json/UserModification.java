package eu.europeana.api2.v2.model.json;

import eu.europeana.api2.model.json.abstracts.ApiResponse;

public class UserModification extends ApiResponse {

	public UserModification() {
		// used by Jackson
		super();
	}

	public UserModification(String apikey, String action) {
		super(apikey, action);
	}
}
