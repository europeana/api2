package eu.europeana.api2.web.controller.user;

import eu.europeana.corelib.db.exception.DatabaseException;
import eu.europeana.corelib.db.service.UserService;
import eu.europeana.corelib.definitions.db.entity.relational.Token;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Willem-Jan Boogerd (www.eledge.net/contact).
 */
@Controller
@RequestMapping(value = "/user/activate")
//@SwaggerIgnore - note that classes are not included by default, so it's not necessary to explicitly tag them
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
            Token tokenEntity = userService.activate(email, token);
            return "redirect:" + tokenEntity.getRedirect();
        } catch (DatabaseException ignore) {
        }
        return "redirect:" + request.getScheme() + "://www.europeana.eu";
    }
}
