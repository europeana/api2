package eu.europeana.api.search.syntax.function;

import static eu.europeana.api.search.syntax.validation.SyntaxErrorUtils.newException;
import static eu.europeana.api.search.syntax.validation.SyntaxErrorUtils.newWrongFunctionArg;
import static java.lang.Math.abs;

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
    updateGeoFieldName(context, params);
    updateLatitudeAndLongitude(context, params);
    updateDistance(context, params);
    return SOLR_GEODISTANCE_QUERY;
  }

  private void updateGeoFieldName(ConverterContext context, List<ArgumentExpression> params) {
    String fieldName = getGeoField(params.get(0), context);
    context.setParameter(Constants.SFIELD_PARAM, (fieldName + Constants.LOCATION_SUFFIX));
  }

  private void updateDistance(ConverterContext context, List<ArgumentExpression> params) {
    Float distance = getFloatValue(params.get(3));
    if(distance <0 || distance >24000.0) newWrongFunctionArg(this.getName(),ArgumentExpression.ARG4,"positive decimal number which is less than possible distance between two points on Earth (20.004 km)");
    context.setParameter(Constants.D_PARAM, String.valueOf(distance));
  }

  private void updateLatitudeAndLongitude(ConverterContext context, List<ArgumentExpression> params) {
    Float latitude = getFloatValue(params.get(1));
    if(abs(latitude) > 90.0) newWrongFunctionArg(this.getName(),ArgumentExpression.ARG2," value for latitude must be between -90.0 and 90.0");
    Float longitude = getFloatValue(params.get(2));
    if(abs(longitude) > 180.0) newWrongFunctionArg(this.getName(),ArgumentExpression.ARG3," value for longitude must be between -180.0 and 180.0");
    context.setParameter(Constants.PT_PARAM, latitude + "," + longitude);
  }

  private Float getFloatValue(ArgumentExpression argument) {
    if ( !(argument instanceof ValueExpression)){
      newException(String.format("Unable to get float value ! Expected Type: %s","ValueExpression"));
    }
    String value = ((ValueExpression) argument).getValue();
    return Float.parseFloat(value);
  }

  private String getGeoField(ArgumentExpression arg, ConverterContext context) {
  if(!(arg instanceof ValueExpression) ){
    newWrongFunctionArg(getName(), ArgumentExpression.ARG1, "valid field name");
  }
  return ParserUtils.getValidFieldFromRegistry((ValueExpression)arg, context,FieldType.GEO,FieldMode.SEARCH);
  }

}
