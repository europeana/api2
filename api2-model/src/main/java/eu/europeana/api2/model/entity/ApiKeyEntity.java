package eu.europeana.api2.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

/**
 * @author Willem-Jan Boogerd (www.eledge.net/contact).
 */
@JsonInclude(NON_EMPTY)
public abstract class ApiKeyEntity {

    protected String publicKey;

    protected String privateKey;

    protected String email;

    protected String firstName;

    protected String lastName;

    protected String company;

    protected String application;

    protected String website;

    protected String description;

    protected Long usageLimit;

    public void setApplication(String application) {
        this.application = application;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public void setUsageLimit(Long usageLimit) {
        this.usageLimit = usageLimit;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

}
