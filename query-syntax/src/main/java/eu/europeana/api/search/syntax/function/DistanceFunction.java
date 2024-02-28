package eu.europeana.api.search.syntax.function;

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
import org.apache.commons.lang3.StringUtils;

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
    if(params!=null && params.size()==4) {
      String fieldName = getField(params.get(0), context);
      String sfield = StringUtils.isNotBlank(fieldName) ? (fieldName + Constants.LOCATION_SUFFIX) : null;

      context.setParameter("sfield", sfield);
      context.setParameter("pt",getFloatValue(params.get(1)) +","+ getFloatValue(params.get(2)));
      context.setParameter("d",getFloatValue(params.get(3)));
      return SOLR_GEODISTANCE_QUERY;
    }
    return null;
  }

  private String getFloatValue(ArgumentExpression argument) {
    if(argument instanceof ValueExpression){
      ValueExpression val = (ValueExpression) argument;
      if(val!=null)
        return String.valueOf(Float.parseFloat(val.getValue())) ;
    }
    return null;
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
