/*
 * Copyright 2007-2012 The Europeana Foundation
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

package eu.europeana.api2.v2.utils;

import eu.europeana.corelib.definitions.solr.TechnicalFacetType;
import eu.europeana.api2.v2.model.json.common.LabelFrequency;
import eu.europeana.api2.v2.model.json.view.submodel.Facet;
import eu.europeana.api2.v2.model.json.view.submodel.SpellCheck;
import eu.europeana.corelib.definitions.model.facets.inverseLogic.CommonPropertyExtractor;
import eu.europeana.corelib.definitions.model.facets.inverseLogic.ImagePropertyExtractor;
import eu.europeana.corelib.definitions.model.facets.inverseLogic.SoundPropertyExtractor;
import eu.europeana.corelib.definitions.model.facets.inverseLogic.VideoPropertyExtractor;
import eu.europeana.corelib.search.service.impl.FacetLabelExtractor;
import eu.europeana.crf_faketags.extractor.MediaTypeEncoding;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.SpellCheckResponse;
import org.apache.solr.client.solrj.response.SpellCheckResponse.Suggestion;

import java.util.*;

/**
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 */
public class ModelUtils {


    // static goodies: a Map containing the technical Facet types &
    // a List containing the technical Facet names
    final static private Map<TechnicalFacetType, Map<String, Long>> technicalFacetMap  = new HashMap<>();
    final static private List<String>                               technicalFacetList = new ArrayList<>();
    static {
        for (final TechnicalFacetType technicalFacetName : TechnicalFacetType.values()) {
            technicalFacetMap.put(technicalFacetName, new HashMap<>());
        }
        for (final TechnicalFacetType technicalFacet : TechnicalFacetType.values()) {
            technicalFacetList.add(technicalFacet.name());
        }
    }

    private ModelUtils() {
        // Constructor must be private
    }

    public static String decodeFacetName(Integer tag) {
        final MediaTypeEncoding mediaType = CommonPropertyExtractor.getType(tag);
        final String mimeType = CommonPropertyExtractor.getMimeType(tag);

        if (StringUtils.isNotBlank(mimeType)) {
            return "MIME_TYPE";
        }

        String label;
        switch (mediaType) {
            case IMAGE:
                label = ImagePropertyExtractor.getAspectRatio(tag);
                if (StringUtils.isNotBlank(label)) {
                    return "IMAGE_ASPECTRATIO";
                }
                label = ImagePropertyExtractor.getColor(tag);
                if (StringUtils.isNotBlank(label)) {
                    return "COLOURPALETTE";
                }
                label = ImagePropertyExtractor.getColorSpace(tag);
                if (StringUtils.isNotBlank(label)) {
                    return "greyscale".equalsIgnoreCase(label) ? "IMAGE_GREYSCALE" : "IMAGE_COLOUR";
                }
                label = ImagePropertyExtractor.getSize(tag);
                if (StringUtils.isNotBlank(label)) {
                    return "IMAGE_SIZE";
                }
                return label;
            case AUDIO:
                label = SoundPropertyExtractor.getDuration(tag);
                if (StringUtils.isNotBlank(label)) {
                    return "SOUND_DURATION";
                }
                label = SoundPropertyExtractor.getQuality(tag);
                if (StringUtils.isNotBlank(label)) {
                    return "SOUND_HQ";
                }
                return "";
            case VIDEO:
                label = VideoPropertyExtractor.getDuration(tag);
                if (StringUtils.isNotBlank(label)) {
                    return "VIDEO_DURATION";
                }
                label = VideoPropertyExtractor.getQuality(tag);
                if (StringUtils.isNotBlank(label)) {
                    return "VIDEO_HD";
                }
                return "";

            default:
                return "";
        }
    }

