package eu.europeana.api2.v2.model.json.view;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import eu.europeana.api2.v2.model.enums.Profile;
import eu.europeana.corelib.definitions.edm.beans.ApiBean;
import eu.europeana.corelib.definitions.edm.beans.BriefBean;
import eu.europeana.corelib.utils.DateUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonPropertyOrder;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

/**
 * The ApiView defines the fields that are returned in search results by default (corresponds to the 'standard' profile)
 */
@JsonInclude(NON_EMPTY)
@JsonPropertyOrder(alphabetic=true)
public class ApiView extends BriefView implements ApiBean {

    private ApiBean bean;
    private boolean[] ugc;
    private String[] country;
    private String[] europeanaCollectionName;
    private String[] dctermsIsPartOf;

    public ApiView(ApiBean bean, String profile, String wskey) {
        super(bean, profile, wskey);
        this.bean = bean;
        ugc = bean.getUgc();
        country = bean.getCountry();
        europeanaCollectionName = bean.getEuropeanaCollectionName();
        dctermsIsPartOf = bean.getDctermsIsPartOf();
    }

    @Override
    public String[] getEdmConcept() {
        return bean.getEdmConcept();
    }

    @Override
    public List<Map<String, String>> getEdmConceptLabel() {
        if (bean.getEdmConceptLabel() != null) {
            return bean.getEdmConceptLabel();
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public String[] getEdmConceptBroaderTerm() {
        if (isProfile(Profile.MINIMAL) || isProfile(Profile.STANDARD) || isProfile(Profile.RICH)) {
            return new String[0];
        }
        return bean.getEdmConceptBroaderTerm();
    }

    @Override
    public List<Map<String, String>> getEdmConceptBroaderLabel() {
        if (isProfile(Profile.MINIMAL) || isProfile(Profile.STANDARD) || isProfile(Profile.RICH)) {
            return Collections.emptyList();
        }
        return bean.getEdmConceptBroaderLabel();
    }

    @Override
    public String[] getEdmTimespanBroaderTerm() {
        return bean.getEdmTimespanBroaderTerm();
    }

    @Override
    public List<Map<String, String>> getEdmTimespanBroaderLabel() {
        return bean.getEdmTimespanBroaderLabel();
    }

    @Override
    public boolean[] getUgc() {
        return ugc;
    }

    @Override
    public void setUgc(boolean[] ugc) {
        this.ugc = ugc;
    }

    @Override
    public String[] getCountry() {
        if (isProfile(Profile.MINIMAL)) {
            return new String[0];
        }
        return country;
    }

    @Override
    public void setCountry(String[] country) {
        this.country = country;
    }

    @Override
    public String[] getEuropeanaCollectionName() {
        return europeanaCollectionName;
    }

    @Override
    public void setEuropeanaCollectionName(String[] europeanaCollectionName) {
        this.europeanaCollectionName = europeanaCollectionName;
    }

    @Override
    public String[] getEdmPlaceBroaderTerm() {
        return bean.getEdmPlaceBroaderTerm();
    }

    @Override
    public List<Map<String, String>> getEdmPlaceAltLabel() {
        if (isProfile(Profile.MINIMAL) || isProfile(Profile.STANDARD) || isProfile(Profile.RICH)) {
            return Collections.emptyList();
        }
        return bean.getEdmPlaceAltLabel();
    }

    @Override
    public String[] getDctermsIsPartOf() {
        return dctermsIsPartOf;
    }

    @Override
    public void setDctermsIsPartOf(String[] dctermsIsPartOf) {
        this.dctermsIsPartOf = dctermsIsPartOf;
    }

    @Override
    public String[] getDcLanguage() {
        return bean.getDcLanguage();
    }

    @Override
    @JsonProperty("timestamp_created_epoch")
    public Date getTimestampCreated() {
        return bean.getTimestampCreated();
    }

    @Override
    @JsonProperty("timestamp_update_epoch")
    public Date getTimestampUpdate() {
        return bean.getTimestampUpdate();
    }

    //TODO cleanup these getter methods, for instance by replacing with @JsonProperty annotations
    @Override
    public Map<String, List<String>> getEdmConceptPrefLabelLangAware() {
        return bean.getEdmConceptPrefLabelLangAware();
    }

    @Override
    public Map<String, List<String>> getEdmConceptBroaderLabelLangAware() {
        return bean.getEdmConceptBroaderLabelLangAware();
    }

    @Override
    public Map<String, List<String>> getEdmPlaceAltLabelLangAware() {
        return bean.getEdmPlaceAltLabelLangAware();
    }

}
