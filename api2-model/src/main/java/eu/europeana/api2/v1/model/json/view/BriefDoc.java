/*
 * Copyright 2007-2012 The Europeana Foundation
 *
 *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved 
 *  by the European Commission;
 *  You may not use this work except in compliance with the Licence.
 *  
 *  You may obtain a copy of the Licence at:
 *  http://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under 
 *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of 
 *  any kind, either express or implied.
 *  See the Licence for the specific language governing permissions and limitations under 
 *  the Licence.
 */

package eu.europeana.api2.v1.model.json.view;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

import eu.europeana.corelib.definitions.solr.beans.ApiBean;
import eu.europeana.corelib.web.service.EuropeanaUrlService;

@JsonSerialize(include = Inclusion.NON_EMPTY)
public class BriefDoc {

	public final static int JSON = 0;
	public final static int SRW = 1;

	private String wskey;
	private boolean isOptOut;
	private EuropeanaUrlService urlService;

	private String fullDocUrl;
	private String title;
	private String url;
	private String thumbnail; //
	private String creator; //
	private String[] dcTermsHasPart;
	private String[] dcTermsIsPartOf;
	private String year;
	private String language;
	private String type;
	private String provider;
	private String dataProvider; //
	private String dataProvider2; //
	private String dataProvider3; //
	private String[] europeanaRights;
	private String positionAvailable;
	private List<String> enrichmentPlaceLatitude;
	private List<String> enrichmentPlaceLongitude;
	private String[] enrichmentPlaceTerm;
	private String[] enrichmentPlaceLabel;
	private String[] enrichmentPeriodTerm;
	private String[] enrichmentPeriodLabel;
	private String enrichmentPeriodBegin; // (datetime)
	private String enrichmentPeriodEnd; // (datetime)
	private String[] enrichmentAgentTerm;
	private String[] enrichmentAgentLabel;
	private String[] enrichmentConceptTerm;
	private String[] enrichmentConceptLabel;

	private Map<String, Object> map;

	public BriefDoc(ApiBean bean, boolean optOut, EuropeanaUrlService urlService) {
		this.isOptOut = optOut;
		this.urlService = urlService;
		if (bean.getTitle() != null && bean.getTitle().length > 0)
			title = bean.getTitle()[0];
		url = bean.getId();
		// description???
		if (bean.getEdmObject() != null && bean.getEdmObject().length > 0)
			thumbnail = bean.getEdmObject()[0];
		if (bean.getDcCreator() != null && bean.getDcCreator().length > 0)
			creator = bean.getDcCreator()[0];
		if (bean.getDctermsHasPart() != null)
			dcTermsHasPart = bean.getDctermsHasPart();
		if (bean.getDctermsIsPartOf() != null)
			dcTermsIsPartOf = bean.getDctermsIsPartOf();
		if (bean.getYear() != null && bean.getYear().length > 0)
			year = bean.getYear()[0];
		if (bean.getLanguage() != null && bean.getLanguage().length > 0)
			language = bean.getLanguage()[0];
		type = bean.getType().toString();
		if (bean.getProvider() != null && bean.getProvider().length > 0)
			provider = bean.getProvider()[0];
		if (bean.getDataProvider() != null && bean.getDataProvider().length > 0)
			dataProvider = bean.getDataProvider()[0];
		if (bean.getEuropeanaCollectionName() != null
				&& bean.getEuropeanaCollectionName().length > 0)
			dataProvider2 = bean.getEuropeanaCollectionName()[0];
		if (bean.getEuropeanaCollectionName() != null
				&& bean.getEuropeanaCollectionName().length > 0)
			dataProvider2 = bean.getEuropeanaCollectionName()[0];
		if (bean.getRights() != null && bean.getRights().length > 0)
			europeanaRights = bean.getRights();
		if (bean.getEdmPlaceLatitude() != null)
			enrichmentPlaceLatitude = bean.getEdmPlaceLatitude();
		if (bean.getEdmPlaceLongitude() != null)
			enrichmentPlaceLongitude = bean.getEdmPlaceLongitude();
		if (bean.getEdmPlace() != null)
			enrichmentPlaceTerm = bean.getEdmPlace();
		if (bean.getEdmPlaceLabel() != null) {
			List<String> terms = new ArrayList<String>();
			for (Map<String, String> entry : bean.getEdmPlaceLabel()) {
				terms.addAll(entry.values());
			}
			enrichmentPlaceLabel = terms.toArray(new String[terms.size()]);
		}
		if (bean.getEdmTimespan() != null)
			enrichmentPeriodTerm = bean.getEdmTimespan();
		if (bean.getEdmTimespanLabel() != null) {
			List<String> terms = new ArrayList<String>();
			for (Map<String, String> entry : bean.getEdmTimespanLabel()) {
				terms.addAll(entry.values());
			}
			enrichmentPeriodLabel = terms.toArray(new String[terms.size()]);
		}
		if (bean.getEdmTimespanBegin() != null
				&& bean.getEdmTimespanBegin().length > 0)
			enrichmentPeriodBegin = bean.getEdmTimespanBegin()[0];
		if (bean.getEdmTimespanEnd() != null
				&& bean.getEdmTimespanEnd().length > 0)
			enrichmentPeriodEnd = bean.getEdmTimespanEnd()[0];
		if (bean.getEdmAgent() != null)
			enrichmentAgentTerm = bean.getEdmAgent();
		if (bean.getEdmAgentLabel() != null) {
			enrichmentAgentLabel = bean.getEdmAgentLabel().toArray(
					new String[bean.getEdmAgentLabel().size()]);
		}
		if (bean.getEdmConcept() != null)
			enrichmentPeriodTerm = bean.getEdmConcept();
		if (bean.getEdmConceptLabel() != null) {
			List<String> terms = new ArrayList<String>();
			for (Map<String, String> map : bean.getEdmConceptLabel()) {
				for (Entry<String, String> entry : map.entrySet()) {
					terms.add(entry.getValue());
				}
			}
			enrichmentConceptLabel = terms.toArray(new String[terms.size()]);
		}
	}

