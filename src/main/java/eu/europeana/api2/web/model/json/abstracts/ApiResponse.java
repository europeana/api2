package eu.europeana.api2.web.model.json.abstracts;

import java.util.Date;

public abstract class ApiResponse {
	
	public String apikey;
	
	public String action;
	
	public boolean success = true;
	
	public Date statsStartTime;

	public long statsDuration = 0;
	
	public ApiResponse(String apikey, String action) {
		this.apikey = apikey;
		this.action = action;
	}
	
}
