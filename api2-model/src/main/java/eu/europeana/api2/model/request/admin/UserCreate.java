package eu.europeana.api2.model.request.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import eu.europeana.api2.model.entity.UserEntity;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

/**
 * @author Willem-Jan Boogerd (www.eledge.net/contact).
 */
@JsonInclude(NON_EMPTY)
public class UserCreate extends UserEntity {

    // TODO default url?
    private String redirect;

    private final String DEFAULTREDIRECTURL = "http://europeana.eu";

    public String getAddress() {
        return address;
    }

    public String getCompany() {
        return company;
    }

    public String getCountry() {
        return country;
    }

    public String getEmail() {
        return email;
    }

    public String getFieldOfWork() {
        return fieldOfWork;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPassword() {
        return password;
    }

    public String getPhone() {
        return phone;
    }

    public String getUsername() {
        return username;
    }

    public String getWebsite() {
        return website;
    }

    public void setRedirect(String redirect) {
        this.redirect = redirect;
    }

    public String getRedirect() {
        return redirect == null ? DEFAULTREDIRECTURL : redirect;
    }
}
