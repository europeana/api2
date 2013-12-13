package eu.europeana.api2.model.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.commons.lang.StringUtils;

import eu.europeana.corelib.web.utils.UrlBuilder;

public class LinkUtils {

	public static String addCampaignCodes(UrlBuilder url, String wskey) {
		url.addParam("utm_source", "api");
		url.addParam("utm_medium", "api");
		url.addParam("utm_campaign", wskey);
		return url.toString();
	}

	public static String encode(String value) {
		if (StringUtils.isBlank(value)) {
			return "";
		}
		try {
			value = URLEncoder.encode(value, "utf-8");
		} catch (UnsupportedEncodingException e) {
			// ignore, won't happen normally
		}
		return value;
	}
}
