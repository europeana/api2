/*
 * Copyright 2007-2019 The Europeana Foundation
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

package eu.europeana.api2.v2.exceptions;


/**
 * Exception that is thrown when the apikey is invalid, or if the number of requests if over it's daily maximum
 */
public class DateMathParseException extends Exception{

    private static final long serialVersionUID = 3900055035686291976L;

    private final String parsing;
    private final String whatsParsed;

    public DateMathParseException(Throwable causedBy, String parsing, String whatsParsed) {
        super(causedBy);
        this.parsing        = parsing;
        this.whatsParsed    = whatsParsed;
    }

    public String getParsing() {
        return parsing;
    }
    public String getWhatsParsed() {
        return whatsParsed;
    }
}
