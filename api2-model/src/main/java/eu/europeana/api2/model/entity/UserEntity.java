package eu.europeana.api2.model.entity;

import eu.europeana.corelib.definitions.users.Role;
import org.apache.commons.lang3.EnumUtils;

/**
 * @author Willem-Jan Boogerd (www.eledge.net/contact).
 */
public class UserEntity {

    protected Long   id;
    protected String email;
    protected String username;
    protected String password;
    protected String firstName;
    protected String lastName;
    protected String company;
    protected String country;
    protected String phone;
    protected String address;
    protected String website;
    protected String fieldOfWork;
    protected Role   role;

    public void setId(Long id) {
        this.id = id;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFieldOfWork(String fieldOfWork) {
        this.fieldOfWork = fieldOfWork;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public void setRole(Role role){
          this.role = role;
    }
}
