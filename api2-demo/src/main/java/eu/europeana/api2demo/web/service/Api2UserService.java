package eu.europeana.api2demo.web.service;

import java.util.List;

import eu.europeana.corelib.definitions.db.entity.relational.SavedItem;

public interface Api2UserService {
	
	List<SavedItem> getFavorites();

}
