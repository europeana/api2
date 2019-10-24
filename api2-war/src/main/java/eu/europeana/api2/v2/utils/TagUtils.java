package eu.europeana.api2.v2.utils;

import eu.europeana.indexing.solr.facet.FacetEncoder;
import eu.europeana.indexing.solr.facet.value.AudioDuration;
import eu.europeana.indexing.solr.facet.value.ImageColorEncoding;
import eu.europeana.indexing.solr.facet.value.ImageSize;
import eu.europeana.indexing.solr.facet.value.VideoDuration;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author lúthien (maike.dulk@europeana.eu)
 */
public class TagUtils {

    private TagUtils() {}

    /**
     * This method is for producing the colour palette filter tags associated with the colourPalette parameter:
     * ../search.json?query=*&colourpalette=%234682b4,%23a9a9a9& ...
     * The faceted COLOURPALETTE refinement (qf=COLOURPALETTE:%234682b4&qf=COLOURPALETTE:%23a9a9a9) is encoded
     * directly in the ImageColorEncoding class
     *
     * @param coloursToEncode a List of Strings containing the hexadecimal colours to encode
     * @return Set of Integers containing the encoded colours
     */
    public static Set<Integer> encodeColourPalette(List<String> coloursToEncode) {
        Set<ImageColorEncoding> encodedColours = new HashSet<>();
        final FacetEncoder      encoder        = new FacetEncoder();
        for (String colourHexValue : coloursToEncode) {
            encodedColours.add(ImageColorEncoding.categorizeImageColor(colourHexValue));
        }
        return encoder.getImageFacetValueCodes(null, null, null, null, encodedColours);
    }

    /**
     * Returns ImageSize enum that matches the input size String
     * @param size String ("small"|"medium"|"large"|"extra_large" or "huge" <- used by Metis)
     * @return ImageSize that matches the input value
     */
    public static ImageSize getImageSize(final String size) {
        if (StringUtils.isBlank(size)) return null;
        switch (size) {
            case "small":
                return ImageSize.SMALL;
            case "medium":
                return ImageSize.MEDIUM;
            case "large":
                return ImageSize.LARGE;
            case "huge":
            case "extra_large":
                return ImageSize.HUGE;
            default:
                return null;
        }
    }

    /**
     * Returns VideoDuration enum that matches the input duration String
     * @param duration String ("short"|"medium"|"long")
     * @return VideoDuration that matches the input value
     */
    public static VideoDuration getVideoDurationCode(String duration) {
        if (StringUtils.isBlank(duration)) return null;
        switch (duration) {
            case "short":
                return VideoDuration.SHORT;
            case "medium":
                return VideoDuration.MEDIUM;
            case "long":
                return VideoDuration.LONG;
            default:
                return null;
        }
    }

    /**
     * Returns AudioDuration enum that matches the input duration String
     * @param duration String ("very_short" or "tiny" (used by Metis)|"short"|"medium"|"long")
     * @return AudioDuration that matches the input value
     */
    public static AudioDuration getAudioDurationCode(String duration) {
        if (StringUtils.isBlank(duration)) return null;
        switch (duration) {
            case "tiny":
            case "very_short":
                return AudioDuration.TINY;
            case "short":
                return AudioDuration.SHORT;
            case "medium":
                return AudioDuration.MEDIUM;
            case "long":
                return AudioDuration.LONG;
            default:
                return null;
        }
    }


    public static boolean isImageMimeType(String type) {
        return (StringUtils.startsWithIgnoreCase(type, "image"));
    }

    public static boolean isSoundMimeType(String type) {
        return (StringUtils.startsWithIgnoreCase(type, "sound") || StringUtils.startsWithIgnoreCase(type, "audio"));
    }

    public static boolean isVideoMimeType(String type) {
        return (StringUtils.startsWithIgnoreCase(type, "video"));
    }

}