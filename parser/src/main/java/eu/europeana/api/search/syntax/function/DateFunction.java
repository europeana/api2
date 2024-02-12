package eu.europeana.api.search.syntax.function;

import eu.europeana.api.search.syntax.converter.ConverterContext;
import eu.europeana.api.search.syntax.field.FieldMode;
import eu.europeana.api.search.syntax.field.FieldRegistry;
import eu.europeana.api.search.syntax.model.ArgumentExpression;
import eu.europeana.api.search.syntax.model.FieldQueryExpression;
import eu.europeana.api.search.syntax.model.FunctionExpression;
import eu.europeana.api.search.syntax.model.ValueExpression;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Hugo
 * @since 7 Feb 2024
 */
public class DateFunction implements FunctionClass {

    public static enum Operation {
        Intersects, Contains, Within
    }

    private static String SOLR_DATE_QUERY 
        = "_query_:\"{!field f=%s op=%s} %s\"";

    private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private static Pattern datePattern = Pattern.compile("[0-9]{4}(-[0-9]{2}(-[0-9]{2})?)?");
    
    /*
    (<YEAR>"-"<SHORTMONTH>"-"<SHORTDAY>) | (<YEAR>"-"<SHORTMONTH>) |(<YEAR>) >|
    <YEAR: ["0"-"9"] ["0"-"9"] ["0"-"9"] ["0"-"9"]>| <SHORTMONTH: ["0"]["1"-"9"]| ["1"] ["0"-"2"]>|
    <SHORTDAY: ["0"]["1"-"9"]| ["1"] ["0"-"9"] | ["2"] ["0"-"9"] | ["3"] ["0"-"1"]>    
    */

    public String getName() {
        return "date";
    }

    @Override
    public void isValid(FunctionExpression expr) {
    }

    /*
     * First argument is a field name and second must be a date
     */
    @Override
    public String toSolr(FunctionExpression expr, ConverterContext context) {
        if ( context.contains(FieldQueryExpression.class) ) {
            throw new RuntimeException("date function cannot be used as a field query expressions");
        }
        List<ArgumentExpression> params = expr.getParameters();
        //check number of arguments and throw parser exception if lower than 2
        if(params != null && params.size()>1) {
            String field = getField(params.get(0));
            String date = getDate(params.get(1), context);
            String operation = getOperation().name();
            return String.format(SOLR_DATE_QUERY, field, operation, date);
        }
        return null;
    }

    protected Operation getOperation() { return Operation.Contains; }

    private String getField(ArgumentExpression arg){
        if ( arg instanceof ValueExpression ) {
            ValueExpression expr = (ValueExpression)arg;
            return FieldRegistry.INSTANCE.getField(expr.getValue()
                                                 , FieldMode.search);
        }
        //throw unknown field
        return null;
    }

    protected String getDate(ArgumentExpression arg, ConverterContext context) {
        if ( arg instanceof ValueExpression ) {
            ValueExpression expr = (ValueExpression)arg;
            String value = expr.getValue();

            if ( value.equals("*") ) { return value; }

            if ( value.equalsIgnoreCase("now") ) { 
                return dateFormat.format(new Date()); 
            }

            if ( datePattern.matcher(value).matches() ) { return value; }

        }

        if(arg instanceof FunctionExpression)
        {
            FunctionClass clazz = ((FunctionExpression) arg).getFunction();
            return clazz.toSolr((FunctionExpression) arg,context);
        }
        //throw for the other types
        return null;
    }

    @Override
    public String toString() {
        return "DateFunction{} " +getName();
    }
}
