package eu.europeana.api2.v2.model.json.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import eu.europeana.api2.model.json.abstracts.ApiResponse;
import eu.europeana.corelib.definitions.db.entity.relational.User;

import java.util.Date;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

/**
 * @deprecated 2018-01-09 old MyEuropeana functionality
 */
@JsonInclude(NON_EMPTY)
@Deprecated
public class Profile extends ApiResponse {

    public String email;

    public String userName;

    public Date registrationDate;

    public Date lastLogin;

    public String firstName;

    public String lastName;

    public String company;

    public String country;

    public String phone;

    public String address;

    public String website;

    public String fieldOfWork;

    public int nrOfSavedItems;

    public int nrOfSavedSearches;

    public int nrOfSocialTags;

    public Profile() {
    }

    public Profile(String apiKey) {
        super(apiKey);
    }

    public void copyDetails(User user) {
        email = user.getEmail();
        userName = user.getUserName();
        registrationDate = user.getRegistrationDate();
        lastLogin = user.getLastLogin();
        firstName = user.getFirstName();
        lastName = user.getLastName();
        company = user.getCompany();
        country = user.getCountry();
        phone = user.getPhone();
        address = user.getAddress();
        website = user.getWebsite();
        fieldOfWork = user.getFieldOfWork();
        nrOfSavedItems = user.getSavedItems().size();
        nrOfSavedSearches = user.getSavedSearches().size();
        nrOfSocialTags = user.getSocialTags().size();
    }

    public String getEmail() {
        return email;
    }

    public String getUserName() {
        return userName;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getCompany() {
        return company;
    }

    public String getCountry() {
        return country;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public String getWebsite() {
        return website;
    }

    public String getFieldOfWork() {
        return fieldOfWork;
    }

    public int getNrOfSavedItems() {
        return nrOfSavedItems;
    }

    public int getNrOfSavedSearches() {
        return nrOfSavedSearches;
    }

    public int getNrOfSocialTags() {
        return nrOfSocialTags;
    }

}
