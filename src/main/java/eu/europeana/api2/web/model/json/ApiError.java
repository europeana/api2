package eu.europeana.api2.web.model.json;

import eu.europeana.api2.web.model.json.abstracts.ApiResponse;

public class ApiError extends ApiResponse {

	public String error;
	
	public boolean success = false;
	
	public ApiError(String apikey, String action, String error) {
		super(apikey, action);
		this.error = error;
	}

}
