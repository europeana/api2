package eu.europeana.api2.model.enums;

public class ApiLimitException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String apikey;
	private String action;
	private String error;
	private long requestNumber;

	public ApiLimitException(String apikey, String action, String error) {
		super();
		this.apikey = apikey;
		this.action = action;
		this.error = error;
	}

	public ApiLimitException(String apikey, String action, String error, long requestNumber) {
		this(apikey, action, error);
		this.requestNumber = requestNumber;
	}

	public String getApikey() {
		return apikey;
	}

	public String getAction() {
		return action;
	}

	public String getError() {
		return error;
	}

	public long getRequestNumber() {
		return requestNumber;
	}
}
