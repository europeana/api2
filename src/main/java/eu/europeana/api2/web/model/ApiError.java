package eu.europeana.api2.web.model;

import eu.europeana.api2.web.model.abstracts.ApiResponse;

public class ApiError extends ApiResponse {

	private String error;

	@Override
	public boolean getSuccess() {
		return false;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

}
