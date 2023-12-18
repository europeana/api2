package eu.europeana.api2.v2.utils;

import eu.europeana.api.commons.oauth2.model.impl.EuropeanaApiCredentials;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.Authentication;

/**
 * Utility class for checking API client keys
 *
 * @author Patrick Ehlert on 13-6-17.
 */
public class ApiKeyUtils{
    private static final Logger LOG                = LogManager.getLogger(ApiKeyUtils.class);

    /** Method to fetch ApiKey from authentication token
     * @param authentication Authentication object
     * @return apikey String
     */
    public static String extractApiKeyFromAuthorization(Authentication authentication) {
        Object credentials = authentication!=null?authentication.getCredentials():null;
        if (credentials instanceof EuropeanaApiCredentials) {
            EuropeanaApiCredentials europeanaCredentials = (EuropeanaApiCredentials) credentials;
            return europeanaCredentials.getApiKey();
        }
        LOG.error("Unable to extract key after Authorization !");
        return null;
    }
}
