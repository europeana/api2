package eu.europeana.api2.v2.utils;

import eu.europeana.api2.v2.model.FacetTag;
import eu.europeana.api2.v2.model.json.common.LabelFrequency;
import eu.europeana.api2.v2.model.json.view.submodel.SpellCheck;
import eu.europeana.corelib.definitions.solr.SolrFacetType;
import eu.europeana.corelib.definitions.solr.TechnicalFacetType;
import eu.europeana.indexing.solr.facet.EncodedFacet;
import eu.europeana.indexing.solr.facet.value.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.solr.client.solrj.response.SpellCheckResponse;
import org.apache.solr.client.solrj.response.SpellCheckResponse.Suggestion;

import java.util.*;


/**
 * @author Willem-Jan Boogerd
 * @author Lúthien
 */
public class ModelUtils {

    private static final String TECHNICALFACETS = "technicalfacets";
    private static final String SOLRFACETS      = "solrfacets";
    private static final String CUSTOMFACETS    = "customfacets";
    private static final String DEFAULT         = "DEFAULT";
//    private static final String NEWLINE         = System.lineSeparator();

    private static final int          FACET_LIMIT        = 150;
    // static goodies: Lists containing the enum Facet type names
    private static final List<String> technicalFacetList = new ArrayList<>();
    private static final List<String> solrFacetList      = new ArrayList<>();
    private static final List<String> enumFacetList      = new ArrayList<>();

    static {
        for (final var technicalFacet : TechnicalFacetType.values()) {
            technicalFacetList.add(technicalFacet.name());
        }
        for (final var solrFacet : SolrFacetType.values()) {
            solrFacetList.add(solrFacet.toString());
        }
        enumFacetList.addAll(technicalFacetList);
        enumFacetList.addAll(solrFacetList);
    }

    private ModelUtils() {}

    /**
     * returns a FacetTag object containing the name and label associated with the tag value
     *
     * @param tag numerically encoded technical facet tag
     * @return FacetTag (String name, String label)
     */
    public static FacetTag decodeFacetTag(Integer tag) {

        final var mediaType = EncodedFacet.MEDIA_TYPE.decodeValue(tag);
        final var mimeType  = EncodedFacet.MIME_TYPE.decodeValue(tag);
        if (mimeType != null) {
            return new FacetTag("MIME_TYPE", mimeType.getValue());
        }

        switch (mediaType) {
            case IMAGE:
                var imageAspectRatio = EncodedFacet.IMAGE_ASPECT_RATIO.decodeValue(tag);
                if (imageAspectRatio != null) {
                    return new FacetTag("IMAGE_ASPECTRATIO", imageAspectRatio.toString());
                }
                var imageColorEncoding = EncodedFacet.IMAGE_COLOR_ENCODING.decodeValue(tag);
                if (imageColorEncoding != null) {
                    return new FacetTag("COLOURPALETTE", imageColorEncoding.getHexStringWithHash());
                }
                var imageColorSpace = EncodedFacet.IMAGE_COLOR_SPACE.decodeValue(tag);
                if (imageColorSpace != null) {
                    return new FacetTag("IMAGE_COLOUR", imageColorSpace.toString());
                }
                var imageSize = EncodedFacet.IMAGE_SIZE.decodeValue(tag);
                if (imageSize != null) {
                    return new FacetTag("IMAGE_SIZE", imageSize.toString());
                }
                return new FacetTag("", "");
            case AUDIO:
                var audioDuration = EncodedFacet.AUDIO_DURATION.decodeValue(tag);
                if (audioDuration != null) {
                    return new FacetTag("SOUND_DURATION", audioDuration.toString());
                }
                var audioQuality = EncodedFacet.AUDIO_QUALITY.decodeValue(tag);
                if (audioQuality != null) {
                    return new FacetTag("SOUND_HQ", audioQuality.toString());
                }
                return new FacetTag("", "");
            case VIDEO:
                var videoDuration = EncodedFacet.VIDEO_DURATION.decodeValue(tag);
                if (videoDuration != null) {
                    return new FacetTag("VIDEO_DURATION", videoDuration.toString());
                }
                var videoQuality = EncodedFacet.VIDEO_QUALITY.decodeValue(tag);
                if (videoQuality != null) {
                    return new FacetTag("VIDEO_HD", videoQuality.toString());
                }
                return new FacetTag("", "");
            default:
                return new FacetTag("", "");
        }
    }

