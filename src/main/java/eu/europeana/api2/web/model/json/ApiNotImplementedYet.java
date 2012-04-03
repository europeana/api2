package eu.europeana.api2.web.model.json;


public class ApiNotImplementedYet extends ApiError {
	
	public ApiNotImplementedYet(String apikey, String action) {
		super(apikey, action, "Not Implemented Yet!");
	}
	
	
}
