/**
 * 
 */
package eu.europeana.api.search.syntax.converter;

import java.util.Stack;

import eu.europeana.api.search.syntax.model.SyntaxExpression;

/**
 * @author Hugo
 * @since 7 Feb 2024
 */
public class ConverterContext extends Stack<SyntaxExpression> {

    public boolean contains(Class clazz) {
        for ( SyntaxExpression expr : this ) {
            if ( clazz.isAssignableFrom(expr.getClass()) ) { return true; }
        }
        return false;
    }
}
