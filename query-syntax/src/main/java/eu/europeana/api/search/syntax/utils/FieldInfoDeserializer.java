package eu.europeana.api.search.syntax.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.europeana.api.search.syntax.field.FieldDeclaration;
import eu.europeana.api.search.syntax.field.FieldRegistry;
import eu.europeana.api.search.syntax.field.FieldType;
import java.io.IOException;

public class FieldInfoDeserializer extends JsonDeserializer<FieldRegistry> {

  @Override
  public FieldRegistry deserialize(JsonParser p, DeserializationContext ctxt)
      throws IOException {

    FieldRegistry fieldRegistry = FieldRegistry.INSTANCE;
    ObjectNode tree = p.readValueAsTree();
    JsonNode field = tree.findValue(Constants.FIELD);

    if(field != null && !field.isEmpty())
    {
      for(JsonNode node : field) {
        fieldRegistry.addField(new FieldDeclaration(
            node.get(Constants.NAME).textValue(),
            FieldType.valueOf(node.get(Constants.TYPE).textValue()),
            node.get(Constants.SEARCHING).textValue(),
            node.get(Constants.FACETING).textValue(),
            node.get(Constants.SORTING_DESCENDING).textValue(),
            node.get(Constants.SORTING_ASCENDING).textValue())
        );
      }

    }
    return fieldRegistry;
  }
}
