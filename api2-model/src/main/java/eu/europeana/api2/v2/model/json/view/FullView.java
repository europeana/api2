/*
 * Copyright 2007-2015 The Europeana Foundation
 *
 * Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 * by the European Commission;
 * You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 * http://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 * any kind, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under
 * the Licence.
 */

package eu.europeana.api2.v2.model.json.view;

import com.fasterxml.jackson.annotation.JsonInclude;
import eu.europeana.corelib.definitions.edm.beans.BriefBean;
import eu.europeana.corelib.definitions.edm.beans.FullBean;
import eu.europeana.corelib.definitions.edm.entity.*;
import eu.europeana.corelib.definitions.solr.DocType;
import eu.europeana.corelib.utils.DateUtils;
import eu.europeana.corelib.web.service.EuropeanaUrlService;
import eu.europeana.corelib.web.service.impl.EuropeanaUrlServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Date;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

@JsonInclude(NON_EMPTY)
public class FullView implements FullBean {

	private FullBean bean;
	private String profile;
	private boolean optOut;
	private String apiKey;
	private EuropeanaUrlService europeanaUrlService;
	private Date timestampCreated;
	private Date timestampUpdated;
	private boolean urlified = false;

	public FullView(FullBean bean, String profile, String apiKey, boolean optOut) {
		this.bean = bean;
		this.optOut = optOut;
		this.profile = profile;
		this.apiKey = apiKey;
		europeanaUrlService = EuropeanaUrlServiceImpl.getBeanInstance();
		extractTimestampCreated();
		extractTimestampUpdated();
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
				String isShownAtLink = europeanaUrlService.getApi2Redirect(apiKey,
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
}
