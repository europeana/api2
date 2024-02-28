package eu.europeana.api.search.syntax;
/**
 * @author Hugo
 */

import eu.europeana.api.search.syntax.converter.ConverterContext;
import eu.europeana.api.search.syntax.field.FieldRegistry;
import eu.europeana.api.search.syntax.function.FunctionRegistry;
import eu.europeana.api.search.syntax.model.FieldQueryExpression;
import eu.europeana.api.search.syntax.model.FunctionExpression;
import eu.europeana.api.search.syntax.model.OrExpression;
import eu.europeana.api.search.syntax.model.SyntaxExpression;
import eu.europeana.api.search.syntax.model.ValueExpression;
import eu.europeana.api.search.syntax.utils.Constants;
import eu.europeana.api.search.syntax.utils.ParserUtils;

public class TestSyntaxConversion {

  static {
    ParserUtils.loadFieldRegistryFromResource(TestSyntaxConversion.class, Constants.FIELD_REGISTRY_XML);
    ParserUtils.loadFunctionRegistry();
  }



  public static void run(SyntaxExpression expr) {
    try {
      System.out.println(expr.toSolr(new ConverterContext()));
    }
    catch(RuntimeException e) {
      System.out.println("Err: " + e.getMessage());
    }
  }

  public static SyntaxExpression test1() {
    return new FunctionExpression(
        FunctionRegistry.INSTANCE.getFunction("date")
        , new ValueExpression("issued")
        , new ValueExpression("1980"));

  }

  public static SyntaxExpression test2() {
    return new OrExpression(
        new FunctionExpression(
            FunctionRegistry.INSTANCE.getFunction("date")
            , new ValueExpression("issued")
            , new ValueExpression("1980"))
        ,
        new FunctionExpression(
            FunctionRegistry.INSTANCE.getFunction("date")
            , new ValueExpression("created")
            , new ValueExpression("1990")));
  }

  public static SyntaxExpression test3() {
    return new FieldQueryExpression(
        FieldRegistry.INSTANCE.getField("issued"),
        new OrExpression(
            new ValueExpression("1980")
            , new ValueExpression("1990")));
  }

  public static SyntaxExpression test4() {
    return new FunctionExpression(
        FunctionRegistry.INSTANCE.getFunction("date")
        , new ValueExpression("issued")
        , new FunctionExpression(
        FunctionRegistry.INSTANCE.getFunction("interval")
        , new ValueExpression("1980")
        , new ValueExpression("1990")
    )
    );

  }

  public static SyntaxExpression test_err1() {
    return new FunctionExpression(
        FunctionRegistry.INSTANCE.getFunction("date")
        , new ValueExpression("issued")
        , new ValueExpression("xpto"));

  }

  public static SyntaxExpression test_err2() {
    return new FunctionExpression(
        FunctionRegistry.INSTANCE.getFunction("date")
        , new ValueExpression("xpto")
        , new ValueExpression("1980"));

  }

  public static SyntaxExpression test_err3() {
    return new FunctionExpression(
        FunctionRegistry.INSTANCE.getFunction("date")
        , new ValueExpression("creator")
        , new ValueExpression("1980"));

  }

  public static SyntaxExpression test_err4() {
    return new FieldQueryExpression(
        FieldRegistry.INSTANCE.getField("issued"),
        new OrExpression(
            new FunctionExpression(
                FunctionRegistry.INSTANCE.getFunction("date")
                , new ValueExpression("issued")
                , new ValueExpression("1980"))
            , new FunctionExpression(
            FunctionRegistry.INSTANCE.getFunction("date")
            , new ValueExpression("created")
            , new ValueExpression("1990"))));
  }

  public static SyntaxExpression test_err5() {
    return new FunctionExpression(
        FunctionRegistry.INSTANCE.getFunction("date")
        , new ValueExpression("issued")
        , new FunctionExpression(
        FunctionRegistry.INSTANCE.getFunction("interval")
        , new ValueExpression("1970")
        , new FunctionExpression(
        FunctionRegistry.INSTANCE.getFunction("interval")
        , new ValueExpression("1980")
        , new ValueExpression("1990")
    )
    )
    );

  }

  /*
   * We should not allow functions in field queries expressions
   * The easiest way is to have a context on the toSolr function
   */

  public static final void main(String[] args) {
    run(test1());
    run(test2());
    run(test3());
    run(test4());
    run(test_err1());
    run(test_err2());
    run(test_err3());
    run(test_err4());
    run(test_err5());
  }
}
