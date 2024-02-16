package eu.europeana.api.search.syntax;

import eu.europeana.api.search.syntax.field.FieldDeclaration;
import eu.europeana.api.search.syntax.field.FieldRegistry;
import eu.europeana.api.search.syntax.function.DateContainsFunction;
import eu.europeana.api.search.syntax.function.DateFunction;
import eu.europeana.api.search.syntax.function.DateIntersectsFunction;
import eu.europeana.api.search.syntax.function.DateWithinFunction;
import eu.europeana.api.search.syntax.function.FunctionRegistry;
import eu.europeana.api.search.syntax.function.IntervalFunction;
import eu.europeana.api.search.syntax.parser.ParseException;
import eu.europeana.api.search.syntax.utils.ParserUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestSearchParser {

  @Before
  public void setup() {

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

  @Test(expected = ParseException.class)
  public void testDateSearchQueryForError_when_wrong_bracket_at_beginning() throws ParseException {
    ParserUtils.parseQueryFilter("(date(created,1920)");
  }
//  @Test(expected =  ParseException.class)
//  public void testDateSearchQueryForError_when_wrong_bracket_at_end() throws ParseException {
//    ParserUtils.parseQueryFilter("date(created,1920))");
//  }

  @Test
  public void testDateSearchQuery_when_() throws  ParseException {

    Assert.assertEquals("_query_:\"{!field f=created_date op=Contains} [1950 TO 1960]\"" ,
        ParserUtils.parseQueryFilter("date(created,interval(1950,1960))"));
    Assert.assertEquals("_query_:\"{!field f=created_date op=Intersects} [1952-01-01 TO 1953-12-31]\"",
        ParserUtils.parseQueryFilter("dateIntersects(created,interval(1952-01-01,1953-12-31))"));
    Assert.assertEquals("_query_:\"{!field f=created_date op=Contains} 1950\"",
        ParserUtils.parseQueryFilter("date(created,1950)"));

    Assert.assertEquals("_query_:\"{!field f=created_date op=Intersects} [1952-01-01 TO *]\"",
        ParserUtils.parseQueryFilter("dateIntersects(created,interval(1952-01-01,*))"));
    Assert.assertEquals("_query_:\"{!field f=created_date op=Intersects} [* TO 1962-01-01]\"",
        ParserUtils.parseQueryFilter("dateIntersects(created,interval(*,1962-01-01))"));


    Assert.assertEquals("(_query_:\"{!field f=created_date op=Contains} 1950\" OR _query_:\"{!field f=created_date op=Contains} 1960\")",
        ParserUtils.parseQueryFilter("date(created,1950) OR date(created,1960)"));

    Assert.assertEquals("(_query_:\"{!field f=created_date op=Contains} [1950 TO 1960]\" OR _query_:\"{!field f=created_date op=Contains} [1970 TO 1980]\")",
        ParserUtils.parseQueryFilter("date(created,interval(1950,1960)) OR date(created,interval(1970,1980))"));

    Assert.assertEquals("issued_date:1950",ParserUtils.parseQueryFilter("issued:1950"));
    Assert.assertEquals("created_date:1980",ParserUtils.parseQueryFilter("created:1980"));
    Assert.assertEquals("created_date:field45",ParserUtils.parseQueryFilter("created:\"field45\""));

    //Complex query example
    //searchController.parseFilterParameter("(\"Europeana Archaeology\") OR skos_concept:(\"http://data.europeana.eu/concept/80\"  OR \"http://vocab.getty.edu/aat/300000810\" OR \"http://vocab.getty.edu/aat/300054328\" OR \"http://vocab.getty.edu/aat/300379558\" OR \"http://vocab.getty.edu/aat/300266711\" OR \"http://vocab.getty.edu/aat/300234110\" OR \"http://vocab.getty.edu/aat/300266151\") OR what:(runsten OR ruin* OR pyramid* OR gravfält OR utgrävning OR fornminnen OR arkeologisk utredning OR bronsålder OR järnålder OR gravröse OR mumie OR lämning OR arkeologi*) OR (edm_datasetName:(91624_*)) OR (edm_datasetName:91669_* AND what:\"Osvald Siren\") OR (PROVIDER:(CARARE OR \"3D ICONS\") AND (provider_aggregation_edm_isShownBy:* AND has_thumbnails:true)) OR (foaf_organization:(*/1482250000004509126 OR */1482250000003772104) AND (provider_aggregation_edm_isShownBy:* AND has_thumbnails:true)) NOT (foaf_organization:(*/1482250000004671150 OR */1482250000002065240))");

  }



  public void testFieldSearchQuery_when_AND_OR_Operator() throws  ParseException {
    Assert.assertEquals("issued_date:1950 AND issued_date:",ParserUtils.parseQueryFilter("issued:1950 AND issued_date:1950"));

  }

}
