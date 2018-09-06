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

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.annotation.JsonInclude;

import eu.europeana.api2.model.utils.LinkUtils;
import eu.europeana.api2.v2.model.enums.Profile;
import eu.europeana.corelib.definitions.edm.beans.BriefBean;
import eu.europeana.corelib.definitions.solr.DocType;
import eu.europeana.corelib.solr.bean.impl.IdBeanImpl;
import eu.europeana.corelib.web.service.EuropeanaUrlService;
import eu.europeana.corelib.web.service.impl.EuropeanaUrlServiceImpl;
import org.codehaus.jackson.annotate.JsonPropertyOrder;

@JsonInclude(NON_EMPTY)
@JsonPropertyOrder(alphabetic=true)
public class BriefView extends IdBeanImpl implements BriefBean {

    protected EuropeanaUrlService urlService;

    protected String profile;
    protected String wskey;
    protected BriefBean bean;
    private String[] thumbnails;

    public BriefView(BriefBean bean, String profile, String wskey) {
        this.bean = bean;
        this.profile = profile;
        this.wskey = wskey;
        urlService = EuropeanaUrlServiceImpl.getBeanInstance();
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
        return null;
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
            return null;
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
    public String[] getDcDescription() {
        return bean.getDcDescription();
    }

    @Override
    public String[] getDctermsSpatial() {
        if (isProfile(Profile.MINIMAL) || isProfile(Profile.STANDARD)) {
            return null;
        }
        return bean.getDctermsSpatial();
    }

    @Override
    public String[] getDcLanguage() {
        if (isProfile(Profile.MINIMAL)) {
            return null;
        }
        return bean.getDcLanguage();
    }

    @Override
    public Map<String, List<String>> getDcLanguageLangAware() {
        if (isProfile(Profile.MINIMAL)) {
            return null;
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
            return null;
        }
        return bean.getEdmPlace();
    }

    @Override
    public List<Map<String, String>> getEdmPlaceLabel() {
        if (isProfile(Profile.MINIMAL)) {
            return null;
        }
        return bean.getEdmPlaceLabel();
    }

    @Override
    public Map<String, List<String>> getEdmPlaceLabelLangAware() {
        if (isProfile(Profile.MINIMAL)) {
            return null;
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
            return null;
        }
        return bean.getEdmTimespan();
    }

    @Override
    public List<Map<String, String>> getEdmTimespanLabel() {
        if (isProfile(Profile.MINIMAL)) {
            return null;
        }
        return bean.getEdmTimespanLabel();
    }

    @Override
    public Map<String, List<String>> getEdmTimespanLabelLangAware() {
        if (isProfile(Profile.MINIMAL)) {
            return null;
        }
        return bean.getEdmTimespanLabelLangAware();
    }

    @Override
    public String[] getEdmTimespanBegin() {
        if (isProfile(Profile.MINIMAL)) {
            return null;
        }
        return bean.getEdmTimespanBegin();
    }

    @Override
    public String[] getEdmTimespanEnd() {
        if (isProfile(Profile.MINIMAL)) {
            return null;
        }
        return bean.getEdmTimespanEnd();
    }

    @Override
    public String[] getEdmAgent() {
        if (isProfile(Profile.MINIMAL) || isProfile(Profile.STANDARD)) {
            return null;
        }
        return bean.getEdmAgent();
    }

    @Override
    public List<Map<String, String>> getEdmAgentLabel() {
        if (isProfile(Profile.MINIMAL) || isProfile(Profile.STANDARD)) {
            return null;
        }
        return bean.getEdmAgentLabel();
    }

    @Override
    public Map<String, List<String>> getEdmAgentLabelLangAware() {
        if (isProfile(Profile.MINIMAL) || isProfile(Profile.STANDARD)) {
            return null;
        }
        return bean.getEdmAgentLabelLangAware();
    }

    @Override
    public String[] getDctermsHasPart() {
        // bean.getDctermsHasPart()
        return null;
    }

    @Override
    public String[] getDcCreator() { return bean.getDcCreator(); }

    @Override
    public Map<String, List<String>> getDcCreatorLangAware() { return bean.getDcCreatorLangAware();
    }

    @Override
    public String[] getDcContributor() {
        if (isProfile(Profile.MINIMAL) || isProfile(Profile.STANDARD)) {
            return null;
        }
        return bean.getDcContributor();
    }

    @Override
    public Map<String, List<String>> getDcContributorLangAware() {
        if (isProfile(Profile.MINIMAL) || isProfile(Profile.STANDARD)) {
            return null;
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

    private String[] getThumbnails() {
        if (thumbnails == null) {
            List<String> thumbs = new ArrayList<>();

            if (bean.getEdmObject() != null) {
                for (String object : bean.getEdmObject()) {
                    String tn = StringUtils.defaultIfBlank(object, "");
                    final String url = urlService.getThumbnailUrl(tn, getType()).toString();
                    if (StringUtils.isNotBlank(url)) {
                        thumbs.add(url.trim());
                    }
                }
            }
            thumbnails = thumbs.toArray(new String[thumbs.size()]);
        }
        return thumbnails;
    }

    public String getLink() {
        return urlService.getApi2RecordJson(wskey, getId()).toString();
    }

    /* January 2018: method potentially deprecated!?
       GUID is a field that was introduced years ago, but there isn't any documentation on it. It's unclear if it's still used */
    public String getGuid() {
        return LinkUtils.addCampaignCodes(urlService.getPortalRecord(getId()), wskey);
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
            String isShownAtLink = urlService.getApi2Redirect(
                    wskey, isShownAt, provider, bean.getId(), profile)
                    .toString();

            isShownAtLinks.add(isShownAtLink);
        }
        return isShownAtLinks.toArray(new String[isShownAtLinks.size()]);
    }

    @Override
    public Boolean getPreviewNoDistribute() {
        if (isProfile(Profile.MINIMAL)) {
            return null; // if we return null the field won't be included in the json result
        }
        return bean.getPreviewNoDistribute() != null ? bean.getPreviewNoDistribute() : false;
    }

    @Override
    public Map<String, List<String>> getDcTitleLangAware() {
        return bean.getDcTitleLangAware();
    }


}
