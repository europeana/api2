package eu.europeana.api2.v2.model.json.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import eu.europeana.corelib.definitions.edm.beans.RichBean;

public class RichView extends ApiView implements RichBean {

    private String[] isShownBy;
    private String[] dcDescription;
    private String[] edmLandingPage;
    private Map<String, List<String>> dcDescriptionLangAware;
    private Map<String, List<String>> dcSubjectLangAware;
    private Map<String, List<String>> dcTypeLangAware;

    public RichView(RichBean bean, String profile, String wskey, long uid,
                    boolean optOut) {
        super(bean, profile, wskey, uid, optOut);
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
        // String provider = getProvider()[0];
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

    public String getAttributionSnippet(){
        return getAttributionSnippet(true, true);
    }

    public String getAttributionSnippet(boolean htmlOut){
        return getAttributionSnippet(true, htmlOut);
    }

    public String getAttributionSnippet(boolean firstOnly, boolean htmlOut){
        String rightsPage = "rel=\"xhv:license http://www.europeana.eu/schemas/edm/rights\"";
        String rightsLabel = "rightslabel";
        String aHref = "<a href=\"";
        String zHref = "\">";
        String hRefa = "</a>";
        String retval = "", landingPage, title = "", creator = "", dataProvider = "", shownAt, rights = "";
        int i, j;

        landingPage = (!ArrayUtils.isEmpty(getEdmLandingPage()) ? getEdmLandingPage()[0] : (!"".equals(getGuid()) ? getGuid() : ""));

        if (!ArrayUtils.isEmpty(getTitle())) {
            j = getTitle().length;
            for (i = 0; i < j; i++) {
                title += getTitle()[i];
                if (firstOnly) {
                    i = j;
                } else if (i < (j - 1)) {
                    title += "; ";
                }
            }
        }

        if (!ArrayUtils.isEmpty(getDcCreator())){
            j = getDcCreator().length;
            for (i = 0; i < j; i++){
                creator += getDcCreator()[i];
                if (firstOnly){
                    i = j;
                } else if (i < (j - 1)){
                    creator += "; ";
                }
            }
            creator += ". ";
        }

        shownAt = !ArrayUtils.isEmpty(getEdmIsShownAt()) ? getEdmIsShownAt()[0] : "";
        if (!ArrayUtils.isEmpty(getDataProvider())) {
            j = getDataProvider().length;
            for (i = 0; i < j; i++) {
                dataProvider += getDataProvider()[i];
                if (firstOnly) {
                    i = j;
                } else if (i < (j - 1)) {
                    dataProvider += "; ";
                }
            }
        }

        if (!ArrayUtils.isEmpty(getRights())){
            rights = getRights()[0];
        }

        if (htmlOut){
            if (!"".equals(title)){
                if (!"".equals(landingPage)) {
                    retval += aHref + landingPage + zHref;
                }
                retval += title;
                if (!"".equals(landingPage)) {
                    retval += hRefa;
                }
                retval += ". ";
            }
            retval += !"".equals(creator) ? creator + ". " : "";

            if (!"".equals(dataProvider)){
                if (!"".equals(shownAt)) {
                    retval += aHref + shownAt + zHref;
                }
                retval += dataProvider;
                if (!"".equals(shownAt)) {
                    retval += hRefa;
                }
                retval += ". ";
            }
            if (!"".equals(rights)){
                retval += aHref + rights + "\" " + rightsPage + ">" + rightsLabel + hRefa + ".";
            }
            return retval;
        } else {
            retval += title;
            retval += (!"".equals(title) && !"".equals(landingPage)) ? " - " : "";
            retval += landingPage;
            retval += (!"".equals(title) || !"".equals(landingPage)) ? ". " : "";
            retval += !"".equals(creator) ? creator + ". " : "";
            retval += dataProvider;
            retval += (!"".equals(dataProvider) && !"".equals(shownAt)) ? " - " : "";
            retval += shownAt;
            retval += (!"".equals(dataProvider) || !"".equals(shownAt)) ? ". " : "";
            retval += !"".equals(rights) ? rightsLabel + " - " + rights + "." : "";
            return retval;
        }
    }
}
