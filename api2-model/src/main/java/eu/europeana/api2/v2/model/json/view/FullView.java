package eu.europeana.api2.v2.model.json.view;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.europeana.corelib.definitions.edm.entity.*;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

import eu.europeana.corelib.definitions.edm.beans.BriefBean;
import eu.europeana.corelib.definitions.edm.beans.FullBean;
import eu.europeana.corelib.definitions.solr.DocType;
import eu.europeana.corelib.utils.DateUtils;
import eu.europeana.corelib.web.service.EuropeanaUrlService;
import eu.europeana.corelib.web.service.impl.EuropeanaUrlServiceImpl;

@JsonSerialize(include = Inclusion.NON_EMPTY)
public class FullView implements FullBean {

	private FullBean bean;
	private String profile;
	private long uid;
	private boolean optOut;
	private EuropeanaUrlService europeanaUrlService;
	private Date timestampCreated;
	private Date timestampUpdated;
	private boolean urlified = false;

	public FullView(FullBean bean, boolean optOut) {
		this.bean = bean;
		this.optOut = optOut;
		europeanaUrlService = EuropeanaUrlServiceImpl.getBeanInstance();
		extractTimestampCreated();
		extractTimestampUpdated();
	}

	public FullView(FullBean bean, String profile, boolean optOut) {
		this(bean, optOut);
		this.profile = profile;
	}

	public FullView(FullBean bean, String profile, long uid, boolean optOut) {
		this(bean, profile, optOut);
		this.uid = uid;
	}

	@Override
	public String getId() {
		return null; // bean.getId();
	}

	@Override
	public Boolean isOptedOut() {
		return null;
	}

	@Override
	public String[] getUserTags() {
		return bean.getUserTags();
	}

	@Override
	public List<? extends Place> getPlaces() {
		@SuppressWarnings("unchecked")
		List<Place> items = (List<Place>) bean.getPlaces();
		for (Place item : items) {
			item.setId(null);
		}
		return items;
	}

	@Override
	public void setPlaces(List<? extends Place> places) {
	}

	@Override
	public List<? extends Agent> getAgents() {
		@SuppressWarnings("unchecked")
		List<Agent> items = (List<Agent>) bean.getAgents();
		for (Agent item : items) {
			item.setId(null);
		}
		return items;
	}

	@Override
	public List<? extends Timespan> getTimespans() {
		@SuppressWarnings("unchecked")
		List<Timespan> items = (List<Timespan>) bean.getTimespans();
		for (Timespan item : items) {
			item.setId(null);
		}
		return items;
	}

	@Override
	public List<? extends Concept> getConcepts() {
		@SuppressWarnings("unchecked")
		List<Concept> items = (List<Concept>) bean.getConcepts();
		for (Concept item : items) {
			item.setId(null);
		}
		return items;
	}

	@Override
	public void setConcepts(List<? extends Concept> concepts) {
	}

	@Override
	public void setAggregations(List<? extends Aggregation> aggregations) {
	}

	@Override
	public List<? extends Proxy> getProxies() {
		@SuppressWarnings("unchecked")
		List<Proxy> items = (List<Proxy>) bean.getProxies();
		for (Proxy item : items) {
			item.setId(null);
		}
		return items;
	}

	@Override
	public void setProxies(List<? extends Proxy> proxies) {
	}

	@Override
	public void setTimespans(List<? extends Timespan> timespans) {
	}

	@Override
	public List<? extends Aggregation> getAggregations() {
		@SuppressWarnings("unchecked")
		List<Aggregation> items = (List<Aggregation>) bean.getAggregations();
		for (Aggregation item : items) {
			item.setId(null);

			// add bt=europanaapi
			String isShownAt = item.getEdmIsShownAt();
			if (!urlified && isShownAt != null) {
				isShownAt = isShownAt
						+ (isShownAt.contains("?") ? "&" : "?")
						+ "bt=europeanaapi";
				// items.get(i).setEdmIsShownAt(isShownAt);

				String provider = item.getEdmProvider().values()
						.iterator().next().get(0);
				String isShownAtLink = europeanaUrlService.getApi2Redirect(uid,
						isShownAt, provider, bean.getAbout(),
						profile).toString();
				item.setEdmIsShownAt(isShownAtLink);
				urlified = true; // do this ONLY ONCE
			}

			// remove edm:object if it is a opted out record
			if (optOut) {
				item.setEdmObject(null);
			}

			// remove webresources IDs
			for (int j = 0, lw = item.getWebResources().size(); j < lw; j++) {
				item.getWebResources().get(j).setId(null);
			}
		}
		return items;
	}

