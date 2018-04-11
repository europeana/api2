package eu.europeana.api2.model.request.admin;

import com.fasterxml.jackson.annotation.JsonInclude;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

/**
 * @author Willem-Jan Boogerd (www.eledge.net/contact).
 * @deprecated 2018-01-09 old MyEuropeana functionality
 */
@JsonInclude(NON_EMPTY)
@Deprecated
public class ApiKeyUpdate extends ApiKeyCreate {

    public Long getUsageLimit() {
        return usageLimit;
    }
}
