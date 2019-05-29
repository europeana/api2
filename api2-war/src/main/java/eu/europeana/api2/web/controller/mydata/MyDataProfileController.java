package eu.europeana.api2.web.controller.mydata;

import eu.europeana.api2.utils.JsonUtils;
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
import org.springframework.web.servlet.ModelAndView;

import java.security.Principal;

/**
 * @deprecated 2018-01-09 old MyEuropeana functionality
 */
@Controller
@Api(value = "my_data", description = " ")
@Deprecated
public class MyDataProfileController extends AbstractUserController {

    @ApiOperation(value = "lets the user fetch their profile", nickname = "fetchMyDataProfile")
    @RequestMapping(value = "/mydata/profile", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    public ModelAndView defaultAction(
            @RequestParam(value = "callback", required = false) String callback,
            Principal principal) {
        Profile response = new Profile(principal.getName());
        User user = getUserByApiId(principal.getName());
        if (user != null) {
            response.copyDetails(user);
        } else {
            response.success = false;
            response.error = "Invalid credentials";
        }
        return JsonUtils.toJson(response, callback);
    }

}