	@Override
	public List<? extends BriefBean> getSimilarItems() {
		return null;
	}

	@Override
	public void setSimilarItems(List<? extends BriefBean> similarItems) {
	}

	@Override
	public List<? extends ProvidedCHO> getProvidedCHOs() {
		@SuppressWarnings("unchecked")
		List<ProvidedCHO> items = (List<ProvidedCHO>) bean.getProvidedCHOs();
		for (ProvidedCHO item : items) {
			item.setId(null);
		}
		return items;
	}

	@Override
	public void setProvidedCHOs(List<? extends ProvidedCHO> providedCHOs) {
	}

	@Override
	public String getAbout() {
		return bean.getAbout();
	}

	@Override
	public EuropeanaAggregation getEuropeanaAggregation() {
		EuropeanaAggregation europeanaAggregation = bean
				.getEuropeanaAggregation();
		europeanaAggregation.setId(null);
		String edmPreview = "";
		if (this.getAggregations().get(0).getEdmObject() != null) {
			String url = this.getAggregations().get(0).getEdmObject();
			if (StringUtils.isNotBlank(url)) {
				edmPreview = europeanaUrlService.getThumbnailUrl(url, getType()).toString();
			}
		}
		europeanaAggregation.setEdmPreview(edmPreview);
		return europeanaAggregation;
	}

	@Override
	public void setEuropeanaAggregation(
			EuropeanaAggregation europeanaAggregation) {
	}

	@Override
	public String[] getTitle() {
		return bean.getTitle();
	}

	@Override
	public String[] getYear() {
		return bean.getYear();
	}

	@Override
	public String[] getProvider() {
		return bean.getProvider();
	}

	@Override
	public String[] getLanguage() {
		return bean.getLanguage();
	}

	@Override
	public DocType getType() {
		return bean.getType();
	}

	@Override
	public int getEuropeanaCompleteness() {
		return bean.getEuropeanaCompleteness();
	}

	@Override
	public String[] getEuropeanaCollectionName() {
		return bean.getEuropeanaCollectionName();
	}

	public String[] getEdmDatasetName() {
		return getEuropeanaCollectionName();
	}

	@Override
	public String[] getCountry() {
		return bean.getCountry();
	}

	@Override
	public Date getTimestamp() {
		return bean.getTimestamp();
	}

	// unwanted setters

	@Override
	public void setEuropeanaId(ObjectId europeanaId) {
	}

	@Override
	public void setTitle(String[] title) {
	}

	@Override
	public void setYear(String[] year) {
	}

	@Override
	public void setProvider(String[] provider) {
	}

	@Override
	public void setLanguage(String[] language) {
	}

	@Override
	public void setType(DocType type) {
	}

	@Override
	public void setEuropeanaCompleteness(int europeanaCompleteness) {
	}

	@Override
	public void setAbout(String about) {
	}

	@Override
	public void setAgents(List<? extends Agent> agents) {
	}

	@Override
	public void setCountry(String[] country) {
	}

	@Override
	public void setEuropeanaCollectionName(String[] europeanaCollectionName) {
	}

	@Override
	public void setOptOut(boolean optOut) {
	}

	public void extractTimestampCreated() {
		if (timestampCreated == null) {
			timestampCreated = bean.getTimestampCreated() != null ? bean.getTimestampCreated()
				: new Date(0);
		}
	}

	public void extractTimestampUpdated() {
		if (timestampUpdated == null) {
			timestampUpdated = bean.getTimestampUpdated() != null ? bean.getTimestampUpdated()
					: new Date(0);
		}
	}

	@JsonProperty("timestamp_created")
	public String getTimestampCreatedAsISO() {
		return DateUtils.format(timestampCreated);
	}

	@Override
	@JsonProperty("timestamp_created_epoch")
	public Date getTimestampCreated() {
		return timestampCreated;
	}

	@JsonProperty("timestamp_update")
	public String getTimestampUpdatedAsISO() {
		return DateUtils.format(timestampUpdated);
	}

	@Override
	@JsonProperty("timestamp_update_epoch")
	public Date getTimestampUpdated() {
		return timestampUpdated;
	}

	@Override
	public void setTimestampCreated(Date timestampCreated) {}

	@Override
	public void setTimestampUpdated(Date timestampUpdated) {}

	@Override
	public List<? extends License> getLicenses() {
		return bean.getLicenses();
	}

	@Override
	public void setLicenses(List<? extends License> licenses) {
		
	}

	public String getAttributionSnippet(){
		return getAttributionSnippet(true, true);
	}

	public String getAttributionSnippet(boolean htmlOut){
		return getAttributionSnippet(true, htmlOut);
	}

