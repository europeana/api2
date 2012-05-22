package eu.europeana.api2.web.security;

import javax.annotation.Resource;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import eu.europeana.api2.web.security.model.Api2ClientDetails;
import eu.europeana.corelib.db.exception.DatabaseException;
import eu.europeana.corelib.db.service.ApiKeyService;
import eu.europeana.corelib.definitions.db.entity.relational.ApiKey;

public class ClientDetailsServiceImpl implements UserDetailsService {
	
	@Resource
	private ApiKeyService apiKeyService;

	@Override
	public UserDetails loadUserByUsername(String clientId) throws UsernameNotFoundException {
		try {
			ApiKey apiKey = apiKeyService.findByID(clientId);
			if (apiKey != null) {
				return new Api2ClientDetails(apiKey);
			}
		} catch (DatabaseException e) {
		}
		throw new UsernameNotFoundException(clientId);
	}

}
