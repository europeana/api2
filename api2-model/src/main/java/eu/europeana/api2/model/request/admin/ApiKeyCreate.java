package eu.europeana.api2.model.request.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import eu.europeana.api2.model.entity.ApiKeyEntity;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

/**
 * @author Willem-Jan Boogerd (www.eledge.net/contact).
 */
@JsonInclude(NON_EMPTY)
public class ApiKeyCreate extends ApiKeyEntity {

    public String getApplication() {
        return application;
    }

    public String getCompany() {
        return company;
    }

    public String getDescription() {
        return description;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getWebsite() {
        return website;
    }
}