	public String getAttributionSnippet(boolean firstOnly, boolean htmlOut){
		String rightsPage = "rel=\"xhv:license http://www.europeana.eu/schemas/edm/rights\"";
		String resPdUsgGd = "resource=\"http://www.europeana.eu/rights/pd-usage-guide/\"";
		String aHref = "<a href=\"";
		String zHref = "\">";
		String ferh = "</a>";
		String naps = "</span>";
		String retval = "", landingPage = "", title = "", creator = "", dataProvider = "", shownAt = "", shownBy = "", rights = "";
		int i, j;


		List<? extends Proxy> prxs = getProxies();
		EuropeanaAggregation euAgg = getEuropeanaAggregation();
		Aggregation agg = getAggregations().get(0);

		// if there are proxies present: check if there are dc:creator / dc:title data in there
		if (!prxs.isEmpty()){
			for (Proxy prx : prxs) {
				// check for dc:creator
				if ("".equals(creator) && prx.getDcCreator() != null && !prx.getDcCreator().isEmpty()){
					if (prx.getDcCreator().get("def") != null) {
						List<String> dcc = stripEmptyStrings(prx.getDcCreator().get("def"));
						// assign possible multiple 'def' entries to creator
						j = dcc.size();
						i = 1;
						creator += "(def) ";
						for (String creatorEntry : dcc) {
							creator += cleanUp(creatorEntry) + (i < j ? "; " : "");
							i++;
						}
					} else {
						// no 'def' key implies 1 or more language-specific keys. loop through those. For every one:
						// loop through possible multiple entries & concat those
						for (Map.Entry<String, List<String>> langCreatorEntry : prx.getDcCreator().entrySet()) {
							List<String> lcev = stripEmptyStrings(langCreatorEntry.getValue());
							j = lcev.size();
							i = 1;
							creator += "(" + langCreatorEntry.getKey() + ") ";
							for (String langCreatorLine : lcev) {
								creator += cleanUp(langCreatorLine) + (i < j ? "; " : "");
								i++;
							}
						}
					}
				}
				// check for dc:title
				if ("".equals(title) && prx.getDcTitle() != null && !prx.getDcTitle().isEmpty()){
					if (prx.getDcTitle().get("def") != null) {
						List<String> dct = stripEmptyStrings(prx.getDcTitle().get("def"));
						j = dct.size();
						i = 1;
						title += "(def) ";
						for (String titleEntry : dct) {
							title += cleanUp(titleEntry) + (i < j ? "; " : "");
							i++;
						}
					} else {
						for (Map.Entry<String, List<String>> langTitleEntry : prx.getDcTitle().entrySet()) {
							List<String> ltev = stripEmptyStrings(langTitleEntry.getValue());
							j = ltev.size();
							i = 1;
							title += "(" + langTitleEntry.getKey() + ") ";
							for (String langTitleLine : ltev) {
								title += cleanUp(langTitleLine) + (i < j ? "; " : "");
								i++;
							}
						}
					}
				}
			}
		}


		// if an aggregation is present; fetch the shownAt & shownBy
		// if the aggregation contains webresources, check if the 'about' URL is equal to
		// the edm:isShownBy URL from the aggregation (if available).
		// If this is the case, fetch the *first* edm:rights from the webresource

		if (agg!= null) {
			// check if aggregation contains an edmIsShownAt URL
			if (!"".equals(agg.getEdmIsShownAt())){
				shownAt = cleanUp(agg.getEdmIsShownAt());
			}
			// check if aggregation contains an edm:isShownBy URL
			if (!"".equals(agg.getEdmIsShownBy())){
				shownBy = agg.getEdmIsShownBy();
				// check if aggregation contains webresources
				if (agg.getWebResources() != null && !agg.getWebResources().isEmpty()) {
					List<? extends WebResource> wRess = agg.getWebResources();
					for (WebResource wRes : wRess) {
						// ++++ leave webresource-level dc:creator for 2nd iteration (David, Antoine) ++++
//						if (wRes.getAbout().equalsIgnoreCase(shownBy)
//								&& wRes.getDcCreator() != null){
//							// there is a webresource where the about URL == aggregation edm:isShownBy
//							// and which has a dc:creator entry
//							if (wRes.getDcCreator().get("def") != null) {
//								List<String> dcc = stripEmptyStrings(wRes.getDcCreator().get("def"));
//								// assign possible multiple 'def' entries to creator
//								j = dcc.size();
//								i = 1;
//								creator += "(def) ";
//								for (String creatorEntry : dcc) {
//									creator += cleanUp(creatorEntry) + (i < j ? "; " : "");
//									i++;
//								}
//							} else {
//								// no 'def' key implies 1 or more language-specific keys. loop through those. For every one:
//								// loop through possible multiple entries & concat those
//								for (Map.Entry<String, List<String>> langCreatorEntry : wRes.getDcCreator().entrySet()) {
//									List<String> lcev = stripEmptyStrings(langCreatorEntry.getValue());
//									j = lcev.size();
//									i = 1;
//									creator += "(" + langCreatorEntry.getKey() + ") ";
//									for (String langCreatorLine : lcev) {
//										creator += cleanUp(langCreatorLine) + (i < j ? "; " : "");
//										i++;
//									}
//								}
//							}
//						} else
						// check if the webResource.about URL == aggregation's isShownBy URL
						if (wRes.getAbout().equalsIgnoreCase(shownBy)
								&& wRes.getWebResourceEdmRights() != null) {
							// fetch edm:rights values
							for (Map.Entry<String, List<String>> wrEdmRights : wRes.getWebResourceEdmRights().entrySet()) {
								List<String> wrer = stripEmptyStrings(wrEdmRights.getValue());
								if (wrer != null && !wrer.isEmpty()){
									rights += cleanUp(wrer.get(0));
									break; // needed ernly wernce
								}
							}
						}
					}
				}
			}

			// check if aggregation contains edm:dataprovider: yes? Get it.
			if (agg.getEdmDataProvider() != null && !agg.getEdmDataProvider().isEmpty()){
				if (agg.getEdmDataProvider().get("def") != null) {
					List<String> edp = stripEmptyStrings(agg.getEdmDataProvider().get("def"));
					j = edp.size();
					i = 1;
					dataProvider += "(def) ";
					for (String dataProviderEntry : edp) {
						dataProvider += cleanUp(dataProviderEntry) + (i < j ? "; " : "");
						i++;
					}
				} else {
					for (Map.Entry<String, List<String>> langDataProviderEntry : agg.getEdmDataProvider().entrySet()) {
						List<String> ldpev = stripEmptyStrings(langDataProviderEntry.getValue());
						j = ldpev.size();
						i = 1;
						dataProvider += "(" + langDataProviderEntry.getKey() + ") ";
						for (String langDataProviderLine : ldpev) {
							dataProvider += cleanUp(langDataProviderLine) + (i < j ? "; " : "");
							i++;
						}
					}
				}
			}

			// If no edm:rights entries were found on the webresources, check the aggregation itself
			if ("".equals(rights) && agg.getEdmRights() != null && !agg.getEdmRights().isEmpty()) {
				for (Map.Entry<String, List<String>> edmRights : agg.getEdmRights().entrySet()) {
					List<String> edr = stripEmptyStrings(edmRights.getValue());
					if (edr != null && !edr.isEmpty()){
						rights += cleanUp(edr.get(0));
						break; // needed ernly wernce
					}
				}
			}
		}

		// check if there's a Europeana Aggregation. If found, get the edm:landingPage
		if (euAgg != null) {
			landingPage = "".equals(euAgg.getEdmLandingPage()) ? "" : euAgg.getEdmLandingPage();

			// if there was no dc:creator in the webresources, check the Europeana aggregation
			if ("".equals(creator) && euAgg.getDcCreator() != null && !euAgg.getDcCreator().isEmpty()) {
				if (euAgg.getDcCreator().get("def") != null) {
					List<String> dcc = stripEmptyStrings(euAgg.getDcCreator().get("def"));
					j = dcc.size();
					i = 1;
					creator += "(def) ";
					for (String creatorEntry : dcc) {
						creator += cleanUp(creatorEntry) + (i < j ? "; " : "");
						i++;
					}
				} else {
					for (Map.Entry<String, List<String>> langCreatorEntry : euAgg.getDcCreator().entrySet()) {
						List<String> lcev = stripEmptyStrings(langCreatorEntry.getValue());
						j = lcev.size();
						i = 1;
						creator += "(" + langCreatorEntry.getKey() + ") ";
						for (String langCreatorLine : lcev) {
							creator += cleanUp(langCreatorLine) + (i < j ? "; " : "");
							i++;
						}
					}
				}
			}
			// if there was no edm:rights in the webresources or in the aggregation itself,
			// check the Europeana aggregation
			if ("".equals(rights) && euAgg.getEdmRights() != null && !euAgg.getEdmRights().isEmpty()) {
				for (Map.Entry<String, List<String>> euEdmRights : euAgg.getEdmRights().entrySet()) {
					List<String> euer = stripEmptyStrings(euEdmRights.getValue());
					if (euer != null && !euer.isEmpty()){
						rights += cleanUp(euer.get(0));
						break; // needed ernly wernce
					}
				}
			}
		}

		// if there was no title found in the proxy, get it from the record object itself
		if ("".equals(title) && getTitle() != null && !ArrayUtils.isEmpty(getTitle())) {
			String[] titles = stripEmptyStrings(getTitle());
			j = titles.length;
			i = 1;
			for (String titlePart : titles) {
				title += cleanUp(titlePart);
				if (firstOnly) {
					break;
				} else if (i < j) {
					title += "; ";
				}
				i++;
			}
		}

		if (htmlOut){
			if (!"".equals(title)){
				if (!"".equals(landingPage)) {
					retval += spannify("about", landingPage) + aHref + landingPage + zHref;
				}
				retval += spannify("property", "dc:title") + title + naps;
				if (!"".equals(landingPage)) {
					retval += ferh;
				}
				retval += ". ";
			}
			retval += !"".equals(creator) ? spannify("property", "dc:creator") + creator + naps + ". " : "";

			if (!"".equals(dataProvider)){
				if (!"".equals(shownAt)) {
					retval += aHref + shownAt + zHref;
				}
				retval += dataProvider + ". ";
				if (!"".equals(shownAt)) {
					retval += ferh;
				}
			}
			if (!"".equals(rights)){
				retval += aHref + rights + "\" " + rightsPage + ">" + getRightsLabel(rights) + ferh + spannify("rel", "cc:useGuidelines") + resPdUsgGd + ".";
			}
			if (!"".equals(landingPage)) {
				retval += naps; // close opening <span about ...>
			}
			return retval;
		} else {
			retval += title;
			retval += (!"".equals(title) && !"".equals(landingPage)) ? " - " : "";
			retval += landingPage;
			retval += (!"".equals(title) || !"".equals(landingPage)) ? ". " : "";
			retval += !"".equals(creator) ? creator + ". " : "";
			retval += dataProvider;
			retval += (!"".equals(dataProvider) && !"".equals(shownAt)) ? " - " : "";
			retval += shownAt;
			retval += (!"".equals(dataProvider) || !"".equals(shownAt)) ? ". " : "";
			retval += !"".equals(rights) ? getRightsLabel(rights) + " - " + rights + "." : "";
			return retval;
		}
	}

