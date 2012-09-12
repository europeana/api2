package eu.europeana.api2demo.web.service;

import eu.europeana.api2demo.web.model.UserFavorites;
import eu.europeana.api2demo.web.model.UserTags;

public interface Api2UserService {

	// FAVORITES
	UserFavorites getFavorites();

	boolean createFavorite(String id);

	boolean deleteFavorite(Long id);

	// TAGS
	UserTags getTags();

	boolean createTag(String id, String tag);

	boolean deleteTag(Long id);
}
