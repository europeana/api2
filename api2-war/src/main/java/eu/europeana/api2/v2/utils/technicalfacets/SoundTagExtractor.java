package eu.europeana.api2.v2.utils.technicalfacets;

import org.apache.commons.lang3.StringUtils;

/**
 * Extracts the pure tags from an audio resource and generates the fake tags.
 */
public class SoundTagExtractor {

//    private SoundTagExtractor(){}
//
//    public static Integer getQualityCode(final Integer bitDepth, final Integer sampleRate, final String fileFormat) {
//        if(bitDepth != null && sampleRate != null && bitDepth >= 16 && sampleRate >= 44100) {
//            return 1;
//        }
//        if(fileFormat != null && (fileFormat.equalsIgnoreCase("alac") || fileFormat.equalsIgnoreCase("flac") ||
//                fileFormat.equalsIgnoreCase("ape") || fileFormat.equalsIgnoreCase("shn") ||
//                fileFormat.equalsIgnoreCase("wav") || fileFormat.equalsIgnoreCase("wma") ||
//                fileFormat.equalsIgnoreCase("aiff") || fileFormat.equalsIgnoreCase("dsd"))) {
//            return 1;
//        }
//        return 0;
//    }
//
//    public static Integer getQualityCode(String soundHQ) {
//        return StringUtils.containsIgnoreCase(soundHQ, "true") ? 1 : 0 ;
//    }
//
//    public static String getQuality(Integer tag) {
//        final int qualityCode = TagEncoding.SOUND_QUALITY.extractValue(tag);
//        if (1 == qualityCode) {
//            return "true";
//        }
//        return "";
//    }
//
//    private static Integer getDurationCode(Long duration) {
//        if (duration == null)  return 0;
//        else if (duration <= 30000L) return 1;
//        else if (duration <= 180000L) return 2;
//        else if (duration <= 360000L) return 3;
//        else return 4;
//    }
//
//    public static String getDuration(Integer tag) {
//        final int durationCode = TagEncoding.SOUND_DURATION.extractValue(tag);
//        switch (durationCode) {
//            case 1:
//                return "very_short";
//            case 2:
//                return "short";
//            case 3:
//                return "medium";
//            case 4:
//                return "long";
//            default:
//                return "";
//        }
//    }
}
