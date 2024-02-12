package eu.europeana.api.search.syntax.model;

import eu.europeana.api.search.syntax.converter.ConverterContext;

public abstract class BinaryArgumentExpression implements SetExpression {

    private TopLevelExpression left;
    private TopLevelExpression right;

    public BinaryArgumentExpression(TopLevelExpression left
                                  , TopLevelExpression right) {
        this.left  = left;
        this.right = right;
    }


    public TopLevelExpression getLeft() {
        return left;
    }

    public TopLevelExpression getRight() {
        return right;
    }

    public void setLeft(TopLevelExpression left) {
        this.left = left;
    }

    public void setRight(TopLevelExpression right) {
        this.right = right;
    }

    public abstract String getOperator();

    @Override
    public String toSolr(ConverterContext context) {
        context.push(this);
        try {
            return ("(" + getLeft().toSolr(context) + getOperator()
                        + getRight().toSolr(context) + ")");
        }
        finally {
            context.pop();
        }
    }
}
