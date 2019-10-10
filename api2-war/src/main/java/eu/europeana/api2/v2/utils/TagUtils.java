package eu.europeana.api2.v2.utils;

import eu.europeana.api2.v2.utils.technicalfacets.*;
import eu.europeana.corelib.definitions.model.Orientation;
import eu.europeana.indexing.solr.facet.FacetEncoder;
import eu.europeana.indexing.solr.facet.value.ImageColorEncoding;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * @author lúthien (maike.dulk@europeana.eu)
 */
public class TagUtils {

    private TagUtils(){}


    // specifically for producing the co
    // lour palette filter tags associated with the colourPalette parameter
    // i.e. as opposed to the colourPaletteFacets as occurring in the qf:refinements / facets
    // uses Metis facet library
    public static Set<Integer> encodeColourPalette(List<String> coloursToEncode){
        Set<ImageColorEncoding> imageColorEncodings = new HashSet<>();
        final FacetEncoder encoder = new FacetEncoder();
        for (String colourHexValue : coloursToEncode){
            imageColorEncodings.add(ImageColorEncoding.categorizeImageColor(colourHexValue));
        }
        return encoder.getImageColorCodes(imageColorEncodings);
    }

    public static Long getSizeCode(final String imageSize) {
        if (StringUtils.isBlank(imageSize)) return 0L;
        switch (imageSize){
            case "small":
                return 1L;
            case "medium":
                return 2L;
            case "large":
                return 3L;
            case "extra_large":
                return 4L;
            default:
                return 0L;
        }
    }

    public static long getVideoDurationCode(String duration) {
        if (StringUtils.isBlank(duration)) return 0L;
        else if(StringUtils.containsIgnoreCase(duration, "short")) return 1L;
        else if(StringUtils.containsIgnoreCase(duration, "medium")) return 2L;
        else if(StringUtils.containsIgnoreCase(duration, "long")) return 3L;
        else return 0L;
    }

    public static long getAudioDurationCode(String duration) {
        if (StringUtils.isBlank(duration)) return 0L;
        else if(StringUtils.equalsIgnoreCase(duration, "very_short")) return 1L;
        else if(StringUtils.equalsIgnoreCase(duration, "short")) return 2L;
        else if(StringUtils.equalsIgnoreCase(duration, "medium")) return 3L;
        else if(StringUtils.equalsIgnoreCase(duration, "long")) return 4L;
        else return 0L;
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


    // 1) converts Lists of any type to List of String
    // 2) replaces NULL Lists with List of String containing just "null" in order to create the default 'zero'
    // positions for every possible qf parameter (if they were NULL they would be skipped in the foreach loops)
    // 3) removes any duplicate values
    private static List<String> fixList(List<?> fixMe){
        ArrayList<String> retval = new ArrayList<>();
        if (fixMe != null && !fixMe.isEmpty()) fixMe.forEach(value -> { retval.add(value.toString());});
        else retval.add("null");
        return new ArrayList<>(new LinkedHashSet<>(retval));
    }

    // why only this property should have its own class representing exactly ONE bit of information?
    // Add that to the mysteries of life ...
    private static Orientation getImageOrientation(String imageAspectRatio){
        if (StringUtils.isBlank(imageAspectRatio)) return null;
        else if (imageAspectRatio.contains("portrait")) return Orientation.PORTRAIT;
        else if (imageAspectRatio.contains("landscape")) return Orientation.LANDSCAPE;
        else return null;
    }
}