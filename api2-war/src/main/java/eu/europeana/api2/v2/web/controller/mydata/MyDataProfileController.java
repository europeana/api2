package eu.europeana.api2.v2.web.controller.mydata;

import java.security.Principal;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import eu.europeana.api2.utils.JsonUtils;
import eu.europeana.api2.v2.model.json.user.Profile;
import eu.europeana.api2.v2.web.controller.abstracts.AbstractUserController;
import eu.europeana.api2.v2.web.swagger.SwaggerSelect;
import eu.europeana.corelib.db.exception.DatabaseException;
import eu.europeana.corelib.definitions.db.entity.relational.ApiKey;

@Controller
//@Api(value = "my_europeana", description = " ")
//@SwaggerSelect
public class MyDataProfileController extends AbstractUserController {

	/**
	 * @param callback
	 * @param principal
	 * @return the JSON response
	 */
	@ApiOperation(value = "lets the user fetch their profile", nickname = "fetchMyDataProfile")
	@RequestMapping(value = "/v2/mydata/profile.json", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
	public ModelAndView defaultAction(
			@RequestParam(value = "callback", required = false) String callback,
			Principal principal) {
		Profile response = new Profile(principal.getName(), "/v2/user/profile.json");
		try {
			ApiKey apiKey = apiKeyService.findByID(principal.getName());
			if (apiKey != null) {
				response.copyDetails(apiKey.getUser());
			} else {
				response.success = false;
				response.error = "Invalid credentials";
			}
		} catch (DatabaseException e) {
			response.success = false;
			response.error = e.getMessage();
		}
		return JsonUtils.toJson(response, callback);
	}
	
}
