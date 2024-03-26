package eu.europeana.api.search.syntax.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.europeana.api.search.syntax.exception.DeserializationException;
import eu.europeana.api.search.syntax.field.FieldDeclaration;
import eu.europeana.api.search.syntax.field.FieldRegistry;
import eu.europeana.api.search.syntax.field.FieldType;

public class FieldInfoDeserializer extends JsonDeserializer<FieldRegistry> {

  /**
   * Read the field information from input xml and register the fields with their corresponding solr field names based on different modes.
   * e.g. field 'issued' has corresponding field name 'issued_date'  which is used for searching in the solr.
   *  0
0   .
   0* @param p Parsed used for reading JSON content
   * @param ctxt Context that can be used to access information about
   *   this deserialization activity.
   *
   * @return FieldRegistry with values from the input
   */
  @Override
  public FieldRegistry deserialize(JsonParser p, DeserializationContext ctxt) {
    FieldRegistry fieldRegistry = FieldRegistry.INSTANCE;
    try {
      ObjectNode tree = p.readValueAsTree();
      JsonNode field = tree.findValue(Constants.FIELD);
      if (field != null && !field.isEmpty()) {
        for (JsonNode node : field) {
          fieldRegistry.addField(new FieldDeclaration(
              node.get(Constants.NAME).textValue(),
              FieldType.getEnumByValue(node.get(Constants.TYPE).textValue()),
              node.get(Constants.SEARCHING).textValue(),
              node.get(Constants.FACETING).textValue(),
              node.get(Constants.SORTING_DESCENDING).textValue(),
              node.get(Constants.SORTING_ASCENDING).textValue())
          );
        }
      }
    } catch (Exception e) {
      throw new DeserializationException(String.format("Exception occurred while loading %s. %s",Constants.FIELD_REGISTRY_XML,e));
    }
    return fieldRegistry;
  }


}
