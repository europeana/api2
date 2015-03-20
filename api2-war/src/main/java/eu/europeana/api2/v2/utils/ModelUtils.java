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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

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
        List<Facet> facets = new ArrayList<Facet>();
        Map<String, Facet> mediaTypeFacets = new HashMap<>();
        Map<String, Integer> mimeTypeFacets = new HashMap<>();


        for (FacetField facetField : facetFields) {
            if (facetField.getValues() != null) {
                Facet facet = new Facet();
                facet.name = facetField.getName();
                System.out.println(facet.name);
                for (FacetField.Count count : facetField.getValues()) {
                    if (StringUtils.isNotEmpty(count.getName()) && count.getCount() > 0) {
                        LabelFrequency value = new LabelFrequency();

                        value.count = count.getCount();
                        value.label = count.getName();
                        if (count.getFacetField().getName().equalsIgnoreCase("facet_tags")) {
                            final String label = FacetLabelExtractor.getFacetLabel(Integer.valueOf(count.getName()));

                            if (!label.equals("")) {
                                value.label = label;
                            }

                            final String facetName = getFacetName(Integer.valueOf(count.getName()));
                            String mimeType = CommonPropertyExtractor.getMimeType(Integer.valueOf(count.getName()));

                            Integer y = Integer.valueOf(count.getName());

                            if (!mimeTypeFacets.containsKey(mimeType)) {
                                mimeTypeFacets.put(mimeType, 0);
                            }


                            mimeTypeFacets.put(mimeType, mimeTypeFacets.get(mimeType).intValue() + 1);

                            if (facetName.equals("")) {
                                 facet.fields.add(value);
                            } else {
                                if (!mediaTypeFacets.containsKey(facetName)) {
                                    Facet f = new Facet();
                                    f.name = facetName;
                                    mediaTypeFacets.put(facetName, f);
                                }
                                if (facetName.equals("VIDEO_HD") || facetName.equals("SOUND_HQ") || facetName.equals("IMAGE_GREYSCALE")) {
                                    value.label = "true";
                                }
                                if (facetName.equals("IMAGE_COLOUR") || facetName.equals("IMAGE_COLOR")) {
                                    value.label = "true";
                                    if (mediaTypeFacets.get(facetName).fields.isEmpty()) {
                                        mediaTypeFacets.get(facetName).fields.add(value);
                                    }
                                    else {
                                        mediaTypeFacets.get(facetName).fields.get(0).count += value.count;
                                    }
                                }
                                else {
                                    mediaTypeFacets.get(facetName).fields.add(value);
                                }
                            }
                        } else {
                            facet.fields.add(value);
                        }
                    }
                }

                if (!facet.name.equalsIgnoreCase("facet_tags") && !facet.fields.isEmpty()) {
                    facets.add(facet);
                }
            }
        }

        for(Map.Entry<String, Facet> f: mediaTypeFacets.entrySet()) {
            if (!f.getValue().fields.isEmpty()) {
                facets.add(f.getValue());
            }
        }

        Facet f = new Facet();
        f.name = "MIME_TYPE";
        for (Map.Entry<String, Integer> mimeType: mimeTypeFacets.entrySet()) {
            LabelFrequency x = new LabelFrequency();
            x.label = mimeType.getKey();
            x.count = mimeType.getValue();

            if (null == x.label || x.label.trim().isEmpty())
            {
                continue;
            }
            f.fields.add(x);
        }

        if (!f.fields.isEmpty()) {
            facets.add(f);
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
