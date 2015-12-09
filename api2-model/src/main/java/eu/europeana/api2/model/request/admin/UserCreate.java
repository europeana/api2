package eu.europeana.api2.model.request.admin;

import com.fasterxml.jackson.annotation.JsonInclude;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

/**
 * @author Willem-Jan Boogerd (www.eledge.net/contact).
 */
@JsonInclude(NON_EMPTY)
public class UserCreate {

    private String email;

    private String username;

    private String password;

    private String firstName;

    private String lastName;

    private String company;

    private String country;

    private String phone;

    private String address;

    private String website;

    private String fieldOfWork;

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
}
