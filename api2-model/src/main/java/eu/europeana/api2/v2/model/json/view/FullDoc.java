package eu.europeana.api2.v2.model.json.view;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;

import eu.europeana.api2.model.utils.Api2UrlService;
import org.apache.commons.lang3.ArrayUtils;

import eu.europeana.api2.model.xml.srw.SrwResponse;
import eu.europeana.corelib.definitions.edm.beans.FullBean;
import eu.europeana.corelib.definitions.edm.entity.Agent;
import eu.europeana.corelib.definitions.edm.entity.Aggregation;
import eu.europeana.corelib.definitions.edm.entity.Concept;
import eu.europeana.corelib.definitions.edm.entity.EuropeanaAggregation;
import eu.europeana.corelib.definitions.edm.entity.Place;
import eu.europeana.corelib.definitions.edm.entity.Proxy;
import eu.europeana.corelib.definitions.edm.entity.Timespan;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * This class is only used in old SRW responses
 * @deprecated officially not supported any more
 */
@Deprecated
@JsonPropertyOrder(alphabetic=true)
public class FullDoc {

	private Map<String, Object> map;
//	boolean isOptedOut = false;

	private String id;
	private String[] europeanaCollectionName;
	private String type;
	private String[] europeanaCountry;
	private String[] europeanaProvider;
	private String[] europeanaIsShownAt;
	private String[] europeanaIsShownBy;
	private String[] thumbnails;
	private String[] europeanaLanguage;
	private String[] europeanaUserTag;
	private String[] europeanaYear;
	private String[] europeanaRights;
	private String[] europeanaDataProvider;
	private String[] europeanaUGC;
	private String[] dcTermsAlternative;
	private String[] dcTermsConformsTo;
	private String[] dcTermsCreated;
	private String[] dcTermsExtent;
	private String[] dcTermsHasFormat;
	private String[] dcTermsHasPart;
	private String[] dcTermsHasVersion;
	private String[] dcTermsIsFormatOf;
	private String[] dcTermsIsPartOf;
	private String[] dcTermsIsReferencedBy;
	private String[] dcTermsIsReplacedBy;
	private String[] dcTermsIsRequiredBy;
	private String[] dcTermsIssued;
	private String[] dcTermsIsVersionOf;
	private String[] dcTermsMedium;
	private String[] dcTermsProvenance;
	private String[] dcTermsReferences;
	private String[] dcTermsReplaces;
	private String[] dcTermsRequires;
	private String[] dcTermsSpatial;
	private String[] dcTermsTableOfContents;
	private String[] dcTermsTemporal;

	private String[] dcContributor;
	private String[] dcCoverage;
	private String[] dcCreator;
	private String[] dcDate;
	private String[] dcDescription;
	private String[] dcFormat;
	private String[] dcIdentifier;
	private String[] dcLanguage;
	private String[] dcPublisher;
	private String[] dcRelation;
	private String[] dcRights;
	private String[] dcSource;
	private String[] dcSubject;
	private String[] dcTitle;
	private String[] dcType;

	private int europeanaCompleteness;
	private String[] enrichmentPlaceTerm;
	private String[] enrichmentPlaceLabel;
	private float enrichmentPlaceLatitude;
	private float enrichmentPlaceLongitude;
	private String[] enrichmentPlaceBroaderTerm;
	private String[] enrichmentPlaceBroaderLabel;

	private String[] enrichmentPeriodTerm;
	private String[] enrichmentPeriodLabel;
	private Date enrichmentPeriodBegin;
	private Date enrichmentPeriodEnd;
	// private String enrichmentPeriodBegin;
	// private String enrichmentPeriodEnd;
	private String[] enrichmentPeriodBroaderTerm;
	private String[] enrichmentPeriodBroaderLabel;

	private String[] enrichmentConceptTerm;
	private String[] enrichmentConceptLabel;
	private String[] enrichmentConceptBroaderTerm;
	private String[] enrichmentConceptBroaderLabel;

	private String[] enrichmentAgentTerm;
	private String[] enrichmentAgentLabel;

