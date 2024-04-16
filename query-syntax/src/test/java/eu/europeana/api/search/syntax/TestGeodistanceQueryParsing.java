package eu.europeana.api.search.syntax;

import eu.europeana.api.search.syntax.exception.QuerySyntaxException;
import eu.europeana.api.search.syntax.utils.Constants;
import eu.europeana.api.search.syntax.utils.ParserUtils;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestGeodistanceQueryParsing {

  @Before
  public void setup() {
    ParserUtils.loadFieldRegistryFromResource(Constants.FIELD_REGISTRY_XML);
    ParserUtils.loadFunctionRegistry(Constants.FUNCTION_REGISTRY_XML);
  }
  @Test
  public void testGeodistance()  {
    Map<String, List<String>> parsedParametersMap = ParserUtils.getParsedParametersMap(new String[]{"distance(coverageLocation,47,12,2000)"});
    Assert.assertEquals("{!geofilt}", parsedParametersMap.get(Constants.FQ_PARAM).get(0));
    Assert.assertEquals("2000.0", parsedParametersMap.get( Constants.D_PARAM).get(0));
    Assert.assertEquals("47.0,12.0", parsedParametersMap.get( Constants.PT_PARAM).get(0));

  }
  @Test (expected = QuerySyntaxException.class)
  public void testGeodistance_whenMultipleOccurance()    {
    ParserUtils.getParsedParametersMap(new String[]{"(distance(coverageLocation,47,12,2000)distance(coverageLocation,47,12,2000))"});
  }
  @Test(expected = QuerySyntaxException.class)
  public void testGeodistance_whenMultipleOccurance_1()  {
     ParserUtils.getParsedParametersMap(new String[]{"distance(coverageLocation,47,12,2000)distance(coverageLocation,47,12,2000)"});
  }

  /**
   * Note: Parser does not check if multiple occurrences of same function provided.
   */
  @Test
  public void testGeodistance_whenMultipleOccurance_2() {
    Assert.assertEquals("({!geofilt} OR {!geofilt})",ParserUtils.getParsedParametersMap(
        new String[]{"distance(coverageLocation,47,12,2000) OR distance(coverageLocation,48,13,3000)"}).get("fq").get(0));
  }
  @Test(expected = QuerySyntaxException.class)
  public void testGeodistance_whenInvalidArguments()  {
    ParserUtils.getParsedParametersMap(new String[]{"distance(^;C,#44%,f~@tr**$(-27#(alph@),2.0.-=00)"});
  }

  @Test(expected = QuerySyntaxException.class)
  public void testGeodistance_whenInvalidArguments_1() {
    ParserUtils.getParsedParametersMap(new String[]{"distance(DELETE * FROM USERS)"});
  }

  @Test(expected = QuerySyntaxException.class)
  public void testGeodistance_whenInvalidArguments_2() {
    ParserUtils.getParsedParametersMap(new String[]{"distance(,,,)"});
  }

  @Test(expected = QuerySyntaxException.class)
  public void testGeodistance_whenInvalidArguments_3() {
    ParserUtils.getParsedParametersMap(new String[]{"distance(coverageLocation,20.4,80.09,-40)"});
  }

}
