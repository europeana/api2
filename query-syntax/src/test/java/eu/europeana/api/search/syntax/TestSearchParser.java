package eu.europeana.api.search.syntax;

import eu.europeana.api.search.syntax.exception.QuerySyntaxException;
import eu.europeana.api.search.syntax.utils.Constants;
import eu.europeana.api.search.syntax.utils.ParserUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestSearchParser {

  @Before
  public void setup() {
    ParserUtils.loadFieldRegistryFromResource(Constants.FIELD_REGISTRY_XML);
    ParserUtils.loadFunctionRegistry(Constants.FUNCTION_REGISTRY_XML);
  }

  @Test(expected = QuerySyntaxException.class)
  public void testDateSearchQueryForError_when_wrong_bracket_at_beginning()
      throws QuerySyntaxException {
    Assert.assertEquals("filter(_query_:\"{!field f=created_date op=Contains}1950\")",
        ParserUtils.getParsedParametersMap(new String[]{"(date(created,1920)"})
            .get(Constants.FQ_PARAM).get(0));
  }

  @Test(expected = QuerySyntaxException.class)
  public void testDateSearchQueryForError_when_wrong_bracket_at_end() throws QuerySyntaxException {
    Assert.assertEquals("filter(_query_:\"{!field f=created_date op=Contains}1950\")",
        ParserUtils.getParsedParametersMap(new String[]{"date(created,1920))"})
            .get(Constants.FQ_PARAM).get(0));
  }

  @Test
  public void testDateSearchQuery_when_simple_dateFunction() throws QuerySyntaxException {
    Assert.assertEquals("_query_:\"{!field f=created_date op=Contains}1950\"",
        ParserUtils.getParsedParametersMap(new String[]{"date(created,1950)"})
            .get(Constants.FQ_PARAM).get(0));
  }

  @Test
  public void testDateSearchQuery_when_including_interval_dateFunction()
      throws QuerySyntaxException {

    Assert.assertEquals("_query_:\"{!field f=created_date op=Contains}[1950 TO 1960]\"",
        ParserUtils.getParsedParametersMap(new String[]{"date(created,interval(1950,1960))"})
            .get(Constants.FQ_PARAM).get(0));
    Assert.assertEquals(
        "_query_:\"{!field f=created_date op=Intersects}[1952-01-01 TO 1953-12-31]\"",
        ParserUtils.getParsedParametersMap(
                new String[]{"dateIntersects(created,interval(1952-01-01,1953-12-31))"})
            .get(Constants.FQ_PARAM).get(0));

    //Test interval with no upper/lower limit
    Assert.assertEquals("_query_:\"{!field f=created_date op=Intersects}[1952-01-01 TO *]\"",
        ParserUtils.getParsedParametersMap(
                new String[]{"dateIntersects(created,interval(1952-01-01,*))"})
            .get(Constants.FQ_PARAM).get(0));
    Assert.assertEquals("_query_:\"{!field f=created_date op=Intersects}[* TO 1962-01-01]\"",
        ParserUtils.getParsedParametersMap(
                new String[]{"dateIntersects(created,interval(*,1962-01-01))"})
            .get(Constants.FQ_PARAM).get(0));

  }

  @Test
  public void testDateSearchQuery_when_AND_OR_operations() throws QuerySyntaxException {
    Assert.assertEquals(
        "(filter(_query_:\"{!field f=created_date op=Contains}1950\") OR filter(_query_:\"{!field f=created_date op=Contains}1960\"))",
        ParserUtils.getParsedParametersMap(new String[]{"date(created,1950) OR date(created,1960)"})
            .get(Constants.FQ_PARAM).get(0));

    Assert.assertEquals(
        "(filter(_query_:\"{!field f=created_date op=Contains}[1950 TO 1960]\") OR filter(_query_:\"{!field f=created_date op=Contains}[1970 TO 1980]\"))",
        ParserUtils.getParsedParametersMap(
                new String[]{"date(created,interval(1950,1960)) OR date(created,interval(1970,1980))"})
            .get(Constants.FQ_PARAM).get(0));
  }

  @Test
  public void testDateSearchQuery_when_NOT_operator() throws QuerySyntaxException {
    String actual = ParserUtils.getParsedParametersMap(new String[]{"NOT date(created,1950)"}).get(Constants.FQ_PARAM).get(0);
    Assert.assertEquals(" NOT (_query_:\"{!field f=created_date op=Contains}1950\")",actual);

    Assert.assertEquals(
        " NOT ((filter(_query_:\"{!field f=created_date op=Contains}[1950 TO 1960]\") OR filter(_query_:\"{!field f=created_date op=Contains}[1970 TO 1980]\")))",
        ParserUtils.getParsedParametersMap(new String[]{
                "NOT(date(created,interval(1950,1960)) OR date(created,interval(1970,1980)) )"})
            .get(Constants.FQ_PARAM).get(0));
  }

  @Test
  public void testDateSearchQuery_when_invalid_field() throws QuerySyntaxException {
    ParserUtils.getParsedParametersMap(new String[]{"date(created,1950)"}).get(Constants.FQ_PARAM);
  }


  @Test
  public void testFieldSearchQuery() throws QuerySyntaxException {
    Assert.assertEquals("issued_date:1950",
        ParserUtils.getParsedParametersMap(new String[]{"issued:1950"}).get(Constants.FQ_PARAM).get(0));
    Assert.assertEquals("created_date:1980",
        ParserUtils.getParsedParametersMap(new String[]{"created:1980"}).get(Constants.FQ_PARAM).get(0));
    Assert.assertEquals("created_date:\"field45\"",
        ParserUtils.getParsedParametersMap(new String[]{"created:\"field45\""})
            .get(Constants.FQ_PARAM).get(0));
    Assert.assertEquals("created_date:\"field45 field46\"",
        ParserUtils.getParsedParametersMap(new String[]{"created:\"field45 field46\""})
            .get(Constants.FQ_PARAM).get(0));
    Assert.assertEquals("created_date:'field45 field46'",
        ParserUtils.getParsedParametersMap(new String[]{"created:'field45 field46'"})
            .get(Constants.FQ_PARAM).get(0));
  }


  @Test
  public void testFieldSearchQuery_when_AND_OR_Operator() throws QuerySyntaxException {
    Assert.assertEquals("(filter(issued_date:1950) AND (filter(issued_date:1960) AND filter(issued_date:1970)))",
        ParserUtils.getParsedParametersMap(
                new String[]{"issued:1950 AND issued:1960 AND issued:1970"}).get(Constants.FQ_PARAM)
            .get(0));

  }
}

