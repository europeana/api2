package eu.europeana.api2.v2.utils;

import eu.europeana.api2.v2.model.FacetTag;
import eu.europeana.corelib.definitions.solr.SolrFacetType;
import eu.europeana.corelib.definitions.solr.TechnicalFacetType;
import eu.europeana.api2.v2.model.json.common.LabelFrequency;
import eu.europeana.api2.v2.model.json.view.submodel.SpellCheck;
import eu.europeana.indexing.solr.facet.*;
import eu.europeana.indexing.solr.facet.value.*;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.solr.client.solrj.response.SpellCheckResponse;
import org.apache.solr.client.solrj.response.SpellCheckResponse.Suggestion;

import java.util.*;


/**
 * @author Willem-Jan Boogerd
 * @author Lúthien
 */
public class ModelUtils {

    private ModelUtils(){}

    private static final int FACET_LIMIT = 150;
    // static goodies: Lists containing the enum Facet type names
    private static final List<String> technicalFacetList = new ArrayList<>();
    private static final List<String> solrFacetList = new ArrayList<>();
    private static final List<String> enumFacetList = new ArrayList<>();
    static {
        for (final TechnicalFacetType technicalFacet : TechnicalFacetType.values()) {
            technicalFacetList.add(technicalFacet.name());
        }
        for (final SolrFacetType solrFacet : SolrFacetType.values()) {
            solrFacetList.add(solrFacet.toString());
        }
        enumFacetList.addAll(technicalFacetList);
        enumFacetList.addAll(solrFacetList);
    }



    /**
     * returns a FacetTag object containing the name and label associated with the tag value
     * @param tag  numerically encoded technical facet tag
     * @return FacetTag (String name, String label)
     */
    public static FacetTag decodeFacetTag(Integer tag) {

        final MediaTypeEncoding mediaType = EncodedFacet.MEDIA_TYPE.decodeValue(tag);
        final MimeTypeEncoding  mimeType  = EncodedFacet.MIME_TYPE.decodeValue(tag);
        if (mimeType != null) {
            return new FacetTag("MIME_TYPE", mimeType.getValue());
        }

        switch (mediaType) {
            case IMAGE:
                ImageAspectRatio imageAspectRatio = EncodedFacet.IMAGE_ASPECT_RATIO.decodeValue(tag);
                if (imageAspectRatio != null) return new FacetTag("IMAGE_ASPECTRATIO", imageAspectRatio.toString());
                ImageColorEncoding imageColorEncoding = EncodedFacet.IMAGE_COLOR_ENCODING.decodeValue(tag);
                if (imageColorEncoding != null) return new FacetTag("COLOURPALETTE",imageColorEncoding.getHexStringWithHash());
                ImageColorSpace imageColorSpace = EncodedFacet.IMAGE_COLOR_SPACE.decodeValue(tag);
                if (imageColorSpace != null) return new FacetTag("IMAGE_COLOUR", imageColorSpace.toString());
                ImageSize imageSize = EncodedFacet.IMAGE_SIZE.decodeValue(tag);
                if (imageSize != null) return new FacetTag("IMAGE_SIZE", imageSize.toString());
                return new FacetTag("", "");
            case AUDIO:
                AudioDuration audioDuration = EncodedFacet.AUDIO_DURATION.decodeValue(tag);
                if (audioDuration != null) return new FacetTag("SOUND_DURATION", audioDuration.toString());
                AudioQuality audioQuality = EncodedFacet.AUDIO_QUALITY.decodeValue(tag);
                if (audioQuality != null) return new FacetTag("SOUND_HQ", audioQuality.toString());
                return new FacetTag("", "");
            case VIDEO:
                VideoDuration videoDuration = EncodedFacet.VIDEO_DURATION.decodeValue(tag);
                if (videoDuration != null) return new FacetTag("VIDEO_DURATION", videoDuration.toString());
                VideoQuality videoQualityn = EncodedFacet.VIDEO_QUALITY.decodeValue(tag);
                if (videoQualityn != null) return new FacetTag("VIDEO_HD", videoQualityn.toString());
                return new FacetTag("", "");
            default:
                return new FacetTag("", "");
        }
    }

    public static boolean containsTechnicalFacet(String[] mixedFacets){
        return containsTechnicalFacet(Arrays.asList(mixedFacets));
    }

    private static boolean containsTechnicalFacet(List<String> mixedFacets){
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

    public static Map<String, String[]> separateAndLimitFacets(String[] mixedFacetArray, boolean defaultFacetsRequested) {
        Map<String, String[]> facetListMap = new HashMap<>();
        String[] customSolrFacets;

        if (defaultFacetsRequested){
            facetListMap.put("technicalfacets", technicalFacetList.toArray(new String[0]));
            facetListMap.put("solrfacets", solrFacetList.toArray(new String[0]));
        } else if (ArrayUtils.isNotEmpty(mixedFacetArray)) {
            facetListMap.put("technicalfacets", ((List<String>)CollectionUtils.intersection(technicalFacetList, Arrays.asList(mixedFacetArray))).toArray(new String[0]));
            facetListMap.put("solrfacets", ((List<String>)CollectionUtils.intersection(solrFacetList, Arrays.asList(mixedFacetArray))).toArray(new String[0]));
        }

        if (ArrayUtils.isNotEmpty(mixedFacetArray)) {
            List<String> customSolrFacetList = ((List<String>) CollectionUtils.subtract(Arrays.asList(mixedFacetArray), enumFacetList));
            if (customSolrFacetList.contains("DEFAULT")) customSolrFacetList.remove("DEFAULT");
            customSolrFacets = (customSolrFacetList).toArray(new String[0]);
            if (defaultFacetsRequested){
                facetListMap.put("customfacets", safelyLimitArray(customSolrFacets, FACET_LIMIT - enumFacetList.size()));
            } else {
                facetListMap.put("customfacets", safelyLimitArray(customSolrFacets, FACET_LIMIT -
                        (facetListMap.get("technicalfacets").length + facetListMap.get("solrfacets").length)));
            }
        }
        return facetListMap;
    }

    private static String[] safelyLimitArray(String[] input, int limit){
        if (limit >= input.length){
            return input;
        } else {
            return Arrays.copyOfRange(input, 0, limit);
        }
    }

}
