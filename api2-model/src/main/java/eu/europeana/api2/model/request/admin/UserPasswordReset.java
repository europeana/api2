package eu.europeana.api2.model.request.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import eu.europeana.api2.model.entity.UserEntity;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

/**
 * @author Lúthien (maike.dulk@europeana.eu)
 * @deprecated 2018-01-09 old MyEuropeana functionality
 */
@Deprecated
@JsonInclude(NON_EMPTY)
public class UserPasswordReset extends UserEntity {

    private String redirect;

    private String token;

    private final String DEFAULTREDIRECTURL = "http://europeana.eu";

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public String getRedirect() {
        return redirect == null ? DEFAULTREDIRECTURL : redirect;
    }

    public void setRedirect(String redirect) {
        this.redirect = redirect;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