    /**
     * returns a FacetTag object containing the name and label associated with the tag value
     *
     * @param tag numerically encoded technical facet tag
     * @return FacetTag (String name, String label)
     */
    public static Map<String, String> findAllFacetsInTag(Integer tag) {
        Map<String, String> result = new HashMap<>();

        final var mediaType = EncodedFacet.MEDIA_TYPE.decodeValue(tag);
        if (mediaType != null) {
            result.put("MEDIA_TYPE", mediaType.toString());
            switch (mediaType) {
                case IMAGE:
                    var imageAspectRatio = EncodedFacet.IMAGE_ASPECT_RATIO.decodeValue(tag);
                    if (imageAspectRatio != null) {
                        result.put("IMAGE_ASPECTRATIO", imageAspectRatio.toString());
                    }
                    var imageColorEncoding = EncodedFacet.IMAGE_COLOR_ENCODING.decodeValue(tag);
                    if (imageColorEncoding != null) {
                        result.put("COLOURPALETTE", imageColorEncoding.getHexStringWithHash());
                    }
                    var imageColorSpace = EncodedFacet.IMAGE_COLOR_SPACE.decodeValue(tag);
                    if (imageColorSpace != null) {
                        result.put("IMAGE_COLOUR", imageColorSpace.toString());
                    }
                    var imageSize = EncodedFacet.IMAGE_SIZE.decodeValue(tag);
                    if (imageSize != null) {
                        result.put("IMAGE_SIZE", imageSize.toString());
                    }
                    break;
                case AUDIO:
                    var audioDuration = EncodedFacet.AUDIO_DURATION.decodeValue(tag);
                    if (audioDuration != null) {
                        result.put("SOUND_DURATION", audioDuration.toString());
                    }
                    var audioQuality = EncodedFacet.AUDIO_QUALITY.decodeValue(tag);
                    if (audioQuality != null) {
                        result.put("SOUND_HQ", audioQuality.toString());
                    }
                    break;
                case VIDEO:
                    var videoDuration = EncodedFacet.VIDEO_DURATION.decodeValue(tag);
                    if (videoDuration != null) {
                        result.put("VIDEO_DURATION", videoDuration.toString());
                    }
                    var videoQuality = EncodedFacet.VIDEO_QUALITY.decodeValue(tag);
                    if (videoQuality != null) {
                        result.put("VIDEO_HD", videoQuality.toString());
                    }
                    break;
                case TEXT:
                    break;
            }
        } else {
            result.put("MEDIA_TYPE", "Unknown");
        }

        final var mimeType = EncodedFacet.MIME_TYPE.decodeValue(tag);
        if (mimeType != null) {
            result.put("MIME_TYPE", mimeType.getValue());
        }

        return result;
    }

    public static SpellCheck convertSpellCheck(SpellCheckResponse response) {
        if (response != null) {
            var spellCheck = new SpellCheck();
            spellCheck.correctlySpelled = response.isCorrectlySpelled();
            for (Suggestion suggestion : response.getSuggestions()) {
                for (var i = 0; i < suggestion.getNumFound(); i++) {
                    var value = new LabelFrequency();
                    value.label = suggestion.getAlternatives().get(i);
                    value.count = suggestion.getAlternativeFrequencies().get(i).longValue();
                    spellCheck.suggestions.add(value);
                }
            }
            return spellCheck;
        }
        return null;
    }

    public static Map<String, String[]> separateAndLimitFacets(String[] mixedFacetArray,
                                                               boolean defaultFacetsRequested) {
        Map<String, String[]> facetListMap = new HashMap<>();
        String[]              customSolrFacets;

        if (defaultFacetsRequested) {
            facetListMap.put(TECHNICALFACETS, technicalFacetList.toArray(new String[0]));
            facetListMap.put(SOLRFACETS, solrFacetList.toArray(new String[0]));
        } else if (ArrayUtils.isNotEmpty(mixedFacetArray)) {
            facetListMap.put(TECHNICALFACETS,
                             ((List<String>) CollectionUtils.intersection(technicalFacetList,
                                                                          Arrays.asList(mixedFacetArray))).toArray(new String[0]));
            facetListMap.put(SOLRFACETS,
                             ((List<String>) CollectionUtils.intersection(solrFacetList,
                                                                          Arrays.asList(mixedFacetArray))).toArray(new String[0]));
        }

        if (ArrayUtils.isNotEmpty(mixedFacetArray)) {
            var customSolrFacetList = ((List<String>) CollectionUtils.subtract(Arrays.asList(mixedFacetArray),
                                                                               enumFacetList));
            if (customSolrFacetList.contains(DEFAULT)) customSolrFacetList.remove(DEFAULT);
            customSolrFacets = (customSolrFacetList).toArray(new String[0]);
            if (defaultFacetsRequested) {
                facetListMap.put(CUSTOMFACETS, safelyLimitArray(customSolrFacets, FACET_LIMIT - enumFacetList.size()));
            } else {
                facetListMap.put(CUSTOMFACETS,
                                 safelyLimitArray(customSolrFacets,
                                                  FACET_LIMIT - (facetListMap.get(TECHNICALFACETS).length +
                                                                 facetListMap.get(SOLRFACETS).length)));
            }
        }
        return facetListMap;
    }

    private static String[] safelyLimitArray(String[] input, int limit) {
        if (limit >= input.length) {
            return input;
        } else {
            return Arrays.copyOfRange(input, 0, limit);
        }
    }

}
