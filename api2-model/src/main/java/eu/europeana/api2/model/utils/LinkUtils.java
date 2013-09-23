package eu.europeana.api2.model.utils;

import eu.europeana.corelib.web.utils.UrlBuilder;

public class LinkUtils {

	public static String addCampaignCodes(UrlBuilder url, String wskey) {
		url.addParam("utm_source", "api");
		url.addParam("utm_medium", "api");
		url.addParam("utm_campaign", wskey);
		return url.toString();
	}

	
}
