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

public enum MediaTypeEncoding {
    IMAGE(1),
    AUDIO(2),
    VIDEO(3),
    TEXT(4),
    INVALID_VALUE(0);

    private final int value;

    MediaTypeEncoding(final int value) {
        this.value = value;
    }

    public int getValue()          {return value;}
    public int getEncodedValue()   {return value << TagEncoding.MEDIA_TYPE.getBitPos();}

    public static MediaTypeEncoding valueOf(final int tag) {
        for (MediaTypeEncoding encoding: MediaTypeEncoding.values()) {
            if (tag == encoding.getValue()) {
                return encoding;
            }
        }
        return INVALID_VALUE;
    }

}
