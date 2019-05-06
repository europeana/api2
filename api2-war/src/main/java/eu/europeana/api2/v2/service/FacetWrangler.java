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

import eu.europeana.api2.v2.exceptions.TechFacetDecodingException;
import eu.europeana.api2.v2.model.json.common.LabelFrequency;
import eu.europeana.api2.v2.model.json.view.submodel.Facet;
import eu.europeana.api2.v2.model.json.view.submodel.FacetRanger;
import eu.europeana.corelib.definitions.solr.SolrFacetType;
import eu.europeana.corelib.definitions.solr.TechnicalFacetType;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.response.RangeFacet;
import org.apache.solr.client.solrj.response.RangeFacet.Count;

import java.util.*;

import static eu.europeana.api2.v2.utils.ModelUtils.decodeFacetTag;

/**
 * Created by luthien on 20/04/2016.
 */

public class FacetWrangler {
    private static Logger log = Logger.getLogger(FacetWrangler.class);

    private Map<TechnicalFacetType, Map<String, Integer>> technicalFacetMap = new EnumMap<>(TechnicalFacetType.class);

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
    public List<Facet> consolidateFacetList(List<FacetField> facetFields,
                                            List<RangeFacet> rangeFacets,
                                            List<String> requestedTechnicalFacets,
                                            boolean defaultFacetsRequested,
                                            Map<String, Integer> technicalFacetLimits,
                                            Map<String, Integer> technicalFacetOffsets) {
        if ((facetFields == null || facetFields.isEmpty()) &&
                (rangeFacets == null || rangeFacets.isEmpty())) return Collections.emptyList();
        final List<Facet> facetList = new ArrayList<>();

        // loop through the list of Facet fields returned by Solr
        if (null != facetFields){
            processFacetFields(facetFields, facetList);
        }

        // loop through the array of requested technical facets, add the new Facet to the facetlist
        if (null != requestedTechnicalFacets && !requestedTechnicalFacets.isEmpty()){
            processTechnicalFacets(requestedTechnicalFacets, defaultFacetsRequested,
                                   technicalFacetLimits, technicalFacetOffsets, facetList);
        }

        // add RangeFacets when available
        if (rangeFacets != null && !rangeFacets.isEmpty()) {
            processRangeFacets(rangeFacets, facetList);
        }

        // sort the List of Facets on #count, descending
        facetList.sort((f1, f2) -> Integer.compare(f2.fields.size(), f1.fields.size()));
        return facetList;
    }

    private void processFacetFields(List<FacetField> facetFields, List<Facet> facetList){
        for (FacetField facetField : facetFields) {
            if (!facetField.getValues().isEmpty()) {
                final Facet facet = new Facet();
                facet.name = facetField.getName();

                if (StringUtils.equalsIgnoreCase(SolrFacetType.TEXT_FULLTEXT.toString(), facet.name)) {
                    facet.name = SolrFacetType.TEXT_FULLTEXT.name();
                } else if (StringUtils.equalsIgnoreCase(SolrFacetType.MEDIA.toString(), facet.name)) {
                    facet.name = SolrFacetType.MEDIA.name();
                } else if (StringUtils.equalsIgnoreCase(SolrFacetType.THUMBNAIL.toString(), facet.name)) {
                    facet.name = SolrFacetType.THUMBNAIL.name();
                } else if (StringUtils.equalsIgnoreCase(SolrFacetType.LANDINGPAGE.toString(), facet.name)) {
                    facet.name = SolrFacetType.LANDINGPAGE.name();
                }

                /* If the facet name is "facet_tags", the field contains the technical metadata.
                 * - for every value of facet_tags, retrieve the human-readable label from the encoded integer value
                 * - match the technical Facet name against the enum TechnicalFacetType
                 * - values associated with the technical Facet are stored in the technicalFacetMap
                 * Note that technical metadata names are encoded numerically (See eu.europeana.technicalfacets) */
                if (facetField.getName().equalsIgnoreCase("facet_tags")) {
                    for (FacetField.Count encodedTechnicalFacet : facetField.getValues()) {
                        if (StringUtils.isNotEmpty(encodedTechnicalFacet.getName())
                            && encodedTechnicalFacet.getCount() > 0) {

                            TechnicalFacetType technicalFacetName;
                            String decodedTechnicalFacetName = "";

                            try {
                                // decode the numerical facet name into a String. If null, proceed with the next value
                                decodedTechnicalFacetName = decodeFacetTag(
                                        Integer.valueOf(encodedTechnicalFacet.getName()), true);
                                if (decodedTechnicalFacetName.isEmpty()) {
                                    log.debug("Decoded technical Facet name is empty");
                                    continue;
                                }
                                technicalFacetName = TechnicalFacetType.valueOf(decodedTechnicalFacetName);

                                // decode the numerical facet value label into a String.
                                // If null, proceed with the next value
                                final String technicalFacetLabel = decodeFacetTag(
                                        Integer.valueOf(encodedTechnicalFacet.getName()), false);
                                if (technicalFacetLabel.isEmpty()) {
                                    log.debug("Decoded technical Facet label is empty");
                                    continue;
                                }

                                // retrieve a possibly earlier stored count value for this label. If not available,
                                // initialise at 0L; then add the count value to the Map for this particular label
                                Integer technicalFacetFieldCount = technicalFacetMap.get(technicalFacetName).get(technicalFacetLabel);
                                if (null == technicalFacetFieldCount) technicalFacetFieldCount = 0;
                                technicalFacetMap.get(technicalFacetName).put(technicalFacetLabel,
                                                                              technicalFacetFieldCount +
                                                                              (int) encodedTechnicalFacet.getCount());
                            } catch (IllegalArgumentException e) {
                                log.debug("error matching decoded technical facet name " +
                                          decodedTechnicalFacetName + " with enum type in [consolidateFacetList] "
                                          + e.getClass().getSimpleName(), e);
                            }
                        }
                    }

                // If it is a Solr Facet field (name != facet_tags), loop through its values.
                // For every value of this Facet field, add a LabelFrequency containing the Facet's name and count
                } else {
                    for (FacetField.Count count : facetField.getValues()) {
                        if (StringUtils.isNotEmpty(count.getName()) && count.getCount() > 0) {
                            final LabelFrequency facetValue = new LabelFrequency();
                            facetValue.count = count.getCount();
                            facetValue.label = count.getName();
                            facet.fields.add(facetValue);
                        }
                    }
                    // If the Solr facet contains values, it is added to the return Facet List
                    if (!facet.fields.isEmpty()) facetList.add(facet);
                }
            }
        }
    }

