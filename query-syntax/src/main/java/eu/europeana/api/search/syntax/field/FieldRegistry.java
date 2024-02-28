/**
 * 
 */
package eu.europeana.api.search.syntax.field;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Hugo
 * @since 7 Feb 2024
 */
public class FieldRegistry {

    public static FieldRegistry INSTANCE = new FieldRegistry();

    public boolean isLoaded= false;

    public Map<String,FieldDeclaration> registry;

    public FieldRegistry() {
        registry = new HashMap();
    }

    public void addField(FieldDeclaration decl) {
        registry.put(decl.getName(), decl);
    }

    public boolean isValid(String name) {
        return registry.containsKey(name);
    }

    public FieldDeclaration getField(String name) {
        return registry.get(name);
    }

    public String getField(String name, FieldMode mode) {
        FieldDeclaration decl = registry.get(name);
        return ( decl != null ? decl.getField(mode) : null);
    }


}
