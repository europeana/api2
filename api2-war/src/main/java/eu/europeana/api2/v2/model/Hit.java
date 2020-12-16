package eu.europeana.api2.v2.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import eu.europeana.api2.utils.SolrEscapeDeserializer;

/**
 * Model class for Hit field for Search requests
 *
 * Sanitized fields correspond to request parameters annotated with @SolrEscape in
 * {@link eu.europeana.api2.v2.web.controller.SearchController#searchJsonGet}
 *
 * @author Srishti Singh
 * Created on 29 Sep 2020
 */
public class Hit {

    @JsonDeserialize(using = SolrEscapeDeserializer.class)
    private String fl ;
    private String selectors;

    public String getFl() {
        return fl;
    }

    public void setFl(String fl) {
        this.fl = fl;
    }

    public String getSelectors() {
        return selectors;
    }

    public void setSelectors(String selectors) {
        this.selectors = selectors;
    }

    @Override
    public String toString() {
        return "Hit{" +
                "fl='" + fl + '\'' +
                ", selectors='" + selectors + '\'' +
                '}';
    }
}
