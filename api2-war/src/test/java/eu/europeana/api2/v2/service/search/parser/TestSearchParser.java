package eu.europeana.api2.v2.service.search.parser;

import eu.europeana.api2.v2.service.search.syntax.field.FieldDeclaration;
import eu.europeana.api2.v2.service.search.syntax.field.FieldRegistry;
import eu.europeana.api2.v2.service.search.syntax.function.DateContainsFunction;
import eu.europeana.api2.v2.service.search.syntax.function.DateFunction;
import eu.europeana.api2.v2.service.search.syntax.function.DateIntersectsFunction;
import eu.europeana.api2.v2.service.search.syntax.function.DateWithinFunction;
import eu.europeana.api2.v2.service.search.syntax.function.FunctionRegistry;
import eu.europeana.api2.v2.service.search.syntax.function.IntervalFunction;
import eu.europeana.api2.v2.web.controller.SearchController;
import eu.europeana.corelib.edm.exceptions.SolrQueryException;
import org.junit.Before;
import org.junit.Test;

public class TestSearchParser {

  private SearchController searchController;
  @Before
  public void setup() {
    searchController = new SearchController(null, null, null);
  }


  static {
    loadFieldRegistry();
    loadFunctionRegistry();
  }

  private static void loadFieldRegistry() {
    FieldRegistry registry = FieldRegistry.INSTANCE;
    registry.addField(new FieldDeclaration("issued", "issued_date", "issued_date", "issued_date_begin", "issued_date_end"));
    registry.addField(new FieldDeclaration("created", "created_date", "created_date", "created_date_begin", "created_date_end"));
  }

  private static void loadFunctionRegistry() {
    FunctionRegistry registry = FunctionRegistry.INSTANCE;
    registry.addFunction(new DateFunction());
    registry.addFunction(new DateContainsFunction());
    registry.addFunction(new DateIntersectsFunction());
    registry.addFunction(new DateWithinFunction());
    registry.addFunction(new IntervalFunction());

  }

  @Test(expected = SolrQueryException.class)
  public void testDateSearchQueryForError_when_wrong_bracket_at_beginning() throws SolrQueryException {
    searchController.parseFilterParameter("(date(created,1920)");    
  }
  @Test(expected = SolrQueryException.class)
  public void testDateSearchQueryForError_when_wrong_bracket_at_end() throws SolrQueryException {
    searchController.parseFilterParameter("date(created,1920))");
  }
  
  @Test
  public void testDateSearchQuery() throws SolrQueryException {
    searchController.parseFilterParameter("date(created,interval(1950,1960))");
    //searchController.parseFilterParameter("dateIntersects(dcterms_created,interval(1952-01-01,1953-12-31))");
    searchController.parseFilterParameter("date(created,1950)");
    searchController.parseFilterParameter("issued:abc");
   // searchController.parseFilterParameter("field:CDE");
    //searchController.parseFilterParameter("\"field\":\"field45\"");



    //Complex query example
    //searchController.parseFilterParameter("(\"Europeana Archaeology\") OR skos_concept:(\"http://data.europeana.eu/concept/80\"  OR \"http://vocab.getty.edu/aat/300000810\" OR \"http://vocab.getty.edu/aat/300054328\" OR \"http://vocab.getty.edu/aat/300379558\" OR \"http://vocab.getty.edu/aat/300266711\" OR \"http://vocab.getty.edu/aat/300234110\" OR \"http://vocab.getty.edu/aat/300266151\") OR what:(runsten OR ruin* OR pyramid* OR gravfält OR utgrävning OR fornminnen OR arkeologisk utredning OR bronsålder OR järnålder OR gravröse OR mumie OR lämning OR arkeologi*) OR (edm_datasetName:(91624_*)) OR (edm_datasetName:91669_* AND what:\"Osvald Siren\") OR (PROVIDER:(CARARE OR \"3D ICONS\") AND (provider_aggregation_edm_isShownBy:* AND has_thumbnails:true)) OR (foaf_organization:(*/1482250000004509126 OR */1482250000003772104) AND (provider_aggregation_edm_isShownBy:* AND has_thumbnails:true)) NOT (foaf_organization:(*/1482250000004671150 OR */1482250000002065240))");

  }

}
