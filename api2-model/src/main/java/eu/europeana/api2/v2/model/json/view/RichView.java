package eu.europeana.api2.v2.model.json.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import eu.europeana.corelib.definitions.edm.beans.RichBean;
import org.codehaus.jackson.annotate.JsonPropertyOrder;

/**
 * A RichView defines the fields that are returned in search results when using the 'rich' profile
 */
@JsonPropertyOrder(alphabetic=true)
public class RichView extends ApiView implements RichBean {

    private RichBean bean;
    private boolean  hasDebugProfile = false;

    public RichView(RichBean bean, String profile, String wskey) {
        super(bean, profile, wskey);
        this.bean = bean;
        this.profile = profile;
        if (StringUtils.containsIgnoreCase(profile, "debug")) {
            hasDebugProfile = true;
        }
    }

    @Override
    public String[] getEdmIsShownBy() {
        if (ArrayUtils.isEmpty(bean.getEdmIsShownBy())) {
            return new String[0];
        }
        List<String> isShownByLinks = new ArrayList<>();
        for (String item : bean.getEdmIsShownBy()) {
            if (StringUtils.isBlank(item)) {
                continue;
            }
            isShownByLinks.add(item);
        }
        return isShownByLinks.toArray(new String[0]);
    }

    @Override
    public String[] getEdmLandingPage() {
        return bean.getEdmLandingPage();
    }

    @Override
    public Map<String, List<String>> getDcTypeLangAware() {
        return bean.getDcTypeLangAware();
    }

    @Override
    public Map<String, List<String>> getDcSubjectLangAware() {
        return bean.getDcSubjectLangAware();
    }


    // temporary added the below for debugging purposes (see EA-1395)

    @Override
    public List<Map<String, String>> getFulltext() {
        return hasDebugProfile ? bean.getFulltext() : Collections.emptyList();
    }

    @Override
    public Map<String, List<String>> getFulltextLangAware() {
        return hasDebugProfile ? bean.getFulltextLangAware() : Collections.emptyMap();
    }
}
