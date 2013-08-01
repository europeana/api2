package eu.europeana.api2demo.web.model;

import eu.europeana.api2.v2.model.json.UserResults;
import eu.europeana.api2.v2.model.json.user.Search;

public class UserSearches extends UserResults<Search> {
	
	public UserSearches() {
		super();
	}
	
	public UserSearches(String apikey, String action) {
		super(apikey, action);
	}

}
