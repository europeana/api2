package eu.europeana.api2.v2.service;



import eu.europeana.api.commons.definitions.vocabulary.Role;
import eu.europeana.api.commons.nosql.service.ApiWriteLockService;
import eu.europeana.api.commons.oauth2.service.impl.EuropeanaClientDetailsService;
import eu.europeana.api.commons.service.authorization.BaseAuthorizationService;
import eu.europeana.api2.model.utils.Api2UrlService;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.provider.ClientDetailsService;

/**
 * For Authorization using the api-commons
 */
public class SearchAuthorizationService extends BaseAuthorizationService {

  private static final Logger LOG = LogManager.getLogger(SearchAuthorizationService.class);

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

    EuropeanaClientDetailsService clientDetails = new EuropeanaClientDetailsService();
    clientDetails.setApiKeyServiceUrl(Api2UrlService.getBeanInstance().getApiKeyServiceurl());
    return clientDetails;
  }

  @Override
  protected String getApiName() {
    return null;
  }
}
