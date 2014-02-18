package eu.europeana.api2.v2.utils;

import javax.servlet.http.HttpServletRequest;

import eu.europeana.corelib.db.entity.enums.RecordType;
import eu.europeana.corelib.definitions.db.entity.relational.ApiKey;

public class LimitRequest {

	private String url;
	private String apiCall;
	private RecordType recordType;
	private String wskey;
	private String callback;
	private ApiKey apiKey;
	private String profile;
	long requestNumber = 0;

	public LimitRequest(String url, String apiCall, RecordType recordType, String wskey, String callback, String profile) {
		super();
		this.url = url;
		this.apiCall = apiCall;
		this.recordType = recordType;
		this.wskey = wskey;
		this.callback = callback;
		this.profile = profile;
	}

	public String getUrl() {
		return url;
	}

	public String getApiCall() {
		return apiCall;
	}

	public RecordType getRecordType() {
		return recordType;
	}

	public String getWskey() {
		return wskey;
	}

	public String getCallback() {
		return callback;
	}

	public String getProfile() {
		return profile;
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
