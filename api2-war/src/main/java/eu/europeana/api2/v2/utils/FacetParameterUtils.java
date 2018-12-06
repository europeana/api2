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

package eu.europeana.api2.v2.utils;

import eu.europeana.api2.v2.model.StringFacetParameter;
import eu.europeana.api2.v2.model.NumericFacetParameter;
import eu.europeana.corelib.definitions.solr.SolrFacetType;
import eu.europeana.corelib.definitions.solr.TechnicalFacetType;
import eu.europeana.corelib.definitions.solr.RangeFacetType;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;


import java.util.*;

/**
 * Utility class for extracting numeric parameters specific to a given facet.
 *
 * @author Peter.Kiraly@kb.nl / Lúthien
 */
public class FacetParameterUtils {

    private static final String DEFAULT_LIMIT_KEY = "f.DEFAULT.facet.limit";
    private static final String DEFAULT_OFFSET_KEY = "f.DEFAULT.facet.offset";
    private static final int LIMIT_FOR_DATA_PROVIDER = 50;
    private static final int LIMIT_FOR_DEFAULT = 50;
    private static final int LIMIT_FOR_CUSTOM = 50;
    private static final int LIMIT_FOR_TECH_DEFAULT = 50;
    private static final int LIMIT_FOR_TECH_CUSTOM = 50;

    private static final String FACET_RANGE = "facet.range";
    private static final String FACET_RANGE_START = "facet.range.start";
    private static final String FACET_RANGE_END = "facet.range.end";
    private static final String FACET_RANGE_GAP = "facet.range.gap";
    private static final String FACET_MINCOUNT = "facet.mincount";

    private static List<String> defaultSolrFacetList;
    private static List<String> rangeFacetList;

    static {
        defaultSolrFacetList = new ArrayList<>();
        rangeFacetList = new ArrayList<>();
        for (SolrFacetType facet : SolrFacetType.values()) defaultSolrFacetList.add(facet.toString());
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
    public static Map<String, String> getDateRangeParams(Map<String, String[]> parameters) {
        Map<String, String> dateRangeParams = new HashMap<>();
        List<String> facetsToRange = new ArrayList<>();

        // retrieve the facets that need to be ranged
        if (parameters.containsKey(FACET_RANGE)){
            Collections.addAll(facetsToRange, StringUtils.stripAll(
                    StringUtils.split(parameters.get(FACET_RANGE)[0], ',')));
        } else {
            return null;
        }

        // next: - loop through facetsToRange
        // - check whether they occur in the rangeFacetList
        // - find the start, end & gap parameters, either global or per field
        // - apply defaults?
        // construct Query parameters with them

        for (String facetToRange : facetsToRange) {
            if (rangeFacetList.contains(facetToRange))

        }

        for (SolrFacetType solrFacet : SolrFacetType.values()) {

        }
//        for (String dateRangeFacet : dateRangeFacets) {
//            saveStringFacetParam(dateRangeFacet, parameters, dateRangeParams);
//        }
        return dateRangeParams;
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

//    private static void saveStringFacetParam(String name, Map<String, String[]> parameters, Map<String, String> facetParams) {
//        StringFacetParameter parameter = null;
//        if (type.equals("limit")) parameter = getFacetLimit(name, parameters, isDefault, isTech);
//        else if (type.equals("offset")) parameter = getFacetOffset(name, parameters, isDefault);
//        if (parameter != null) facetParams.put(parameter.getName(), parameter.getValue());
//
//    }
//
//    private static StringFacetParameter extractStringParameter(String key, Map<String, String[]> parameters){
//        if (parameters.containsKey(key)) {
//            String[] value = parameters.get(key);
//            return new StringFacetParameter(key, value[0]);
//        }
//    }

    public static String getLimitForDataProvider(){
        return String.valueOf(LIMIT_FOR_DATA_PROVIDER);
    }
}
