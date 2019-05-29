package eu.europeana.api2.v2.model.json.user;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Date;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

/**
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 * @deprecated 2018-01-09 old MyEuropeana functionality
 */
@JsonInclude(NON_EMPTY)
@Deprecated
public class Search {

    public Long id;

    public String query;

    public String queryString;

    public Date dateSaved;

    public Long getId() {
        return id;
    }

    public String getQuery() {
        return query;
    }

    public String getQueryString() {
        return "query=" + queryString;
    }

    public Date getDateSaved() {
        return dateSaved;
    }
}