	//	removes empty Strings from String arrays
	private String[] stripEmptyStrings(String[] swissCheese){
		List<String> solidCheese = new ArrayList<String>();
		for (String cheeseOrHole : swissCheese){
			if (!"".equals(cheeseOrHole)){
				solidCheese.add(cheeseOrHole);
			}
		}
		return solidCheese.toArray(new String[ solidCheese.size() ]);
	}

	//	removes "" and null elements from String Lists
	private List stripEmptyStrings(List swissCheese){
		swissCheese.removeAll(Arrays.asList("", null));
		return swissCheese;
	}

	private String cleanUp(String input){
		if (input.endsWith(".")){
			return input.substring(0, input.length() - 1).trim();
		} else {
			return input.trim();
		}
	}

	private String getRightsLabel(String rightsURL) {
		String rightsLabel = "could not determine rights label";
		String rightsPattern = "zero|mark|/by/|/by-sa/|/by-nd/|/by-nc/|/by-nc-sa/|/by-nc-nd/|orphan|rr-p|rr-f|out-of-copyright|unknown";
		final Matcher m = Pattern.compile(rightsPattern).matcher(rightsURL);
		if (m.find())
			switch (m.group()) {
				case "zero": rightsLabel = "Public Domain"; break;
				case "mark": rightsLabel = "Public Domain"; break;
				case "/by/": rightsLabel = "CC BY"; break;
				case "/by-sa/": rightsLabel = "CC BY-SA"; break;
				case "/by-nd/": rightsLabel = "CC BY-ND"; break;
				case "/by-nc/": rightsLabel = "CC BY-NC"; break;
				case "/by-nc-sa/": rightsLabel = "CC BY-NC-SA"; break;
				case "/by-nc-nd/": rightsLabel = "CC BY-NC-ND"; break;
				case "orphan": rightsLabel = "Orphan Work"; break;
				case "rr-p": rightsLabel = "Rights Reserved - Paid Access"; break;
				case "rr-f": rightsLabel = "Rights Reserved - Free Access"; break;
				case "out-of-copyright": rightsLabel = "Out of copyright - non commercial re-use"; break;
				case "unknown": rightsLabel = "Unknown"; break;
			}
		return rightsLabel;
	}

	private String spannify(String spanType, String spanning){
		return "<span " + spanType + "=\"" + spanning + "\">";
	}
}
