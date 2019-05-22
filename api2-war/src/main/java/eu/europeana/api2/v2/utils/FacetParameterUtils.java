package eu.europeana.api2.v2.utils;

import eu.europeana.api2.v2.exceptions.DateMathParseException;
import eu.europeana.api2.v2.exceptions.InvalidGapException;
import eu.europeana.api2.v2.model.NumericFacetParameter;
import eu.europeana.corelib.definitions.solr.SolrFacetType;
import eu.europeana.corelib.definitions.solr.TechnicalFacetType;
import eu.europeana.corelib.definitions.solr.RangeFacetType;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Utility class for extracting numeric parameters specific to a given facet.
 *
 * @author Peter.Kiraly@kb.nl / Lúthien
 */
public class FacetParameterUtils {

    private static final String DEFAULT_LIMIT_KEY       = "f.DEFAULT.facet.limit";
    private static final String DEFAULT_OFFSET_KEY      = "f.DEFAULT.facet.offset";
    private static final int LIMIT_FOR_DATA_PROVIDER    = 50;
    private static final int LIMIT_FOR_DEFAULT          = 50;
    private static final int LIMIT_FOR_CUSTOM           = 50;
    private static final int LIMIT_FOR_TECH_DEFAULT     = 50;
    private static final int LIMIT_FOR_TECH_CUSTOM      = 50;

    private static final String FACET_RANGE             = "facet.range";
    private static final String FACET_RANGE_START       = "facet.range.start";
    private static final String FACET_RANGE_END         = "facet.range.end";
    private static final String FACET_RANGE_GAP         = "facet.range.gap";
    private static final String FACET_MINCOUNT          = "facet.mincount";

    private static final String DEFAULT_START           = "0001-01-01T00:00:00Z"; // year 0 does not exist
    private static final String NOW                     = "now";
    private static final String DEFAULT_GAP             = "+1YEAR";

    private static final long MAX_RANGE_FACETS          = 30000L;

    private static List<String> defaultSolrFacetList;
    private static List<String> rangeFacetList;
    private static List<String> rangeSpecifiers = Arrays.asList("start", "end", "gap");

    private static DateFormat solrDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    static {
        defaultSolrFacetList = new ArrayList<>();
        for (SolrFacetType facet : SolrFacetType.values()) defaultSolrFacetList.add(facet.toString());
        rangeFacetList = new ArrayList<>();
        for (RangeFacetType facet : RangeFacetType.values()) rangeFacetList.add(facet.toString());
    }

    /**
     * Returns all relevant parameters of a given type (right now: limit and offset)
     *
     * @param type                   Type of parameter. Currently we support "limit" and "offset"
     * @param solrFacets             List of Solr facets
     * @param parameters             The request parameters
     * @param defaultFacetsRequested Whether or not the default facets should be checked
     */
    public static Map<String, Integer> getSolrFacetParams(String type, String[] solrFacets,
                                                          Map<String, String[]> parameters,
                                                          boolean defaultFacetsRequested) {
        Map<String, Integer> solrFacetParams = new HashMap<>();
        if (defaultFacetsRequested) {
            for (SolrFacetType solrFacet : SolrFacetType.values()) {
                saveNumericFacetParam(type, solrFacet.name(), parameters, true, false, solrFacetParams);
            }
        }

        if (ArrayUtils.isNotEmpty(solrFacets)) {
            for (String solrFacetName : solrFacets) {
                if (!(defaultFacetsRequested && defaultSolrFacetList.contains(solrFacetName))) { // no duplicate DEFAULT facets
                    saveNumericFacetParam(type, solrFacetName, parameters, defaultSolrFacetList.contains(solrFacetName), false, solrFacetParams);
                }
            }
        }
        return solrFacetParams;
    }
    /**
     * Returns all relevant parameters of a given type (right now: limit and offset)
     *
     * @param type                   Type of parameter. Currently we support "limit" and "offset"
     * @param technicalFacets        List of technical metadata facets
     * @param parameters             The request parameters
     * @param defaultFacetsRequested Whether or not the all technical metadata facets should be checked
     */
    public static Map<String, Integer> getTechnicalFacetParams(String type, String[] technicalFacets, Map<String,
            String[]> parameters, boolean defaultFacetsRequested) {
        Map<String, Integer> technicalFacetParams = new HashMap<>();
        if (defaultFacetsRequested) {
            for (TechnicalFacetType technicalFacet : TechnicalFacetType.values()){
                saveNumericFacetParam(type, technicalFacet.name(), parameters, true, true, technicalFacetParams);
            }
        } else if (ArrayUtils.isNotEmpty(technicalFacets)) {
            for (String technicalFacetName : technicalFacets) {
                saveNumericFacetParam(type, technicalFacetName, parameters, false, true, technicalFacetParams);
            }
        }
        return technicalFacetParams;
    }

