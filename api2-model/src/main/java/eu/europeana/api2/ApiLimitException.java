/*
 * Copyright 2007-2015 The Europeana Foundation
 *
 * Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 * by the European Commission;
 * You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 * http://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 * any kind, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under
 * the Licence.
 */

package eu.europeana.api2;

public class ApiLimitException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String apikey;
	private String action;
	private String error;
	private long requestNumber;
	private int httpStatus;

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

	public ApiLimitException(String apikey, String action, String error, long requestNumber, int httpStatus) {
		this(apikey, action, error, requestNumber);
		this.httpStatus = httpStatus;
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

	public int getHttpStatus() {
		return httpStatus;
	}
}
