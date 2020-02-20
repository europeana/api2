package eu.europeana.api2.web.security.oauth2;

import eu.europeana.api2.web.security.model.OAuth2ClientDetails;
import eu.europeana.corelib.db.exception.DatabaseException;
import eu.europeana.corelib.db.service.ApiKeyService;
import eu.europeana.corelib.definitions.db.entity.relational.ApiKey;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * The entry point into the database of clients.
 */
@Service("api2_oauth2_clientDetailsService")
public class OAuth2ClientDetailsService implements ClientDetailsService {

    @Resource
    private ApiKeyService apiKeyService;

    @Override
    /**
     * Loads ClientDetails object belongs to an apiKey
     */
    public ClientDetails loadClientByClientId(String oAuthClientId)
            throws OAuth2Exception {
        try {
            ApiKey apiKey = apiKeyService.findByID(oAuthClientId);
            if (apiKey != null) {
                return new OAuth2ClientDetails(apiKey);
            }
        } catch (DatabaseException e) {
            LogManager.getLogger(this.getClass()).error(e.getMessage());
        }
        throw new OAuth2Exception("OAuth2 ClientId unknown");
    }
}
