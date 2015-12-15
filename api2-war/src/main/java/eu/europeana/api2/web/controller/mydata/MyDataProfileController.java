/*
 * Copyright 2007-2015 The Europeana Foundation
 *
 * Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 * by the European Commission;
 * You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 * http://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 * any kind, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under
 * the Licence.
 */

package eu.europeana.api2.web.controller.mydata;

import java.security.Principal;

import io.swagger.annotations.Api;
import eu.europeana.corelib.definitions.db.entity.relational.User;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import eu.europeana.api2.utils.JsonUtils;
import eu.europeana.api2.v2.model.json.user.Profile;
import eu.europeana.api2.web.controller.abstracts.AbstractUserController;
import eu.europeana.api2.v2.web.swagger.SwaggerSelect;

@Controller
@Api(value = "my_data", description = " ")
@SwaggerSelect
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
