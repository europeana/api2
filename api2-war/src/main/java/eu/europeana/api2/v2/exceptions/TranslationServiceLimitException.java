package eu.europeana.api2.v2.exceptions;

import eu.europeana.corelib.web.exception.EuropeanaException;
import eu.europeana.corelib.web.exception.ProblemType;

public class TranslationServiceLimitException extends EuropeanaException {

    public TranslationServiceLimitException(Exception e) {
        super(ProblemType.TRANSLATION_SERVICE_LIMIT_ERROR, e);
    }
}

