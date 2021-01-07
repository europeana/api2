package eu.europeana.api2.v2.service;

import eu.europeana.api2.v2.model.FacetTag;
import eu.europeana.api2.v2.model.json.common.LabelFrequency;
import eu.europeana.api2.v2.model.json.view.submodel.Facet;
import eu.europeana.api2.v2.model.json.view.submodel.FacetRanger;
import eu.europeana.corelib.definitions.solr.SolrFacetType;
import eu.europeana.corelib.definitions.solr.TechnicalFacetType;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.RangeFacet;
import org.apache.solr.client.solrj.response.RangeFacet.Count;

import java.util.*;

import static eu.europeana.api2.v2.utils.ModelUtils.decodeFacetTag;

/**
 * Created by luthien on 20/04/2016.
 * Modified on 8/10/2019 to work with Metis-indexing solr facet library
 */

public class FacetWrangler {
  
    private static final Logger LOG = LogManager.getLogger(FacetWrangler.class);

    private Map<String, Map<String, Integer>> technicalFacetMap = new LinkedHashMap<>();

    public FacetWrangler() {
        for (final var technicalFacet : TechnicalFacetType.values()) {
            technicalFacetMap.put(technicalFacet.getRealName(), new LinkedHashMap<>()); // LinkedHashMap to preserve ordering
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
        for (var facetField : facetFields) {
            if (!facetField.getValues().isEmpty()) {
                final var facet = new Facet();
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
                    for (var encodedTechnicalFacet : facetField.getValues()) {
                        if (StringUtils.isNotEmpty(encodedTechnicalFacet.getName())
                            && encodedTechnicalFacet.getCount() > 0) {
                            FacetTag facetTag = null;

                            try {
                                facetTag = decodeFacetTag(Integer.valueOf(encodedTechnicalFacet.getName()));
                                if (StringUtils.isAnyBlank(facetTag.getLabel(), facetTag.getName())) {
                                    LOG.debug("Decoded technical Facet's name and/or label is empty");
                                    continue;
                                }

                                // retrieve a possibly earlier stored count value for this label. If not available,
                                // initialise at 0L; then add the count value to the Map for this particular label
                                var technicalFacetFieldCount = technicalFacetMap.get(facetTag.getName()).get(facetTag.getLabel());
                                if (null == technicalFacetFieldCount) technicalFacetFieldCount = 0;
                                technicalFacetMap.get(facetTag.getName()).put(facetTag.getLabel(),
                                                                              technicalFacetFieldCount +
                                                                              (int) encodedTechnicalFacet.getCount());
                            } catch (IllegalArgumentException e) {
                                assert facetTag != null;
                                LOG.debug("error matching decoded technical facet name {} with enum type in [consolidateFacetList]: {}",
                                          facetTag.getName(), e.getClass().getSimpleName(), e);
                            }
                        }
                    }

                // If it is a Solr Facet field (name != facet_tags), loop through its values.
                // For every value of this Facet field, add a LabelFrequency containing the Facet's name and count
                } else {
                    for (var count : facetField.getValues()) {
                        if (StringUtils.isNotEmpty(count.getName()) && count.getCount() > 0) {
                            final var facetValue = new LabelFrequency();
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
        for (var requestedFacetName : requestedTechnicalFacets) {

            try {
                var    cantMatchFacetName = false;
                var    skipIfNoDefault = false;
                var facetLimit  = "f." + requestedFacetName + ".facet.limit";
                var facetOffset = "f." + requestedFacetName + ".facet.offset";

                if (technicalFacetMap.get(requestedFacetName).isEmpty()) {
                    LOG.debug("couldn't match requested technical facet {}", requestedFacetName);
                    cantMatchFacetName = true;
                }

                final var facet = new Facet();
                facet.name = requestedFacetName;
                if (!requestedTechnicalFacets.contains(facet.name) && !defaultFacetsRequested) {
                    skipIfNoDefault = true; // note that this is not an error
                }

                if (cantMatchFacetName || skipIfNoDefault) {
                    continue; // stop & proceed with next one
                }

                var from = Math.min(((null != technicalFacetOffsets && technicalFacetOffsets.containsKey(facetOffset)) ?
                                     technicalFacetOffsets.get(facetOffset) : 0),
                                    technicalFacetMap.get(requestedFacetName).size() - 1);
                int to   = Math.min(((null != technicalFacetLimits  && technicalFacetLimits.containsKey(facetLimit))   ?
                                     technicalFacetLimits.get(facetLimit) + from :
                                     technicalFacetMap.get(requestedFacetName).size()),
                                    technicalFacetMap.get(requestedFacetName).size());

                List<String> keyList = new ArrayList<>(technicalFacetMap.get(requestedFacetName).keySet());
                for (var i = from; i < to; i++) {
                    final var freq = new LabelFrequency();
                    freq.label = keyList.get(i);
                    freq.count = technicalFacetMap.get(requestedFacetName).get(freq.label);
                    facet.fields.add(freq);
                }
                facetList.add(facet);

            } catch (IllegalArgumentException e) {
                LOG.error("error matching requested technical facet name {} with enum type in [consolidateFacetList]: {}",
                          requestedFacetName, e.getClass().getSimpleName(), e);
            }
        }
    }

    private void processRangeFacets(List<RangeFacet> rangeFacets, List<Facet> facetList){
        for (RangeFacet rangeFacet : rangeFacets) {
            if (!rangeFacet.getCounts().isEmpty()) {
                final var facetRanger = new FacetRanger();
                facetRanger.name = rangeFacet.getName();
                for (var countObject : rangeFacet.getCounts()) {
                    var count = (Count) countObject;
                    if (StringUtils.isNotEmpty(count.getValue()) && count.getCount() > 0) {
                        final var rangeValue = new LabelFrequency();
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
        var gapString = gap.toString();
        // this splits eg "1883-01-01T00:00:00Z" in ["1883"],["01"], ["01"], ["00:00:00Z"]
        var dateParts = StringUtils.split(value, "-T");
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
