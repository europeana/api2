package eu.europeana.api.search.syntax.model;

import eu.europeana.api.search.syntax.converter.ConverterContext;

/*
 * CONSIDER HAVING A TYPE
 * CONSIDER THE INTERVAL BEING A CLASS (perhaps from java.time)
 * 
 * new ValueExptression<String>("");
 * new ValueExptression<Date>(new Date());
 */
public class ValueExpression implements FieldArgumentExpression
                                      , ArgumentExpression
                                      , TopLevelExpression {

    private String value;

    public ValueExpression(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public void visit(ExpressionModelVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toSolr(ConverterContext context) {
        return value;
    }

    @Override
    public String toString() {
        return "ValueExpression{" +
            "value='" + value + '\'' +
            '}';
    }
}
