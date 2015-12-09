package eu.europeana.api2.model.response.admin;

import eu.europeana.api2.model.entity.ApiKeyEntity;
import eu.europeana.api2.model.json.abstracts.ApiResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Willem-Jan Boogerd (www.eledge.net/contact).
 */
public class ApiKeyResponse extends ApiResponse {

    List<ApiKeyEntity> apiKeys = new ArrayList<>();

    public ApiKeyResponse() {
        // used by Jackson
        super();
    }

    public ApiKeyResponse(String apikey) {
        super(apikey);
    }

    public List<ApiKeyEntity> getApiKeys() {
        return apiKeys;
    }
}
