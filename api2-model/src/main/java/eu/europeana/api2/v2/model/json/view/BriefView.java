package eu.europeana.api2.v2.model.json.view;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import eu.europeana.api2.model.utils.Api2UrlService;
import eu.europeana.api2.model.utils.LinkUtils;
import eu.europeana.api2.v2.model.enums.Profile;
import eu.europeana.corelib.definitions.edm.beans.BriefBean;
import eu.europeana.corelib.solr.bean.impl.IdBeanImpl;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

/**
 * A BriefView defines the fields that are returned in search results when using the 'minimal' profile
 */
@JsonInclude(NON_EMPTY)
@JsonPropertyOrder(alphabetic=true)
public class BriefView extends IdBeanImpl implements BriefBean {

    private static final Logger          LOG = LogManager.getLogger(BriefView.class);
    protected            Api2UrlService  urlService;

    protected Set<Profile> profiles;
    protected String wskey;
    protected BriefBean bean;
    protected String requestRoute;
    private String[] thumbnails;

    public BriefView(BriefBean bean, Set<Profile> profiles, String wskey, String requestRoute) {
        this.bean = bean;
        this.profiles = profiles;
        this.wskey = wskey;
        this.requestRoute = requestRoute;
        urlService = Api2UrlService.getBeanInstance();
    }

    public String getProfile() {
        return null; // profile;
    }

    public void setProfile(Set<Profile> profiles) {
        this.profiles = profiles;
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
//        if (profiles.contains(Profile.MINIMAL)) {
//            return new String[0];
//        }
        return bean.getLanguage();
    }

    @Override
    public String[] getRights() {
        return bean.getRights();
    }

    @Override
    public String getType() {
        return bean.getType();
    }

    @Override
    public String[] getDctermsSpatial() {
        if (profiles.contains(Profile.MINIMAL) || profiles.contains(Profile.STANDARD)) {
            return new String[0];
        }
        return bean.getDctermsSpatial();
    }

    @Override
    public String[] getDcLanguage() {
        if (profiles.contains(Profile.MINIMAL)) {
            return new String[0];
        }
        return bean.getDcLanguage();
    }

    @Override
    public Map<String, List<String>> getDcLanguageLangAware() {
        if (profiles.contains(Profile.MINIMAL)) {
            return Collections.emptyMap();
        }
        return bean.getDcLanguageLangAware();
    }

    @Override
    public int getEuropeanaCompleteness() {
        return bean.getEuropeanaCompleteness();
    }

    @Override
    public String getContentTier() {
        if (profiles.contains(Profile.DEBUG)) {
            return bean.getContentTier();
        }
        return null;
    }

    @Override
    public String getMetadataTier() {
        if (profiles.contains(Profile.DEBUG)) {
            return bean.getMetadataTier();
        }
        return null;
    }

    @Override
    public String[] getEdmPlace() {
        if (profiles.contains(Profile.MINIMAL) || profiles.contains(Profile.STANDARD)) {
            return new String[0];
        }
        return bean.getEdmPlace();
    }

    @Override
    public List<Map<String, String>> getEdmPlaceLabel() {
        if (profiles.contains(Profile.MINIMAL)) {
            return Collections.emptyList();
        }
        return bean.getEdmPlaceLabel();
    }

