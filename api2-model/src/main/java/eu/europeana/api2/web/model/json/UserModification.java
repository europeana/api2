package eu.europeana.api2.web.model.json;

import eu.europeana.api2.web.model.json.abstracts.ApiResponse;

public class UserModification extends ApiResponse {

	public UserModification() {
		// used by Jackson
		super();
	}

	public UserModification(String apikey, String action) {
		super(apikey, action);
	}
}
