/*
 * Copyright 2007-2016 The Europeana Foundation
 *
 *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 *  by the European Commission;
 *  You may not use this work except in compliance with the Licence.
 *
 *  You may obtain a copy of the Licence at:
 *  http://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under
 *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 *  any kind, either express or implied.
 *  See the Licence for the specific language governing permissions and limitations under
 *  the Licence.
 */

package eu.europeana.api2.v2.service;

import eu.europeana.api2.v2.model.json.common.LabelFrequency;
import eu.europeana.api2.v2.model.json.view.submodel.Facet;
import eu.europeana.corelib.definitions.solr.TechnicalFacetType;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.log4j.Logger;
import java.util.*;

import static eu.europeana.api2.v2.utils.ModelUtils.decodeFacetTag;

/**
 * Created by luthien on 20/04/2016.
 */

public class FacetWrangler {
    private static Logger log = Logger.getLogger(FacetWrangler.class);

    private Map<TechnicalFacetType, Map<String, Integer>> technicalFacetMap = new HashMap<>();

    public FacetWrangler() {
        for (final TechnicalFacetType technicalFacet : TechnicalFacetType.values()) {
            technicalFacetMap.put(technicalFacet, new LinkedHashMap<>()); // LinkedHashMap to preserve ordering
        }
    }

