package eu.europeana.api2.v2.service.search.syntax.function;

import eu.europeana.api2.v2.service.search.syntax.converter.ConverterContext;
import eu.europeana.api2.v2.service.search.syntax.model.FunctionExpression;

public class IntervalFunction implements FunctionClass{

  @Override
  public String getName() {
    return "interval";
  }

  @Override
  public void isValid(FunctionExpression expr) {

  }

  @Override
  public String toSolr(FunctionExpression expr, ConverterContext context) {
    System.out.println();
    return "["+expr.getParameters().get(0)+","+expr.getParameters().get(1)+"]";
  }

  @Override
  public String toString() {
    return "IntervalFunction{} " +getName();
  }
}
