package eu.europeana.api.search.syntax.utils;

import eu.europeana.api.search.syntax.converter.ConverterContext;
import eu.europeana.api.search.syntax.model.SyntaxExpression;
import eu.europeana.api.search.syntax.parser.ParseException;
import eu.europeana.api.search.syntax.parser.SearchExpressionParser;

public class ParserUtils {

  private ParserUtils(){
  }
  public static String parseQueryFilter(String queryString) throws ParseException {
    SyntaxExpression expr = getParsedModel(queryString);
    String solrFormat = expr.toSolr(new ConverterContext());
    System.out.println(queryString + " => " +solrFormat);
    return solrFormat;
  }

  private static SyntaxExpression getParsedModel(String queryString) throws ParseException {
    SearchExpressionParser parser = new SearchExpressionParser(new java.io.StringReader(queryString));
    return parser.parse();
  }

}
