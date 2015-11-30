package eu.europeana.api2.web.controller.admin;

import eu.europeana.api2.v2.web.swagger.SwaggerIgnore;
import eu.europeana.corelib.db.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;

/**
 * @author Willem-Jan Boogerd (www.eledge.net/contact).
 */
@Controller
@RequestMapping("/admin/user")
@SwaggerIgnore
public class UserAdminController {

    @Resource
    private UserService userService;


}
