package eu.europeana.api2.v2.service.search.parser;

import eu.europeana.api2.v2.service.search.syntax.converter.*;
import eu.europeana.api2.v2.service.search.syntax.field.*;
import eu.europeana.api2.v2.service.search.syntax.function.*;
import eu.europeana.api2.v2.service.search.syntax.model.*;



public class TestSyntaxConversion {


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
            new FunctionExpression(
                FunctionRegistry.INSTANCE.getFunction("date")
                , new ValueExpression("issued")
                , new ValueExpression("1980"))
            , new FunctionExpression(
            FunctionRegistry.INSTANCE.getFunction("date")
            , new ValueExpression("created")
            , new ValueExpression("1990"))));
  }

  public static SyntaxExpression test4() {
    return new FieldQueryExpression(
        FieldRegistry.INSTANCE.getField("issued"),
        new OrExpression(
            new ValueExpression("1980")
            , new ValueExpression("1990")));
  }

  public static SyntaxExpression test5() {
    return new FieldQueryExpression(
        FieldRegistry.INSTANCE.getField("issued"),
            new ValueExpression("1980")
             );
  }

  /*
   * We should not allow functions in field queries expressions
   * The easiest way is to have a context on the toSolr function
   */

  public static final void main(String[] args) {
  //  run(test1());
   // run(test2());
   // run(test3());
   // run(test4());
    run(test5());
  }
}
