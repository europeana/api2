package eu.europeana.api2;

import eu.europeana.corelib.web.exception.EuropeanaException;
import eu.europeana.corelib.web.exception.ProblemType;

/**
 * Exception that is thrown when the provided apikey is invalid or missing
 */
public class ApiKeyException extends EuropeanaException {

    private static final long serialVersionUID = 1853292262382041306L;

    private final String apikey;

    /**
     * Create a new API-key exception
     */
    public ApiKeyException(ProblemType problemType, String apikey) {
        super(problemType);
        this.apikey = apikey;
    }

    public String getApikey() {
        return apikey;
    }

}
