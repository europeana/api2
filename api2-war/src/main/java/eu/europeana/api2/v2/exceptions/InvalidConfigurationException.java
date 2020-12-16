package eu.europeana.api2.v2.exceptions;

import eu.europeana.corelib.web.exception.EuropeanaException;
import eu.europeana.corelib.web.exception.ProblemType;


/**
 * Represents an exception that occurs when application configuration settings are either missing or invalid.
 */
public class InvalidConfigurationException extends EuropeanaException {

    public InvalidConfigurationException(ProblemType problem) {
        super(problem);
    }

    public InvalidConfigurationException(ProblemType problem, String errorDetails) {
        super(problem, errorDetails);
    }

    public InvalidConfigurationException(ProblemType problem, Throwable causedBy) {
        super(problem, causedBy);
    }

    public InvalidConfigurationException(ProblemType problem, String errorDetails, Throwable causedBy) {
        super(problem, errorDetails, causedBy);
    }
}
