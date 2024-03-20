package eu.europeana.api.search.syntax.function;

import static eu.europeana.api.search.syntax.validation.SyntaxValidation.checkArgumentNotFunction;

import eu.europeana.api.search.syntax.converter.ConverterContext;
import eu.europeana.api.search.syntax.model.ArgumentExpression;
import eu.europeana.api.search.syntax.model.FunctionExpression;
import java.util.List;

public class IntervalFunction extends DateFunction implements FunctionClass {

    public static final String NAME = "interval";

    private static String solrIntervalQuery = "[%s TO %s]";

    @Override
    public String getName() { return NAME; }

    @Override
    public String toSolr(FunctionExpression expr, ConverterContext context) {
        List<ArgumentExpression> params = expr.getParameters();
        ArgumentExpression arg1 = params.get(0);
        ArgumentExpression arg2 = params.get(1);
        checkArgumentNotFunction(ArgumentExpression.ARG1, expr, arg1);
        checkArgumentNotFunction(ArgumentExpression.ARG2, expr, arg2);
        return String.format(solrIntervalQuery,
                             getDate(ArgumentExpression.ARG1, arg1, context),
                             getDate(ArgumentExpression.ARG2, arg2, context));
    }

    @Override
    public String toString() {
        return "IntervalFunction{} " + getName();
    }
}