	public Map<String, Object> asMap() {
		if (map == null) {
			map = new LinkedHashMap<String, Object>();

			addValue("guid", getGuid());
			addValue("title", title);
			addValue("link", getLink(JSON));
			addValue("description", getDescription());
			if (!isOptOut) {
				addValue("enclosure", thumbnail);
			}
			addValue("dc:creator", creator);
			addValue("dcterms:hasPart", dcTermsHasPart);
			addValue("dcterms:isPartOf", dcTermsIsPartOf);
			addValue("europeana:year", year);
			addValue("europeana:language", language);
			addValue("europeana:type", type);
			addValue("europeana:provider", provider);
			addValue("europeana:dataProvider", dataProvider);
			addValue("europeana:collectionName", dataProvider2);
			addValue("edm:provider", dataProvider3);
			addValue("europeana:rights", europeanaRights);
			addValue("enrichment:place_latitude", enrichmentPlaceLatitude);
			addValue("enrichment:place_longitude", enrichmentPlaceLongitude);
			addValue("enrichment:place_term", enrichmentPlaceTerm);
			addValue("enrichment:place_label", enrichmentPlaceLabel);
			addValue("enrichment:period_term", enrichmentPeriodTerm);
			addValue("enrichment:period_label", enrichmentPeriodLabel);
			addValue("enrichment:period_begin", enrichmentPeriodBegin); // (datetime)
			addValue("enrichment:period_end", enrichmentPeriodEnd); // (datetime)
			addValue("enrichment:agent_term", enrichmentAgentTerm);
			addValue("enrichment:agent_label", enrichmentAgentLabel);
			addValue("enrichment:concept_term", enrichmentConceptTerm);
			addValue("enrichment:concept_label", enrichmentConceptLabel);
		}

		return map;
	}

	private void addValue(String key, Object value) {
		if (value != null) {
			map.put(key, value);
		}
	}

	public String getFullDocUrl() {
		return fullDocUrl;
	}

