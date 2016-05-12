package eu.europeana.api2.web.controller.admin;

import eu.europeana.api2.model.json.abstracts.ApiResponse;
import eu.europeana.api2.model.request.admin.UserCreate;
import eu.europeana.api2.model.request.admin.UserPasswordReset;
import eu.europeana.api2.model.response.admin.UserResponse;
import eu.europeana.api2.v2.model.json.ModificationConfirmation;
import eu.europeana.api2.v2.web.swagger.SwaggerIgnore;
import eu.europeana.corelib.db.exception.DatabaseException;
import eu.europeana.corelib.db.service.UserService;
import eu.europeana.corelib.definitions.db.entity.relational.User;
import eu.europeana.corelib.web.exception.EmailServiceException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.security.Principal;

/**
 * @author Willem-Jan Boogerd (www.eledge.net/contact).
 */
@RestController
@RequestMapping("/admin/user")
@SwaggerIgnore
public class UserAdminController {

    @Resource
    private UserService userService;

    @Value("${api2.canonical.url}")
    private String apiUrl;

    @RequestMapping(value = "/{term:.+}",
            method = {RequestMethod.GET},
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse find(
            @PathVariable String term,
            Principal principal
    ) {
        UserResponse response = new UserResponse(principal.getName());
        try {
            User user = StringUtils.contains(term, "@") ?
                    userService.findByEmail(term) :
                    NumberUtils.isNumber(term) ?
                            userService.findByID(NumberUtils.createLong(term)) :
                            userService.findByName(term);
            if (user != null) {
                response.getUsers().add(toEntity(user));
            }
        } catch (DatabaseException e) {
            response.success = false;
            response.error = e.getMessage();
        }

        return response;
    }

    @RequestMapping(
            method = {RequestMethod.POST},
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ApiResponse createUser(
            @RequestBody UserCreate registration,
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
                    registration.getRedirect(),
                    apiUrl
            );
            response.success = true;
        } catch (DatabaseException | EmailServiceException e) {
            response.success = false;
            response.error = e.getMessage();
        }
        return response;
    }

    private UserResponse.User toEntity(User user) {
        if (user != null) {
            UserResponse.User entity = new UserResponse().new User();
            entity.setId(user.getId());
            entity.setEmail(user.getEmail());
            entity.setUsername(user.getUserName());
            entity.setFirstName(user.getFirstName());
            entity.setLastName(user.getLastName());
            entity.setCompany(user.getCompany());
            entity.setCountry(user.getCountry());
            entity.setPhone(user.getPhone());
            entity.setAddress(user.getAddress());
            entity.setWebsite(user.getWebsite());
            entity.setFieldOfWork(user.getFieldOfWork());
            return entity;
        }
        return null;
    }

    @RequestMapping(
            path = {"/forgot"},
            method = {RequestMethod.POST},
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ApiResponse forgotPassword(
            @RequestBody UserPasswordReset passwordReset,
            Principal principal
    ) {
        ModificationConfirmation response = new ModificationConfirmation(principal.getName());
        try {
            userService.sendResetPasswordToken(
                    passwordReset.getEmail(),
                    passwordReset.getRedirect(),
                    apiUrl
            );
            response.success = true;
        } catch (DatabaseException | EmailServiceException e) {
            response.success = false;
            response.error = e.getMessage();
        }
        return response;
    }

    @RequestMapping(
            path = {"/resetpassword"},
            method = {RequestMethod.POST},
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ApiResponse resetPassword(
            @RequestBody UserPasswordReset passwordReset,
            Principal principal
    ) {
        ModificationConfirmation response = new ModificationConfirmation(principal.getName());
        try {
            userService.resetPassword(
                    passwordReset.getEmail(),
                    passwordReset.getToken(),
                    passwordReset.getPassword()
            );
            response.success = true;
        } catch (DatabaseException | EmailServiceException e) {
            response.success = false;
            response.error = e.getMessage();
        }
        return response;
    }

}
