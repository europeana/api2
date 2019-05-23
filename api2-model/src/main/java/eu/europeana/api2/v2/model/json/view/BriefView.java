package eu.europeana.api2.v2.model.json.view;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import java.util.*;

import eu.europeana.api2.model.utils.Api2UrlService;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.annotation.JsonInclude;

import eu.europeana.api2.model.utils.LinkUtils;
import eu.europeana.api2.v2.model.enums.Profile;
import eu.europeana.corelib.definitions.edm.beans.BriefBean;
import eu.europeana.corelib.definitions.solr.DocType;
import eu.europeana.corelib.solr.bean.impl.IdBeanImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.annotate.JsonPropertyOrder;

/**
 * A BriefView defines the fields that are returned in search results when using the 'minimal' profile
 */
@JsonInclude(NON_EMPTY)
@JsonPropertyOrder(alphabetic=true)
public class BriefView extends IdBeanImpl implements BriefBean {

    private static final Logger  LOG = LogManager.getLogger(BriefView.class);
    protected Api2UrlService urlService;

    protected String profile;
    protected String wskey;
    protected BriefBean bean;
    private String[] thumbnails;

    public BriefView(BriefBean bean, String profile, String wskey) {
        this.bean = bean;
        this.profile = profile;
        this.wskey = wskey;
        urlService = Api2UrlService.getBeanInstance();
    }

