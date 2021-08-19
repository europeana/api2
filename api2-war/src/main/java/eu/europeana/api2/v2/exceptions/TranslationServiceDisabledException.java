package eu.europeana.api2.v2.exceptions;


import eu.europeana.corelib.web.exception.EuropeanaException;
import eu.europeana.corelib.web.exception.ProblemType;

/**
 * Exception that is thrown when a request comes in that requires a translation service, but the app has the
 * translation.enabled feature disabled
 */
public class TranslationServiceDisabledException extends EuropeanaException{

    public TranslationServiceDisabledException() {
        super(ProblemType.TRANSLATION_SERVICE_DISABLED);
    }
}
