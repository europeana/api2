package eu.europeana.api2.model.response.admin;

import eu.europeana.api2.model.json.abstracts.ApiResponse;
import eu.europeana.api2.model.request.admin.ApiKeyUpdate;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Willem-Jan Boogerd (www.eledge.net/contact).
 */
public class ApiKeyResponse extends ApiResponse {

    List<ApiKey> apiKeys = new ArrayList<>();

    public ApiKeyResponse() {
        // used by Jackson
        super();
    }

    public ApiKeyResponse(String apikey) {
        super(apikey);
    }

    public List<ApiKey> getApiKeys() {
        return apiKeys;
    }

    public class ApiKey extends ApiKeyUpdate {

        public String getPrivateKey() {
            return privateKey;
        }

        public String getPublicKey() {
            return publicKey;
        }

        public String getLevel() {
            return level;
        }
    }
}
