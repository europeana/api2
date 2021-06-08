package eu.europeana.api2.model.utils;

/**
 * @deprecated We'll stop using Google Analytics soon
 */
@Deprecated(since = "May 2021")
public class LinkUtils {

	public static String addCampaignCodes(String url, String wskey) {
		StringBuilder s = new StringBuilder(url);
		s.append("?utm_source=api");
		s.append("&utm_medium=api");
        s.append("&utm_campaign=");
		s.append(wskey);
		return s.toString();
	}
}
