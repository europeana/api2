/**
 * 
 */
package eu.europeana.api2.v2.service.search.syntax.model;

/**
 * @author Hugo
 * @since 7 Feb 2024
 */
public interface ExpressionModelVisitor
{

    public void visit(AndExpression and);

    public void visit(FieldQueryExpression arg);

    public void visit(FunctionExpression arg);

    public void visit(NotExpression arg);

    public void visit(OrExpression arg);

    public void visit(ValueExpression arg);
}