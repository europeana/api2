package eu.europeana.api.search.syntax.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.europeana.api.search.syntax.exception.DeserializationException;
import eu.europeana.api.search.syntax.function.FunctionClass;
import eu.europeana.api.search.syntax.function.FunctionRegistry;
import java.lang.reflect.Constructor;
import org.apache.commons.lang3.StringUtils;

public class FunctionInfoDeserializer extends JsonDeserializer<FunctionRegistry> {

  public static final String CLASSNAME = "classname";

  /**
   * Read the Function class names from input file located in resources folder and register the respective function objects.
   * @param p Parser used for reading JSON content
   * @param ctxt Context that can be used to access information about
   *   this deserialization activity.
   *
   * @return FunctionRegistry with values from the input
   */
  @Override
  public FunctionRegistry deserialize(JsonParser p, DeserializationContext ctxt){

    FunctionRegistry registry = FunctionRegistry.INSTANCE;
    try {
      ObjectNode tree = p.readValueAsTree();
      JsonNode functionNode = tree.findValue(Constants.FUNCTION);
      if (functionNode != null && !functionNode.isEmpty()) {
        for (JsonNode node : functionNode) {
          if (node.get(CLASSNAME) != null) {
            String classname = node.get(CLASSNAME).textValue();
            if (StringUtils.isNotBlank(classname)) {
              Class<?> functionClass = Class.forName(classname);
              Constructor<?> cons = functionClass.getConstructor();
              registry.addFunction((FunctionClass) cons.newInstance());
            }
          }
        }
      }
    } catch (Exception e) {
      throw new DeserializationException(String.format("Exception occurred while loading %s. %s",Constants.FUNCTION_REGISTRY_XML,e));
    }
    return registry;
  }
}
