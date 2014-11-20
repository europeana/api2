package eu.europeana.api2.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.europeana.api2.v2.model.xml.rss.fieldtrip.FieldTripImage;
import eu.europeana.api2.v2.model.xml.rss.fieldtrip.FieldTripItem;
import eu.europeana.corelib.definitions.solr.beans.ApiBean;
import eu.europeana.corelib.definitions.solr.beans.BriefBean;
import eu.europeana.corelib.definitions.solr.beans.RichBean;
import eu.europeana.corelib.utils.StringArrayUtils;
import eu.europeana.corelib.web.service.EuropeanaUrlService;

public class FieldTripUtils {

	Logger log = Logger.getLogger(FieldTripUtils.class.getCanonicalName());

	private EuropeanaUrlService urlService;

	private final static SimpleDateFormat FIELDTRIP_DATE_FORMATTER = new SimpleDateFormat("EEE, MMM d yyyy HH:mm:ss Z");
	private final static Pattern DESCRIPTION_PATTERN = Pattern.compile("(<description>)(.*?)(</description>)", Pattern.DOTALL);
	private final static Pattern ATTRIBUTION_PATTERN = Pattern.compile("(<attribution>)(.*?)(</attribution>)", Pattern.DOTALL);

        
	public FieldTripUtils(EuropeanaUrlService urlService) {
		this.urlService = urlService;
	}
        
        /**
	 * returns a populated FieldTripItem
         * <p> The FieldTripItem contains the guid, title, description, translated
         * EdmIsShownAt label text, thumbnail image, geographical location (point),
         * publication date and link retrieved from the provided Bean instance and
         * the translatedEdmIsShownAtLabel parameter
	 * @param bean containing the Solr query data
	 * @param translatedEdmIsShownAtLabel containing the EdmIsShownAt label 
         * text, translated in the appropriate language
	 * @return FieldTripItem instance
	 *   
	 */
	public FieldTripItem createItem(RichBean bean, String translatedEdmIsShownAtLabel) {
		FieldTripItem item = new FieldTripItem();
		item.guid = urlService.getPortalRecord(false, bean.getId()).toString();
		item.title = getTitle(bean);
		item.description = extractDescription(bean.getDcDescription());
                item.description = addShownAt(item.description, translatedEdmIsShownAtLabel, bean.getEdmIsShownAt());
		item.images = getThumbnail(bean);
		item.point = getPoint(bean);
		item.pubDate = getPublicationDate(bean.getTimestampCreated());
		item.link = getLink(bean);
		return item;
	}

	public String cleanRss(String xml) {
		xml = cleanDescriptions(xml);
		xml = cleanAttributions(xml);
		return xml;
	}

	private String cleanDescriptions(String xml) {
		return cleanElements(DESCRIPTION_PATTERN.matcher(xml));
	}

	private String cleanAttributions(String xml) {
		return cleanElements(ATTRIBUTION_PATTERN.matcher(xml));
	}

	private String cleanElements(Matcher matcher) {
		StringBuffer result = new StringBuffer();
		while (matcher.find()) {
			matcher.appendReplacement(result, cleanElement(matcher));
		}
		matcher.appendTail(result);
		return result.toString();
	}

	private String cleanElement(Matcher matcher) {
		if (matcher.group(2).contains("&lt;")) {
			return matcher.group(1) + "<![CDATA[" + unescapeTags(matcher.group(2)) + "]]>" + matcher.group(3);
		} else {
			return matcher.group(0);
		}
	}

	private String unescapeTags(String xml) {
		return xml.replace("&lt;", "<").replace("&gt;", ">");
	}

	private String getLink(RichBean bean) {
		String link = null;
		if (StringArrayUtils.isNotBlank(bean.getEdmIsShownAt())) {
			for (String candidate : bean.getEdmIsShownAt()) {
				if (StringUtils.isNotBlank(candidate)) {
					link = candidate;
					break;
				}
			}
		}
		if (link == null) {
			for (String candidate : bean.getEdmLandingPage()) {
				if (StringUtils.isNotBlank(candidate)) {
					link = candidate;
					break;
				}
			}
		}
		return link;
	}

	private String getPublicationDate(Date timestampCreated) {
		if (timestampCreated == null) {
			return null;
		}
		return FIELDTRIP_DATE_FORMATTER.format(timestampCreated);
	}
        
