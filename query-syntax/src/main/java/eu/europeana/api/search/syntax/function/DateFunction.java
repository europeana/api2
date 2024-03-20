/**
 *
 */
package eu.europeana.api.search.syntax.function;

import static eu.europeana.api.search.syntax.validation.SyntaxErrorUtils.newWrongFunctionArg;
import static eu.europeana.api.search.syntax.validation.SyntaxErrorUtils.newWrongQueryExpression;

import eu.europeana.api.search.syntax.converter.ConverterContext;
import eu.europeana.api.search.syntax.field.FieldMode;
import eu.europeana.api.search.syntax.field.FieldType;
import eu.europeana.api.search.syntax.model.ArgumentExpression;
import eu.europeana.api.search.syntax.model.FieldQueryExpression;
import eu.europeana.api.search.syntax.model.FunctionExpression;
import eu.europeana.api.search.syntax.model.ValueExpression;
import eu.europeana.api.search.syntax.utils.ParserUtils;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Hugo
 * @since 7 Feb 2024
 */
public class DateFunction implements FunctionClass {

    public static final String NAME = "date";
    public enum Operation {INTERSECTS, CONTAINS, WITHIN}

    private static final String SOLR_DATE_QUERY = "_query_:\"{!field f=%s op=%s}%s\"";
    private static final String DATE_WILDCARD   = "*";
    private static final String DATE_NOW        = "now";

    private DateFormat dateFormat  = new SimpleDateFormat("yyyy-MM-dd");
    private static final Pattern    datePattern = Pattern.compile("[0-9]{4}(-[0-9]{2}(-[0-9]{2})?)?");


    @Override
    public String getName() { return NAME; }

    @Override
    public int getArgumentNr() { return 2; }

    @Override
    public boolean isValid(FunctionExpression expr) {
        return true;
    }

    @Override
    public String toString() { return "DateFunction{} " + getName(); }

    /** Convert Function expression into solr format . Also validate the function before converting in solr format.     *
     * First argument for expression is a field name and second must be a date.  e.g. date(created,1950)
     */
    @Override
    public String toSolr(FunctionExpression expr, ConverterContext context) {

        if (context.contains(FieldQueryExpression.class) ) {
            newWrongQueryExpression(getName());
        }
        expr.validateArgs();
        List<ArgumentExpression> params = expr.getParameters();
        String field = getDateField(context, params);
        String date = getDate(ArgumentExpression.ARG2, params.get(1), context);
        String operation = getOperation().name();

        return String.format(SOLR_DATE_QUERY, field, operation, date);

    }

    private String getDateField(ConverterContext context, List<ArgumentExpression> params) {
        if( !(params.get(0) instanceof ValueExpression) ) {
            newWrongFunctionArg(getName(), ArgumentExpression.ARG1, "valid field name");
        }
        return ParserUtils.getValidFieldFromRegistry((ValueExpression) params.get(0),
            context,FieldType.date,FieldMode.search);

    }

    protected Operation getOperation() { return Operation.CONTAINS; }

        protected String getDate(String argNr, ArgumentExpression arg
                               , ConverterContext context) {
            if (arg instanceof ValueExpression  expr) {
                String value = expr.getValue();
                if(StringUtils.isNotBlank(value)) {
                    if (value.equals(DATE_WILDCARD) || datePattern.matcher(value).matches()) {
                        return value;
                    }
                    if (value.equalsIgnoreCase(DATE_NOW)) {
                        return dateFormat.format(new Date());
                    }
                }
            }else if(arg instanceof FunctionExpression  functionExpr  && functionExpr.getFunction() instanceof IntervalFunction  ) {
                    return arg.toSolr(context);
            }
            newWrongFunctionArg(getName(), argNr, "date expression or interval function");
            return null;
        }


}
