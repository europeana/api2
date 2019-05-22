package eu.europeana.api2.v2.model.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import eu.europeana.api2.v2.model.json.abstracts.AbstractSearchResults;
import eu.europeana.api2.v2.model.json.view.submodel.Facet;
import eu.europeana.api2.v2.model.json.view.submodel.HighlightHit;
import eu.europeana.api2.v2.model.json.view.submodel.SpellCheck;
import eu.europeana.corelib.definitions.model.web.BreadCrumb;

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

/**
 * @author Willem-Jan Boogerd
 */
@JsonInclude(NON_EMPTY)
public class SearchResults<T> extends AbstractSearchResults<T> {

    public List<BreadCrumb> breadCrumbs;

    public List<Facet> facets;

    public List<HighlightHit> hits;

    public SpellCheck spellcheck;

    public SearchResults(String apikey) {
        super(apikey);
    }
}
