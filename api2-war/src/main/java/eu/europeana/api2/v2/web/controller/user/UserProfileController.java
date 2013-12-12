package eu.europeana.api2.v2.web.controller.user;

import java.security.Principal;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import eu.europeana.api2.utils.JsonUtils;
import eu.europeana.api2.v2.model.json.user.Profile;
import eu.europeana.api2.v2.web.controller.abstracts.AbstractUserController;
import eu.europeana.corelib.definitions.db.entity.relational.User;

@Controller
public class UserProfileController extends AbstractUserController {

	@RequestMapping(value = "/v2/user/profile.json", produces = MediaType.APPLICATION_JSON_VALUE)
	public ModelAndView defaultAction(
			@RequestParam(value = "callback", required = false) String callback, 
			Principal principal) {
		Profile response = new Profile(getApiId(principal), "/v2/user/profile.json");
		User user = userService.findByEmail(principal.getName());
		if (user != null) {
			response.copyDetails(user);
		} else {
			response.success = false;
			response.error = "User Profile not retrievable...";
		}
		return JsonUtils.toJson(response, callback);
	}
	
}
