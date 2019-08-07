package eu.europeana.api2.v2.utils.technicalfacets;

import eu.europeana.indexing.solr.facet.EncodedFacet;
import eu.europeana.indexing.solr.facet.value.VideoDuration;
import eu.europeana.indexing.solr.facet.value.VideoQuality;
import org.apache.commons.lang3.StringUtils;

/**
 * Extracts the pure tags from a video resource and generates the fake tags.
 */
public class VideoTagExtractor {

    private VideoTagExtractor(){}

    public static VideoQuality decodeQuality(String videoQuality) {
        return StringUtils.containsIgnoreCase(videoQuality, "true") ? VideoQuality.HIGH : null ;
    }

    public static String getQuality(Integer tag) {
        final VideoQuality quality = EncodedFacet.VIDEO_QUALITY.decodeValue(tag);
        if (VideoQuality.HIGH == quality) {
            return "true";
        }
        return "";
    }

    public static VideoDuration decodeDuration(String duration) {
        if (StringUtils.isBlank(duration)) return null;
        else if(StringUtils.containsIgnoreCase(duration, "short")) return VideoDuration.SHORT;
        else if(StringUtils.containsIgnoreCase(duration, "medium")) return VideoDuration.MEDIUM;
        else if(StringUtils.containsIgnoreCase(duration, "long")) return VideoDuration.LONG;
        else return null;
    }

    public static String getDuration(Integer tag) {
        final VideoDuration duration = EncodedFacet.VIDEO_DURATION.decodeValue(tag);
        if (duration == null) return "";
        switch (duration) {
            case SHORT:
                return "short";
            case MEDIUM:
                return "medium";
            case LONG:
                return "long";
            default:
                return "";
        }
    }

}
