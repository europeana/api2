package eu.europeana.api2demo.web.model;

import eu.europeana.api2.v2.model.json.user.Profile;

public class UserProfile extends Profile {

	public UserProfile() {
		super();
	}
	
	public UserProfile(String apikey, String action) {
		super(apikey, action);
	}
	
}
