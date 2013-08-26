package eu.europeana.api2.v2.web.controller.abstracts;

import java.security.Principal;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

import eu.europeana.api2.model.utils.LinkUtils;
import eu.europeana.api2.v2.model.json.abstracts.UserObject;
import eu.europeana.corelib.db.service.UserService;
import eu.europeana.corelib.definitions.db.entity.relational.abstracts.EuropeanaUserObject;

public abstract class AbstractUserController {

	@Resource(name = "corelib_db_userService")
	protected UserService userService;

	@Value("#{europeanaProperties['api2.url']}")
	private String apiUrl;
	
	@Value("#{europeanaProperties['portal.server']}")
	private String portalServer;

	@Value("#{europeanaProperties['portal.name']}")
	private String portalName;
	
	private String portalUrl;
	
	protected String getApiId(Principal principal) {
		if (principal instanceof OAuth2Authentication) {
			OAuth2Authentication authentication = (OAuth2Authentication) principal;
			return authentication.getAuthorizationRequest().getClientId();
		}
		return "testing";
	}

	protected void copyUserObjectData(String wskey, UserObject to, EuropeanaUserObject from) {
		to.id = from.getId();
		to.europeanaId = from.getEuropeanaUri();
		to.title = from.getTitle();
		to.dateSaved = from.getDateSaved();
		to.type = from.getDocType();
		to.edmPreview = from.getEuropeanaObject();
		to.link = LinkUtils.getLink(wskey, apiUrl, from.getEuropeanaUri());
		to.guid = LinkUtils.getGuid(wskey, getPortalUrl(), from.getEuropeanaUri());
	}

	private String getPortalUrl() {
		if (portalUrl == null) {
			StringBuilder sb = new StringBuilder(portalServer);
			if (!portalServer.endsWith("/") && !portalName.startsWith("/")) {
				sb.append("/");
			}
			sb.append(portalName);
			portalUrl = sb.toString();
		}
		return portalUrl;
	}
	
}