	public void setFullDocUrl(String fullDocUrl) {
		this.fullDocUrl = fullDocUrl;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getThumbnail() {
		return thumbnail;
	}

	public void setThumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public String[] getDcTermsHasPart() {
		return dcTermsHasPart;
	}

	public void setDcTermsHasPart(String[] dcTermsHasPart) {
		this.dcTermsHasPart = dcTermsHasPart;
	}

	public String[] getDcTermsIsPartOf() {
		return dcTermsIsPartOf;
	}

	public void setDcTermsIsPartOf(String[] dcTermsIsPartOf) {
		this.dcTermsIsPartOf = dcTermsIsPartOf;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public String getDataProvider() {
		return dataProvider;
	}

	public void setDataProvider(String dataProvider) {
		this.dataProvider = dataProvider;
	}

	public String[] getEuropeanaRights() {
		return europeanaRights;
	}

	public void setEuropeanaRights(String[] europeanaRights) {
		this.europeanaRights = europeanaRights;
	}

	public String getPositionAvailable() {
		return positionAvailable;
	}

	public void setPositionAvailable(String positionAvailable) {
		this.positionAvailable = positionAvailable;
	}

	public float getEnrichmentPlaceLatitude() {
		if (enrichmentPlaceLatitude != null
				&& enrichmentPlaceLatitude.size() > 0
				&& !StringUtils.isBlank(enrichmentPlaceLatitude.get(0))) {
			return Float.valueOf(enrichmentPlaceLatitude.get(0));
		}
		return 0;
	}

	public void setEnrichmentPlaceLatitude(List<String> enrichmentPlaceLatitude) {
		this.enrichmentPlaceLatitude = enrichmentPlaceLatitude;
	}

	public float getEnrichmentPlaceLongitude() {
		if (enrichmentPlaceLongitude != null
				&& enrichmentPlaceLongitude.size() > 0
				&& !StringUtils.isBlank(enrichmentPlaceLongitude.get(0))) {
			return Float.valueOf(enrichmentPlaceLongitude.get(0));
		}
		return 0;
	}

	public void setEnrichmentPlaceLongitude(
			List<String> enrichmentPlaceLongitude) {
		this.enrichmentPlaceLongitude = enrichmentPlaceLongitude;
	}

	public String[] getEnrichmentPlaceTerm() {
		return enrichmentPlaceTerm;
	}

	public void setEnrichmentPlaceTerm(String[] enrichmentPlaceTerm) {
		this.enrichmentPlaceTerm = enrichmentPlaceTerm;
	}

	public String[] getEnrichmentPlaceLabel() {
		return enrichmentPlaceLabel;
	}

	public void setEnrichmentPlaceLabel(String[] enrichmentPlaceLabel) {
		this.enrichmentPlaceLabel = enrichmentPlaceLabel;
	}

	public String[] getEnrichmentPeriodTerm() {
		return enrichmentPeriodTerm;
	}

	public void setEnrichmentPeriodTerm(String[] enrichmentPeriodTerm) {
		this.enrichmentPeriodTerm = enrichmentPeriodTerm;
	}

	public String[] getEnrichmentPeriodLabel() {
		return enrichmentPeriodLabel;
	}

	public void setEnrichmentPeriodLabel(String[] enrichmentPeriodLabel) {
		this.enrichmentPeriodLabel = enrichmentPeriodLabel;
	}

	public String getEnrichmentPeriodBegin() {
		return enrichmentPeriodBegin;
	}

	public void setEnrichmentPeriodBegin(String enrichmentPeriodBegin) {
		this.enrichmentPeriodBegin = enrichmentPeriodBegin;
	}

	public String getEnrichmentPeriodEnd() {
		return enrichmentPeriodEnd;
	}

	public void setEnrichmentPeriodEnd(String enrichmentPeriodEnd) {
		this.enrichmentPeriodEnd = enrichmentPeriodEnd;
	}

	public String[] getEnrichmentAgentTerm() {
		return enrichmentAgentTerm;
	}

	public void setEnrichmentAgentTerm(String[] enrichmentAgentTerm) {
		this.enrichmentAgentTerm = enrichmentAgentTerm;
	}

	public String[] getEnrichmentAgentLabel() {
		return enrichmentAgentLabel;
	}

	public void setEnrichmentAgentLabel(String[] enrichmentAgentLabel) {
		this.enrichmentAgentLabel = enrichmentAgentLabel;
	}

	public String[] getEnrichmentConceptTerm() {
		return enrichmentConceptTerm;
	}

	public void setEnrichmentConceptTerm(String[] enrichmentConceptTerm) {
		this.enrichmentConceptTerm = enrichmentConceptTerm;
	}

	public String[] getEnrichmentConceptLabel() {
		return enrichmentConceptLabel;
	}

	public void setEnrichmentConceptLabel(String[] enrichmentConceptLabel) {
		this.enrichmentConceptLabel = enrichmentConceptLabel;
	}

	public String getWskey() {
		return wskey;
	}

	public void setWskey(String wskey) {
		this.wskey = wskey;
	}

	public String getGuid() {
		return urlService.getPortalRecord(false, url).toString();
	}

	public String getLink(int target) {
		String extension;
		switch (target) {
		case SRW:
			extension = EuropeanaUrlService.EXT_SRW;
			break;
		case JSON:
		default:
			extension = EuropeanaUrlService.EXT_JSON;
			break;
		}
		return urlService.getApi1Record(wskey, url, extension).toString();
	}

	public String getDescription() {
		StringBuilder sb = new StringBuilder();
		if (StringUtils.isNotBlank(getCreator())) {
			sb.append(trimDescription(getCreator()));
		}
		if (StringUtils.isNotBlank(getYear())) {
			if (sb.length() > 0) {
				sb.append("; ");
			}
			sb.append(trimDescription(getYear()));
		}
		if (StringUtils.isNotBlank(getProvider())) {
			if (sb.length() > 0) {
				sb.append("; ");
			}
			sb.append(getProvider());
		}
		return sb.toString();
	}

	private String trimDescription(String item) {
		item = StringUtils.trim(item);
		if (item.endsWith(";")) {
			item = StringUtils.trim(item.substring(0, item.length() - 1));
		}
		return item;
	}
}