    /**
     * Consolidates the regular and technical metadata facets.
     * The method loops through the List of FacetFields returned by Solr, extracts both the regular as the encoded
     * technical metadata facets, converts them to Facet objects, adds those to a List and returns that List.
     * @param facetFields the List of facetfields as returned by Solr
     * @return a List of Facet objects representing both the regular, and the technical metadata facets
     */
    public List<Facet> consolidateFacetList(List<FacetField> facetFields, List<String> requestedTechnicalFacets,
                                                   boolean defaultFacetsRequested, Map<String, Integer> technicalFacetLimits,
                                                   Map<String, Integer> technicalFacetOffsets) {
        if (facetFields == null || facetFields.isEmpty()) return null;
        final List<Facet>                                facetList          = new ArrayList<>();

        // loop through the list of Facet fields returned by Solr
        for (FacetField facetField : facetFields) {
            if (facetField.getValues() != null) {
                final Facet facet = new Facet();
                facet.name = facetField.getName();

                if (StringUtils.equalsIgnoreCase(TechnicalFacetType.IS_FULLTEXT.name(), facet.name)) {
                    facet.name = TechnicalFacetType.IS_FULLTEXT.getRealName();
                } else if (StringUtils.equalsIgnoreCase(TechnicalFacetType.HAS_MEDIA.name(), facet.name)) {
                    facet.name = TechnicalFacetType.HAS_MEDIA.getRealName();
                } else if (StringUtils.equalsIgnoreCase(TechnicalFacetType.HAS_THUMBNAILS.name(), facet.name)) {
                    facet.name = TechnicalFacetType.HAS_THUMBNAILS.getRealName();
                }

                /* If it ain't a Solr Facet field (name == facet_tags), it contains the technical metadata:
                 * - for every value of facet_tags, retrieve the human-readable label from the encoded integer value
                 * - match the technical Facet name against the enum TechnicalFacetType
                 * - values associated with the technical Facet are stored in the technicalFacetMap
                 * Note that technical metadata names are encoded numerically (See eu.europeana.crf_faketags) */
                if (facetField.getName().equalsIgnoreCase("facet_tags")) {
                    for (FacetField.Count encodedTechnicalFacet : facetField.getValues()) {
                        if (StringUtils.isNotEmpty(encodedTechnicalFacet.getName()) && encodedTechnicalFacet.getCount() > 0) {

                            final TechnicalFacetType technicalFacetName;
                            // decode the numerical facet name into a String. If null, proceed with the next value
                            final String decodedTechnicalFacetName = decodeFacetTag(Integer.valueOf(encodedTechnicalFacet.getName()), true);
                            if (decodedTechnicalFacetName.isEmpty()) continue;
                            try {
                                technicalFacetName = TechnicalFacetType.valueOf(decodedTechnicalFacetName);
                            } catch (IllegalArgumentException e) {
                                log.error("error matching decoded technical facet name " + decodedTechnicalFacetName + " with enum type in [consolidateFacetList] " + e.getClass().getSimpleName(), e);
                                continue;
                            }

                            // decode the numerical facet value label into a String. If null, proceed with the next value
                            final String technicalFacetLabel = decodeFacetTag(Integer.valueOf(encodedTechnicalFacet.getName()), false);
                            if (technicalFacetLabel.isEmpty()) continue;

                            // retrieve a possibly earlier stored count value for this label. If not available, initialise at 0L
                            // then add the count value to the Map for this particular label
                            Integer technicalFacetFieldCount = technicalFacetMap.get(technicalFacetName).get(technicalFacetLabel);
                            if (null == technicalFacetFieldCount) technicalFacetFieldCount = 0;
                            technicalFacetMap.get(technicalFacetName).put(technicalFacetLabel, technicalFacetFieldCount + (int) encodedTechnicalFacet.getCount());
                        }
                    }

                /* If it is a Solr facet field, loop through its values. If it is a Solr Facet field (name != facet_tags):
                 * - for every value of this Facet field, add a LabelFrequency containing the Facet's name and count */
                } else {
                    for (FacetField.Count count : facetField.getValues()) {
                        if (StringUtils.isNotEmpty(count.getName()) && count.getCount() > 0) {
                            final LabelFrequency facetValue = new LabelFrequency();
                            facetValue.count = count.getCount();
                            facetValue.label = count.getName();
                            facet.fields.add(facetValue);
                            continue;
                        }
                    }
                    // If the Solr facet contains values, it is added to the return Facet List
                    if (!facet.fields.isEmpty()) facetList.add(facet);
                }
            }
        }
        // loop through the array of requested technical facets; match them to the Enum type; set the requested
        // limit / offset range and retrieve the technical facet values that were stored in technicalFacetMap above.
        // Then add the new Facet to the facetlist
        for (String requestedFacetName : requestedTechnicalFacets){
            TechnicalFacetType matchedFacetName;
            try {
                matchedFacetName = TechnicalFacetType.valueOf(requestedFacetName);
            } catch (IllegalArgumentException e) {
                log.error("error matching requested technical facet name " + requestedFacetName + " with enum type in [consolidateFacetList] " + e.getClass().getSimpleName(), e);
                continue;
            }
            String facetLimit = "f." + matchedFacetName + ".facet.limit";
            String facetOffset = "f." + matchedFacetName + ".facet.offset";
            if (technicalFacetMap.get(matchedFacetName).isEmpty()) continue;
            int from = Math.min(((null != technicalFacetOffsets && technicalFacetOffsets.containsKey(facetOffset)) ?
                    technicalFacetOffsets.get(facetOffset) : 0), technicalFacetMap.get(matchedFacetName).size() - 1);
            int to = Math.min(((null != technicalFacetLimits && technicalFacetLimits.containsKey(facetLimit)) ?
                            technicalFacetLimits.get(facetLimit) + from : technicalFacetMap.get(matchedFacetName).size() - 1),
                    technicalFacetMap.get(matchedFacetName).size() - 1);

            final Facet facet = new Facet();
            facet.name = matchedFacetName.getRealName();
            if ( null != requestedTechnicalFacets               &&
                    !requestedTechnicalFacets.contains(facet.name)  &&
                    !defaultFacetsRequested) continue;

            List<String> keyList = new ArrayList<>(technicalFacetMap.get(matchedFacetName).keySet());
            for (int i = from; i < to; i++){
                final LabelFrequency freq = new LabelFrequency();
                freq.label = keyList.get(i);
                freq.count = technicalFacetMap.get(matchedFacetName).get(freq.label);
                facet.fields.add(freq);
            }
            facetList.add(facet);
        }

        // sort the List of Facets on #count, descending
        facetList.sort((f1, f2) -> Integer.compare(f2.fields.size(), f1.fields.size()));
        return facetList;
    }



}
