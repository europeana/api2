/**
 *
 */
package eu.europeana.api.search.syntax.function;

import static eu.europeana.api.search.syntax.validation.SyntaxErrorUtils.newWrongFunctionArg;
import static eu.europeana.api.search.syntax.validation.SyntaxErrorUtils.newWrongQueryExpression;
import static eu.europeana.api.search.syntax.validation.SyntaxValidation.checkFieldType;

import eu.europeana.api.search.syntax.converter.ConverterContext;
import eu.europeana.api.search.syntax.field.FieldDeclaration;
import eu.europeana.api.search.syntax.field.FieldMode;
import eu.europeana.api.search.syntax.field.FieldType;
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

    public static final String NAME = "date";
    public static enum Operation { Intersects, Contains, Within }

    private static String SOLR_DATE_QUERY = "_query_:\"{!field f=%s op=%s}%s\"";
    private static String DATE_WILDCARD   = "*";
    private static String DATE_NOW        = "now";

    private static DateFormat dateFormat  = new SimpleDateFormat("yyyy-MM-dd");
    private static Pattern    datePattern = Pattern.compile("[0-9]{4}(-[0-9]{2}(-[0-9]{2})?)?");
    
    /*
    (<YEAR>"-"<SHORTMONTH>"-"<SHORTDAY>) | (<YEAR>"-"<SHORTMONTH>) |(<YEAR>) >|
    <YEAR: ["0"-"9"] ["0"-"9"] ["0"-"9"] ["0"-"9"]>| <SHORTMONTH: ["0"]["1"-"9"]| ["1"] ["0"-"2"]>|
    <SHORTDAY: ["0"]["1"-"9"]| ["1"] ["0"-"9"] | ["2"] ["0"-"9"] | ["3"] ["0"-"1"]>    
    */

    @Override
    public String getName() { return NAME; }

    @Override
    public int getArgumentNr() { return 2; }

    @Override
    public void isValid(FunctionExpression expr) {}

    @Override
    public String toString() { return "DateFunction{} " + getName(); }

    /*
     * First argument is a field name and second must be a date
     */
    @Override
    public String toSolr(FunctionExpression expr, ConverterContext context) {
        if ( context.contains(FieldQueryExpression.class) ) {
            newWrongQueryExpression(getName());
        }
        List<ArgumentExpression> params = expr.getParameters();
        String field     = getField(params.get(0), context);
        String date      = getDate(ArgumentExpression.ARG2
                                 , params.get(1), context);
        String operation = getOperation().name();
        return String.format(SOLR_DATE_QUERY, field, operation, date);
    }

    protected Operation getOperation() { return Operation.Contains; }

    protected String getDate(String argNr, ArgumentExpression arg
                           , ConverterContext context) {
        if ( arg instanceof ValueExpression ) {
            ValueExpression expr = (ValueExpression)arg;
            String value = expr.getValue();

            if ( value.equals(DATE_WILDCARD) ) { return value; }

            if ( value.equalsIgnoreCase(DATE_NOW) ) {
                return dateFormat.format(new Date()); 
            }

            if ( datePattern.matcher(value).matches() ) { return value; }
        }

        if(arg instanceof FunctionExpression) {
            FunctionClass clazz = ((FunctionExpression) arg).getFunction();
            if ( clazz.getClass() == IntervalFunction.class ) {
                return arg.toSolr(context);
            }
        }

        newWrongFunctionArg(getName(), argNr, "date expression or interval function");
        return null;
    }

    private String getField(ArgumentExpression arg, ConverterContext ctxt) {
        if ( arg instanceof ValueExpression ) {
            ValueExpression expr = (ValueExpression)arg;

            FieldDeclaration field = ctxt.getField(expr.getValue());
            checkFieldType(field, FieldType.date, field.getType());

            return field.getField(FieldMode.search);
        }
        return null;
    }
}
