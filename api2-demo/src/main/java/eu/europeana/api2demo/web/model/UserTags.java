package eu.europeana.api2demo.web.model;

import eu.europeana.api2.web.model.json.UserResults;
import eu.europeana.api2.web.model.json.user.Tag;

public class UserTags extends UserResults<Tag> {
	
	public UserTags() {
		super();
	}
	
	public UserTags(String apikey, String action) {
		super(apikey, action);
	}

}
