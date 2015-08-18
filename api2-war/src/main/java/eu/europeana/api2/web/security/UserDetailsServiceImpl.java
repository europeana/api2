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

package eu.europeana.api2.web.security;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import eu.europeana.api2.web.security.model.Api2ClientDetails;
import eu.europeana.api2.web.security.model.Api2UserDetails;
import eu.europeana.corelib.db.exception.DatabaseException;
import eu.europeana.corelib.db.service.ApiKeyService;
import eu.europeana.corelib.db.service.UserService;
import eu.europeana.corelib.definitions.db.entity.relational.ApiKey;
import eu.europeana.corelib.definitions.db.entity.relational.User;

public class UserDetailsServiceImpl implements UserDetailsService {

	@Resource
	private ApiKeyService apiKeyService;

	@Resource
	private UserService userService;

	@Override
	public UserDetails loadUserByUsername(String key)
			throws UsernameNotFoundException {
		if (StringUtils.contains(key, "@")) {
			User user = userService.findByEmail(key);
			if (user != null) {
				return new Api2UserDetails(user);
			}
		} else {
			try {
				ApiKey apiKey = apiKeyService.findByID(key);
				if (apiKey != null) {
					return new Api2ClientDetails(apiKey);
				}
			} catch (DatabaseException e) {
				// ignore
			}
		}
		throw new UsernameNotFoundException(key);
	}

}