        /**
	 * returns the itemDescription postfixed with the HTML formatted 'EDM is 
         * shown at' data provided in the other parameters
	 * @param itemDescription String containing the description
	 * @param translatedEdmIsShownAtLabel translated label text
         * @param edmIsShownAt containing the URL to be linked to
	 * @return String with added 'EDM is shown at' HTML code
	 *   
	 */
	private String addShownAt(String itemDescription, String translatedEdmIsShownAtLabel, String[] edmIsShownAt) {
            StringBuilder sb = new StringBuilder();
            sb.append(itemDescription);
            if (edmIsShownAt != null && edmIsShownAt.length > 0
				&& StringUtils.isNotBlank(edmIsShownAt[0])) {
                sb.append("<p>");
                sb.append(translatedEdmIsShownAtLabel);
                sb.append(": <a href=\"");
                sb.append(edmIsShownAt[0]);
                sb.append("\">");
                sb.append(edmIsShownAt[0]);
                sb.append("</a>");
                sb.append("<p>");
            } 
            return sb.toString();
        }

	private String extractDescription(String[] dcDescription) {
		List<String> descriptions = new ArrayList<String>();
		for (String t : dcDescription) {
			if (StringUtils.isNotBlank(t)) {
				t = t.trim().replaceAll("\n+", "\n").replace("\n", "</p>\n<p>");
				descriptions.add("<p>" + t + "</p>");
			}
		}
		if (descriptions.size() > 0) {
			return StringUtils.join(descriptions, "\n");
		}
		return null;
	}

	private List<FieldTripImage> getThumbnail(RichBean bean) {
		List<FieldTripImage> images = new ArrayList<FieldTripImage>();

		String attribution = getAttribution(bean);

		if (bean.getEdmIsShownBy() != null) {
			for (String object : bean.getEdmIsShownBy()) {
				if (StringUtils.isNotBlank(object)) {
					FieldTripImage image = new FieldTripImage(object);
					image.attribution = attribution;
					images.add(image);
				}
			}
		}
		return images;
	}

	private String getAttribution(RichBean bean) {
		List<String> attribution = new ArrayList<String>();
		if (StringArrayUtils.isNotBlank(bean.getRights())) {
			String right = null;
			for (String candidate : bean.getRights()) {
				if (StringUtils.isNotBlank(candidate)) {
					right = candidate;
					break;
				}
			}
			attribution.add(String.format("<a href=\"%s\">Copyrights</a>", right));
		}
		/*
		if (StringArrayUtils.isNotBlank(bean.getDcCreator())) {
			attribution.add(String.format("Creator: %s", StringUtils.join(bean.getDcCreator(), ", ")));
		}
		if (StringArrayUtils.isNotBlank(bean.getProvider())) {
			attribution.add(String.format("Data provider: %s", StringUtils.join(bean.getProvider(), ", ")));
		}
		*/
		if (StringArrayUtils.isNotBlank(bean.getDataProvider())) {
			attribution.add(String.format("%s", StringUtils.join(bean.getDataProvider(), ", ")));
		}
		return StringUtils.join(attribution, " ");
	}

	private String getPoint(ApiBean bean) {
		String latitude = null;
		if (bean.getEdmPlaceLatitude() != null) {
			for (String coordinate : bean.getEdmPlaceLatitude()) {
				if (isValidCoordinate(coordinate)) {
					latitude = coordinate;
					break;
				}
			}
		}
		String longitude = null;
		if (bean.getEdmPlaceLongitude() != null) {
			for (String coordinate : bean.getEdmPlaceLongitude()) {
				if (isValidCoordinate(coordinate)) {
					longitude = coordinate;
					break;
				}
			}
		}
		if (StringUtils.isNotBlank(latitude) && StringUtils.isNotBlank(longitude)) {
			return latitude + " " + longitude;
		}
		return null;
	}

	private boolean isValidCoordinate(String coordinate) {
		return (StringUtils.isNotBlank(coordinate) 
				&& !coordinate.equals("0") 
				&& !coordinate.equals("0.0"));
	}

	private String getTitle(BriefBean bean) {
		if (!ArrayUtils.isEmpty(bean.getTitle())) {
			for (String title : bean.getTitle()) {
				if (!StringUtils.isBlank(title)) {
					return title;
				}
			}
		}
		return bean.getDataProvider()[0] + " " + bean.getId();
	}
}
