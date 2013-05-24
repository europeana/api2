package eu.europeana.api2demo.web.model;

import eu.europeana.api2.v2.model.json.UserResults;
import eu.europeana.corelib.definitions.db.entity.relational.SavedSearch;

public class UserSearches extends UserResults<SavedSearch> {
	
	public UserSearches() {
		super();
	}
	
	public UserSearches(String apikey, String action) {
		super(apikey, action);
	}

}
