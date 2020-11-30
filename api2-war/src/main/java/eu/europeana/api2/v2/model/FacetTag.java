package eu.europeana.api2.v2.model;

import eu.europeana.corelib.definitions.solr.TechnicalFacetType;
import org.apache.commons.lang3.StringUtils;

/**
 * Data object to simplify working with the Metis.indexing solr tag library
 *
 * @author luthien
 */


public class FacetTag {

    private String name;
    private String label;

    public FacetTag(String name, String label) {
        this.name = name;
        this.label = label;
    }

    public String getName() {
        if (name != null) {
            return name;
        } else {
            return "";
        }
    }

    public String getLabel() {
        if ((StringUtils.isNotBlank(label)) && StringUtils.equals(name, TechnicalFacetType.COLOURPALETTE.getRealName())) {
            return translateMetisTerms(label);
        } else if (StringUtils.isNotBlank(label)) {
            return translateMetisTerms(StringUtils.lowerCase(label));
        } else {
            return "";
        }
    }

    private String translateMetisTerms(String metisTerm) {
        switch (metisTerm) {
            case "tiny":
                return "very_short";
            case "huge":
                return "extra_large";
            case "high":
            case "color":
                return "true";
            case "grayscale":
            case "other":
                return "false";
            default:
                return metisTerm;
        }
    }
}
