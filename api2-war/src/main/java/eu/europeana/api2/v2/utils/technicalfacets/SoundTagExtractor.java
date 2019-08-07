package eu.europeana.api2.v2.utils.technicalfacets;

import eu.europeana.indexing.solr.facet.EncodedFacet;
import eu.europeana.indexing.solr.facet.value.AudioDuration;
import eu.europeana.indexing.solr.facet.value.AudioQuality;
import org.apache.commons.lang3.StringUtils;

/**
 * Extracts the pure tags from an audio resource and generates the fake tags.
 */
public class SoundTagExtractor {

    private SoundTagExtractor(){}

    public static AudioQuality decodeQuality(String soundHQ) {
        return StringUtils.containsIgnoreCase(soundHQ, "true") ? AudioQuality.HIGH : null ;
    }

    public static String getQuality(Integer tag) {
        final AudioQuality quality = EncodedFacet.AUDIO_QUALITY.decodeValue(tag);
        if (AudioQuality.HIGH == quality) {
            return "true";
        }
        return "";
    }

    public static AudioDuration decodeDuration(String duration) {
        if (StringUtils.isBlank(duration)) return null;
        else if(StringUtils.equalsIgnoreCase(duration, "very_short")) return AudioDuration.TINY;
        else if(StringUtils.equalsIgnoreCase(duration, "short")) return AudioDuration.SHORT;
        else if(StringUtils.equalsIgnoreCase(duration, "medium")) return AudioDuration.MEDIUM;
        else if(StringUtils.equalsIgnoreCase(duration, "long")) return AudioDuration.LONG;
        else return null;
    }

    public static String getDuration(Integer tag) {
        final AudioDuration duration = EncodedFacet.AUDIO_DURATION.decodeValue(tag);
        if (duration == null) return "";
        switch (duration) {
            case TINY:
                return "very_short";
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
