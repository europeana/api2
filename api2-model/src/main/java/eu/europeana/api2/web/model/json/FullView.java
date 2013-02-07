package eu.europeana.api2.web.model.json;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

import eu.europeana.corelib.definitions.solr.DocType;
import eu.europeana.corelib.definitions.solr.beans.BriefBean;
import eu.europeana.corelib.definitions.solr.beans.FullBean;
import eu.europeana.corelib.definitions.solr.entity.Agent;
import eu.europeana.corelib.definitions.solr.entity.Aggregation;
import eu.europeana.corelib.definitions.solr.entity.Concept;
import eu.europeana.corelib.definitions.solr.entity.EuropeanaAggregation;
import eu.europeana.corelib.definitions.solr.entity.Place;
import eu.europeana.corelib.definitions.solr.entity.ProvidedCHO;
import eu.europeana.corelib.definitions.solr.entity.Proxy;
import eu.europeana.corelib.definitions.solr.entity.Timespan;
import eu.europeana.corelib.utils.OptOutDatasetsUtil;

@JsonSerialize(include = Inclusion.NON_EMPTY)
public class FullView implements FullBean {

	protected static String apiUrl;
	protected static String portalUrl;

	FullBean bean;
	String profile;
	long uid;

	public FullView(FullBean bean) {
		this.bean = bean;
	}

	public FullView(FullBean bean, String profile) {
		this(bean);
		this.profile = profile;
	}

	public FullView(FullBean bean, String profile, long uid) {
		this(bean, profile);
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
		List<Place> items =(List<Place>) bean.getPlaces();
		for (int i=0, la=items.size(); i < la; i++) {
			items.get(i).setId(null);
		}
		return items;
	}

	@Override
	public void setPlaces(List<? extends Place> places) {}

	@Override
	public List<? extends Agent> getAgents() {
		@SuppressWarnings("unchecked")
		List<Agent> items =(List<Agent>) bean.getAgents();
		for (int i=0, la=items.size(); i < la; i++) {
			items.get(i).setId(null);
		}
		return items;
	}

	@Override
	public List<? extends Timespan> getTimespans() {
		@SuppressWarnings("unchecked")
		List<Timespan> items =(List<Timespan>) bean.getTimespans();
		for (int i=0, la=items.size(); i < la; i++) {
			items.get(i).setId(null);
		}
		return items;
	}

	@Override
	public List<? extends Concept> getConcepts() {
		@SuppressWarnings("unchecked")
		List<Concept> items =(List<Concept>) bean.getConcepts();
		for (int i=0, la=items.size(); i < la; i++) {
			items.get(i).setId(null);
		}
		return items;
	}

	@Override
	public void setConcepts(List<? extends Concept> concepts) {}

	@Override
	public void setAggregations(List<? extends Aggregation> aggregations) {}

	@Override
	public List<? extends Proxy> getProxies() {
		@SuppressWarnings("unchecked")
		List<Proxy> items =(List<Proxy>) bean.getProxies();
		for (int i=0, la=items.size(); i < la; i++) {
			items.get(i).setId(null);
		}
		return items;
	}

	@Override
	public void setProxies(List<? extends Proxy> proxies) {}

	@Override
	public void setTimespans(List<? extends Timespan> timespans) {}

	@Override
	public List<? extends Aggregation> getAggregations() {
		@SuppressWarnings("unchecked")
		List<Aggregation> items = (List<Aggregation>) bean.getAggregations();
		for (int i=0, la=items.size(); i < la; i++) {
			items.get(i).setId(null);

			// add bt=europanaapi
			String isShownAt = items.get(i).getEdmIsShownAt();
			if (isShownAt != null) {
				isShownAt = isShownAt + (isShownAt.indexOf("?") > -1 ? "&" : "?") + "bt=europeanaapi";
				// items.get(i).setEdmIsShownAt(isShownAt);

				String provider = items.get(i).getEdmProvider().values().iterator().next().get(0);
				String isShownAtLink = String.format(
						"%s/%d/redirect.json?shownAt=%s&provider=%s&id=http://www.europeana.eu/resolve/record%s&profile=%s", 
						apiUrl,
						uid,
						encode(isShownAt),
						encode(provider),
						bean.getAbout(),
						profile);
				items.get(i).setEdmIsShownAt(isShownAtLink);
			}

			// remove edm:object if it is a opted out record
			if (OptOutDatasetsUtil.checkById(bean.getAbout())) {
				items.get(i).setEdmObject(null);
			}

			// remove webresources IDs
			for (int j = 0, lw = items.get(i).getWebResources().size(); j < lw; j++) {
				items.get(i).getWebResources().get(j).setId(null);
			}
		}
		return items;
	}

	@Override
	public List<? extends BriefBean> getSimilarItems() {
		return null;
	}

	@Override
	public void setSimilarItems(List<? extends BriefBean> similarItems) {}

	@Override
	public List<? extends ProvidedCHO> getProvidedCHOs() {
		@SuppressWarnings("unchecked")
		List<ProvidedCHO> items =(List<ProvidedCHO>) bean.getProvidedCHOs();
		for (int i=0, la=items.size(); i < la; i++) {
			items.get(i).setId(null);
		}
		return items;
	}

	@Override
	public void setProvidedCHOs(List<? extends ProvidedCHO> providedCHOs) {}

	@Override
	public String getAbout() {
		return bean.getAbout();
	}

	@Override
	public EuropeanaAggregation getEuropeanaAggregation() {
		EuropeanaAggregation europeanaAggregation = bean.getEuropeanaAggregation();
		europeanaAggregation.setId(null);
		return europeanaAggregation;
	}

	@Override
	public void setEuropeanaAggregation(EuropeanaAggregation europeanaAggregation) {}

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
	public void setEuropeanaId(ObjectId europeanaId) {}

	@Override
	public void setTitle(String[] title) {}

	@Override
	public void setYear(String[] year) {}

	@Override
	public void setProvider(String[] provider) {}

	@Override
	public void setLanguage(String[] language) {}

	@Override
	public void setType(DocType type) {}

	@Override
	public void setEuropeanaCompleteness(int europeanaCompleteness) {}

	@Override
	public void setAbout(String about) {}

	@Override
	public void setAgents(List<? extends Agent> agents) {}

	@Override
	public void setCountry(String[] country) {}

	@Override
	public void setEuropeanaCollectionName(String[] europeanaCollectionName) {}

	@Override
	public void setOptOut(boolean optOut) {}

	public static void setApiUrl(String _apiUrl) {
		apiUrl = _apiUrl;
	}

	public static void setPortalUrl(String _portalUrl) {
		portalUrl = _portalUrl;
	}
	
	private String encode(String value) {
		if (StringUtils.isBlank(value)) {
			return "";
		}
		try {
			value = URLEncoder.encode(value, "utf-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return value;
	}
}
