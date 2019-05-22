package eu.europeana.api2;

/**
 * Exception that is thrown when the apikey is invalid, or if the number of requests if over it's daily maximum
 */
public class ApiLimitException extends Exception {

    private static final long serialVersionUID = 1L;

    private String apikey;
    private String error;
    private long requestNumber;
    private int httpStatus;

    public ApiLimitException(String apikey, String error) {
        super();
        this.apikey = apikey;
        this.error = error;
    }

    public ApiLimitException(String apikey, String error, long requestNumber) {
        this(apikey, error);
        this.requestNumber = requestNumber;
    }

    public ApiLimitException(String apikey, String error, long requestNumber, int httpStatus) {
        this(apikey, error, requestNumber);
        this.httpStatus = httpStatus;
    }

    public String getApikey() {
        return apikey;
    }

    public String getError() {
        return error;
    }

    public long getRequestNumber() {
        return requestNumber;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
