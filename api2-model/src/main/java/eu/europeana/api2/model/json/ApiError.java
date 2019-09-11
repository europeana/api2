package eu.europeana.api2.model.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import eu.europeana.api2.model.json.abstracts.ApiResponse;
import eu.europeana.corelib.web.exception.EuropeanaException;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

/**
 * For serializing api error responses in JSON format
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 */
@JsonInclude(NON_EMPTY)
@JsonPropertyOrder({"apikey", "success", "error", "errorDetails", "errorCode", "requestNumber"})
public class ApiError extends ApiResponse {

    private String errorCode = null;
    private String errorDetails = null;

    public ApiError(String apikey, String errorMsg) {
        super(apikey);
        this.success = false;
        this.error = errorMsg;
    }

    public ApiError(String apikey, String errorMsg, String errorDetails) {
        super(apikey);
        this.success = false;
        this.error = errorMsg;
        this.errorDetails = errorDetails;
    }

    public ApiError(String apikey, EuropeanaException ex) {
        this(apikey, ex.getMessage(), ex.getErrorDetails());
        this.errorCode = ex.getErrorCode();
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorDetails() {
        return errorDetails;
    }
}
