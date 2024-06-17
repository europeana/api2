package eu.europeana.api.search.syntax.model;

import eu.europeana.api.search.syntax.converter.ConverterContext;
import eu.europeana.api.search.syntax.field.FieldDeclaration;
import eu.europeana.api.search.syntax.field.FieldMode;

public class FieldQueryExpression implements TopLevelExpression {

    private FieldDeclaration        field;
    private FieldArgumentExpression value;

    public FieldQueryExpression(FieldDeclaration field
                              , FieldArgumentExpression value) {
        this.field = field;
        this.value = value;
    }

    public FieldDeclaration getField() {
        return field;
    }

    public void setField(FieldDeclaration field) {
        this.field = field;
    }

    public FieldArgumentExpression getValue() {
        return value;
    }

    public void setValue(FieldArgumentExpression value) {
        this.value = value;
    }

    @Override
    public void visit(ExpressionModelVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toSolr(ConverterContext context) {
        context.push(this);
        try {
            if(context.getIsToSurroundWithFilterFunction()) {
                return "filter(" + this.field.getField(FieldMode.SEARCH)
                    + ":" + value.toSolr(context) + ")";
            }
            return this.field.getField(FieldMode.SEARCH)
                + ":" + value.toSolr(context);
        }
        finally { context.pop(); }
    }
}
