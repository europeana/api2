package eu.europeana.api2.v2.model.json;

import eu.europeana.api2.model.json.abstracts.ApiResponse;

public class ModificationConfirmation extends ApiResponse {

	public ModificationConfirmation() {
		// used by Jackson
		super();
	}

	public ModificationConfirmation(String apikey) {
		super(apikey);
	}
}
