package eu.europeana.api2.model.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import eu.europeana.api2.model.json.abstracts.ApiResponse;
import eu.europeana.corelib.web.exception.EuropeanaException;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

/**
 * For serializing api error responses in JSON format
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 */
@JsonInclude(NON_EMPTY)
@JsonPropertyOrder({"apikey", "success", "error", "message", "code", "requestNumber"})
public class ApiError extends ApiResponse {

    private String code = null;
    @JsonProperty(value = "message")
    private String details = null;

    public ApiError(String apikey, String error) {
        super(apikey);
        this.success = false;
        this.requestNumber = null;
        this.error = error;
    }

    public ApiError(String apikey, String error, String details, String code) {
        super(apikey);
        this.success = false;
        this.requestNumber = null;
        this.error = error;
        this.details = details;
        this.code = code;
    }

    public ApiError(String apikey, EuropeanaException ex) {
        this(apikey, ex.getMessage(), ex.getErrorDetails(), ex.getErrorCode());
        this.code = ex.getErrorCode();
    }

    /**
     * Europeana specific error code. We generally use codes in the form of <http status code>-<letter>
     * @return String containing Europeana error code
     */
    public String getCode() {
        return code;
    }

    public String getDetails() {
        return details;
    }
}
