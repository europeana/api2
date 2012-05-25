package eu.europeana.api2.web.controller.abstracts;

import java.security.Principal;

import javax.annotation.Resource;

import org.springframework.security.oauth2.provider.OAuth2Authentication;

import eu.europeana.corelib.db.service.UserService;

public abstract class AbstractUserController {
	
	@Resource(name="corelib_db_userService")
	protected UserService userService;

	protected String getApiId(Principal principal) {
		if (principal instanceof OAuth2Authentication) {
			OAuth2Authentication authentication = (OAuth2Authentication) principal;
			return authentication.getAuthorizationRequest().getClientId();
		}
		return "testing";
	}

}
