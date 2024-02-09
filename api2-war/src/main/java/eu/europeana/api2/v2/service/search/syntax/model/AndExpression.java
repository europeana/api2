package eu.europeana.api2.v2.service.search.syntax.model;

public class AndExpression extends BinaryArgumentExpression {

    public AndExpression(TopLevelExpression left
                      , TopLevelExpression right) {
        super(left, right);
    }

    @Override
    public String getOperator() {
        return " AND ";
    }

    @Override
    public void visit(ExpressionModelVisitor visitor) {
        visitor.visit(this);
    }
}