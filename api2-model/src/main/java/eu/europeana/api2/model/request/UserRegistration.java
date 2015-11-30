package eu.europeana.api2.model.request;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Date;

/**
 * @author Willem-Jan Boogerd (www.eledge.net/contact).
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_EMPTY)
public class UserRegistration {

    private String email;

    private String userName;

    private String password;

    private Date registrationDate;

    private Date lastLogin;

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

    public Date getLastLogin() {
        return lastLogin;
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

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public String getUserName() {
        return userName;
    }

    public String getWebsite() {
        return website;
    }
}
