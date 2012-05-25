package eu.europeana.api2demo.web.model;

import eu.europeana.api2.web.model.json.UserResults;
import eu.europeana.api2.web.model.json.user.Favorite;
import eu.europeana.corelib.definitions.db.entity.relational.SavedItem;

public class UserFavorites extends UserResults<Favorite> {
	
	public UserFavorites() {
		super();
	}
	
	public UserFavorites(String apikey, String action) {
		super(apikey, action);
	}

}
