package eu.europeana.api2.web.controller.admin;

import eu.europeana.api2.model.request.UserRegistration;
import eu.europeana.api2.utils.JsonUtils;
import eu.europeana.api2.v2.model.json.ModificationConfirmation;
import eu.europeana.api2.v2.web.swagger.SwaggerIgnore;
import eu.europeana.corelib.db.exception.DatabaseException;
import eu.europeana.corelib.db.service.UserService;
import eu.europeana.corelib.web.exception.EmailServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.security.Principal;

/**
 * @author Willem-Jan Boogerd (www.eledge.net/contact).
 */
@Controller
@RequestMapping("/admin/user")
@SwaggerIgnore
public class UserAdminController {

    @Resource
    private UserService userService;

    @Value("${api2.canonical.url}")
    private String apiUrl;

    @RequestMapping(
            method = {RequestMethod.POST},
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ModelAndView createUser(
            @RequestBody UserRegistration registration,
            @RequestParam(value = "callback", required = false) String callback,
            Principal principal
    ) {
        ModificationConfirmation response = new ModificationConfirmation(principal.getName());
        try {
            userService.create(
                    registration.getEmail(),
                    registration.getUsername(),
                    registration.getPassword(),
                    registration.getCompany(),
                    registration.getCountry(),
                    registration.getFirstName(),
                    registration.getLastName(),
                    registration.getWebsite(),
                    registration.getAddress(),
                    registration.getPhone(),
                    registration.getFieldOfWork(),
                    apiUrl);
            response.success = true;
        } catch (DatabaseException | EmailServiceException e) {
            response.success = false;
            response.error = e.getMessage();
        }
        return JsonUtils.toJson(response, callback);
    }

}
