package eu.europeana.api2demo;

public class Config {
	
	public static final String URI_API2 = "http://localhost:8080/api";

	public static final String URI_FAVORITES_GET = URI_API2 + "/user/favorite.json";

	public static final String URI_FAVORITES_CREATE = URI_API2 + "/user/favorite.json?action=CREATE&objectid=";
	
	public static final String URI_FAVORITES_DELETE = URI_API2 + "/user/favorite.json?action=DELETE&objectid=";

	public static final String URI_TAGS_GET = URI_API2 + "/user/tag.json";

	public static final String URI_TAGS_CREATE = URI_API2 + "/user/tag.json?action=CREATE&objectid=";
	
	public static final String URI_TAGS_DELETE = URI_API2 + "/user/tag.json?action=DELETE&objectid=";
	
}
