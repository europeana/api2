package eu.europeana.api2.v2.exceptions;

import eu.europeana.corelib.web.exception.EuropeanaException;
import eu.europeana.corelib.web.exception.ProblemType;

/**
 * This exception is thrown if the Translation API client has returned 5xx errors.
 */
public class TranslationServiceNotAvailableException extends EuropeanaException {

    public TranslationServiceNotAvailableException(String msg, Exception e) {
        super(ProblemType.TRANSLATION_SERVICE_LIMIT_ERROR, msg, e);
    }
}

