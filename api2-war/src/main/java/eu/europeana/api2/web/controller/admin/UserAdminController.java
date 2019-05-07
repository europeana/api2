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
import eu.europeana.corelib.web.exception.ProblemType;
import eu.europeana.corelib.web.exception.EmailServiceException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;

/**
 * @author Willem-Jan Boogerd (www.eledge.net/contact).
 * @deprecated 2018-01-09 old MyEuropeana functionality
 */
//@RestController
//@RequestMapping("/admin/user")
@SwaggerIgnore
@Deprecated
public class UserAdminController {

//    @Resource
//    private UserService userService;
//
//    @Value("${api2.canonical.url}")
//    private String apiUrl;
//
//    @RequestMapping(value = "/{term:.+}",
//            method = {RequestMethod.GET},
//            produces = MediaType.APPLICATION_JSON_VALUE)
//    public ApiResponse find(
//            @PathVariable String term,
//            Principal principal
//    ) {
//        UserResponse response = new UserResponse(principal.getName());
//        try {
//            User user = StringUtils.contains(term, "@") ?
//                    userService.findByEmail(term) :
//                    NumberUtils.isNumber(term) ?
//                            userService.findByID(NumberUtils.createLong(term)) :
//                            userService.findByName(term);
//            if (user != null) {
//                response.getUsers().add(toEntity(user));
//            }
//        } catch (DatabaseException e) {
//            response.success = false;
//            response.error = e.getMessage();
//        }
//
//        return response;
//    }
//
//    @RequestMapping(
//            method = {RequestMethod.POST},
//            consumes = MediaType.APPLICATION_JSON_VALUE,
//            produces = MediaType.APPLICATION_JSON_VALUE
//    )
//    public ApiResponse createUser(
//            @RequestBody UserCreate registration,
//            Principal principal
//    ) {
//        ModificationConfirmation response = new ModificationConfirmation(principal.getName());
//        try {
//            userService.create(
//                    registration.getEmail(),
//                    registration.getUsername(),
//                    registration.getPassword(),
//                    registration.getCompany(),
//                    registration.getCountry(),
//                    registration.getFirstName(),
//                    registration.getLastName(),
//                    registration.getWebsite(),
//                    registration.getAddress(),
//                    registration.getPhone(),
//                    registration.getFieldOfWork(),
//                    registration.getRedirect(),
//                    apiUrl
//            );
//            response.success = true;
//        } catch (DatabaseException | EmailServiceException e) {
//            response.success = false;
//            response.error = e.getMessage();
//        }
//        return response;
//    }
//
//    @RequestMapping(value = "/{term:.+}",
//            method = {RequestMethod.PUT},
//            consumes = MediaType.APPLICATION_JSON_VALUE,
//            produces = MediaType.APPLICATION_JSON_VALUE
//    )
//    public ApiResponse updateUser(
//            @RequestBody UserCreate update,
//            @PathVariable String term,
//            Principal principal,
//            HttpServletResponse httpResponse) {
//        UserResponse response = new UserResponse(principal.getName());
//        response.success = false;
//
//        if (null == update.getEmail() || null == update.getUsername() || null == update.getPassword()){
//            if (null == update.getEmail()){
//                response.error = ProblemType.MISSING_PARAM_EMAIL.getMessage();
//            } else if (null == update.getUsername()){
//                response.error = ProblemType.MISSING_PARAM_USERNAME.getMessage();
//            } else {
//                response.error = ProblemType.MISSING_PARAM_PASSWORD.getMessage();
//            }
//            httpResponse.setStatus(400);
//            return response;
//        }
//
//        try {
//            User user = StringUtils.contains(term, "@") ?
//                        userService.findByEmail(term) :
//                        NumberUtils.isNumber(term) ?
//                        userService.findByID(NumberUtils.createLong(term)) :
//                        userService.findByName(term);
//            if (user != null) {
//                userService.update(user,
//                                update.getEmail(),
//                                update.getUsername(),
//                                update.getPassword(),
//                                update.getCompany(),
//                                update.getCountry(),
//                                update.getFirstName(),
//                                update.getLastName(),
//                                update.getWebsite(),
//                                update.getAddress(),
//                                update.getPhone(),
//                                update.getFieldOfWork());
//                response.success = true;
//                response.getUsers().add(toEntity(user));
//            } else {
//                response.error = ProblemType.NO_USER.getMessage();
//            }
//        } catch (DatabaseException e) {
//            httpResponse.setStatus(418);
//            e.printStackTrace();
//            response.error = e.getMessage();
//        }
//        return response;
//    }
//
//    @RequestMapping(value = "/{term:.+}",
//                    method = {RequestMethod.DELETE},
//                    consumes = MediaType.APPLICATION_JSON_VALUE,
//                    produces = MediaType.APPLICATION_JSON_VALUE
//    )
//    public ApiResponse deleteUser(
//            @PathVariable String term,
//            Principal principal,
//            HttpServletResponse httpResponse) {
//        UserResponse response = new UserResponse(principal.getName());
//        response.success = false;
//
//        try {
//            User user = StringUtils.contains(term, "@") ?
//                        userService.findByEmail(term) :
//                        NumberUtils.isNumber(term) ?
//                        userService.findByID(NumberUtils.createLong(term)) :
//                        userService.findByName(term);
//            if (user != null) {
//                userService.delete(user);
//                response.success = true;
//                httpResponse.setStatus(204); // no content
//            } else {
//                response.error = ProblemType.NO_USER.getMessage();
//            }
//        } catch (DatabaseException e) {
//            httpResponse.setStatus(500);
//            e.printStackTrace();
//            response.error = e.getMessage();
//        }
//        return response;
//    }
//
//    private UserResponse.User toEntity(User user) {
//        if (user != null) {
//            UserResponse.User entity = new UserResponse().new User();
//            entity.setId(user.getId());
//            entity.setEmail(user.getEmail());
//            entity.setUsername(user.getUserName());
//            entity.setFirstName(user.getFirstName());
//            entity.setLastName(user.getLastName());
//            entity.setCompany(user.getCompany());
//            entity.setCountry(user.getCountry());
//            entity.setPhone(user.getPhone());
//            entity.setAddress(user.getAddress());
//            entity.setWebsite(user.getWebsite());
//            entity.setFieldOfWork(user.getFieldOfWork());
//            entity.setRole(user.getRole());
//            return entity;
//        }
//        return null;
//    }
//
//    /**
//     *
//     * @param passwordReset
//     * @param principal
//     * @return
//     * @deprecated 2018-01-09 old MyEuropeana functionality
//     */
//    @RequestMapping(
//            path = {"/forgot"},
//            method = {RequestMethod.POST},
//            consumes = MediaType.APPLICATION_JSON_VALUE,
//            produces = MediaType.APPLICATION_JSON_VALUE
//    )
//    @Deprecated
//    public ApiResponse forgotPassword(
//            @RequestBody UserPasswordReset passwordReset,
//            Principal principal
//    ) {
//        ModificationConfirmation response = new ModificationConfirmation(principal.getName());
//        try {
//            userService.sendResetPasswordToken(
//                    passwordReset.getEmail(),
//                    passwordReset.getRedirect(),
//                    apiUrl
//            );
//            response.success = true;
//        } catch (DatabaseException | EmailServiceException e) {
//            response.success = false;
//            response.error = e.getMessage();
//        }
//        return response;
//    }
//
//    /**
//     *
//     * @param passwordReset
//     * @param principal
//     * @return
//     * @deprecated 2018-01-09 old MyEuropeana functionality
//     */
//    @RequestMapping(
//            path = {"/resetpassword"},
//            method = {RequestMethod.POST},
//            consumes = MediaType.APPLICATION_JSON_VALUE,
//            produces = MediaType.APPLICATION_JSON_VALUE
//    )
//    public ApiResponse resetPassword(
//            @RequestBody UserPasswordReset passwordReset,
//            Principal principal
//    ) {
//        ModificationConfirmation response = new ModificationConfirmation(principal.getName());
//        try {
//            userService.resetPassword(
//                    passwordReset.getEmail(),
//                    passwordReset.getToken(),
//                    passwordReset.getPassword()
//            );
//            response.success = true;
//        } catch (DatabaseException | EmailServiceException e) {
//            response.success = false;
//            response.error = e.getMessage();
//        }
//        return response;
//    }

}
