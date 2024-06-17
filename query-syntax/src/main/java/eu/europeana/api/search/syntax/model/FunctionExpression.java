package eu.europeana.api.search.syntax.model;

import static eu.europeana.api.search.syntax.validation.SyntaxErrorUtils.newMissingFunctionArg;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import eu.europeana.api.search.syntax.converter.ConverterContext;
import eu.europeana.api.search.syntax.function.FunctionClass;

public class FunctionExpression implements TopLevelExpression ,ArgumentExpression{

    private   FunctionClass   function;
    private  List<ArgumentExpression> paramList;

    public FunctionExpression(FunctionClass function) {
        this.function  = function;
        this.paramList = Collections.emptyList();
    }

    public FunctionExpression(FunctionClass function
                            , List<ArgumentExpression> params) {
        this.function  = function;
        this.paramList = (params!=null ? params: Collections.emptyList());
    }

    public FunctionExpression(FunctionClass function
                            , ArgumentExpression... params) {
        this.function  = function;
        this.paramList = (params!=null ? Arrays.asList(params):Collections.emptyList());
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
            validateArgs();
            return function.toSolr(this, context);
        }
        finally { context.pop(); }
    }

    public void validateArgs() {
        List<ArgumentExpression> params = paramList;
        int size = params.size();
        int args = function.getArgumentNr();
        if(size < args) {
            newMissingFunctionArg(function.getName(), args, size);
        }
    }

    @Override
    public String toString() {
        return "FunctionExpression{" +
            "function=" + function +
            ", paramList=" + paramList +
            '}';
    }

}
