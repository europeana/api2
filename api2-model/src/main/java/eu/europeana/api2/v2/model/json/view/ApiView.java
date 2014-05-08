package eu.europeana.api2.v2.model.json.view;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

import eu.europeana.api2.model.enums.Profile;
import eu.europeana.corelib.definitions.solr.beans.ApiBean;
import eu.europeana.corelib.definitions.solr.beans.BriefBean;
import eu.europeana.corelib.utils.DateUtils;

@JsonSerialize(include = Inclusion.NON_EMPTY)
public class ApiView extends BriefView implements ApiBean {

	private String[] edmConceptTerm;
	// private Map<String, String> edmConceptPrefLabel;
	private List<Map<String, String>> edmConceptPrefLabel;
	// private String[] edmConceptPrefLabel;
	private String[] edmConceptBroaderTerm;
	private List<Map<String, String>> edmConceptBroaderLabel;
	private String[] edmTimespanBroaderTerm;
	private List<Map<String, String>> edmTimespanBroaderLabel;
	private String[] recordHashFirstSix;
	private boolean[] ugc;
	private int completeness;
	private String[] country;
	private String debugQuery;
	private String[] europeanaCollectionName;
	private int index;
	private String[] edmPlaceBroaderTerm;
	private List<Map<String, String>> edmPlaceAltLabel;
	private String[] dctermsIsPartOf;
	private Date timestampCreated;
	private Date timestampUpdate;

	public ApiView(ApiBean bean, String profile, String wskey, long uid, boolean optOut) {
		super((BriefBean) bean, profile, wskey, uid, optOut);

		edmConceptTerm = bean.getEdmConcept();
		if (bean.getEdmConceptLabel() != null) {
			edmConceptPrefLabel = bean.getEdmConceptLabel();
		}
		edmConceptBroaderTerm = bean.getEdmConceptBroaderTerm();
		edmConceptBroaderLabel = bean.getEdmConceptBroaderLabel();
		edmTimespanBroaderTerm = bean.getEdmTimespanBroaderTerm();
		edmTimespanBroaderLabel = bean.getEdmTimespanBroaderLabel();
		// recordHashFirstSix = bean.get;
		ugc = bean.getUgc();
		completeness = bean.getEuropeanaCompleteness();
		country = bean.getCountry();
		// debugQuery = bean.get;
		europeanaCollectionName = bean.getEuropeanaCollectionName();
		// index = bean.get;
		edmPlaceBroaderTerm = bean.getEdmPlaceBroaderTerm();
		edmPlaceAltLabel = bean.getEdmPlaceAltLabel();
		dctermsIsPartOf = bean.getDctermsIsPartOf();
		timestampCreated = bean.getTimestampCreated();
		timestampUpdate = bean.getTimestampUpdate();
	}

	@Override
	public String[] getEdmConcept() {
		return edmConceptTerm;
	}

	public void setEdmConceptTerm(String[] edmConceptTerm) {
		this.edmConceptTerm = edmConceptTerm;
	}

	@Override
	public List<Map<String, String>> getEdmConceptLabel() {
		return edmConceptPrefLabel;
	}

	public void setEdmConceptPrefLabel(
			List<Map<String, String>> edmConceptPrefLabel) {
		this.edmConceptPrefLabel = edmConceptPrefLabel;
	}

	@Override
	public String[] getEdmConceptBroaderTerm() {
		if (isProfile(Profile.MINIMAL) || isProfile(Profile.STANDARD)
				|| isProfile(Profile.RICH)) {
			return null;
		}
		return edmConceptBroaderTerm;
	}

	public void setEdmConceptBroaderTerm(String[] edmConceptBroaderTerm) {
		this.edmConceptBroaderTerm = edmConceptBroaderTerm;
	}

	@Override
	public List<Map<String, String>> getEdmConceptBroaderLabel() {
		if (isProfile(Profile.MINIMAL) || isProfile(Profile.STANDARD)
				|| isProfile(Profile.RICH)) {
			return null;
		}
		return edmConceptBroaderLabel;
	}