    // NOTE that there can be more than one facet range parameter for every field, eg:
    // facet.range=timestamp & &facet.range.start=0000-01-01T00:00:00Z & &facet.range.end=NOW & facet.range.gap=+1DAY
    public static Map<String, String> getDateRangeParams(Map<String, String[]> parameters) throws DateMathParseException,
                                                                                                  InvalidGapException {
        Map<String, String> dateRangeParams = new HashMap<>();

        // first, retrieve & validate field values from comma-separated facet.range parameter
        List<String> facetsToRange = retrieveRangeFacetList(parameters, FACET_RANGE);
        StringBuilder facetRangeValue = new StringBuilder();
        for (String facetToRange : facetsToRange) {
            facetRangeValue.append(facetToRange);
            facetRangeValue.append(",");
        }
        if (!facetsToRange.isEmpty()){
            dateRangeParams.put(FACET_RANGE, StringUtils.chop(facetRangeValue.toString()));
        }

        // and last, process the field-specific start, end & gap parameters
        for (String facetToRange : facetsToRange){
            for (String rangeSpecifier : rangeSpecifiers){
                String globalSpecifier = FACET_RANGE + "." + rangeSpecifier;
                String fieldSpecifier = "f." + facetToRange + "." + FACET_RANGE + "." + rangeSpecifier;
                if (parameters.containsKey(globalSpecifier)){
                    dateRangeParams.put(fieldSpecifier, plusificate(parameters.get(globalSpecifier)[0], rangeSpecifier));
                } else if (parameters.containsKey(fieldSpecifier)){
                    dateRangeParams.put(fieldSpecifier, plusificate(parameters.get(fieldSpecifier)[0], rangeSpecifier));
                } else {
                    dateRangeParams.put(fieldSpecifier, getDefaultValue(rangeSpecifier));
                }
            }
            DateMathParser.exceedsMaxNrOfGaps(
                        dateRangeParams.get("f." + facetToRange + "." + FACET_RANGE + ".start"),
                        dateRangeParams.get("f." + facetToRange + "." + FACET_RANGE + ".end"),
                        dateRangeParams.get("f." + facetToRange + "." + FACET_RANGE + ".gap"),
                        MAX_RANGE_FACETS);


        }
        return dateRangeParams;
    }

    private static String getDefaultValue(String rangeSpecifier){
        String defaultValue;
        switch(rangeSpecifier){
            case "start":
                defaultValue = DEFAULT_START;
                break;
            case "end":
                defaultValue = thisVeryMoment();
                break;
            case "gap":
                defaultValue = DEFAULT_GAP;
                break;
            default:
                defaultValue = "";
        }
        return defaultValue;
    }

    private static String plusificate(String plusMinusNow, String rangeSpecifier){
        if (StringUtils.equalsIgnoreCase(rangeSpecifier, "gap") &&
            !StringUtils.startsWithAny(plusMinusNow, "+", "-")){
            return "+" + plusMinusNow;
        } else if (StringUtils.equalsIgnoreCase(NOW, plusMinusNow)){
            return thisVeryMoment();
        } else {
            return plusMinusNow;
        }
    }

    private static String thisVeryMoment(){
        return solrDateFormat.format(new Date());
    }

    // retrieve the facets that need to be ranged, but check them against the values in Enum RangeFacetType
    private static List<String> retrieveRangeFacetList(Map<String, String[]> parameters, String pattern){
        List<String> facetsToRangeTemp = new ArrayList<>();
        List<String> facetsToRange = new ArrayList<>();
        if (parameters.containsKey(pattern)){
            Collections.addAll(facetsToRangeTemp, StringUtils.stripAll(
                    StringUtils.split(parameters.get(pattern)[0], ',')));
            for (String facetToRangeTemp : facetsToRangeTemp) {
                if (rangeFacetList.contains(facetToRangeTemp)){
                    facetsToRange.add(facetToRangeTemp);
                }
            }
        }
        return facetsToRange;
    }


    /**
     * Extracts and saves parameter of a given type (right now: limit and offset) belongs to a facet
     *
     * @param type        The type of parameter (limit or offset)
     * @param name        The name of the facet
     * @param parameters  Request parameters
     * @param isDefault   The facet is a default facet
     * @param facetParams The container to save into
     */
    private static void saveNumericFacetParam(String type, String name, Map<String, String[]> parameters,
                                              boolean isDefault, boolean isTech, Map<String, Integer> facetParams) {
        NumericFacetParameter parameter = null;
        if (type.equals("limit")) parameter = getFacetLimit(name, parameters, isDefault, isTech);
        else if (type.equals("offset")) parameter = getFacetOffset(name, parameters, isDefault);
        if (parameter != null) facetParams.put(parameter.getName(), parameter.getValue());

    }

    private static NumericFacetParameter getFacetLimit(String facet, Map<String, String[]> parameters,
                                                       boolean isDefault, boolean isTech) {
        String key = "f." + facet + ".facet.limit";
        Integer defaultLimit;
        if (isTech) defaultLimit = isDefault ? LIMIT_FOR_TECH_DEFAULT : LIMIT_FOR_TECH_CUSTOM;
        else defaultLimit = isDefault ? (StringUtils.equals(facet, "DATA_PROVIDER") ? LIMIT_FOR_DATA_PROVIDER : LIMIT_FOR_DEFAULT) : LIMIT_FOR_CUSTOM;
        return extractNumericParameter(key, DEFAULT_LIMIT_KEY, parameters, isDefault, defaultLimit);
    }

    private static NumericFacetParameter getFacetOffset(String facet, Map<String, String[]> parameters,
                                                        boolean isDefault) {
        String key = "f." + facet + ".facet.offset";
        return extractNumericParameter(key, DEFAULT_OFFSET_KEY, parameters, isDefault, null);
    }

    private static NumericFacetParameter extractNumericParameter(String key, String defaultKey, Map<String, String[]> parameters,
                                                                 boolean isDefault, Integer defaultValue) {
        if (parameters.containsKey(key)) {
            String[] value = parameters.get(key);
            return new NumericFacetParameter(key, value[0]);
        }
        if (isDefault && parameters.containsKey(defaultKey)) {
            String[] value = parameters.get(defaultKey);
            return new NumericFacetParameter(key, value[0]);
        }
        if (defaultValue != null) return new NumericFacetParameter(key, defaultValue);
        return null;
    }

    public static String getLimitForDataProvider(){
        return String.valueOf(LIMIT_FOR_DATA_PROVIDER);
    }
}
