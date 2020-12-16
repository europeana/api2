package eu.europeana.api2.v2.model.json.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import eu.europeana.corelib.definitions.edm.beans.RichBean;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * A RichView defines the fields that are returned in search results when using the 'rich' profile
 */
@JsonPropertyOrder(alphabetic=true)
public class RichView extends ApiView implements RichBean {

    private String[] isShownBy;
    private String[] edmLandingPage;
    private Map<String, List<String>> dcSubjectLangAware;
    private Map<String, List<String>> dcTypeLangAware;

    // temporary added for debugging purposes (see EA-1395)
    private List<Map<String, String>> fulltext;
    // temporary added for debugging purposes (see EA-1395)
    private Map<String, List<String>> fulltextLangAware;

    public RichView(RichBean bean, String profile, String wskey, String requestRoute) {
        super(bean, profile, wskey, requestRoute);
        isShownBy = bean.getEdmIsShownBy();
        edmLandingPage = bean.getEdmLandingPage();
        dcTypeLangAware = bean.getDcTypeLangAware();
        dcSubjectLangAware = bean.getDcSubjectLangAware();

        if (StringUtils.containsIgnoreCase(profile, "debug")) {
            fulltext = bean.getFulltext();
            fulltextLangAware = bean.getFulltextLangAware();
        }
    }

    @Override
    public String[] getEdmIsShownBy() {
        if (ArrayUtils.isEmpty(isShownBy)) {
            return isShownBy;
        }
        List<String> isShownByLinks = new ArrayList<>();
        for (String item : isShownBy) {
            if (StringUtils.isBlank(item)) {
                continue;
            }
            isShownByLinks.add(item);
        }
        return isShownByLinks.toArray(new String[0]);
    }

    @Override
    public String[] getEdmLandingPage() {
        return edmLandingPage;
    }

    @Override
    public Map<String, List<String>> getDcTypeLangAware() {
        return dcTypeLangAware;
    }

    @Override
    public Map<String, List<String>> getDcSubjectLangAware() {
        return dcSubjectLangAware;
    }

    @Override
    // temporary added for debugging purposes (see EA-1395)
    public List<Map<String, String>> getFulltext() {
        return fulltext;
    }

    @Override
    // temporary added for debugging purposes (see EA-1395)
    public Map<String, List<String>> getFulltextLangAware() {
        return fulltextLangAware;
    }
}
