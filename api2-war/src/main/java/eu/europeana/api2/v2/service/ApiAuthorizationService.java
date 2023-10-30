package eu.europeana.api2.v2.service;


import eu.europeana.api.commons.definitions.vocabulary.Role;
import eu.europeana.api.commons.nosql.service.ApiWriteLockService;
import eu.europeana.api.commons.service.authorization.BaseAuthorizationService;
import eu.europeana.api2.v2.utils.ApiConstants;
import javax.annotation.Resource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.stereotype.Service;

/**
 * For Authorization using the api-commons
 */
@Service
public class ApiAuthorizationService extends BaseAuthorizationService {

  private static final Logger LOG = LogManager.getLogger(ApiAuthorizationService.class);

  @Resource(name = ApiConstants.API_KEY_SERVICE_CLIENT_DETAILS)
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
