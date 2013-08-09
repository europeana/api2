package eu.europeana.api2.v2.web.controller.abstracts;

import java.security.Principal;

import javax.annotation.Resource;

import org.springframework.security.oauth2.provider.OAuth2Authentication;

import eu.europeana.api2.v2.model.json.abstracts.UserObject;
import eu.europeana.corelib.db.service.UserService;
import eu.europeana.corelib.definitions.db.entity.relational.abstracts.EuropeanaUserObject;

public abstract class AbstractUserController {

	@Resource(name = "corelib_db_userService")
	protected UserService userService;

	protected String getApiId(Principal principal) {
		if (principal instanceof OAuth2Authentication) {
			OAuth2Authentication authentication = (OAuth2Authentication) principal;
			return authentication.getAuthorizationRequest().getClientId();
		}
		return "testing";
	}

	protected void copyUserObjectData(UserObject to, EuropeanaUserObject from) {
		to.id = from.getId();
		to.title = from.getTitle();
		to.dateSaved = from.getDateSaved();
		to.type = from.getDocType();
		to.edmPreview = from.getEuropeanaObject();
		to.europeanaId = from.getEuropeanaUri();
	}
}
