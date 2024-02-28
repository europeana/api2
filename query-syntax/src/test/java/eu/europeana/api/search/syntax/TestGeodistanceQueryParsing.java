package eu.europeana.api.search.syntax;

import eu.europeana.api.search.syntax.parser.ParseException;
import eu.europeana.api.search.syntax.utils.Constants;
import eu.europeana.api.search.syntax.utils.ParserUtils;
import org.junit.Before;
import org.junit.Test;

public class TestGeodistanceQueryParsing {

  @Before
  public void setup() {
    ParserUtils.loadFieldRegistryFromResource(this.getClass(), Constants.FIELD_REGISTRY_XML);
    ParserUtils.loadFunctionRegistry();
  }
  @Test
  public void testGeodistance() throws ParseException {
    ParserUtils.parseQueryFilter("distance(coverageLocation,47,12,2000)");
  }


}
