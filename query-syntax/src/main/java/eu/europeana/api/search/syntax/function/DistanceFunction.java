package eu.europeana.api.search.syntax.function;

import static eu.europeana.api.search.syntax.validation.SyntaxErrorUtils.newException;
import static eu.europeana.api.search.syntax.validation.SyntaxErrorUtils.newWrongFunctionArg;

import eu.europeana.api.search.syntax.converter.ConverterContext;
import eu.europeana.api.search.syntax.field.FieldMode;
import eu.europeana.api.search.syntax.field.FieldType;
import eu.europeana.api.search.syntax.model.ArgumentExpression;
import eu.europeana.api.search.syntax.model.FunctionExpression;
import eu.europeana.api.search.syntax.model.ValueExpression;
import eu.europeana.api.search.syntax.utils.Constants;
import eu.europeana.api.search.syntax.utils.ParserUtils;
import java.util.List;

public class DistanceFunction implements FunctionClass {

  public static final String SOLR_GEODISTANCE_QUERY = "{!geofilt}";

  @Override
  public String getName() {
    return "distance";
  }

  @Override
  public int getArgumentNr() {
    return 4;
  }

  @Override
  public boolean isValid(FunctionExpression expr) {
    return true;
  }

  /*
  */
  @Override
  public String toSolr(FunctionExpression expr, ConverterContext context) {
    List<ArgumentExpression> params = expr.getParameters();
    expr.validateArgs();
    String fieldName = getGeoField(params.get(0), context);
    context.setParameter(Constants.SFIELD_PARAM, (fieldName + Constants.LOCATION_SUFFIX));
    context.setParameter(Constants.PT_PARAM,
        getFloatValue(params.get(1), ArgumentExpression.ARG2) + "," + getFloatValue(params.get(2),
            ArgumentExpression.ARG3));
    context.setParameter(Constants.D_PARAM, getFloatValue(params.get(3), ArgumentExpression.ARG4));
    return SOLR_GEODISTANCE_QUERY;
  }

  private String getFloatValue(ArgumentExpression argument, String arg) {
    if ( !(argument instanceof ValueExpression)){
      newException(String.format("Unable to get float value ! Expected Type: %s","ValueExpression"));
    }
    String value = ((ValueExpression) argument).getValue();
    float f = Float.parseFloat(value);
    if(f<0)
       newWrongFunctionArg(this.getName(),arg," Positive Decimal number");
    return String.valueOf(f);
  }

  private String getGeoField(ArgumentExpression arg, ConverterContext context) {
  if(!(arg instanceof ValueExpression) ){
    newWrongFunctionArg(getName(), ArgumentExpression.ARG1, "valid field name");
  }
  return ParserUtils.getValidFieldFromRegistry((ValueExpression)arg, context,FieldType.geo,FieldMode.search);
  }

}
