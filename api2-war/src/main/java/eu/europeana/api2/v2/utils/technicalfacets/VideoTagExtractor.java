/*
 * Copyright 2007-2019 The Europeana Foundation
 *
 *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 *  by the European Commission;
 *  You may not use this work except in compliance with the Licence.
 *
 *  You may obtain a copy of the Licence at:
 *  http://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under
 *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 *  any kind, either express or implied.
 *  See the Licence for the specific language governing permissions and limitations under
 *  the Licence.
 */

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
