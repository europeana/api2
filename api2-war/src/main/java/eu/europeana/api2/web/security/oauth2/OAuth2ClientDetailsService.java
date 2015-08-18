/*
 * Copyright 2007-2015 The Europeana Foundation
 *
 * Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 * by the European Commission;
 * You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 * http://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 * any kind, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under
 * the Licence.
 */

package eu.europeana.api2.web.security.oauth2;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.provider.ClientDetails;

import eu.europeana.api2.web.security.model.Api2OAuth2ClientDetails;
import eu.europeana.corelib.db.exception.DatabaseException;
import eu.europeana.corelib.db.service.ApiKeyService;
import eu.europeana.corelib.definitions.db.entity.relational.ApiKey;

/**
 * The entry point into the database of clients.
 */
public class OAuth2ClientDetailsService implements
		org.springframework.security.oauth2.provider.ClientDetailsService {

	@Resource
	private ApiKeyService apiKeyService;

	@Override
	/**
	 * Loads ClientDetails object belongs to an apiKey
	 */
	public ClientDetails loadClientByClientId(String oauthClientId)
			throws OAuth2Exception {
		try {
			ApiKey apiKey = apiKeyService.findByID(oauthClientId);
			if (apiKey != null) {
				return new Api2OAuth2ClientDetails(apiKey.getId(),
						apiKey.getPrivateKey());
			}
		} catch (DatabaseException e) {
			Logger.getLogger(this.getClass()).error(e.getMessage());
			e.printStackTrace();
			
		}
		throw new OAuth2Exception("OAuth2 ClientId unknown");
	}
}
