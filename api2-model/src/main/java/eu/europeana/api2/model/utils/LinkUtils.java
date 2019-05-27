package eu.europeana.api2.model.utils;

/**
 * @deprecated 2018-01-09 Seems like we don't use campaigncodes anymore, even though they are still present in some
 * links (e.g. search results item.guid)
 */
@Deprecated
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