	public void setEdmConceptBroaderLabel(
			List<Map<String, String>> edmConceptBroaderLabel) {
		this.edmConceptBroaderLabel = edmConceptBroaderLabel;
	}

	@Override
	public String[] getEdmTimespanBroaderTerm() {
		return edmTimespanBroaderTerm;
	}

	public void setEdmTimespanBroaderTerm(String[] edmTimespanBroaderTerm) {
		this.edmTimespanBroaderTerm = edmTimespanBroaderTerm;
	}

	@Override
	public List<Map<String, String>> getEdmTimespanBroaderLabel() {
		return edmTimespanBroaderLabel;
	}

	public void setEdmTimespanBroaderLabel(
			List<Map<String, String>> edmTimespanBroaderLabel) {
		this.edmTimespanBroaderLabel = edmTimespanBroaderLabel;
	}

	public String[] getRecordHashFirstSix() {
		return recordHashFirstSix;
	}

	public void setRecordHashFirstSix(String[] recordHashFirstSix) {
		this.recordHashFirstSix = recordHashFirstSix;
	}

	@Override
	public boolean[] getUgc() {
		return ugc;
	}

	@Override
	public void setUgc(boolean[] ugc) {
		this.ugc = ugc;
	}

	public int getCompleteness() {
		return completeness;
	}

	public void setCompleteness(int completeness) {
		this.completeness = completeness;
	}

	@Override
	public String[] getCountry() {
		if (isProfile(Profile.MINIMAL) || isProfile(Profile.STANDARD)
				|| isProfile(Profile.RICH)) {
			return null;
		}
		return country;
	}

	@Override
	public void setCountry(String[] country) {
		this.country = country;
	}

	public String getDebugQuery() {
		return debugQuery;
	}

	public void setDebugQuery(String debugQuery) {
		this.debugQuery = debugQuery;
	}

	@Override
	public String[] getEuropeanaCollectionName() {
		return europeanaCollectionName;
	}

	public String[] getEdmDatasetName() {
		return getEuropeanaCollectionName();
	}

	@Override
	public void setEuropeanaCollectionName(String[] europeanaCollectionName) {
		this.europeanaCollectionName = europeanaCollectionName;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	@Override
	public String[] getEdmPlaceBroaderTerm() {
		return edmPlaceBroaderTerm;
	}

	public void setEdmPlaceBroaderTerm(String[] edmPlaceBroaderTerm) {
		this.edmPlaceBroaderTerm = edmPlaceBroaderTerm;
	}

	@Override
	public List<Map<String, String>> getEdmPlaceAltLabel() {
		if (isProfile(Profile.MINIMAL) || isProfile(Profile.STANDARD)
				|| isProfile(Profile.RICH)) {
			return null;
		}
		return edmPlaceAltLabel;
	}

	public void setEdmPlaceAltLabel(List<Map<String, String>> edmPlaceAltLabel) {
		this.edmPlaceAltLabel = edmPlaceAltLabel;
	}

	@Override
	public String[] getDctermsIsPartOf() {
		return dctermsIsPartOf;
	}

	@Override
	public void setDctermsIsPartOf(String[] dctermsIsPartOf) {
		this.dctermsIsPartOf = dctermsIsPartOf;
	}

	@JsonProperty("timestamp_created")
	public String getTimestampCreatedString() {
		if (timestampCreated != null) {
			return DateUtils.format(timestampCreated);
		}
		return null;
	}

	@Override
	@JsonProperty("timestamp_created_epoch")
	public Date getTimestampCreated() {
		return timestampCreated;
	}

	@JsonProperty("timestamp_update")
	public String getTimestampUpdateString() {
		if (timestampUpdate != null) {
			return DateUtils.format(timestampUpdate);
		}
		return null;
	}

	@Override
	@JsonProperty("timestamp_update_epoch")
	public Date getTimestampUpdate() {
		return timestampUpdate;
	}
}
