package eu.europeana.api2.v2.exceptions;


import eu.europeana.corelib.web.exception.EuropeanaException;
import eu.europeana.corelib.web.exception.ProblemType;

/**
 * Exception that is thrown when there is an error using the translation service
 */
public class TranslationException extends EuropeanaException {

    public TranslationException(Exception e) {
        super(ProblemType.TRANSLATION_SERVICE_ERROR, e);
    }
}
