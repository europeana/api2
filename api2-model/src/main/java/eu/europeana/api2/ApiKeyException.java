package eu.europeana.api2;

import eu.europeana.corelib.web.exception.EuropeanaException;
import eu.europeana.corelib.web.exception.ProblemType;

/**
 * Exception that is thrown when the apikey is invalid
 */
public class ApiKeyException extends EuropeanaException {

    private static final long serialVersionUID = 1853292262382041306L;

    private final String apikey;
    private final String error;

    public ApiKeyException(String apikey, String error) {
        super(ProblemType.APIKEY_INVALID);
        this.apikey = apikey;
        this.error = error;
    }

    public String getApikey() {
        return apikey;
    }

    public String getError() {
        return error;
    }

}
