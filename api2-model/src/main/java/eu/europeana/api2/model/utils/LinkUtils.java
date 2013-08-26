package eu.europeana.api2.model.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.commons.lang.StringUtils;

import eu.europeana.corelib.definitions.model.ThumbSize;

public class LinkUtils {

	private static final String IMAGE_SITE = "http://europeanastatic.eu/api/image";
	private static final String RECORD_PATH = "/v2/record/";
	private static final String PORTAL_PATH = "/record/";
	private static final String RECORD_EXT = ".json";
	private static final String PORTAL_PARAMS = ".html?utm_source=api&utm_medium=api&utm_campaign=";
	private static final String WSKEY_PARAM = "?wskey=";
	private static final String URI_PARAM = "?uri=";
	private static final String SIZE_PARAM = "&size=";
	private static final String TYPE_PARAM = "&type=";
	
	public static String getLink(String wskey, String apiUrl, String id) {
		StringBuilder url = new StringBuilder(StringUtils.stripEnd(apiUrl, "/"));
		url.append(RECORD_PATH).append(id.substring(1)).append(RECORD_EXT);
		url.append(WSKEY_PARAM).append(wskey);
		return url.toString();
	}

	public static String getGuid(String wskey, String portalUrl, String id) {
		StringBuilder url = new StringBuilder(portalUrl);
		url.append(PORTAL_PATH).append(id.substring(1));
		url.append(PORTAL_PARAMS).append(wskey);
		return url.toString();
	}
	
	public static String getThumbnailUrl(String thumbnail, String type) {
		StringBuilder url = new StringBuilder(IMAGE_SITE);
		try {
			url.append(URI_PARAM).append(URLEncoder.encode(thumbnail, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		url.append(SIZE_PARAM).append(ThumbSize.LARGE);
		url.append(TYPE_PARAM).append(type);
		return url.toString();
	}

	
}
