package eu.europeana.api2.model.request;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * @author Willem-Jan Boogerd (www.eledge.net/contact).
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_EMPTY)
public class ApiKeyUpdate extends ApiKeyCreate {

    private Long usageLimit;

    public Long getUsageLimit() {
        return usageLimit;
    }
}
