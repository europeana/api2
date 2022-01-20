package eu.europeana.api2.v2.exceptions;

import eu.europeana.corelib.web.exception.EuropeanaException;
import eu.europeana.corelib.web.exception.ProblemType;

/**
 * Thrown when a required parameters is missing (e.g. lang parameter when translate profile is specified)
 */
public class InvalidParamValueException extends EuropeanaException {

    public InvalidParamValueException(String message){
        super(ProblemType.INVALID_PARAMETER_VALUE, message);
    }

}
