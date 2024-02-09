package eu.europeana.api2.v2.service.search.syntax.model;

import java.util.Arrays;
import java.util.List;

import eu.europeana.api2.v2.service.search.syntax.converter.ConverterContext;
import eu.europeana.api2.v2.service.search.syntax.function.FunctionClass;

public class FunctionExpression implements TopLevelExpression ,ArgumentExpression{

    private FunctionClass            function;
    private List<ArgumentExpression> paramList;

    public FunctionExpression(FunctionClass function
                            , List<ArgumentExpression> params) {
        this.function  = function;
        this.paramList = params;
    }

    public FunctionExpression(FunctionClass function
                            , ArgumentExpression... params) {
        this.function  = function;
        this.paramList = Arrays.asList(params);
    }

    public FunctionClass getFunction() {
        return function;
    }

    public List<ArgumentExpression> getParameters() {
        return paramList;
    }

    @Override
    public void visit(ExpressionModelVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toSolr(ConverterContext context) {
        context.push(this);
        try {
            return function.toSolr(this, context);
        }
        finally { context.pop(); }
    }

    @Override
    public String toString() {
        return "FunctionExpression{" +
            "function=" + function +
            ", paramList=" + paramList +
            '}';
    }
}
