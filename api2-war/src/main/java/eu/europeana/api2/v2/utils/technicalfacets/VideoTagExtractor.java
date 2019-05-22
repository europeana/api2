package eu.europeana.api2.v2.utils.technicalfacets;

import org.apache.commons.lang3.StringUtils;

/**
 * Extracts the pure tags from a video resource and generates the fake tags.
 */
public class VideoTagExtractor {

    private VideoTagExtractor(){}

    public static Integer getQualityCode(String videoQuality) {
        return StringUtils.containsIgnoreCase(videoQuality, "true") ? 1 : 0 ;
    }

    public static String getQuality(Integer tag) {
        final int qualityCode = TagEncoding.VIDEO_QUALITY.extractValue(tag);
        if (1 == qualityCode) {
            return "true";
        }
        return "";
    }

    public static Integer getDurationCode(String duration) {
        if (StringUtils.isBlank(duration)) return 0;
        else if(StringUtils.containsIgnoreCase(duration, "short")) return 1;
        else if(StringUtils.containsIgnoreCase(duration, "medium")) return 2;
        else if(StringUtils.containsIgnoreCase(duration, "long")) return 3;
        else return 0;
    }

    public static String getDuration(Integer tag) {
        final int durationCode = TagEncoding.VIDEO_DURATION.extractValue(tag);
        switch (durationCode) {
            case 1:
                return "short";
            case 2:
                return "medium";
            case 3:
                return "long";
            default:
                return "";
        }
    }

}
