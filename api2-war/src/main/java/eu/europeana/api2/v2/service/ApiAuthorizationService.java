package eu.europeana.api2.v2.service;

import eu.europeana.api.commons.definitions.vocabulary.Role;
import eu.europeana.api.commons.nosql.service.ApiWriteLockService;
import eu.europeana.api.commons.service.authorization.BaseAuthorizationService;
import javax.annotation.Resource;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.stereotype.Service;
import static eu.europeana.api2.v2.utils.ApiConstants.API_KEY_SERVICE_CLIENT_DETAILS;

/**
 * For Authorization using the api-commons
 */
@Service
public class ApiAuthorizationService extends BaseAuthorizationService {

  @Resource(name = API_KEY_SERVICE_CLIENT_DETAILS)
  ClientDetailsService clientService;

  @Override
  protected ApiWriteLockService getApiWriteLockService() {
    return null;
  }

  @Override
  protected Role getRoleByName(String s) {
    return null;
  }

  @Override
  protected String getSignatureKey() {
    return null;
  }

  @Override
  protected ClientDetailsService getClientDetailsService() {
    return clientService;
  }

  @Override
  protected String getApiName() {
    return null;
  }
}
