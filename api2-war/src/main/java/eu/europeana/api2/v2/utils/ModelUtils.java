package eu.europeana.api2.v2.utils;

import eu.europeana.api2.v2.utils.technicalfacets.*;
import eu.europeana.corelib.definitions.solr.SolrFacetType;
import eu.europeana.corelib.definitions.solr.TechnicalFacetType;
import eu.europeana.api2.v2.model.json.common.LabelFrequency;
import eu.europeana.api2.v2.model.json.view.submodel.SpellCheck;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
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
     * if boolean name = true, returns the facet name associated with the tag value
     * otherwise, returns the facet label
     * @param tag  numerically encoded technical facet tag
     * @param name whether to return the tag name (true) or label (false)
     * @return tag name / label (String)
     */
    public static String decodeFacetTag(Integer tag, boolean name) {
        final MediaTypeEncoding mediaType = CommonTagExtractor.getType(tag);
        final String mimeType = CommonTagExtractor.getMimeType(tag);

        if (StringUtils.isNotBlank(mimeType)) return name ? "MIME_TYPE" : mimeType;

        String label;
        switch (mediaType) {
            case IMAGE:
                label = ImageTagExtractor.getAspectRatio(tag);
                if (StringUtils.isNotBlank(label)) return name ? "IMAGE_ASPECTRATIO" : label;
                label = ImageTagExtractor.getColor(tag);
                if (StringUtils.isNotBlank(label)) return name ? "COLOURPALETTE" : label;
                label = ImageTagExtractor.getColorSpace(tag);
                if (StringUtils.isNotBlank(label)) return name ? "IMAGE_COLOUR" : label;
                label = ImageTagExtractor.getSize(tag);
                if (StringUtils.isNotBlank(label)) return name ? "IMAGE_SIZE" : label;
                return label;
            case AUDIO:
                label = SoundTagExtractor.getDuration(tag);
                if (StringUtils.isNotBlank(label)) return name ? "SOUND_DURATION" : label;
                label = SoundTagExtractor.getQuality(tag);
                if (StringUtils.isNotBlank(label)) return name ? "SOUND_HQ" : label;
                return "";
            case VIDEO:
                label = VideoTagExtractor.getDuration(tag);
                if (StringUtils.isNotBlank(label)) return name ? "VIDEO_DURATION" : label;
                label = VideoTagExtractor.getQuality(tag);
                if (StringUtils.isNotBlank(label)) return name ? "VIDEO_HD" : label;
                return "";
            default:
                return "";
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
