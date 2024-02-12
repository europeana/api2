package eu.europeana.api.search.syntax.function;

import eu.europeana.api.search.syntax.converter.ConverterContext;
import eu.europeana.api.search.syntax.model.ArgumentExpression;
import eu.europeana.api.search.syntax.model.FunctionExpression;
import java.util.List;

public class IntervalFunction extends DateFunction implements FunctionClass {

  @Override
  public String getName() {
    return "interval";
  }

  @Override
  public void isValid(FunctionExpression expr) {

  }

  @Override
  public String toSolr(FunctionExpression expr, ConverterContext context) {

    List<ArgumentExpression> parameters = expr.getParameters();

    if(parameters !=null && parameters.size()>1) {
      String s = "[" + getDate(parameters.get(0), context) + " TO " + getDate(
          parameters.get(1), context) + "]";
      //System.out.println(s);
      return s;
    }
    return null;
  }

  @Override
  public String toString() {
    return "IntervalFunction{} " +getName();
  }
}
