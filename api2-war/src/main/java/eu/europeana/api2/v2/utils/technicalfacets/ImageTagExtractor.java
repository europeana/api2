package eu.europeana.api2.v2.utils.technicalfacets;

import eu.europeana.indexing.solr.facet.EncodedFacet;
import eu.europeana.indexing.solr.facet.value.ImageAspectRatio;
import eu.europeana.indexing.solr.facet.value.ImageColorEncoding;
import eu.europeana.indexing.solr.facet.value.ImageColorSpace;
import eu.europeana.indexing.solr.facet.value.ImageSize;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import eu.europeana.corelib.definitions.model.Orientation;

/**
 * Extracts the pure tags from an image resource and generates the fake tags.
 */
public class ImageTagExtractor {

    public static ImageSize decodeSize(final String imageSize) {
        if (StringUtils.isBlank(imageSize)) return null;
        switch (imageSize){
            case "small":
                return ImageSize.SMALL;
            case "medium":
                return ImageSize.MEDIUM;
            case "large":
                return ImageSize.LARGE;
            case "extra_large":
                return ImageSize.HUGE;
            default:
                return null;
        }
    }

    public static String getSize(int tag) {
        final ImageSize size = EncodedFacet.IMAGE_SIZE.decodeValue(tag);
        if (size == null) return "";
        switch (size) {
            case SMALL:
                return "small";
            case MEDIUM:
                return "medium";
            case LARGE:
                return "large";
            case HUGE:
                return "extra_large";
            default:
                return "";
        }
    }

    // TODO JV: should this not use the same string values as the method below ('true' and 'false')?
    public static ImageColorSpace decodeColorSpace(final String colorSpace) {
        if (StringUtils.isBlank(colorSpace)) return null;
        else if (StringUtils.containsIgnoreCase(colorSpace, "rgb")) return ImageColorSpace.COLOR;
        else if (StringUtils.containsIgnoreCase(colorSpace, "gray") || colorSpace.toLowerCase().contains("grey")) return ImageColorSpace.GRAYSCALE;
        else if (StringUtils.containsIgnoreCase(colorSpace, "cmyk")) return ImageColorSpace.OTHER;
        else return null;
    }

    public static String getColorSpace(int tag) {
        final ImageColorSpace colorSpace = EncodedFacet.IMAGE_COLOR_SPACE.decodeValue(tag);
        if (ImageColorSpace.GRAYSCALE == colorSpace) {
            return "false";
        } else if (ImageColorSpace.COLOR == colorSpace || ImageColorSpace.OTHER == colorSpace) {
            return "true";
        }
        return "";
    }

    public static ImageAspectRatio decodeAspectRatio(final String imageAspectRatio) {
        if (StringUtils.isBlank(imageAspectRatio)) return null;
        else if (imageAspectRatio.contains("portrait")) return ImageAspectRatio.PORTRAIT;
        else if (imageAspectRatio.contains("landscape")) return ImageAspectRatio.LANDSCAPE;
        else return null;
    }

    public static String getAspectRatio(int tag) {
        final ImageAspectRatio aspectRatio = EncodedFacet.IMAGE_ASPECT_RATIO.decodeValue(tag);
        if (aspectRatio == null) return "";
        switch (aspectRatio) {
            case PORTRAIT:
                return Orientation.getValue(Orientation.PORTRAIT);
            case LANDSCAPE:
                return Orientation.getValue(Orientation.LANDSCAPE);
            default:
                return "";
        }
    }

    public static ImageColorEncoding decodeColor(final String colour) {
        return ImageColorEncoding.categorizeImageColor(colour);
    }

    public static String getColor(int tag) {
        return Optional.ofNullable(EncodedFacet.IMAGE_COLOR_ENCODING.decodeValue(tag)).map(ImageColorEncoding::getHexStringWithHash).orElse("");
    }
}
