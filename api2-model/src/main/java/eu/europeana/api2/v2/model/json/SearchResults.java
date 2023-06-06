package eu.europeana.api2.v2.model.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import eu.europeana.api2.v2.model.json.abstracts.AbstractSearchResults;
import eu.europeana.api2.v2.model.json.view.submodel.Facet;
import eu.europeana.api2.v2.model.json.view.submodel.HighlightHit;
import eu.europeana.api2.v2.model.json.view.submodel.SpellCheck;

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

/**
 * @author Willem-Jan Boogerd
 */
@JsonInclude(NON_EMPTY)
public class SearchResults<T> extends AbstractSearchResults<T> {

    public List<Facet> facets;

    public List<HighlightHit> hits;

    @Deprecated(since = "May 2021") // not used
    public SpellCheck spellcheck;

    public SearchResults(String apikey) {
        super(apikey);
    }
}
