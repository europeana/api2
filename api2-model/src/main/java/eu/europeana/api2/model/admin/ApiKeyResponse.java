package eu.europeana.api2.model.admin;

import eu.europeana.api2.model.json.abstracts.ApiResponse;

/**
 * @author Willem-Jan Boogerd (www.eledge.net/contact).
 */
public class ApiKeyResponse extends ApiResponse {

    public ApiKeyResponse() {
        // used by Jackson
        super();
    }

    public ApiKeyResponse(String apikey) {
        super(apikey);
    }


}
