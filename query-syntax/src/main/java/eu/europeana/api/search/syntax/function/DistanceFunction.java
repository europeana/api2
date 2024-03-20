package eu.europeana.api.search.syntax.function;

import static eu.europeana.api.search.syntax.validation.SyntaxErrorUtils.newException;
import static eu.europeana.api.search.syntax.validation.SyntaxErrorUtils.newMissingFunctionArg;
import static eu.europeana.api.search.syntax.validation.SyntaxErrorUtils.newWrongFunctionArg;
import static eu.europeana.api.search.syntax.validation.SyntaxValidation.checkFieldType;

import eu.europeana.api.search.syntax.converter.ConverterContext;
import eu.europeana.api.search.syntax.field.FieldDeclaration;
import eu.europeana.api.search.syntax.field.FieldMode;
import eu.europeana.api.search.syntax.field.FieldType;
import eu.europeana.api.search.syntax.model.ArgumentExpression;
import eu.europeana.api.search.syntax.model.FunctionExpression;
import eu.europeana.api.search.syntax.model.ValueExpression;
import eu.europeana.api.search.syntax.utils.Constants;
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
  public void isValid(FunctionExpression expr) {
  }
  @Override
  public String toSolr(FunctionExpression expr, ConverterContext context) {
    List<ArgumentExpression> params = expr.getParameters();
      int size = params==null?0: params.size();
      if(size<4) {
        newMissingFunctionArg(this.getName(), 4,size );
      }
      String fieldName = getField(params.get(0), context);
      String sfield = fieldName + Constants.LOCATION_SUFFIX;
      context.setParameter(Constants.SFIELD_PARAM, sfield);
      context.setParameter(Constants.PT_PARAM,getFloatValue(params.get(1),ArgumentExpression.ARG2) +","+ getFloatValue(params.get(2),
          ArgumentExpression.ARG3));
      context.setParameter(Constants.D_PARAM,getFloatValue(params.get(3), ArgumentExpression.ARG4));
      return SOLR_GEODISTANCE_QUERY;
  }

  private String getFloatValue(ArgumentExpression argument, String arg) {
    if (!(argument instanceof ValueExpression) ) {
      newException(String.format("Unable to get float value ! Expected Type: %s","ValueExpression"));
    }
    String value = ((ValueExpression) argument).getValue();
    float f = Float.parseFloat(value);
    if(Math.signum(f) == -1.0f)
       newWrongFunctionArg(this.getName(),arg," Positive Decimal number");
    return String.valueOf(f);
  }

  private String getField(ArgumentExpression argumentExpression, ConverterContext context) {

  if(argumentExpression instanceof ValueExpression)
  {
    String value = ((ValueExpression) argumentExpression).getValue();
    FieldDeclaration field = context.getField(value);
    checkFieldType(field, FieldType.geo);
    return field.getField(FieldMode.search);
  }
  return null;
  }
}
