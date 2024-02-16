package eu.europeana.api.search.syntax.utils;

import eu.europeana.api.search.syntax.converter.ConverterContext;
import eu.europeana.api.search.syntax.field.FieldDeclaration;
import eu.europeana.api.search.syntax.field.FieldRegistry;
import eu.europeana.api.search.syntax.field.FieldType;
import eu.europeana.api.search.syntax.function.DateContainsFunction;
import eu.europeana.api.search.syntax.function.DateFunction;
import eu.europeana.api.search.syntax.function.DateIntersectsFunction;
import eu.europeana.api.search.syntax.function.DateWithinFunction;
import eu.europeana.api.search.syntax.function.FunctionRegistry;
import eu.europeana.api.search.syntax.function.IntervalFunction;
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


  /** Temporary loading of field and function registry values
   * To be updated /removed once registry loading logic in place
   */
  public static void loadFieldRegistry() {
    FieldRegistry registry = FieldRegistry.INSTANCE;
    registry.addField(new FieldDeclaration("issued", FieldType.date, "issued_date", "issued_date", "issued_date_begin", "issued_date_end"));
    registry.addField(new FieldDeclaration("created", FieldType.date, "created_date", "created_date", "created_date_begin", "created_date_end"));
    registry.addField(new FieldDeclaration("creator", FieldType.text, "creator", "creator", "creator", "creator"));
  }

  public static void loadFunctionRegistry() {
    FunctionRegistry registry = FunctionRegistry.INSTANCE;
    registry.addFunction(new DateFunction());
    registry.addFunction(new DateContainsFunction());
    registry.addFunction(new DateIntersectsFunction());
    registry.addFunction(new DateWithinFunction());
    registry.addFunction(new IntervalFunction());
  }

}
