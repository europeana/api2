package eu.europeana.api2.v2.utils;

import eu.europeana.api2.v2.utils.technicalfacets.CommonTagExtractor;
import eu.europeana.api2.v2.utils.technicalfacets.ImageTagExtractor;
import eu.europeana.api2.v2.utils.technicalfacets.SoundTagExtractor;
import eu.europeana.api2.v2.utils.technicalfacets.VideoTagExtractor;
import eu.europeana.indexing.solr.facet.FacetEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author lúthien (maike.dulk@europeana.eu)
 */
public class TagUtils {

    private TagUtils(){}

    // 1) the fixlist() method cleans up duplicates and replaces NULL Lists with a list containing only "null". This is
    // to facilitate the default "0" values of the filter tags.
    // 2) The nested forEach creates a cartesian combination of the input lists
    // 3) The filtertag integers are assembled right away inside the loops, as it adds nothing to delegate that to
    // multiple downstream methods. But it compacts the code by a factor 4 at least.
    public static Set<Integer> imageFilterTags(final List<String> imageMimeTypeFacets, final List<String> imageSizeFacets,
                                                final List<String> imageColourSpaceFacets, final List<String> imageAspectRatioFacets,
                                                final List<String> imageColourPaletteFacets) {
        return new FacetEncoder().getImageFacetFilterCodes(
            convert(imageMimeTypeFacets, CommonTagExtractor::decodeMimeType),
            convert(imageSizeFacets, ImageTagExtractor::decodeSize),
            convert(imageColourSpaceFacets, ImageTagExtractor::decodeColorSpace),
            convert(imageAspectRatioFacets, ImageTagExtractor::decodeAspectRatio),
            convert(imageColourPaletteFacets, ImageTagExtractor::decodeColor)
        );
    }

    public static Set<Integer> soundFilterTags(List<String> soundMimeTypeFacets, List<String> soundHQFacets, List<String> soundDurationFacets) {
        return new FacetEncoder().getAudioFacetFilterCodes(
            convert(soundMimeTypeFacets, CommonTagExtractor::decodeMimeType),
            convert(soundHQFacets, SoundTagExtractor::decodeQuality),
            convert(soundDurationFacets, SoundTagExtractor::decodeDuration)
        );
    }

    public static Set<Integer> videoFilterTags(List<String> videoMimeTypeFacets, List<String> videoHDFacets, List<String> videoDurationFacets) {
        return new FacetEncoder().getVideoFacetFilterCodes(
            convert(videoMimeTypeFacets, CommonTagExtractor::decodeMimeType),
            convert(videoHDFacets, VideoTagExtractor::decodeQuality),
            convert(videoDurationFacets, VideoTagExtractor::decodeDuration)
        );
    }

    public static Set<Integer> otherFilterTags(List<String> otherMimeTypeFacets) {
        return new FacetEncoder().getTextFacetFilterCodes(
            convert(otherMimeTypeFacets, CommonTagExtractor::decodeMimeType)
        );
    }

    private static <T> Set<T> convert(List<String> input, Function<String, T> converter) {
        if (input == null) return Collections.emptySet();
        return input.stream().map(converter).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    // specifically for producing the colour palette filter tags associated with the colourPalette parameter
    // i.e. as opposed to the colourPaletteFacets as occurring in the qf:refinements / facets
    public static List<Integer> colourPaletteFilterTags(List<String> colourPalette) {
        return new ArrayList<>(imageFilterTags(null, null, null, null, colourPalette));
    }
}