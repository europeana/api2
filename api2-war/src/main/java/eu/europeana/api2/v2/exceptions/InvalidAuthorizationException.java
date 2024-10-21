package eu.europeana.api2.v2.exceptions;

import eu.europeana.corelib.web.exception.EuropeanaException;
import eu.europeana.corelib.web.exception.ProblemType;

public class InvalidAuthorizationException extends EuropeanaException {

    public InvalidAuthorizationException(ProblemType problemType) {
        super(problemType);
    }

}
