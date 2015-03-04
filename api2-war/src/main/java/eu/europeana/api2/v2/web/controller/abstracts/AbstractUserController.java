package eu.europeana.api2.v2.web.controller.abstracts;

import java.security.Principal;

import javax.annotation.Resource;

import org.springframework.security.oauth2.provider.OAuth2Authentication;

import eu.europeana.api2.model.utils.LinkUtils;
import eu.europeana.api2.v2.model.json.abstracts.UserObject;
import eu.europeana.corelib.db.service.ApiKeyService;
import eu.europeana.corelib.db.service.UserService;
import eu.europeana.corelib.definitions.db.entity.relational.abstracts.EuropeanaUserObject;
import eu.europeana.corelib.web.service.EuropeanaUrlService;

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

	protected void copyUserObjectData(String wskey, UserObject to, EuropeanaUserObject from) {
		to.id = from.getId();
		to.europeanaId = from.getEuropeanaUri();
		to.title = from.getTitle();
		to.dateSaved = from.getDateSaved();
		to.type = from.getDocType();
		to.edmPreview = from.getEuropeanaObject();
		to.link = urlService.getApi2RecordJson(wskey, from.getEuropeanaUri()).toString();
		to.guid = LinkUtils.addCampaignCodes(urlService.getPortalRecord(false, from.getEuropeanaUri()), wskey);
	}

}
