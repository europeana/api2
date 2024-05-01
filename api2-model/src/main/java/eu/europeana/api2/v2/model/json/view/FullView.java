package eu.europeana.api2.v2.model.json.view;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import eu.europeana.corelib.definitions.edm.beans.FullBean;
import eu.europeana.corelib.definitions.edm.entity.*;
import eu.europeana.corelib.solr.entity.AggregationImpl;
import eu.europeana.corelib.solr.entity.EuropeanaAggregationImpl;
import eu.europeana.corelib.solr.entity.QualityAnnotationImpl;
import eu.europeana.corelib.utils.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

/**
 * A FullView is the API view of FullBean object. The FulLBean is retrieved directly from Mongo, but the API adds and
 * alters specific fields, for example it generates proper API urls for the isShownAt and edmPreview fields
 */
@JsonInclude(NON_EMPTY)
@JsonPropertyOrder(alphabetic=true)
public class FullView implements FullBean {

    private FullBean bean;
    private Date timestampCreated;
    private Date timestampUpdated;

    public FullView(FullBean bean) {
        this.bean = bean;
        extractTimestampCreated();
        extractTimestampUpdated();
    }

    // TODO check if setting id's to null is still neccessary

    @Override
    public String getId() {
       return null; // bean.getId();
    }

    @Override
    public String[] getUserTags() {
        return bean.getUserTags();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<? extends Place> getPlaces() {
        if (bean.getPlaces() != null) {
            List<Place> items = (List<Place>) bean.getPlaces();
            for (Place item : items) {
                item.setId(null);
            }
            return items;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<? extends Agent> getAgents() {
        if (bean.getAgents() != null) {
            List<Agent> items = (List<Agent>) bean.getAgents();
            for (Agent item : items) {
                item.setId(null);
            }
            return items;
        }
        return null;
    }

    @Override
    public List<? extends Organization> getOrganizations() {
        return bean.getOrganizations();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<? extends Timespan> getTimespans() {
        if (bean.getTimespans() != null) {
            List<Timespan> items = (List<Timespan>) bean.getTimespans();
            for (Timespan item : items) {
                item.setId(null);
            }
            return items;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<? extends Concept> getConcepts() {
        if (bean.getConcepts() != null) {
            List<Concept> items = (List<Concept>) bean.getConcepts();
            for (Concept item : items) {
                item.setId(null);
            }
            return items;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<? extends Proxy> getProxies() {
        if (bean.getProxies() != null) {
            List<Proxy> items = (List<Proxy>) bean.getProxies();
            for (Proxy item : items) {
                item.setId(null);
            }
            return items;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<? extends Aggregation> getAggregations() {
        if (bean.getAggregations() != null) {
            List<? extends Aggregation> items =  bean.getAggregations();
            for (Aggregation item : items) {
                AggregationImpl aggregation = (AggregationImpl) item;

                // Add quality annotations in json response
                if (bean.getQualityAnnotations() != null) {
                    List<QualityAnnotation> qualityAnnotations = new ArrayList<>();
                    bean.getQualityAnnotations().stream().forEach(anno -> {
                                if (StringUtils.equals(aggregation.getAbout(), anno.getTarget()[0])) {
                                    // Don't modify values in the existing QA of th record as that will be used later in other places like Europeana Aggregation
                                    QualityAnnotation qa = new QualityAnnotationImpl();
                                    qa.setCreated(anno.getCreated());
                                    qa.setTarget(anno.getTarget());
                                    qa.setBody(anno.getBody());
                                    qualityAnnotations.add(qa);
                                }});
                    aggregation.setDqvHasQualityAnnotation(qualityAnnotations);
                }
                aggregation.setId(null);
                // also remove webresources IDs
                for (int j = 0, lw = item.getWebResources().size(); j < lw; j++) {
                    aggregation.getWebResources().get(j).setId(null);
                }
            }
            return items;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<? extends ProvidedCHO> getProvidedCHOs() {
        if (bean.getProvidedCHOs() != null) {
            List<ProvidedCHO> items = (List<ProvidedCHO>) bean.getProvidedCHOs();
            for (ProvidedCHO item : items) {
                item.setId(null);
            }
            return items;
        }
        return null;
    }

    @Override
    public String getAbout() {
        return bean.getAbout();
    }

    @Override
    public EuropeanaAggregation getEuropeanaAggregation() {
        if (bean.getEuropeanaAggregation() != null) {
            EuropeanaAggregation europeanaAggregation =  bean.getEuropeanaAggregation();
            EuropeanaAggregationImpl ea = (EuropeanaAggregationImpl) europeanaAggregation;

            // Add quality annotations in json response
            if (ea.getDqvHasQualityAnnotation() != null && bean.getQualityAnnotations() != null) {
                List<QualityAnnotation> qualityAnnotations = new ArrayList<>();
                for (String qa : ea.getDqvHasQualityAnnotation()) {
                    for (QualityAnnotation fBeanQA : bean.getQualityAnnotations()) {
                        if (StringUtils.equals(qa, fBeanQA.getAbout())) {
                            QualityAnnotation annotation = new QualityAnnotationImpl();
                            annotation.setCreated(fBeanQA.getCreated());
                            annotation.setBody(fBeanQA.getBody());
                            annotation.setTarget(new String [] {ea.getAbout()}); // target value will be '/aggregation/europeana/RECORD_ID'

                            qualityAnnotations.add(annotation);
                        }
                    }
                }
                ea.setHasQualityAnnotation(qualityAnnotations);
            }
            return ea;
        }
        return null;
    }

    /**
     * @deprecated unused, there are no records that have this field
     */
    @Override
    @Deprecated(since = "June 2021", forRemoval = false)
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
    public String getType() {
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

    private void extractTimestampCreated() {
        if (timestampCreated == null) {
            timestampCreated = bean.getTimestampCreated() != null ? bean.getTimestampCreated() : new Date(0);
        }
    }

    private void extractTimestampUpdated() {
        if (timestampUpdated == null) {
            timestampUpdated = bean.getTimestampUpdated() != null ? bean.getTimestampUpdated() : new Date(0);
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
    public List<? extends License> getLicenses() {
        return bean.getLicenses();
    }

    @Override
    public List<? extends Service> getServices() {
        return bean.getServices();
    }

    @Override
    public List<? extends QualityAnnotation> getQualityAnnotations() {
        // EA-3652 remove quality annotations from the response. will be added in aggregation
        return  null;
       // return bean.getQualityAnnotations();
    }

    // unwanted setters

    @Override
    public void setPlaces(List<? extends Place> places) {
        // left empty intentionally
    }

    @Override
    public void setConcepts(List<? extends Concept> concepts) {
        // left empty intentionally
    }

    @Override
    public void setAggregations(List<? extends Aggregation> aggregations) {
        // left empty intentionally
    }

    @Override
    public void setProxies(List<? extends Proxy> proxies) {
        // left empty intentionally
    }

    @Override
    public void setTimespans(List<? extends Timespan> timespans) {
        // left empty intentionally
    }

    @Override
    public void setProvidedCHOs(List<? extends ProvidedCHO> providedCHOs) {
        // left empty intentionally
    }

    @Override
    public void setEuropeanaAggregation(EuropeanaAggregation europeanaAggregation) {
        // left empty intentionally
    }

    @Override
    public void setEuropeanaId(ObjectId europeanaId) {
        // left empty intentionally
    }

    @Override
    public void setTitle(String[] title) {
        // left empty intentionally
    }

    @Override
    public void setYear(String[] year) {
        // left empty intentionally
    }

    @Override
    public void setProvider(String[] provider) {
        // left empty intentionally
    }

    @Override
    public void setLanguage(String[] language) {
        // left empty intentionally
    }

    @Override
    public void setType(String type) {
        // left empty intentionally
    }

    @Override
    public void setEuropeanaCompleteness(int europeanaCompleteness) {
        // left empty intentionally
    }

    @Override
    public void setAbout(String about) {
        // left empty intentionally
    }

    @Override
    public void setAgents(List<? extends Agent> agents) {
        // left empty intentionally
    }

    @Override
    public void setOrganizations(List<? extends Organization> organizations) {
        // left empty intentionally
    }

    @Override
    public void setCountry(String[] country) {
        // left empty intentionally
    }

    @Override
    public void setEuropeanaCollectionName(String[] europeanaCollectionName) {
        // left empty intentionally
    }

    @Override
    public void setTimestampCreated(Date timestampCreated) {
        // left empty intentionally
    }

    @Override
    public void setTimestampUpdated(Date timestampUpdated) {
        // left empty intentionally
    }

    @Override
    public void setLicenses(List<? extends License> licenses) {
        // left empty intentionally
    }

    @Override
    public void setServices(List<? extends Service> services) {
        // left empty intentionally
    }

    @Override
    public void setQualityAnnotations(List<? extends QualityAnnotation> qualityAnnotations) {
        // left empty intentionally
    }

}
