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

import eu.europeana.api2.v2.model.NumericFacetParameter;
import eu.europeana.corelib.definitions.solr.SolrFacetType;
import eu.europeana.corelib.definitions.solr.TechnicalFacetType;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for extracting numeric parameters specific to a given facet.
 *
 * @author Peter.Kiraly@kb.nl
 */
public class FacetParameterUtils {

    final static String DEFAULT_LIMIT_KEY = "f.DEFAULT.facet.limit";
    final static String DEFAULT_OFFSET_KEY = "f.DEFAULT.facet.offset";
    final static int LIMIT_FOR_DATA_PROVIDER = 3000;
    final static int LIMIT_FOR_DEFAULT = 750;
    final static int LIMIT_FOR_CUSTOM = 50;
    final static int LIMIT_FOR_TECH_DEFAULT = 750;
    final static int LIMIT_FOR_TECH_CUSTOM = 50;

    private static List<String> defaultSolrFacetList;

    static{
        if (defaultSolrFacetList == null) {
            defaultSolrFacetList = new ArrayList<>();
            for (SolrFacetType facet : SolrFacetType.values()) defaultSolrFacetList.add(facet.toString());
        }
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
            for (SolrFacetType solrFacet : SolrFacetType.values()) saveFacetParam(type, solrFacet.name(), parameters, true, false, solrFacetParams);
        }

        if (ArrayUtils.isNotEmpty(solrFacets)) {
            for (String solrFacetName : solrFacets) {
                if (!(defaultFacetsRequested && defaultSolrFacetList.contains(solrFacetName))) { // no duplicate DEFAULT facets
                    saveFacetParam(type, solrFacetName, parameters, defaultSolrFacetList.contains(solrFacetName), false, solrFacetParams);
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
            for (TechnicalFacetType technicalFacet : TechnicalFacetType.values()) saveFacetParam(type, technicalFacet.name(), parameters, true, true, technicalFacetParams);
        } else if (ArrayUtils.isNotEmpty(technicalFacets)) {
            for (String technicalFacetName : technicalFacets) saveFacetParam(type, technicalFacetName, parameters, false, true, technicalFacetParams);
        }
        return technicalFacetParams;
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
    private static void saveFacetParam(String type, String name, Map<String, String[]> parameters,
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
        return extractParameter(key, DEFAULT_LIMIT_KEY, parameters, isDefault, defaultLimit);
    }

    private static NumericFacetParameter getFacetOffset(String facet, Map<String, String[]> parameters,
                                                        boolean isDefault) {
        String key = "f." + facet + ".facet.offset";
        return extractParameter(key, DEFAULT_OFFSET_KEY, parameters, isDefault, null);
    }

    private static NumericFacetParameter extractParameter(String key, String defaultKey, Map<String, String[]> parameters,
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
}
