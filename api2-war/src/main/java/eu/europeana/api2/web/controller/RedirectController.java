package eu.europeana.api2.web.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import eu.europeana.corelib.db.logging.api.ApiLogger;
import eu.europeana.corelib.db.logging.api.enums.RecordType;
import eu.europeana.corelib.db.service.UserService;
import eu.europeana.corelib.definitions.db.entity.relational.User;

@Controller
public class RedirectController {

	@Resource
	private ApiLogger apiLogger;

	@Resource(name = "corelib_db_userService")
	private UserService userService;

	/*
	 * The page where you are redirected to the isShownAt and isShownBy links
	 */
	@RequestMapping(value = "/{uid}/redirect.json", method = RequestMethod.GET)
	public String handleRedirect(
			@PathVariable String uid,
			@RequestParam(value = "profile", required = false, defaultValue = "full") String profile,
			@RequestParam(value = "shownAt", required = true) String isShownAt,
			@RequestParam(value = "provider", required = true) String provider,
			@RequestParam(value = "id", required = true) String id,
			HttpServletRequest request) throws Exception {

		String redirect;
		if (isShownAt != null) {
			redirect = isShownAt;
		} else {
			throw new IllegalArgumentException(
					"Expected to find 'shownAt' in the request URL");
		}
		User user = userService.findByID(Long.parseLong(uid));
		String wskey = uid;
		if (user != null) {
			wskey = user.getApiKeys().iterator().next().getId();
		}
		apiLogger.saveApiRequest(wskey, id, RecordType.REDIRECT, profile);

		return "redirect:" + redirect;
	}
}
