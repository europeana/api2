package eu.europeana.api2.v2.service;



import eu.europeana.api.commons.definitions.vocabulary.Role;
import eu.europeana.api.commons.nosql.service.ApiWriteLockService;
import eu.europeana.api.commons.oauth2.service.impl.EuropeanaClientDetailsService;
import eu.europeana.api.commons.service.authorization.BaseAuthorizationService;
import javax.annotation.Resource;
import org.springframework.security.oauth2.provider.ClientDetailsService;

/**
 * For Authorization using the api-commons
 */
public class SearchAuthorizationService extends BaseAuthorizationService {

  @Resource(name = "searchClientDetails")
  ClientDetailsService clientDetailsService;


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
    clientDetails.setApiKeyServiceUrl("http://apikey.acceptance.eanadev.org/apikey");
    System.out.println("clientDetailsService:--------------------------> "+ (clientDetailsService!= null?clientDetailsService.toString():null));
    return clientDetails;
  }

  @Override
  protected String getApiName() {
    return null;
  }
}
