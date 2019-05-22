package eu.europeana.api2.web.controller.user;

import eu.europeana.api2.model.json.abstracts.ApiResponse;
import eu.europeana.api2.v2.model.json.user.Profile;
import eu.europeana.api2.web.controller.abstracts.AbstractUserController;
import eu.europeana.corelib.definitions.db.entity.relational.User;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;

/**
 * @deprecated 2018-01-09 old MyEuropeana functionality
 */
@Controller
@RequestMapping(value = "/user/profile")
@Api(value = "my_europeana", description = " ")
@Deprecated
public class UserProfileController extends AbstractUserController {

    @ApiOperation(value = "fetch a user's profile", nickname = "fetchUserProfile")
    @RequestMapping(
            produces = MediaType.APPLICATION_JSON_VALUE,
            method = RequestMethod.GET
    )
    @ResponseBody
    public ApiResponse get(
            @RequestParam(value = "callback", required = false) String callback,
            Principal principal) {
        Profile response = new Profile(getApiId(principal));
        User user = getUserByPrincipal(principal);
        if (user != null) {
            response.copyDetails(user);
        } else {
            response.success = false;
            response.error = "User Profile not retrievable...";
        }
        return response;
//        return JsonUtils.toJson(response, callback);
    }

}
