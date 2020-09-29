package eu.europeana.api2.v2.model;

import eu.europeana.corelib.definitions.db.entity.relational.ApiKey;

/**
 * @deprecated (since = "march 2019")
 */
@Deprecated
public class LimitResponse {

    private ApiKey apiKey;
    private long requestNumber = 0;

    @Deprecated
    public LimitResponse(ApiKey apiKey, long requestNumber) {
        super();
        this.apiKey = apiKey;
        this.requestNumber = requestNumber;
    }

    @Deprecated
    public ApiKey getApiKey() {
        return apiKey;
    }

    @Deprecated
    public void setApiKey(ApiKey apiKey) {
        this.apiKey = apiKey;
    }

    @Deprecated
    public long getRequestNumber() {
        return requestNumber;
    }

    @Deprecated
    public void setRequestNumber(long requestNumber) {
        this.requestNumber = requestNumber;
    }
}
