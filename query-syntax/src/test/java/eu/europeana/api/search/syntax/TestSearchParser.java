package eu.europeana.api.search.syntax;

import eu.europeana.api.search.syntax.parser.ParseException;
import eu.europeana.api.search.syntax.utils.ParserUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestSearchParser {

  @Before
  public void setup() {
    ParserUtils.loadFieldRegistry();
    ParserUtils.loadFunctionRegistry();
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
  public void testDateSearchQuery_when_simple_dateFunction() throws  ParseException {
    Assert.assertEquals("_query_:\"{!field f=created_date op=Contains}1950\"",
        ParserUtils.parseQueryFilter("date(created,1950)"));
 }

  @Test
  public void testDateSearchQuery_when_including_interval_dateFunction() throws  ParseException {

    Assert.assertEquals("_query_:\"{!field f=created_date op=Contains}[1950 TO 1960]\"" ,
        ParserUtils.parseQueryFilter("date(created,interval(1950,1960))"));
    Assert.assertEquals("_query_:\"{!field f=created_date op=Intersects}[1952-01-01 TO 1953-12-31]\"",
        ParserUtils.parseQueryFilter("dateIntersects(created,interval(1952-01-01,1953-12-31))"));

    //Test interval with no upper/lower limit
    Assert.assertEquals("_query_:\"{!field f=created_date op=Intersects}[1952-01-01 TO *]\"",
        ParserUtils.parseQueryFilter("dateIntersects(created,interval(1952-01-01,*))"));
    Assert.assertEquals("_query_:\"{!field f=created_date op=Intersects}[* TO 1962-01-01]\"",
        ParserUtils.parseQueryFilter("dateIntersects(created,interval(*,1962-01-01))"));

  }
  @Test
  public void testDateSearchQuery_when_AND_OR_operations() throws  ParseException {
    Assert.assertEquals("(_query_:\"{!field f=created_date op=Contains}1950\" OR _query_:\"{!field f=created_date op=Contains}1960\")",
        ParserUtils.parseQueryFilter("date(created,1950) OR date(created,1960)"));

    Assert.assertEquals("(_query_:\"{!field f=created_date op=Contains}[1950 TO 1960]\" OR _query_:\"{!field f=created_date op=Contains}[1970 TO 1980]\")",
        ParserUtils.parseQueryFilter("date(created,interval(1950,1960)) OR date(created,interval(1970,1980))"));
  }
  @Test
  public void testDateSearchQuery_when_NOT_operator() throws  ParseException {
    Assert.assertEquals(" NOT (_query_:\"{!field f=created_date op=Contains}1950\")",
        ParserUtils.parseQueryFilter("NOT date(created,1950)"));

    Assert.assertEquals(" NOT ((_query_:\"{!field f=created_date op=Contains}[1950 TO 1960]\" OR _query_:\"{!field f=created_date op=Contains}[1970 TO 1980]\"))",
        ParserUtils.parseQueryFilter("NOT(date(created,interval(1950,1960)) OR date(created,interval(1970,1980)) )"));
  }

  @Test
  public void testDateSearchQuery_when_invalid_field() throws ParseException {
    ParserUtils.parseQueryFilter("date(created,1950)");
  }


  @Test
  public void testFieldSearchQuery() throws  ParseException {
    Assert.assertEquals("issued_date:1950",ParserUtils.parseQueryFilter("issued:1950"));
    Assert.assertEquals("created_date:1980",ParserUtils.parseQueryFilter("created:1980"));
    Assert.assertEquals("created_date:\"field45\"",ParserUtils.parseQueryFilter("created:\"field45\""));
    Assert.assertEquals("created_date:\"field45 field46\"",ParserUtils.parseQueryFilter("created:\"field45 field46\""));
    Assert.assertEquals("created_date:\'field45 field46\'",ParserUtils.parseQueryFilter("created:\'field45 field46\'"));
  }



  public void testFieldSearchQuery_when_AND_OR_Operator() throws  ParseException {
    Assert.assertEquals("issued_date:1950 AND issued_date:1950",ParserUtils.parseQueryFilter("issued:1950 AND issued_date:1950"));

  }

}
