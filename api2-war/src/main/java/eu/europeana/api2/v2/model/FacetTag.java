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

    /**
     * @return facet name
     */
    public String getName() {
        if (name == null) {
            return "";
        } else {
            return name;
        }
    }

    /**
     * @return facet value
     */
    public String getLabel() {
        if (StringUtils.isBlank(label)) {
            return "";
        }
        // make sure we always output colour palette values in uppercase
        if (StringUtils.equals(name, TechnicalFacetType.COLOURPALETTE.getRealName())) {
            return translateMetisTerms(StringUtils.upperCase(label));
        }
        // all other values should be lowercase
        return translateMetisTerms(StringUtils.lowerCase(label));
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