	public FullDoc(FullBean bean) {
		id = Api2UrlService.getBeanInstance().getRecordResolveUrl(bean.getAbout());
		europeanaCollectionName = bean.getEuropeanaCollectionName();
		if (bean.getType() != null)
			type = bean.getType().toString();

		EuropeanaAggregation euAggregation = bean.getEuropeanaAggregation();
		if (euAggregation != null) {
			try {
				europeanaCountry = map2Array(euAggregation.getEdmCountry());
			} catch (Exception e) {
			}
			europeanaLanguage = map2Array(euAggregation.getEdmLanguage());
		}

		for (Aggregation aggregation : bean.getAggregations()) {
			europeanaProvider = add(europeanaProvider,
					map2Array(aggregation.getEdmProvider()));
			europeanaIsShownAt = add(europeanaIsShownAt,
					aggregation.getEdmIsShownAt());
			europeanaIsShownBy = add(europeanaIsShownBy,
					aggregation.getEdmIsShownBy());
			thumbnails = add(thumbnails, aggregation.getEdmObject());
			// addValue(fields, EUROPEANA_USERTAG, bean.getEuropeanaUserTag());
			europeanaRights = add(europeanaRights,
					map2Array(aggregation.getEdmRights()));
			europeanaDataProvider = add(europeanaDataProvider,
					map2Array(aggregation.getEdmDataProvider()));
			europeanaUGC = add(europeanaUGC, aggregation.getEdmUgc());
		}

		for (Proxy proxy : bean.getProxies()) {
			dcTermsAlternative = add(dcTermsAlternative,
					map2Array(proxy.getDctermsAlternative()));
			dcTermsConformsTo = add(dcTermsConformsTo,
					map2Array(proxy.getDctermsConformsTo()));
			dcTermsCreated = add(dcTermsCreated,
					map2Array(proxy.getDctermsCreated()));
			dcTermsExtent = add(dcTermsExtent,
					map2Array(proxy.getDctermsExtent()));
			dcTermsHasFormat = add(dcTermsHasFormat,
					map2Array(proxy.getDctermsHasFormat()));
			dcTermsHasPart = add(dcTermsHasPart,
					map2Array(proxy.getDctermsHasPart()));
			dcTermsHasVersion = add(dcTermsHasVersion,
					map2Array(proxy.getDctermsHasVersion()));
			dcTermsIsFormatOf = add(dcTermsIsFormatOf,
					map2Array(proxy.getDctermsIsFormatOf()));
			dcTermsIsPartOf = add(dcTermsIsPartOf,
					map2Array(proxy.getDctermsIsPartOf()));
			dcTermsIsReferencedBy = add(dcTermsIsReferencedBy,
					map2Array(proxy.getDctermsIsReferencedBy()));
			dcTermsIsReplacedBy = add(dcTermsIsReplacedBy,
					map2Array(proxy.getDctermsIsReplacedBy()));
			dcTermsIsRequiredBy = add(dcTermsIsRequiredBy,
					map2Array(proxy.getDctermsIsRequiredBy()));
			dcTermsIssued = add(dcTermsIssued,
					map2Array(proxy.getDctermsIssued()));
			dcTermsIsVersionOf = add(dcTermsIsVersionOf,
					map2Array(proxy.getDctermsIsVersionOf()));
			dcTermsMedium = add(dcTermsMedium,
					map2Array(proxy.getDctermsMedium()));
			dcTermsProvenance = add(dcTermsProvenance,
					map2Array(proxy.getDctermsProvenance()));
			dcTermsReferences = add(dcTermsReferences,
					map2Array(proxy.getDctermsReferences()));
			dcTermsReplaces = add(dcTermsReplaces,
					map2Array(proxy.getDctermsReplaces()));
			dcTermsRequires = add(dcTermsRequires,
					map2Array(proxy.getDctermsRequires()));
			dcTermsSpatial = add(dcTermsSpatial,
					map2Array(proxy.getDctermsSpatial()));
			dcTermsTableOfContents = add(dcTermsTableOfContents,
					map2Array(proxy.getDctermsTOC()));
			dcTermsTemporal = add(dcTermsTemporal,
					map2Array(proxy.getDctermsTemporal()));

			dcContributor = add(dcContributor,
					map2Array(proxy.getDcContributor()));
			dcCoverage = add(dcCoverage, map2Array(proxy.getDcCoverage()));
			dcCreator = add(dcCreator, map2Array(proxy.getDcCreator()));
			dcDate = add(dcDate, map2Array(proxy.getDcDate()));
			dcDescription = add(dcDescription,
					map2Array(proxy.getDcDescription()));
			dcFormat = add(dcFormat, map2Array(proxy.getDcFormat()));
			dcIdentifier = add(dcIdentifier, map2Array(proxy.getDcIdentifier()));
			dcLanguage = add(dcLanguage, map2Array(proxy.getDcLanguage()));
			dcPublisher = add(dcPublisher, map2Array(proxy.getDcPublisher()));
			dcRelation = add(dcRelation, map2Array(proxy.getDcRelation()));

			dcRights = add(dcRights, map2Array(proxy.getDcRights()));
			dcSource = add(dcSource, map2Array(proxy.getDcSource()));
			dcSubject = add(dcSubject, map2Array(proxy.getDcSubject()));
			dcTitle = add(dcTitle, map2Array(proxy.getDcTitle()));
			dcType = add(dcType, map2Array(proxy.getDcType()));

			europeanaYear = add(europeanaYear, map2Array(proxy.getYear()));
		}

		europeanaCompleteness = bean.getEuropeanaCompleteness();

		for (Place item : bean.getPlaces()) {
			enrichmentPlaceTerm = add(enrichmentPlaceTerm, item.getAbout()); // ????
			enrichmentPlaceLabel = add(enrichmentPlaceLabel,
					map2Array(item.getPrefLabel()));
			if (item.getLatitude() != null) {
				enrichmentPlaceLatitude = item.getLatitude();
			}
			if (item.getLongitude() != null) {
				enrichmentPlaceLongitude = item.getLongitude();
			}
			// addValue(fields, ENRICHMENT_PLACE_BROADER_TERM,
			// bean.getEnrichmentPlaceBroaderTerm());
			// addValue(fields, ENRICHMENT_PLACE_BROADER_LABEL,
			// bean.getEnrichmentPlaceBroaderLabel());
		}

		for (Timespan item : bean.getTimespans()) {
			enrichmentPeriodTerm = add(enrichmentPeriodTerm, item.getAbout()); // ????
			enrichmentPeriodLabel = add(enrichmentPeriodLabel,
					map2Array(item.getPrefLabel()));
			try {
				if (item.getBegin() != null) {
					enrichmentPeriodBegin = new SimpleDateFormat("yyyy-mm-dd")
							.parse(map2Array(item.getBegin())[0]);
				}
				if (item.getEnd() != null) {
					enrichmentPeriodEnd = new SimpleDateFormat("yyyy-mm-dd")
							.parse(map2Array(item.getEnd())[0]);
				}
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// addValue(fields, ENRICHMENT_PERIOD_BROADER_TERM,
			// bean.getEnrichmentPeriodBroaderTerm());
			// addValue(fields, ENRICHMENT_PERIOD_BROADER_LABEL,
			// bean.getEnrichmentPeriodBroaderLabel());
		}

		for (Concept item : bean.getConcepts()) {
			enrichmentConceptTerm = add(enrichmentConceptTerm, item.getAbout()); // ????
			enrichmentConceptLabel = add(enrichmentConceptLabel,
					map2Array(item.getPrefLabel())); // ????
			// addValue(fields, ENRICHMENT_CONCEPT_BROADER_TERM,
			// bean.getEnrichmentConceptBroaderTerm());
			// addValue(fields, ENRICHMENT_CONCEPT_BROADER_LABEL,
			// bean.getEnrichmentConceptBroaderLabel());
		}

		for (Agent item : bean.getAgents()) {
			enrichmentAgentTerm = add(enrichmentAgentTerm, item.getAbout()); // ????
			enrichmentAgentLabel = add(enrichmentAgentLabel,
					map2Array(item.getPrefLabel())); // ????
		}
	}

	public Map<String, Object> asMap() {
		if (map == null) {
			map = new LinkedHashMap<String, Object>();

			addValue("europeana:uri", getId());
			addValue("europeana:country", getEuropeanaCountry());
			addValue("europeana:provider", getEuropeanaProvider());
			addValue("europeana:collectionName", getEuropeanaCollectionName());
			addValue("edm:datasetName", getEuropeanaCollectionName());
			addValue("europeana:isShownAt", getEuropeanaIsShownAt());
			addValue("europeana:isShownBy", getEuropeanaIsShownBy());
//			if (!isOptedOut) {
				addValue("europeana:object", getThumbnails());
//			}

			addValue("europeana:language", getEuropeanaLanguage());
			addValue("europeana:type", getType());
			addValue("europeana:userTag", getEuropeanaUserTag());
			addValue("europeana:year", getEuropeanaYear());
			addValue("europeana:rights", getEuropeanaRights());
			addValue("europeana:dataProvider", getEuropeanaDataProvider());
			addValue("europeana:UGC", getEuropeanaUGC());

			addValue("dcterms:alternative", getDcTermsAlternative());
			addValue("dcterms:conformsTo", getDcTermsConformsTo());
			addValue("dcterms:created", getDcTermsCreated());
			addValue("dcterms:extent", getDcTermsExtent());
			addValue("dcterms:hasFormat", getDcTermsHasFormat());
			addValue("dcterms:hasPart", getDcTermsHasPart());
			addValue("dcterms:hasVersion", getDcTermsHasVersion());
			addValue("dcterms:isFormatOf", getDcTermsIsFormatOf());
			addValue("dcterms:isPartOf", getDcTermsIsPartOf());
			addValue("dcterms:isReferencedBy", getDcTermsIsReferencedBy());
			addValue("dcterms:isReplacedBy", getDcTermsIsReplacedBy());
			addValue("dcterms:isRequiredBy", getDcTermsIsRequiredBy());
			addValue("dcterms:issued", getDcTermsIssued());
			addValue("dcterms:isVersionOf", getDcTermsIsVersionOf());
			addValue("dcterms:medium", getDcTermsMedium());
			addValue("dcterms:provenance", getDcTermsProvenance());
			addValue("dcterms:references", getDcTermsReferences());
			addValue("dcterms:replaces", getDcTermsReplaces());
			addValue("dcterms:requires", getDcTermsRequires());
			addValue("dcterms:spatial", getDcTermsSpatial());
			addValue("dcterms:tableOfContents", getDcTermsTableOfContents());
			addValue("dcterms:temporal", getDcTermsTemporal());

			addValue("dc:contributor", getDcContributor());
			addValue("dc:coverage", getDcCoverage());
			addValue("dc:creator", getDcCreator());
			addValue("dc:date", getDcDate());
			addValue("dc:description", getDcDescription());
			addValue("dc:format", getDcFormat());
			addValue("dc:identifier", getDcIdentifier());
			addValue("dc:language", getDcLanguage());
			addValue("dc:publisher", getDcPublisher());
			addValue("dc:relation", getDcRelation());
			addValue("dc:rights", getDcRights());
			addValue("dc:source", getDcSource());
			addValue("dc:subject", getDcSubject());
			addValue("dc:title", getDcTitle());
			addValue("dc:type", getDcType());

			addValue("europeana:completeness",
					Integer.toString(getEuropeanaCompleteness()));
			addValue("enrichment:place_term", getEnrichmentPlaceTerm());
			addValue("enrichment:place_label", getEnrichmentPlaceLabel());
			if ((getEnrichmentPlaceLatitude() != 0)
					|| (getEnrichmentPlaceLongitude() != 0)) {
				addValue("enrichment:place_latitude",
						Float.toString(getEnrichmentPlaceLatitude()));
				addValue("enrichment:place_longitude",
						Float.toString(getEnrichmentPlaceLongitude()));
			}
			addValue("enrichment:place_broader_term",
					getEnrichmentPlaceBroaderTerm());
			addValue("enrichment:place_broader_label",
					getEnrichmentPlaceBroaderLabel());

			addValue("enrichment:period_term", getEnrichmentPeriodTerm());
			addValue("enrichment:period_label", getEnrichmentPeriodLabel());
			if (getEnrichmentPeriodBegin() != null) {
				addValue("enrichment:period_begin", getEnrichmentPeriodBegin()
						.toString());
			}
			if (getEnrichmentPeriodEnd() != null) {
				addValue("enrichment:period_end", getEnrichmentPeriodEnd()
						.toString());
			}
			addValue("enrichment:period_broader_term",
					getEnrichmentPeriodBroaderTerm());
			addValue("enrichment:period_broader_label",
					getEnrichmentPeriodBroaderLabel());

			addValue("enrichment:concept_term", getEnrichmentConceptTerm());
			addValue("enrichment:concept_label", getEnrichmentConceptLabel());
			addValue("enrichment:concept_broader_term",
					getEnrichmentConceptBroaderTerm());
			addValue("enrichment:concept_broader_label",
					getEnrichmentConceptBroaderLabel());

			addValue("enrichment:agent_term", getEnrichmentAgentTerm());
			addValue("enrichment:agent_label", getEnrichmentAgentLabel());
		}
		return map;
	}

	private void addValue(String key, Object value) {
		if (value != null) {
			if (value instanceof String[] && ((String[]) value).length == 1) {
				map.put(key, ((String[]) value)[0]);
			} else {
				map.put(key, value);
			}
		}
	}

	/*
	 * public Map<String, Object> asMap() { List<MetaDataFieldPresentation>
	 * fields = new ArrayList<MetaDataFieldPresentation>();
	 * 
	 * 
	 * return fields; }
	 */

	private String[] map2Array(Map<String, List<String>> map) {
		if (map == null) {
			return null;
		}
		List<String> values = new ArrayList<String>();
		for (List<String> entry : map.values()) {
			if (entry != null){
                            values.addAll(entry);
                        }
		}
		return values.toArray(new String[values.size()]);
	}

	private String[] add(String[] field, String value) {
		if (value != null) {
			field = (String[]) ArrayUtils.add(field, value);
		}
		return field;
	}

	private String[] add(String[] field, String[] value) {
		if (value != null) {
			if (field == null) {
				field = value;
			} else {
				field = (String[]) ArrayUtils.addAll(field, value);
			}
		}
		return field;
	}

	@XmlElement(name = "uri", namespace = SrwResponse.NS_EUROPEANA)
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@XmlElement(name = "collectionName", namespace = SrwResponse.NS_EUROPEANA)
	public String[] getEuropeanaCollectionName() {
		return europeanaCollectionName;
	}

	public void setEuropeanaCollectionName(String[] europeanaCollectionName) {
		this.europeanaCollectionName = europeanaCollectionName;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@XmlElement(name = "country", namespace = SrwResponse.NS_EUROPEANA)
	public String[] getEuropeanaCountry() {
		return europeanaCountry;
	}

	public void setEuropeanaCountry(String[] europeanaCountry) {
		this.europeanaCountry = europeanaCountry;
	}

	@XmlElement(name = "provider", namespace = SrwResponse.NS_EUROPEANA)
	public String[] getEuropeanaProvider() {
		return europeanaProvider;
	}

	public void setEuropeanaProvider(String[] europeanaProvider) {
		this.europeanaProvider = europeanaProvider;
	}

	@XmlElement(name = "isShownAt", namespace = SrwResponse.NS_EUROPEANA)
	public String[] getEuropeanaIsShownAt() {
		return europeanaIsShownAt;
	}

	public void setEuropeanaIsShownAt(String[] europeanaIsShownAt) {
		this.europeanaIsShownAt = europeanaIsShownAt;
	}

	@XmlElement(name = "isShownBy", namespace = SrwResponse.NS_EUROPEANA)
	public String[] getEuropeanaIsShownBy() {
		return europeanaIsShownBy;
	}

	public void setEuropeanaIsShownBy(String[] europeanaIsShownBy) {
		this.europeanaIsShownBy = europeanaIsShownBy;
	}

	@XmlElement(name = "object", namespace = SrwResponse.NS_EUROPEANA)
	public String[] getThumbnails() {
//		if (isOptedOut) {
//			return null;
//		}
		return thumbnails;
	}

	public void setThumbnails(String[] thumbnails) {
		this.thumbnails = thumbnails;
	}

	@XmlElement(name = "language", namespace = SrwResponse.NS_EUROPEANA)
	public String[] getEuropeanaLanguage() {
		return europeanaLanguage;
	}

	public void setEuropeanaLanguage(String[] europeanaLanguage) {
		this.europeanaLanguage = europeanaLanguage;
	}

	@XmlElement(name = "userTag", namespace = SrwResponse.NS_EUROPEANA)
	public String[] getEuropeanaUserTag() {
		return europeanaUserTag;
	}

	public void setEuropeanaUserTag(String[] europeanaUserTag) {
		this.europeanaUserTag = europeanaUserTag;
	}

	@XmlElement(name = "year", namespace = SrwResponse.NS_EUROPEANA)
	public String[] getEuropeanaYear() {
		return europeanaYear;
	}

	public void setEuropeanaYear(String[] europeanaYear) {
		this.europeanaYear = europeanaYear;
	}

	@XmlElement(name = "rights", namespace = SrwResponse.NS_EUROPEANA)
	public String[] getEuropeanaRights() {
		return europeanaRights;
	}

	public void setEuropeanaRights(String[] europeanaRights) {
		this.europeanaRights = europeanaRights;
	}

	@XmlElement(name = "dataProvider", namespace = SrwResponse.NS_EUROPEANA)
	public String[] getEuropeanaDataProvider() {
		return europeanaDataProvider;
	}

	public void setEuropeanaDataProvider(String[] europeanaDataProvider) {
		this.europeanaDataProvider = europeanaDataProvider;
	}

	@XmlElement(name = "ugc", namespace = SrwResponse.NS_EUROPEANA)
	public String[] getEuropeanaUGC() {
		return europeanaUGC;
	}

	public void setEuropeanaUGC(String[] europeanaUGC) {
		this.europeanaUGC = europeanaUGC;
	}

	@XmlElement(name = "alternative", namespace = SrwResponse.NS_DCTERMS)
	public String[] getDcTermsAlternative() {
		return dcTermsAlternative;
	}

	public void setDcTermsAlternative(String[] dcTermsAlternative) {
		this.dcTermsAlternative = dcTermsAlternative;
	}

	@XmlElement(name = "conformsTo", namespace = SrwResponse.NS_DCTERMS)
	public String[] getDcTermsConformsTo() {
		return dcTermsConformsTo;
	}

	public void setDcTermsConformsTo(String[] dcTermsConformsTo) {
		this.dcTermsConformsTo = dcTermsConformsTo;
	}

	@XmlElement(name = "created", namespace = SrwResponse.NS_DCTERMS)
	public String[] getDcTermsCreated() {
		return dcTermsCreated;
	}

	public void setDcTermsCreated(String[] dcTermsCreated) {
		this.dcTermsCreated = dcTermsCreated;
	}

	@XmlElement(name = "extent", namespace = SrwResponse.NS_DCTERMS)
	public String[] getDcTermsExtent() {
		return dcTermsExtent;
	}

	public void setDcTermsExtent(String[] dcTermsExtent) {
		this.dcTermsExtent = dcTermsExtent;
	}

	@XmlElement(name = "hasFormat", namespace = SrwResponse.NS_DCTERMS)
	public String[] getDcTermsHasFormat() {
		return dcTermsHasFormat;
	}

	public void setDcTermsHasFormat(String[] dcTermsHasFormat) {
		this.dcTermsHasFormat = dcTermsHasFormat;
	}

	@XmlElement(name = "hasPart", namespace = SrwResponse.NS_DCTERMS)
	public String[] getDcTermsHasPart() {
		return dcTermsHasPart;
	}

	public void setDcTermsHasPart(String[] dcTermsHasPart) {
		this.dcTermsHasPart = dcTermsHasPart;
	}

	@XmlElement(name = "hasVersion", namespace = SrwResponse.NS_DCTERMS)
	public String[] getDcTermsHasVersion() {
		return dcTermsHasVersion;
	}

	public void setDcTermsHasVersion(String[] dcTermsHasVersion) {
		this.dcTermsHasVersion = dcTermsHasVersion;
	}

	@XmlElement(name = "isFormatOf", namespace = SrwResponse.NS_DCTERMS)
	public String[] getDcTermsIsFormatOf() {
		return dcTermsIsFormatOf;
	}

	public void setDcTermsIsFormatOf(String[] dcTermsIsFormatOf) {
		this.dcTermsIsFormatOf = dcTermsIsFormatOf;
	}

	@XmlElement(name = "isPartOf", namespace = SrwResponse.NS_DCTERMS)
	public String[] getDcTermsIsPartOf() {
		return dcTermsIsPartOf;
	}

	public void setDcTermsIsPartOf(String[] dcTermsIsPartOf) {
		this.dcTermsIsPartOf = dcTermsIsPartOf;
	}

	@XmlElement(name = "isReferencedBy", namespace = SrwResponse.NS_DCTERMS)
	public String[] getDcTermsIsReferencedBy() {
		return dcTermsIsReferencedBy;
	}

	public void setDcTermsIsReferencedBy(String[] dcTermsIsReferencedBy) {
		this.dcTermsIsReferencedBy = dcTermsIsReferencedBy;
	}

	@XmlElement(name = "isReplacedBy", namespace = SrwResponse.NS_DCTERMS)
	public String[] getDcTermsIsReplacedBy() {
		return dcTermsIsReplacedBy;
	}

	public void setDcTermsIsReplacedBy(String[] dcTermsIsReplacedBy) {
		this.dcTermsIsReplacedBy = dcTermsIsReplacedBy;
	}

	@XmlElement(name = "isRequiredBy", namespace = SrwResponse.NS_DCTERMS)
	public String[] getDcTermsIsRequiredBy() {
		return dcTermsIsRequiredBy;
	}

	public void setDcTermsIsRequiredBy(String[] dcTermsIsRequiredBy) {
		this.dcTermsIsRequiredBy = dcTermsIsRequiredBy;
	}

	@XmlElement(name = "issued", namespace = SrwResponse.NS_DCTERMS)
	public String[] getDcTermsIssued() {
		return dcTermsIssued;
	}

	public void setDcTermsIssued(String[] dcTermsIssued) {
		this.dcTermsIssued = dcTermsIssued;
	}

	@XmlElement(name = "isVersionOf", namespace = SrwResponse.NS_DCTERMS)
	public String[] getDcTermsIsVersionOf() {
		return dcTermsIsVersionOf;
	}

	public void setDcTermsIsVersionOf(String[] dcTermsIsVersionOf) {
		this.dcTermsIsVersionOf = dcTermsIsVersionOf;
	}

	@XmlElement(name = "medium", namespace = SrwResponse.NS_DCTERMS)
	public String[] getDcTermsMedium() {
		return dcTermsMedium;
	}

	public void setDcTermsMedium(String[] dcTermsMedium) {
		this.dcTermsMedium = dcTermsMedium;
	}

	@XmlElement(name = "provenance", namespace = SrwResponse.NS_DCTERMS)
	public String[] getDcTermsProvenance() {
		return dcTermsProvenance;
	}

	public void setDcTermsProvenance(String[] dcTermsProvenance) {
		this.dcTermsProvenance = dcTermsProvenance;
	}

	@XmlElement(name = "references", namespace = SrwResponse.NS_DCTERMS)
	public String[] getDcTermsReferences() {
		return dcTermsReferences;
	}

	public void setDcTermsReferences(String[] dcTermsReferences) {
		this.dcTermsReferences = dcTermsReferences;
	}

	@XmlElement(name = "replaces", namespace = SrwResponse.NS_DCTERMS)
	public String[] getDcTermsReplaces() {
		return dcTermsReplaces;
	}

	public void setDcTermsReplaces(String[] dcTermsReplaces) {
		this.dcTermsReplaces = dcTermsReplaces;
	}

	@XmlElement(name = "requires", namespace = SrwResponse.NS_DCTERMS)
	public String[] getDcTermsRequires() {
		return dcTermsRequires;
	}

	public void setDcTermsRequires(String[] dcTermsRequires) {
		this.dcTermsRequires = dcTermsRequires;
	}

	@XmlElement(name = "spatial", namespace = SrwResponse.NS_DCTERMS)
	public String[] getDcTermsSpatial() {
		return dcTermsSpatial;
	}

	public void setDcTermsSpatial(String[] dcTermsSpatial) {
		this.dcTermsSpatial = dcTermsSpatial;
	}

	@XmlElement(name = "tableOfContents", namespace = SrwResponse.NS_DCTERMS)
	public String[] getDcTermsTableOfContents() {
		return dcTermsTableOfContents;
	}

	public void setDcTermsTableOfContents(String[] dcTermsTableOfContents) {
		this.dcTermsTableOfContents = dcTermsTableOfContents;
	}

	@XmlElement(name = "temporal", namespace = SrwResponse.NS_DCTERMS)
	public String[] getDcTermsTemporal() {
		return dcTermsTemporal;
	}

	public void setDcTermsTemporal(String[] dcTermsTemporal) {
		this.dcTermsTemporal = dcTermsTemporal;
	}

	@XmlElement(name = "contributor", namespace = SrwResponse.NS_DC)
	public String[] getDcContributor() {
		return dcContributor;
	}

	public void setDcContributor(String[] dcContributor) {
		this.dcContributor = dcContributor;
	}

	@XmlElement(name = "coverage", namespace = SrwResponse.NS_DC)
	public String[] getDcCoverage() {
		return dcCoverage;
	}

	public void setDcCoverage(String[] dcCoverage) {
		this.dcCoverage = dcCoverage;
	}

	@XmlElement(name = "creator", namespace = SrwResponse.NS_DC)
	public String[] getDcCreator() {
		return dcCreator;
	}

	public void setDcCreator(String[] dcCreator) {
		this.dcCreator = dcCreator;
	}

	@XmlElement(name = "date", namespace = SrwResponse.NS_DC)
	public String[] getDcDate() {
		return dcDate;
	}

	public void setDcDate(String[] dcDate) {
		this.dcDate = dcDate;
	}

	@XmlElement(name = "description", namespace = SrwResponse.NS_DC)
	public String[] getDcDescription() {
		return dcDescription;
	}

	public void setDcDescription(String[] dcDescription) {
		this.dcDescription = dcDescription;
	}

	@XmlElement(name = "format", namespace = SrwResponse.NS_DC)
	public String[] getDcFormat() {
		return dcFormat;
	}

	public void setDcFormat(String[] dcFormat) {
		this.dcFormat = dcFormat;
	}

	@XmlElement(name = "identifier", namespace = SrwResponse.NS_DC)
	public String[] getDcIdentifier() {
		return dcIdentifier;
	}

	public void setDcIdentifier(String[] dcIdentifier) {
		this.dcIdentifier = dcIdentifier;
	}

	@XmlElement(name = "language", namespace = SrwResponse.NS_DC)
	public String[] getDcLanguage() {
		return dcLanguage;
	}

	public void setDcLanguage(String[] dcLanguage) {
		this.dcLanguage = dcLanguage;
	}

	@XmlElement(name = "publisher", namespace = SrwResponse.NS_DC)
	public String[] getDcPublisher() {
		return dcPublisher;
	}

	public void setDcPublisher(String[] dcPublisher) {
		this.dcPublisher = dcPublisher;
	}

	@XmlElement(name = "relation", namespace = SrwResponse.NS_DC)
	public String[] getDcRelation() {
		return dcRelation;
	}

	public void setDcRelation(String[] dcRelation) {
		this.dcRelation = dcRelation;
	}

	@XmlElement(name = "rights", namespace = SrwResponse.NS_DC)
	public String[] getDcRights() {
		return dcRights;
	}

	public void setDcRights(String[] dcRights) {
		this.dcRights = dcRights;
	}

	@XmlElement(name = "source", namespace = SrwResponse.NS_DC)
	public String[] getDcSource() {
		return dcSource;
	}

	public void setDcSource(String[] dcSource) {
		this.dcSource = dcSource;
	}

	@XmlElement(name = "subject", namespace = SrwResponse.NS_DC)
	public String[] getDcSubject() {
		return dcSubject;
	}

	public void setDcSubject(String[] dcSubject) {
		this.dcSubject = dcSubject;
	}

	@XmlElement(name = "title", namespace = SrwResponse.NS_DC)
	public String[] getDcTitle() {
		return dcTitle;
	}

	public void setDcTitle(String[] dcTitle) {
		this.dcTitle = dcTitle;
	}

	@XmlElement(name = "type", namespace = SrwResponse.NS_DC)
	public String[] getDcType() {
		return dcType;
	}

	public void setDcType(String[] dcType) {
		this.dcType = dcType;
	}

	@XmlElement(name = "completeness", namespace = SrwResponse.NS_EUROPEANA)
	public int getEuropeanaCompleteness() {
		return europeanaCompleteness;
	}

	public void setEuropeanaCompleteness(int europeanaCompleteness) {
		this.europeanaCompleteness = europeanaCompleteness;
	}

	@XmlElement(name = "place_term", namespace = SrwResponse.NS_ENRICHMENT)
	public String[] getEnrichmentPlaceTerm() {
		return enrichmentPlaceTerm;
	}

	public void setEnrichmentPlaceTerm(String[] enrichmentPlaceTerm) {
		this.enrichmentPlaceTerm = enrichmentPlaceTerm;
	}

	@XmlElement(name = "place_label", namespace = SrwResponse.NS_ENRICHMENT)
	public String[] getEnrichmentPlaceLabel() {
		return enrichmentPlaceLabel;
	}

	public void setEnrichmentPlaceLabel(String[] enrichmentPlaceLabel) {
		this.enrichmentPlaceLabel = enrichmentPlaceLabel;
	}

	@XmlElement(name = "place_latitude", namespace = SrwResponse.NS_ENRICHMENT)
	public float getEnrichmentPlaceLatitude() {
		return enrichmentPlaceLatitude;
	}

	public void setEnrichmentPlaceLatitude(float enrichmentPlaceLatitude) {
		this.enrichmentPlaceLatitude = enrichmentPlaceLatitude;
	}

	@XmlElement(name = "place_longitude", namespace = SrwResponse.NS_ENRICHMENT)
	public float getEnrichmentPlaceLongitude() {
		return enrichmentPlaceLongitude;
	}

	public void setEnrichmentPlaceLongitude(float enrichmentPlaceLongitude) {
		this.enrichmentPlaceLongitude = enrichmentPlaceLongitude;
	}

	@XmlElement(name = "place_broader_label", namespace = SrwResponse.NS_ENRICHMENT)
	public String[] getEnrichmentPlaceBroaderTerm() {
		return enrichmentPlaceBroaderTerm;
	}

	public void setEnrichmentPlaceBroaderTerm(
			String[] enrichmentPlaceBroaderTerm) {
		this.enrichmentPlaceBroaderTerm = enrichmentPlaceBroaderTerm;
	}

	@XmlElement(name = "place_broader_label", namespace = SrwResponse.NS_ENRICHMENT)
	public String[] getEnrichmentPlaceBroaderLabel() {
		return enrichmentPlaceBroaderLabel;
	}

	public void setEnrichmentPlaceBroaderLabel(
			String[] enrichmentPlaceBroaderLabel) {
		this.enrichmentPlaceBroaderLabel = enrichmentPlaceBroaderLabel;
	}

	@XmlElement(name = "period_term", namespace = SrwResponse.NS_ENRICHMENT)
	public String[] getEnrichmentPeriodTerm() {
		return enrichmentPeriodTerm;
	}

	public void setEnrichmentPeriodTerm(String[] enrichmentPeriodTerm) {
		this.enrichmentPeriodTerm = enrichmentPeriodTerm;
	}

	@XmlElement(name = "period_label", namespace = SrwResponse.NS_ENRICHMENT)
	public String[] getEnrichmentPeriodLabel() {
		return enrichmentPeriodLabel;
	}

	public void setEnrichmentPeriodLabel(String[] enrichmentPeriodLabel) {
		this.enrichmentPeriodLabel = enrichmentPeriodLabel;
	}

	@XmlElement(name = "period_begin", namespace = SrwResponse.NS_ENRICHMENT)
	public Date getEnrichmentPeriodBegin() {
		return enrichmentPeriodBegin;
	}

	public void setEnrichmentPeriodBegin(Date enrichmentPeriodBegin) {
		this.enrichmentPeriodBegin = enrichmentPeriodBegin;
	}

	@XmlElement(name = "period_end", namespace = SrwResponse.NS_ENRICHMENT)
	public Date getEnrichmentPeriodEnd() {
		return enrichmentPeriodEnd;
	}

	public void setEnrichmentPeriodEnd(Date enrichmentPeriodEnd) {
		this.enrichmentPeriodEnd = enrichmentPeriodEnd;
	}

	@XmlElement(name = "period_broader_term", namespace = SrwResponse.NS_ENRICHMENT)
	public String[] getEnrichmentPeriodBroaderTerm() {
		return enrichmentPeriodBroaderTerm;
	}

	public void setEnrichmentPeriodBroaderTerm(
			String[] enrichmentPeriodBroaderTerm) {
		this.enrichmentPeriodBroaderTerm = enrichmentPeriodBroaderTerm;
	}

	@XmlElement(name = "period_broader_label", namespace = SrwResponse.NS_ENRICHMENT)
	public String[] getEnrichmentPeriodBroaderLabel() {
		return enrichmentPeriodBroaderLabel;
	}

	public void setEnrichmentPeriodBroaderLabel(
			String[] enrichmentPeriodBroaderLabel) {
		this.enrichmentPeriodBroaderLabel = enrichmentPeriodBroaderLabel;
	}

	@XmlElement(name = "concept_term", namespace = SrwResponse.NS_ENRICHMENT)
	public String[] getEnrichmentConceptTerm() {
		return enrichmentConceptTerm;
	}

	public void setEnrichmentConceptTerm(String[] enrichmentConceptTerm) {
		this.enrichmentConceptTerm = enrichmentConceptTerm;
	}

	@XmlElement(name = "concept_label", namespace = SrwResponse.NS_ENRICHMENT)
	public String[] getEnrichmentConceptLabel() {
		return enrichmentConceptLabel;
	}

	public void setEnrichmentConceptLabel(String[] enrichmentConceptLabel) {
		this.enrichmentConceptLabel = enrichmentConceptLabel;
	}

	@XmlElement(name = "concept_broader_term", namespace = SrwResponse.NS_ENRICHMENT)
	public String[] getEnrichmentConceptBroaderTerm() {
		return enrichmentConceptBroaderTerm;
	}

	public void setEnrichmentConceptBroaderTerm(
			String[] enrichmentConceptBroaderTerm) {
		this.enrichmentConceptBroaderTerm = enrichmentConceptBroaderTerm;
	}

	@XmlElement(name = "concept_broader_label", namespace = SrwResponse.NS_ENRICHMENT)
	public String[] getEnrichmentConceptBroaderLabel() {
		return enrichmentConceptBroaderLabel;
	}

	public void setEnrichmentConceptBroaderLabel(
			String[] enrichmentConceptBroaderLabel) {
		this.enrichmentConceptBroaderLabel = enrichmentConceptBroaderLabel;
	}

	@XmlElement(name = "agent_term", namespace = SrwResponse.NS_ENRICHMENT)
	public String[] getEnrichmentAgentTerm() {
		return enrichmentAgentTerm;
	}

	public void setEnrichmentAgentTerm(String[] enrichmentAgentTerm) {
		this.enrichmentAgentTerm = enrichmentAgentTerm;
	}

	@XmlElement(name = "agent_label", namespace = SrwResponse.NS_ENRICHMENT)
	public String[] getEnrichmentAgentLabel() {
		return enrichmentAgentLabel;
	}

	public void setEnrichmentAgentLabel(String[] enrichmentAgentLabel) {
		this.enrichmentAgentLabel = enrichmentAgentLabel;
	}

//	public void setOptedOut(boolean isOptedOut) {
//		this.isOptedOut = isOptedOut;
//	}
}
