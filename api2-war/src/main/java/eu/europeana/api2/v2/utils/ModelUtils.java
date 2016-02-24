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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.europeana.corelib.definitions.model.facets.inverseLogic.*;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.SpellCheckResponse;
import org.apache.solr.client.solrj.response.SpellCheckResponse.Suggestion;

import eu.europeana.api2.v2.model.enums.FacetNames;
import eu.europeana.api2.v2.model.json.common.LabelFrequency;
import eu.europeana.api2.v2.model.json.view.submodel.Facet;
import eu.europeana.api2.v2.model.json.view.submodel.SpellCheck;
import eu.europeana.corelib.search.service.impl.FacetLabelExtractor;
import eu.europeana.crf_faketags.extractor.MediaTypeEncoding;


/**
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 */
public class ModelUtils {

    private ModelUtils() {
        // Constructor must be private
    }

    public static String getFacetName(Integer tag) {
        final MediaTypeEncoding mediaType = CommonPropertyExtractor.getType(tag);
        final String            mimeType  = CommonPropertyExtractor.getMimeType(tag);

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

    public static List<Facet> conventFacetList(List<FacetField> facetFields) {

        if (facetFields == null || facetFields.isEmpty()) {
            return null;
        }

        final List<Facet> facets = new ArrayList<>();
        final Map<FacetNames, Map<String, Long>> mediaTypeFacets = new HashMap<>();

        /*
         * init to make thing easier :)
         */
        for (final FacetNames facetName : FacetNames.values()) {
            mediaTypeFacets.put(facetName, new HashMap<String, Long>());
        }

        for (FacetField facetField : facetFields) {
            if (facetField.getValues() != null) {
                final Facet facet = new Facet();
                facet.name = facetField.getName();

                if (FacetNames.IS_FULLTEXT.name().equalsIgnoreCase(facet.name)) {
                    facet.name = FacetNames.IS_FULLTEXT.getRealName();
                } else if (FacetNames.HAS_MEDIA.name().equalsIgnoreCase(facet.name)) {
                    facet.name = FacetNames.HAS_MEDIA.getRealName();
                } else if (FacetNames.HAS_THUMBNAILS.name().equalsIgnoreCase(facet.name)) {
                    facet.name = FacetNames.HAS_THUMBNAILS.getRealName();
                }


                /*
                 * demultiplex the face_tags into proper facets (see FacetNames)
                 */
                for (FacetField.Count count : facetField.getValues()) {
                    if (StringUtils.isNotEmpty(count.getName()) && count.getCount() > 0) {
                        if (!facetField.getName().equalsIgnoreCase("facet_tags")) {
                            final LabelFrequency value = new LabelFrequency();
                            value.count = count.getCount();
                            value.label = count.getName();
                            facet.fields.add(value);
                            continue;
                        }

                        final Integer tag = Integer.valueOf(count.getName());
                        final String label = FacetLabelExtractor.getFacetLabel(tag).trim();

                        if (label.isEmpty()) {
                            continue;
                        }

                        final FacetNames facetName = FacetNames.valueOf(getFacetName(tag).trim().toUpperCase());

                        Long value = mediaTypeFacets.get(facetName).get(label);

                        if (null == value) {
                            value = 0L;
                        }
                        mediaTypeFacets.get(facetName).put(label, value + count.getCount());
                    }
                }

                /*
                 * note that only facets that have values are returned
                 *  facet_tags shouldn't contain any values after the processing done above
                 */
                if (!facet.fields.isEmpty()) {
                    facets.add(facet);
                }
            }
        }

        for (Map.Entry<FacetNames, Map<String, Long>> facetNameValues : mediaTypeFacets.entrySet()) {
            if (facetNameValues.getValue().isEmpty()) {
                continue;
            }

            final Facet facet = new Facet();
            facet.name = facetNameValues.getKey().getRealName();

            for (final Map.Entry<String, Long> value : facetNameValues.getValue().entrySet()) {
                final LabelFrequency freq = new LabelFrequency();
                freq.label = value.getKey();
                freq.count = value.getValue();

                facet.fields.add(freq);
            }
            facets.add(facet);
        }


        /*
         * sort the label of each facet
         */
        for (final Facet facet : facets) {
            Collections.sort(facet.fields, new Comparator<LabelFrequency>() {
                @Override
                public int compare(LabelFrequency o1, LabelFrequency o2) {
                    return Long.compare(o2.count, o1.count);
                }
            });
        }

        return facets;
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

}
