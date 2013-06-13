package eu.europeana.api2.v2.web.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import eu.europeana.corelib.db.entity.enums.RecordType;
import eu.europeana.corelib.db.service.ApiLogService;
import eu.europeana.corelib.db.service.OAuth2TokenService;
import eu.europeana.corelib.db.service.UserService;
import eu.europeana.corelib.definitions.db.entity.relational.User;

@Controller
public class RedirectController {

	@Resource
	private ApiLogService apiLogService;

	@Resource(name = "corelib_db_userService")
	private UserService userService;
	
	@Resource
	private OAuth2TokenService oAuth2TokenService;

	/*
	 * The page where you are redirected to the isShownAt and isShownBy links
	 */
	@RequestMapping(value = { "/{uid}/redirect", "/{uid}/redirect.json" }, method = RequestMethod.GET)
	public String handleRedirect(
			@PathVariable String uid,
			@RequestParam(value = "profile", required = false, defaultValue = "full") String profile,
			@RequestParam(value = "shownAt", required = true) String isShownAt,
			@RequestParam(value = "provider", required = true) String provider,
			@RequestParam(value = "id", required = true) String id,
			HttpServletRequest request) throws Exception {

		if (StringUtils.isBlank(isShownAt)) {
			throw new IllegalArgumentException(
					"Expected to find 'shownAt' in the request URL");
		}
		User user = userService.findByID(Long.parseLong(uid));
		String wskey = uid;
		if (user != null) {
			wskey = user.getApiKeys().iterator().next().getId();
		}
		apiLogService.logApiRequest(wskey, id, RecordType.REDIRECT, profile);

		return "redirect:" + isShownAt;
	}

	@RequestMapping(value = { "/clearTokens" }, method = RequestMethod.GET)
	public String removeAll() {
		oAuth2TokenService.removeAll();
		return "cleared";
	}
}
