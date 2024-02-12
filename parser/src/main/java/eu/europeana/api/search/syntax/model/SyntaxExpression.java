/**
 * 
 */
package eu.europeana.api.search.syntax.model;

import eu.europeana.api.search.syntax.converter.SyntaxConverter;

/**
 * @author Hugo
 * @since 7 Feb 2024
 */
public interface SyntaxExpression extends SyntaxConverter {

    public void visit(ExpressionModelVisitor visitor);
}