    // matches the requested technical Facets to the Enum type; then set the requested limit / offset range and
    // retrieve the technical facet values that were stored in technicalFacetMap above.
    // Add them to the facetlist.
    private void processTechnicalFacets(List<String> requestedTechnicalFacets,
                                        boolean defaultFacetsRequested,
                                        Map<String, Integer> technicalFacetLimits,
                                        Map<String, Integer> technicalFacetOffsets,
                                        List<Facet> facetList){
        for (String requestedFacetName : requestedTechnicalFacets) {

            TechnicalFacetType matchedFacetName;
            try {
                boolean cantMatchFacetName = false;
                boolean skipIfNoDefault = false;
                matchedFacetName = TechnicalFacetType.valueOf(requestedFacetName);
                String facetLimit  = "f." + matchedFacetName + ".facet.limit";
                String facetOffset = "f." + matchedFacetName + ".facet.offset";

                if (technicalFacetMap.get(matchedFacetName).isEmpty()) {
                    log.debug("couldn't match requested technical facet " + requestedFacetName);
                    cantMatchFacetName = true;
                }

                final Facet facet = new Facet();
                facet.name = matchedFacetName.getRealName();
                if (!requestedTechnicalFacets.contains(facet.name) && !defaultFacetsRequested) {
                    skipIfNoDefault = true; // note that this is not an error
                }

                if (cantMatchFacetName || skipIfNoDefault) {
                    continue; // stop & proceed with next one
                }

                int from = Math.min(((null != technicalFacetOffsets && technicalFacetOffsets.containsKey(facetOffset)) ?
                                     technicalFacetOffsets.get(facetOffset) : 0),
                                    technicalFacetMap.get(matchedFacetName).size() - 1);
                int to   = Math.min(((null != technicalFacetLimits  && technicalFacetLimits.containsKey(facetLimit))   ?
                                     technicalFacetLimits.get(facetLimit) + from :
                                     technicalFacetMap.get(matchedFacetName).size()),
                                    technicalFacetMap.get(matchedFacetName).size());

                List<String> keyList = new ArrayList<>(technicalFacetMap.get(matchedFacetName).keySet());
                for (int i = from; i < to; i++) {
                    final LabelFrequency freq = new LabelFrequency();
                    freq.label = keyList.get(i);
                    freq.count = technicalFacetMap.get(matchedFacetName).get(freq.label);
                    facet.fields.add(freq);
                }
                facetList.add(facet);

            } catch (IllegalArgumentException e) {
                log.error("error matching requested technical facet name " + requestedFacetName +
                          " with enum type in [consolidateFacetList] " + e.getClass().getSimpleName(), e);
            }

        }
    }

    private void processRangeFacets(List<RangeFacet> rangeFacets, List<Facet> facetList){
        for (RangeFacet rangeFacet : rangeFacets) {
            if (!rangeFacet.getCounts().isEmpty()) {
                final FacetRanger facetRanger = new FacetRanger();
                facetRanger.name = rangeFacet.getName();
                for (Object countObject : rangeFacet.getCounts()) {
                    Count count = (Count) countObject;
                    if (StringUtils.isNotEmpty(count.getValue()) && count.getCount() > 0) {
                        final LabelFrequency rangeValue = new LabelFrequency();
                        rangeValue.count = count.getCount();
                        rangeValue.label = formatDateString(count.getValue(), rangeFacet.getGap());
                        facetRanger.ranges.add(rangeValue);
                    }
                }
                // If the Range facet contains values, it is added to the return Facet List
                if (!facetRanger.ranges.isEmpty()) facetList.add(facetRanger);
            }
        }
    }

    private String formatDateString(String value, Object gap){
        String gapString = gap.toString();
        // this splits eg "1883-01-01T00:00:00Z" in ["1883"],["01"], ["01"], ["00:00:00Z"]
        String[] dateParts = StringUtils.split(value, "-T");
        if (StringUtils.containsIgnoreCase(gapString, "DAY")){
            return dateParts[0] + "-" + dateParts[1] + "-" + dateParts[2];
        } else if (StringUtils.containsIgnoreCase(gapString, "MONTH")){
            return dateParts[0] + "-" + dateParts[1];
        } else if (StringUtils.containsIgnoreCase(gapString, "YEAR")) {
            return dateParts[0];
        }
        // if in doubt, return the whole string
        return value;
    }

}
