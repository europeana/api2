package eu.europeana.api.search.syntax.model;

public class OrExpression extends BinaryArgumentExpression {

    public OrExpression(TopLevelExpression left
                      , TopLevelExpression right) {
        super(left, right);
    }

    @Override
    public String getOperator() {
        return " OR ";
    }

    @Override
    public void visit(ExpressionModelVisitor visitor) {
        visitor.visit(this);
    }
}
