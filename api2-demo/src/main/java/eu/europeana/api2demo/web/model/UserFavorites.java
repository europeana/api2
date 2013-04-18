package eu.europeana.api2demo.web.model;

import eu.europeana.api2.v2.model.json.UserResults;
import eu.europeana.api2.v2.model.json.user.Favorite;

public class UserFavorites extends UserResults<Favorite> {
	
	public UserFavorites() {
		super();
	}
	
	public UserFavorites(String apikey, String action) {
		super(apikey, action);
	}

}
