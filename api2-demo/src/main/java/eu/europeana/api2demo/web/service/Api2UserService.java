package eu.europeana.api2demo.web.service;

import eu.europeana.api2demo.web.model.UserFavorites;

public interface Api2UserService {
	
	UserFavorites getFavorites();

	boolean createFavorite(String id);

	boolean deleteFavorite(Long id);
	
}
