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

public enum TagEncoding {

    TAG_TYPE(28, 3),
    MEDIA_TYPE(25, 3),
    MIME_TYPE(15, 10),
    IMAGE_SIZE(12, 3),
    IMAGE_COLOURSPACE(10, 2),
    IMAGE_ASPECTRATIO(8, 2),
    IMAGE_COLOUR(0, 8),
    SOUND_QUALITY(13, 2),
    SOUND_DURATION(10, 3),
    VIDEO_QUALITY(13, 2),
    VIDEO_DURATION(10, 3);

    private final int bitPos;
    private final int numOfBits;

    private TagEncoding(final int bitPos, final int numOfBits) {
        this.bitPos = bitPos;
        this.numOfBits = numOfBits;
    }

    public int getBitPos()           {return bitPos;}
    public int getNumOfBits()        {return numOfBits;}
    public int getMask()             {return ((1 << numOfBits) - 1) << bitPos;}
    public int extractValue(int tag) {return (tag & getMask()) >> bitPos;}
}
