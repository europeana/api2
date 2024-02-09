/**
 * 
 */
package eu.europeana.api2.v2.service.search.syntax.function;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Hugo
 * @since 7 Feb 2024
 */
public class FunctionRegistry {

    public static FunctionRegistry INSTANCE = new FunctionRegistry();

    public Map<String,FunctionClass> registry;

    public FunctionRegistry() {
        registry = new HashMap();
    }

    public void addFunction(FunctionClass function) {
        registry.put(function.getName(), function);
    }

    public boolean isDeclared(String name) {
        return registry.containsKey(name);
    }

    public FunctionClass getFunction(String name) {
        return registry.get(name);
    }
}
