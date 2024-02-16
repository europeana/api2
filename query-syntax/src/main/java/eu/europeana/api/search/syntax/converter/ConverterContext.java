/**
 * 
 */
package eu.europeana.api.search.syntax.converter;

import static eu.europeana.api.search.syntax.validation.SyntaxErrorUtils.newUnknownField;

import java.util.Stack;

import eu.europeana.api.search.syntax.field.FieldDeclaration;
import eu.europeana.api.search.syntax.field.FieldRegistry;
import eu.europeana.api.search.syntax.function.FunctionRegistry;
import eu.europeana.api.search.syntax.model.SyntaxExpression;

/**
 * @author Hugo
 * @since 7 Feb 2024
 */
public class ConverterContext extends Stack<SyntaxExpression> {

    private FieldRegistry    fieldRegistry;
    private FunctionRegistry functionRegistry;

    public ConverterContext() {
        fieldRegistry    = FieldRegistry.INSTANCE;
        functionRegistry = FunctionRegistry.INSTANCE;
    }

    public boolean contains(Class clazz) {
        for ( SyntaxExpression expr : this ) {
            if ( clazz.isAssignableFrom(expr.getClass()) ) { return true; }
        }
        return false;
    }

    public FieldDeclaration getField(String name) {
        FieldDeclaration field = fieldRegistry.getField(name);
        if ( field == null ) { newUnknownField(name); }
        return field;
    }
}
