/**
 * 
 */
package eu.europeana.api.search.syntax.converter;

import static eu.europeana.api.search.syntax.validation.SyntaxErrorUtils.newUnknownField;

import eu.europeana.api.search.syntax.field.FieldDeclaration;
import eu.europeana.api.search.syntax.field.FieldRegistry;
import eu.europeana.api.search.syntax.function.FunctionRegistry;
import eu.europeana.api.search.syntax.model.SyntaxExpression;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * @author Hugo
 * @since 7 Feb 2024
 */
public class ConverterContext extends Stack<SyntaxExpression> {

    private FieldRegistry    fieldRegistry;
    private FunctionRegistry functionRegistry;
    private Map<String,List<String>> paramMap = new LinkedHashMap<>();

    private boolean isToSurroundWithFilterFunction= false;

    public ConverterContext() {
        fieldRegistry    = FieldRegistry.INSTANCE;
        functionRegistry = FunctionRegistry.INSTANCE;
    }
    public Set<Map.Entry<String, List<String>>>getParameters() {
        return paramMap.entrySet();
    }

    public void setParameter(String key, List<String> value) {
        this.paramMap.put(key, value);
    }

    public boolean contains(Class<?> clazz) {
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

    public boolean getIsToSurroundWithFilterFunction() {
        return isToSurroundWithFilterFunction;
    }
    public  void setIsToSurroundWithFilterFunction(boolean value) {
        this.isToSurroundWithFilterFunction=value;
    }
}
