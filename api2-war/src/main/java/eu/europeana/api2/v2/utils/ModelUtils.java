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

import java.awt.*;
import java.util.*;
import java.util.List;

import eu.europeana.corelib.search.service.impl.FacetLabelExtractor;
import eu.europeana.corelib.search.service.inverseLogic.CommonPropertyExtractor;
import eu.europeana.corelib.search.service.inverseLogic.ImagePropertyExtractor;
import eu.europeana.corelib.search.service.inverseLogic.SoundPropertyExtractor;
import eu.europeana.corelib.search.service.inverseLogic.VideoPropertyExtractor;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.SpellCheckResponse;
import org.apache.solr.client.solrj.response.SpellCheckResponse.Suggestion;

import eu.europeana.api2.v2.model.json.common.LabelFrequency;
import eu.europeana.api2.v2.model.json.view.submodel.Facet;
import eu.europeana.api2.v2.model.json.view.submodel.SpellCheck;

/**
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 */
public class ModelUtils {

	private ModelUtils() {
		// Constructor must be private
	}

    public static String getFacetName(Integer tag) {
        final Integer mediaType = CommonPropertyExtractor.getType(tag);
        final String  mimeType  = CommonPropertyExtractor.getMimeType(tag);

        if (null != mimeType && !mimeType.trim().isEmpty()) {
            return "MIME_TYPE";
        }

        String label;
        switch (mediaType) {
            case 1:
                label = ImagePropertyExtractor.getAspectRatio(tag);
                if(!label.equals("")) {
                    return "IMAGE_ASPECTRATIO";
                }
                label = ImagePropertyExtractor.getColor(tag);
                if(!label.equals("")) {
                    return "COLOURPALETTE";
                }
                label = ImagePropertyExtractor.getColorSpace(tag);
                if(!label.equals("")) {
                    return "grayscale".equals(label) ? "IMAGE_GREYSCALE": "IMAGE_COLOUR" ;
                }
                label = ImagePropertyExtractor.getSize(tag);
                if (!label.equals("")) {
                    return "IMAGE_SIZE";
                }
                return label;
            case 2:
                label = SoundPropertyExtractor.getDuration(tag);
                if(!label.equals("")) {
                    return "SOUND_DURATION";
                }
                label = SoundPropertyExtractor.getQuality(tag);
                if (!label.equals("")) {
                    return "SOUND_HQ";
                }
                return "";
            case 3:
                label = VideoPropertyExtractor.getDuration(tag);
                if(!label.equals("")) {
                    return "VIDEO_DURATION";
                }
                label = VideoPropertyExtractor.getQuality(tag);
                if (!label.equals("")) {
                    return "VIDEO_HD";
                }
                return "";

            default: return "";
        }
    }

	public static List<Facet> conventFacetList(List<FacetField> facetFields) {
        if (null == facetFields || facetFields.isEmpty()) {
            return null;
        }
        final List<Facet> facets = new ArrayList<Facet>();
        final Map<String, Facet> mediaTypeFacets = new HashMap<>();
        final Map<String, Long> mimeTypeFacets = new HashMap<>();


        for (FacetField facetField : facetFields) {
            if (facetField.getValues() != null) {
                final Facet facet = new Facet();
                facet.name = facetField.getName();

                if (facet.name.equalsIgnoreCase("is_fulltext")) {
                    facet.name = "TEXT_FULLTEXT";
                }
                else if (facet.name.equalsIgnoreCase("has_media")) {
                    facet.name = "MEDIA";
                }
                else if (facet.name.equalsIgnoreCase("has_thumbnails")) {
                    facet.name = "THUMBNAIL";
                }

                for (FacetField.Count count : facetField.getValues()) {
                    if (StringUtils.isNotEmpty(count.getName()) && count.getCount() > 0) {
                        final LabelFrequency value = new LabelFrequency();

                        value.count = count.getCount();
                        value.label = count.getName();

                        if (!count.getFacetField().getName().equalsIgnoreCase("facet_tags")) {
                            facet.fields.add(value);
                            continue;
                        }

                        final Integer tag = Integer.valueOf(count.getName());
                        final String label = FacetLabelExtractor.getFacetLabel(tag).trim();
                        final String facetName = getFacetName(tag).trim();

                        if (label.isEmpty() || facetName.isEmpty()) {
                            facet.fields.add(value);
                        } else if (facetName.equalsIgnoreCase("MIME_TYPE")) {
                            Long newVal = 0L;
                            if (mimeTypeFacets.containsKey(label)) {
                                newVal = mimeTypeFacets.get(label);
                            }
                            newVal += count.getCount();
                            mimeTypeFacets.put(label, newVal);
                        } else {
                            if (!mediaTypeFacets.containsKey(facetName)) {
                                final Facet f = new Facet();
                                f.name = facetName;
                                mediaTypeFacets.put(facetName, f);
                            }
                            value.label = label;

                            mediaTypeFacets.get(facetName).fields.add(value);
                        }
                    }
                }

                if (facet.name.equalsIgnoreCase("facet_tags")) continue;

                if (facet.fields.isEmpty()) {
                    final LabelFrequency freq = new LabelFrequency();
                    freq.label = "true";
                    freq.count = 0;
                    facet.fields.add(freq);
                }
                facets.add(facet);
            }
        }

        if (!mimeTypeFacets.isEmpty()) {
            Facet f = new Facet();
            f.name = "MIME_TYPE";

            for (Map.Entry<String, Long> mimeType: mimeTypeFacets.entrySet()) {
               LabelFrequency freq = new LabelFrequency();
                freq.label = mimeType.getKey();
                freq.count = mimeType.getValue();

                f.fields.add(freq);
            }
            facets.add(f);
        }

        for (Map.Entry<String, Facet> facet: mediaTypeFacets.entrySet()) {
            final String name = facet.getKey();
            switch(name) {
                case "VIDEO_HD":
                case "SOUND_HQ":
                case "IMAGE_GREYSCALE":
                case "IMAGE_COLOUR":
                case "IMAGE_COLOR":
                    LabelFrequency freq = new LabelFrequency();
                    freq.label = "true";
                    freq.count = 0;

                    for (LabelFrequency field: facet.getValue().fields) {
                        freq.count += field.count;
                    }
                    facet.getValue().fields.clear();
                    facet.getValue().fields.add(freq);
                    facets.add(facet.getValue());

                    break;

                default:
                    facets.add(facet.getValue());
            }
        }

        for (Facet f: facets) {
            Collections.sort(f.fields, new Comparator<LabelFrequency>() {
                @Override
                public int compare(LabelFrequency o1, LabelFrequency o2) {
                    return o1.label.trim().compareTo(o2.label.trim());
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
					value.count = suggestion.getAlternativeFrequencies().get(i)
							.longValue();
					spellCheck.suggestions.add(value);
				}
			}
			return spellCheck;
		}
		return null;
	}

}
