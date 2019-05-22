package eu.europeana.api2.model.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import eu.europeana.api2.ApiLimitException;
import eu.europeana.api2.model.json.abstracts.ApiResponse;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

/**
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 */
@JsonInclude(NON_EMPTY)
public class ApiError extends ApiResponse {

	public boolean success = false;

	public ApiError(String apikey, String error) {
		super(apikey);
		this.error = error;
	}

	public ApiError(String apikey, String error,
					long requestNumber) {
		this(apikey, error);
		this.requestNumber = requestNumber;
	}

	public ApiError(ApiLimitException ex) {
		this(ex.getApikey(), ex.getError(), ex.getRequestNumber());
	}
}
