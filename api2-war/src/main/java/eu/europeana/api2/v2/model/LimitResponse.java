package eu.europeana.api2.v2.model;

import eu.europeana.corelib.definitions.db.entity.relational.ApiKey;

public class LimitResponse {

	private ApiKey apiKey;
	private long requestNumber = 0;

	public LimitResponse(ApiKey apiKey, long requestNumber) {
		super();
		this.apiKey = apiKey;
		this.requestNumber = requestNumber;
	}

	public ApiKey getApiKey() {
		return apiKey;
	}

	public void setApiKey(ApiKey apiKey) {
		this.apiKey = apiKey;
	}

	public long getRequestNumber() {
		return requestNumber;
	}

	public void setRequestNumber(long requestNumber) {
		this.requestNumber = requestNumber;
	}
}
