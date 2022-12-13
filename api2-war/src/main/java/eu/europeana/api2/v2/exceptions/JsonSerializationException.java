package eu.europeana.api2.v2.exceptions;

import eu.europeana.corelib.web.exception.EuropeanaException;
import eu.europeana.corelib.web.exception.ProblemType;

public class JsonSerializationException extends EuropeanaException {

    public JsonSerializationException(Exception e) {
        super(ProblemType.SERIALIZATION_ERROR, e.getMessage(), e);
    }
}