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

package eu.europeana.api2.web.controller.abstracts;

import java.security.Principal;

import javax.annotation.Resource;

import eu.europeana.corelib.db.exception.DatabaseException;
import eu.europeana.corelib.definitions.db.entity.relational.ApiKey;
import eu.europeana.corelib.definitions.db.entity.relational.User;
import org.apache.logging.log4j.LogManager;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

import eu.europeana.api2.model.utils.LinkUtils;
import eu.europeana.api2.v2.model.json.abstracts.UserObject;
import eu.europeana.corelib.db.service.ApiKeyService;
import eu.europeana.corelib.db.service.UserService;
import eu.europeana.corelib.definitions.db.entity.relational.abstracts.EuropeanaUserObject;
import eu.europeana.corelib.web.service.EuropeanaUrlService;

/**
 * @deprecated 2018-01-09 old MyEuropeana functionality
 */
@Deprecated
public abstract class AbstractUserController {

	@Resource(name = "corelib_db_userService")
	protected UserService userService;

	@Resource(name = "corelib_db_apiKeyService")
	protected ApiKeyService apiKeyService;
	
	@Resource
	private EuropeanaUrlService urlService;

	protected String getApiId(Principal principal) {
		if (principal instanceof OAuth2Authentication) {
			OAuth2Authentication authentication = (OAuth2Authentication) principal;
			return authentication.getOAuth2Request().getClientId();
		}
		return "invalid";
	}

	protected User getUserByApiId(String apiId) {
		User user = null;
		try {
			ApiKey apiKey = apiKeyService.findByID(apiId);
			if (apiKey != null) {
				user = userService.findByEmail(apiKey.getEmail());
			}
		} catch (DatabaseException e) {
			LogManager.getLogger(AbstractUserController.class).error("Error checking API key: {}", e.getMessage(), e);
		}
		return user;
	}

	protected User getUserByPrincipal(Principal principal) {
		if (principal != null) {
			return userService.findByEmail(principal.getName());
		}
		return null;
	}

	protected void copyUserObjectData(String wskey, UserObject to, EuropeanaUserObject from) {
		to.id = from.getId();
		to.europeanaId = from.getEuropeanaUri();
		to.title = from.getTitle();
		to.dateSaved = from.getDateSaved();
		to.type = from.getDocType();
		to.edmPreview = from.getEuropeanaObject();
		to.link = urlService.getApi2RecordJson(wskey, from.getEuropeanaUri()).toString();
		to.guid = LinkUtils.addCampaignCodes(urlService.getPortalRecord(from.getEuropeanaUri()), wskey);
	}

}
