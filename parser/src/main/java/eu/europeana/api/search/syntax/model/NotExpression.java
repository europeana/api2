package eu.europeana.api.search.syntax.model;

import eu.europeana.api.search.syntax.converter.ConverterContext;

public class NotExpression implements SetExpression {

    public static String operator = " NOT ";

    private TopLevelExpression expr;

    public NotExpression(TopLevelExpression expr) {
        this.expr = expr;
    }

    public TopLevelExpression getExpression() {
        return expr;
    }

    public void setExpression(TopLevelExpression expr) {
        this.expr = expr;
    }

    public String getOperator() {
        return operator;
    }

    @Override
    public void visit(ExpressionModelVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toSolr(ConverterContext context) {
        context.push(this);
        try {
            return (getOperator() + "(" + expr.toSolr(context) + ")");
        }
        finally { context.pop(); }
    }
}
