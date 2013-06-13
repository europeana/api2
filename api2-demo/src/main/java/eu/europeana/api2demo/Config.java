package eu.europeana.api2demo;

import org.springframework.beans.factory.annotation.Value;

public class Config {

	@Value("#{europeanaProperties['api2.url']}")
	private String apiUrl;

	private static final String URI_API2 = "/v2";

	private static final String URI_FAVORITES_GET = "/v2/user/favorite.json";

	private static final String URI_FAVORITES_CREATE = "/v2/user/favorite.json?action=CREATE&objectid=";
	
	private static final String URI_FAVORITES_DELETE = "/v2/user/favorite.json?action=DELETE&objectid=";

	private static final String URI_TAGS_GET = "/v2/user/tag.json";

	private static final String URI_TAGS_CREATE = "/v2/user/tag.json?action=CREATE&objectid=";
	
	private static final String URI_TAGS_DELETE = "/v2/user/tag.json?action=DELETE&objectid=";

	private static final String URI_SEARCHES_GET = "/v2/user/savedsearch.json";

	private static final String URI_SEARCHES_DELETE = "/v2/user/savedsearch.json?action=DELETE&objectid=";

	public String getApiUrl() {
		return apiUrl;
	}

	public String getUriApi2() {
		return getApiUrl() + URI_API2;
	}

	public String getUriFavoritesGet() {
		return getApiUrl() + URI_FAVORITES_GET;
	}

	public String getUriFavoritesCreate() {
		return getApiUrl() + URI_FAVORITES_CREATE;
	}

	public String getUriFavoritesDelete() {
		return getApiUrl() + URI_FAVORITES_DELETE;
	}

	public String getUriTagsGet() {
		return getApiUrl() + URI_TAGS_GET;
	}

	public String getUriTagsCreate() {
		return getApiUrl() + URI_TAGS_CREATE;
	}

	public String getUriTagsDelete() {
		return getApiUrl() + URI_TAGS_DELETE;
	}

	public String getUriSearchesGet() {
		return getApiUrl() + URI_SEARCHES_GET;
	}

	public String getUriSearchesDelete() {
		return getApiUrl() + URI_SEARCHES_DELETE;
	}
}
