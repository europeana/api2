package eu.europeana.api2.v2.model.json.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import eu.europeana.corelib.definitions.edm.beans.RichBean;
import org.codehaus.jackson.annotate.JsonPropertyOrder;

@JsonPropertyOrder(alphabetic=true)
public class RichView extends ApiView implements RichBean {

    private String[] isShownBy;
    private String[] dcDescription;
    private String[] edmLandingPage;
    private Map<String, List<String>> dcDescriptionLangAware;
    private Map<String, List<String>> dcSubjectLangAware;
    private Map<String, List<String>> dcTypeLangAware;

    public RichView(RichBean bean, String profile, String wskey) {
        super(bean, profile, wskey);
        dcDescription = bean.getDcDescription();
        isShownBy = bean.getEdmIsShownBy();
        edmLandingPage = bean.getEdmLandingPage();
        dcTypeLangAware = bean.getDcTypeLangAware();
        dcSubjectLangAware = bean.getDcSubjectLangAware();
        dcDescriptionLangAware = bean.getDcDescriptionLangAware();
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
        return isShownByLinks.toArray(new String[isShownByLinks.size()]);
    }

    @Override
    public String[] getDcDescription() {
        return dcDescription;
    }

    @Override
    public String[] getEdmLandingPage() {
        return edmLandingPage;
    }

    @Override
    public Map<String, List<String>> getDcDescriptionLangAware() {
        return dcDescriptionLangAware;
    }

    @Override
    public Map<String, List<String>> getDcTypeLangAware() {
        return dcTypeLangAware;
    }

    @Override
    public Map<String, List<String>> getDcSubjectLangAware() {
        return dcSubjectLangAware;
    }
}
