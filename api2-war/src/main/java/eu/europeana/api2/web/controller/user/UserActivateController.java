package eu.europeana.api2.web.controller.user;

import eu.europeana.api2.v2.web.swagger.SwaggerIgnore;
import eu.europeana.corelib.db.exception.DatabaseException;
import eu.europeana.corelib.db.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Willem-Jan Boogerd (www.eledge.net/contact).
 */
@Controller
@RequestMapping(value = "/user/activate")
@SwaggerIgnore
public class UserActivateController {

    @Resource
    private UserService userService;

    @RequestMapping(
            value = "/{email:.+}/{token}",
            method = RequestMethod.GET
    )
    public String activate(
            @PathVariable String email,
            @PathVariable String token,
            HttpServletRequest request
    ) {
        try {
            userService.activate(email, token);
        } catch (DatabaseException ignore) {
        }
        return "redirect:" + request.getScheme() + "://www.europeana.eu";
    }
}