    public String getProfile() {
        return null; // profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    @Override
    public String[] getTitle() {
        return bean.getTitle();
    }

    @Override
    public String[] getEdmObject() {
        // return bean.getEdmObject();
        return new String[0];
    }

    @Override
    public String[] getEdmIsShownBy() {
        return bean.getEdmIsShownBy();
    }

    @Override
    public String[] getDcDescription() {
        return bean.getDcDescription();
    }

    @Override
    public Map<String, List<String>> getDcDescriptionLangAware() {
        return bean.getDcDescriptionLangAware();
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
    public String[] getDataProvider() {
        return bean.getDataProvider();
    }

    @Override
    public String[] getLanguage() {
        if (isProfile(Profile.MINIMAL)) {
            return new String[0];
        }
        return bean.getLanguage();
    }

    @Override
    public String[] getRights() {
        return bean.getRights();
    }

    @Override
    public DocType getType() {
        return bean.getType();
    }

    @Override
    public String[] getDctermsSpatial() {
        if (isProfile(Profile.MINIMAL) || isProfile(Profile.STANDARD)) {
            return new String[0];
        }
        return bean.getDctermsSpatial();
    }

    @Override
    public String[] getDcLanguage() {
        if (isProfile(Profile.MINIMAL)) {
            return new String[0];
        }
        return bean.getDcLanguage();
    }

    @Override
    public Map<String, List<String>> getDcLanguageLangAware() {
        if (isProfile(Profile.MINIMAL)) {
            return Collections.emptyMap();
        }
        return bean.getDcLanguageLangAware();
    }

    @Override
    public int getEuropeanaCompleteness() {
        return bean.getEuropeanaCompleteness();
    }

    @Override
    public String[] getEdmPlace() {
        if (isProfile(Profile.MINIMAL) || isProfile(Profile.STANDARD)) {
            return new String[0];
        }
        return bean.getEdmPlace();
    }

    @Override
    public List<Map<String, String>> getEdmPlaceLabel() {
        if (isProfile(Profile.MINIMAL)) {
            return Collections.emptyList();
        }
        return bean.getEdmPlaceLabel();
    }

    @Override
    public Map<String, List<String>> getEdmPlaceLabelLangAware() {
        if (isProfile(Profile.MINIMAL)) {
            return Collections.emptyMap();
        }
        return bean.getEdmPlaceLabelLangAware();
    }

    @Override
    public List<String> getEdmPlaceLatitude() {
        return bean.getEdmPlaceLatitude();
    }

    @Override
    public List<String> getEdmPlaceLongitude() {
        return bean.getEdmPlaceLongitude();
    }

    @Override
    public String[] getEdmTimespan() {
        if (isProfile(Profile.MINIMAL) || isProfile(Profile.STANDARD)) {
            return new String[0];
        }
        return bean.getEdmTimespan();
    }

    @Override
    public List<Map<String, String>> getEdmTimespanLabel() {
        if (isProfile(Profile.MINIMAL)) {
            return Collections.emptyList();
        }
        return bean.getEdmTimespanLabel();
    }

    @Override
    public Map<String, List<String>> getEdmTimespanLabelLangAware() {
        if (isProfile(Profile.MINIMAL)) {
            return Collections.emptyMap();
        }
        return bean.getEdmTimespanLabelLangAware();
    }

    @Override
    public String[] getEdmTimespanBegin() {
        if (isProfile(Profile.MINIMAL)) {
            return new String[0];
        }
        return bean.getEdmTimespanBegin();
    }

    @Override
    public String[] getEdmTimespanEnd() {
        if (isProfile(Profile.MINIMAL)) {
            return new String[0];
        }
        return bean.getEdmTimespanEnd();
    }

    @Override
    public String[] getEdmAgent() {
        if (isProfile(Profile.MINIMAL) || isProfile(Profile.STANDARD)) {
            return new String[0];
        }
        return bean.getEdmAgent();
    }

    @Override
    public List<Map<String, String>> getEdmAgentLabel() {
        if (isProfile(Profile.MINIMAL) || isProfile(Profile.STANDARD)) {
            return Collections.emptyList();
        }
        return bean.getEdmAgentLabel();
    }

    @Override
    public Map<String, List<String>> getEdmAgentLabelLangAware() {
        if (isProfile(Profile.MINIMAL) || isProfile(Profile.STANDARD)) {
            return Collections.emptyMap();
        }
        return bean.getEdmAgentLabelLangAware();
    }

    @Override
    public String[] getDctermsHasPart() {
        // bean.getDctermsHasPart()
        return new String[0];
    }

    @Override
    public String[] getDcCreator() { return bean.getDcCreator(); }

    @Override
    public Map<String, List<String>> getDcCreatorLangAware() { return bean.getDcCreatorLangAware();
    }

    @Override
    public String[] getDcContributor() {
        if (isProfile(Profile.MINIMAL) || isProfile(Profile.STANDARD)) {
            return new String[0];
        }
        return bean.getDcContributor();
    }

    @Override
    public Map<String, List<String>> getDcContributorLangAware() {
        if (isProfile(Profile.MINIMAL) || isProfile(Profile.STANDARD)) {
            return Collections.emptyMap();
        }
        return bean.getDcContributorLangAware();
    }

    @Override
    public Date getTimestamp() {
        if (isProfile(Profile.MINIMAL)) {
            return null;
        }
        return bean.getTimestamp();
    }

    @Override
    public String getId() {
        return bean.getId();
    }

    /**
     * We need to convert all edmPreview values (which are original image urls) to proper API thumbnail urls
     * If there are no edmPreview values, we use edmObject instead. If that's not available we use edmIsShownBy
     * (this is similar to edmPreview generation for records in FullView class)
     * @return String array containing thumbnail links
     */
    private String[] getThumbnails() {
        if (thumbnails == null) {
            List<String> thumbs = new ArrayList<>();

            /// first try to generate from edmPreview
            String preview = getFirstNonEmptyString(bean.getEdmPreview());
            if (StringUtils.isNotEmpty(preview)) {
                thumbs.add(urlService.getThumbnailUrl(preview, getType()));
                LOG.debug("edmPreview {}, result = {}",  preview, thumbs.get(0));
            } else {
                // second try edmObject
                String object = getFirstNonEmptyString(bean.getEdmObject());
                if (StringUtils.isNotEmpty(object)) {
                    thumbs.add(urlService.getThumbnailUrl(object, getType()));
                    LOG.debug("edmObject {}, result = {}",  object, thumbs.get(0));
                } else {
                    String isShownBy = getFirstNonEmptyString(bean.getEdmIsShownBy());
                    if (StringUtils.isNotEmpty(isShownBy)) {
                        thumbs.add(urlService.getThumbnailUrl(isShownBy, getType()));
                        LOG.debug("edmIsShownBy {}, result = {}",  isShownBy, thumbs.get(0));
                    }
                }
            }
            thumbnails = thumbs.toArray(new String[0]);
        }
        return thumbnails;
    }

    private String getFirstNonEmptyString(String[] array) {
        if (array ==  null) {
            return null;
        }
        for (String s : array) {
            if (StringUtils.isNotEmpty(s)) {
                return s;
            }
        }
        return null;
    }

    public String getLink() {
        return urlService.getRecordApi2Url(getId(), wskey);
    }

    /* January 2018: method potentially deprecated!?
       GUID is a field that was introduced years ago, but there isn't any documentation on it. It's unclear if it's still used */
    public String getGuid() {
        return LinkUtils.addCampaignCodes(urlService.getRecordPortalUrl(getId()), wskey);
    }

    @Override
    public String[] getEdmPreview() {
        return getThumbnails();
    }

    @Override
    public float getScore() {
        return bean.getScore();
    }

    protected boolean isProfile(Profile p) {
        return StringUtils.containsIgnoreCase(profile, p.getName());
    }

    @Override
    public String[] getEdmIsShownAt() {
        if (ArrayUtils.isEmpty(bean.getEdmIsShownAt())) {
            return bean.getEdmIsShownAt();
        }
        String[] temp = getProvider();
        String provider = "";
        if (temp != null) {
            provider = temp[0];
        }
        List<String> isShownAtLinks = new ArrayList<>();
        for (String isShownAt : bean.getEdmIsShownAt()) {
            if (StringUtils.isBlank(bean.getEdmIsShownAt()[0])) {
                continue;
            }
            String isShownAtLink = urlService.getRedirectUrl(wskey, isShownAt, provider, bean.getId(), profile);
            isShownAtLinks.add(isShownAtLink);
        }
        return isShownAtLinks.toArray(new String[0]);
    }

    @Override
    public Boolean getPreviewNoDistribute() {
        if (isProfile(Profile.MINIMAL)) {
            return null; // if we return null the field won't be included in the json result
        }
        return bean.getPreviewNoDistribute() != null ? bean.getPreviewNoDistribute() : Boolean.FALSE;
    }

    @Override
    public Map<String, List<String>> getDcTitleLangAware() {
        return bean.getDcTitleLangAware();
    }


}
