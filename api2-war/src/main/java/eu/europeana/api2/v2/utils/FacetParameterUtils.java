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

    private static List<String> solrFacetList;
    private static List<String> technicalFacetList;

    /**
     * Returns all relevant parameters of a given type (right now: limit and offset)
     *
     * @param type                     Type of parameter. Currently we support "limit" and "offset"
     * @param facets                   List of facets
     * @param parameters               The request parameters
     * @param isDefaultFacetsRequested Whether or not the default facets should be checked
     */
    public static Map<String, Integer> getFacetParams(String type, String[] facets,
                                                      Map<String, String[]> parameters,
                                                      boolean isDefaultFacetsRequested) {
        createFacetLists();
        Map<String, Integer> facetParams = new HashMap<>();
        if (isDefaultFacetsRequested) {
            for (SolrFacetType facet : SolrFacetType.values()) {
                saveFacetParam(type, facet.name(), parameters, true, facetParams);
            }
        }

        if (ArrayUtils.isNotEmpty(facets)) {
            for (String facet : facets) {
                saveFacetParam(type, facet, parameters, solrFacetList.contains(facet), facetParams);
            }
        }
        return facetParams;
    }

    public static void createFacetLists() {
        if (solrFacetList == null) {
            solrFacetList = new ArrayList<>();
            for (SolrFacetType facet : SolrFacetType.values()) {
                solrFacetList.add(facet.toString());
            }
        }
        if (solrFacetList == null) {
            solrFacetList = new ArrayList<>();
            for (SolrFacetType facet : SolrFacetType.values()) {
                solrFacetList.add(facet.toString());
            }
            technicalFacetList = new ArrayList<>();
            for (TechnicalFacetType technicalFacet : TechnicalFacetType.values()) {
                technicalFacetList.add(technicalFacet.toString());
            }
        }
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
    public static void saveFacetParam(String type, String name, Map<String, String[]> parameters,
                                      boolean isDefault, Map<String, Integer> facetParams) {
        NumericFacetParameter parameter = null;
        if (type.equals("limit")) {
            parameter = getFacetLimit(name, parameters, isDefault);
        } else if (type.equals("offset")) {
            parameter = getFacetOffset(name, parameters, isDefault);
        }
        if (parameter != null) {
            facetParams.put(parameter.getName(), parameter.getValue());
        }
    }

    public static NumericFacetParameter getFacetLimit(String facet, Map<String, String[]> parameters, boolean isDefault) {
        String key = "f." + facet + ".facet.limit";
        Integer defaultLimit = isDefault ? (StringUtils.equals(facet, "DATA_PROVIDER") ? LIMIT_FOR_DATA_PROVIDER : LIMIT_FOR_DEFAULT) : LIMIT_FOR_CUSTOM;
        return extractParameter(key, DEFAULT_LIMIT_KEY, parameters, isDefault, defaultLimit);
    }

    public static NumericFacetParameter getFacetOffset(String facet, Map<String, String[]> parameters, boolean isDefault) {
        String key = "f." + facet + ".facet.offset";
        return extractParameter(key, DEFAULT_OFFSET_KEY, parameters, isDefault, null);
    }

    public static NumericFacetParameter extractParameter(String key, String defaultKey,
                                                         Map<String, String[]> parameters, boolean isDefault, Integer defaultValue) {

        if (parameters.containsKey(key)) {
            String[] value = parameters.get(key);
            return new NumericFacetParameter(key, value[0]);
        }
        if (isDefault && parameters.containsKey(defaultKey)) {
            String[] value = parameters.get(defaultKey);
            return new NumericFacetParameter(key, value[0]);
        }
        if (defaultValue != null) {
            return new NumericFacetParameter(key, defaultValue);
        }
        return null;
    }
}