    /**
     * Consolidates the regular and technical metadata facets.
     * The method loops through the List of FacetFields returned by Solr, extracts both the regular as the encoded
     * technical metadata facets, converts them to Facet objects, adds those to a List and returns that List.
     * @param facetFields the List of facetfields as returned by Solr
     * @return a List of Facet objects representing both the regular, and the technical metadata facets
     */
    public static List<Facet> consolidateFacetList(List<FacetField> facetFields, List<String> requestedTechnicalFacets, boolean defaultFacetsRequested) {
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

                /* For every Facet field, loop through its values. If it is a Solr Facet field (name != facet_tags):
                 * - for every value of this Facet field, add a LabelFrequency containing the Facet's name and count
                 * If it ain't a Solr Facet field (name == facet_tags), it contains the technical metadata:
                 * - for every value of facet_tags, retrieve the human-readable label from the encoded integer value
                 * - match the technical Facet name against the enum TechnicalFacetType
                 * - values associated with the technical Facet are stored in the technicalFacetMap
                 * Note that technical metadata names are encoded numerically (See eu.europeana.crf_faketags) */
                for (FacetField.Count count : facetField.getValues()) {
                    if (StringUtils.isNotEmpty(count.getName()) && count.getCount() > 0) {
                        if (!facetField.getName().equalsIgnoreCase("facet_tags")) {
                            final LabelFrequency facetValue = new LabelFrequency();
                            facetValue.count = count.getCount();
                            facetValue.label = count.getName();
                            facet.fields.add(facetValue);
                            continue;
                        }

                        final Integer technicalFacetTag = Integer.valueOf(count.getName());
                        final String technicalFacetLabel = FacetLabelExtractor.getFacetLabel(technicalFacetTag).trim();

                        if (technicalFacetLabel.isEmpty()) continue;

                        final TechnicalFacetType technicalFacetFinalName = TechnicalFacetType.valueOf(decodeFacetName(technicalFacetTag).trim().toUpperCase());
                        Long technicalFacetValue = technicalFacetMap.get(technicalFacetFinalName).get(technicalFacetLabel);
                        if (null == technicalFacetValue) technicalFacetValue = 0L;
                        technicalFacetMap.get(technicalFacetFinalName).put(technicalFacetLabel, technicalFacetValue + count.getCount());
                    }
                }
                // If the Solr facet contains values, it is added to the return Facet List
                if (!facet.fields.isEmpty()) facetList.add(facet);
            }
        }
        // loop through the technicalFacetMap, now populated with the retrieved technical metadata values
        // If there are values present for a technical facet type, create a generic Facet for that technical facet type
        // note that this is done in the same manner as for the Solr facets in the above FOR loop
        // Then add the new Facet to the return Facet List
        for (Map.Entry<TechnicalFacetType, Map<String, Long>> technicalFacet : technicalFacetMap.entrySet()) {
            if (technicalFacet.getValue().isEmpty()) continue;

            final Facet facet = new Facet();
            facet.name = technicalFacet.getKey().getRealName();
            if ( null != requestedTechnicalFacets               &&
                !requestedTechnicalFacets.contains(facet.name)  &&
                !defaultFacetsRequested) continue;

            for (final Map.Entry<String, Long> value : technicalFacet.getValue().entrySet()) {
                final LabelFrequency freq = new LabelFrequency();
                freq.label = value.getKey();
                freq.count = value.getValue();
                facet.fields.add(freq);
            }
            facetList.add(facet);
        }

        // sort the List of Facets on #count, descending
        facetList.sort((f1, f2) -> Integer.compare(f2.fields.size(), f1.fields.size()));
        return facetList;
    }

    public static boolean containsTechnicalFacet(String[] mixedFacets){
        return containsTechnicalFacet(Arrays.asList(mixedFacets));
    }

    public static boolean containsTechnicalFacet(List<String> mixedFacets){
        return CollectionUtils.containsAny(mixedFacets, technicalFacetList);
    }

    public static SpellCheck convertSpellCheck(SpellCheckResponse response) {
        if (response != null) {
            SpellCheck spellCheck = new SpellCheck();
            spellCheck.correctlySpelled = response.isCorrectlySpelled();
            for (Suggestion suggestion : response.getSuggestions()) {
                for (int i = 0; i < suggestion.getNumFound(); i++) {
                    LabelFrequency value = new LabelFrequency();
                    value.label = suggestion.getAlternatives().get(i);
                    value.count = suggestion.getAlternativeFrequencies().get(i).longValue();
                    spellCheck.suggestions.add(value);
                }
            }
            return spellCheck;
        }
        return null;
    }

    public static Map<String, String[]> separateFacets(String[] mixedFacetArray) {
        Map<String, String[]> facetListMap = new HashMap<>();
        if (ArrayUtils.isNotEmpty(mixedFacetArray)) {
            facetListMap.put("solrfacets", ((List<String>)CollectionUtils.subtract(Arrays.asList(mixedFacetArray), technicalFacetList)).toArray(new String[0]));
            facetListMap.put("technicalfacets", ((List<String>)CollectionUtils.intersection(technicalFacetList, Arrays.asList(mixedFacetArray))).toArray(new String[0]));
        }
        return facetListMap;
    }

}
