package eu.europeana.api2.web.model;

import eu.europeana.api2.web.model.abstracts.ApiResponse;

public class ApiError extends ApiResponse {
	
	

	@Override
	public boolean getHasError() {
		return true;
	}
	
}