    @Override
    public Map<String, List<String>> getEdmPlaceLabelLangAware() {
        if (profiles.contains(Profile.MINIMAL)) {
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
        if (profiles.contains(Profile.MINIMAL) || profiles.contains(Profile.STANDARD)) {
            return new String[0];
        }
        return bean.getEdmTimespan();
    }

    @Override
    public List<Map<String, String>> getEdmTimespanLabel() {
        if (profiles.contains(Profile.MINIMAL)) {
            return Collections.emptyList();
        }
        return bean.getEdmTimespanLabel();
    }

    @Override
    public Map<String, List<String>> getEdmTimespanLabelLangAware() {
        if (profiles.contains(Profile.MINIMAL)) {
            return Collections.emptyMap();
        }
        return bean.getEdmTimespanLabelLangAware();
    }

    @Override
    public String[] getEdmTimespanBegin() {
        if (profiles.contains(Profile.MINIMAL)) {
            return new String[0];
        }
        return bean.getEdmTimespanBegin();
    }

    @Override
    public String[] getEdmTimespanEnd() {
        if (profiles.contains(Profile.MINIMAL)) {
            return new String[0];
        }
        return bean.getEdmTimespanEnd();
    }

    @Override
    public String[] getEdmAgent() {
        if (profiles.contains(Profile.MINIMAL) || profiles.contains(Profile.STANDARD)) {
            return new String[0];
        }
        return bean.getEdmAgent();
    }

    @Override
    public List<Map<String, String>> getEdmAgentLabel() {
        if (profiles.contains(Profile.MINIMAL) || profiles.contains(Profile.STANDARD)) {
            return Collections.emptyList();
        }
        return bean.getEdmAgentLabel();
    }

    @Override
    public Map<String, List<String>> getEdmAgentLabelLangAware() {
        if (profiles.contains(Profile.MINIMAL) || profiles.contains(Profile.STANDARD)) {
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
        if (profiles.contains(Profile.MINIMAL) || profiles.contains(Profile.STANDARD)) {
            return new String[0];
        }
        return bean.getDcContributor();
    }

    @Override
    public Map<String, List<String>> getDcContributorLangAware() {
        if (profiles.contains(Profile.MINIMAL) || profiles.contains(Profile.STANDARD)) {
            return Collections.emptyMap();
        }
        return bean.getDcContributorLangAware();
    }

    @Override
    public Date getTimestamp() {
        if (profiles.contains(Profile.MINIMAL)) {
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
    @SuppressWarnings("squid:S2384") // no need to make copy of result as we generate on the fly
    private String[] getThumbnails() {
        if (thumbnails == null) {
            List<String> thumbs = new ArrayList<>();

            /// first try to generate from edmPreview
            String preview = getFirstNonEmptyString(bean.getEdmPreview());
            if (StringUtils.isNotEmpty(preview)) {
                thumbs.add(urlService.getThumbnailUrl(requestRoute, preview, getType()));
                LOG.debug("edmPreview {}, result = {}",  preview, thumbs.get(0));
            } else {
                // second try edmObject
                String object = getFirstNonEmptyString(bean.getEdmObject());
                if (StringUtils.isNotEmpty(object)) {
                    thumbs.add(urlService.getThumbnailUrl(requestRoute, object, getType()));
                    LOG.debug("edmObject {}, result = {}",  object, thumbs.get(0));
                } else {
                    String isShownBy = getFirstNonEmptyString(bean.getEdmIsShownBy());
                    if (StringUtils.isNotEmpty(isShownBy)) {
                        thumbs.add(urlService.getThumbnailUrl(requestRoute, isShownBy, getType()));
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
        return urlService.getRecordApi2Url(requestRoute, getId(), wskey);
    }

    /* January 2018: method potentially deprecated!?
       GUID is a field that was introduced years ago, but there isn't any documentation on it. It's unclear if it's still used */
    public String getGuid() {
        return LinkUtils.addCampaignCodes(urlService.getRecordPortalUrl(requestRoute, getId()), wskey);
    }

    @Override
    public String[] getEdmPreview() {
        return getThumbnails();
    }

    @Override
    public float getScore() {
        return bean.getScore();
    }

//    protected boolean isProfile(Profile p) {
//        return StringUtils.containsIgnoreCase(profile, p.getName());
//    }

    @Override
    public String[] getEdmIsShownAt() {
        if (ArrayUtils.isEmpty(bean.getEdmIsShownAt())) {
            return bean.getEdmIsShownAt();
        }
        List<String> isShownAtLinks = new ArrayList<>();
        for (String isShownAt : bean.getEdmIsShownAt()) {
            if (StringUtils.isBlank(bean.getEdmIsShownAt()[0])) {
                continue;
            }
            isShownAtLinks.add(isShownAt);
        }
        return isShownAtLinks.toArray(new String[0]);
    }

    @Override
    public Boolean getPreviewNoDistribute() {
        if (profiles.contains(Profile.MINIMAL)) {
            return null; // if we return null the field won't be included in the json result
        }
        return bean.getPreviewNoDistribute() != null ? bean.getPreviewNoDistribute() : Boolean.FALSE;
    }

    @Override
    public Map<String, List<String>> getDcTitleLangAware() {
        return bean.getDcTitleLangAware();
    }


}
