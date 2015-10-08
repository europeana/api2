/*
 * Copyright 2007-2015 The Europeana Foundation
 *
 * Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 * by the European Commission;
 * You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 * http://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 * any kind, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under
 * the Licence.
 */

package eu.europeana.api2.v2.utils;

public enum FacetNames {
    MIME_TYPE("MIME_TYPE"),

    IS_FULLTEXT("TEXT_FULLTEXT"),
    HAS_MEDIA("MEDIA"),
    HAS_THUMBNAILS("THUMBNAILS"),

    IMAGE_SIZE("IMAGE_SIZE"),
    IMAGE_ASPECTRATIO("IMAGE_ASPECTRATIO"),
    IMAGE_COLOUR("IMAGE_COLOUR"),
    IMAGE_GREYSCALE("IMAGE_GREYSCALE"),

    COLOURPALETTE("COLOURPALETTE"),

    VIDEO_DURATION("VIDEO_DURATION"),
    VIDEO_HD("VIDEO_HD"),

    SOUND_HQ("SOUND_HQ"),
    SOUND_DURATION("SOUND_DURATION");

    private final String realName_;

    FacetNames(String realName) {
        realName_ = realName;
    }

    public String getRealName() {return realName_;}

}
