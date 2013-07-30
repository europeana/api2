package eu.europeana.api2demo.web.model;

import eu.europeana.api2.v2.model.json.UserResults;
import eu.europeana.api2.v2.model.json.user.SavedItem;

public class UserSavedItems extends UserResults<SavedItem> {
	
	public UserSavedItems() {
		super();
	}
	
	public UserSavedItems(String apikey, String action) {
		super(apikey, action);
	}

}
