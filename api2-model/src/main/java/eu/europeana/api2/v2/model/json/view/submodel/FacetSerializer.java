package eu.europeana.api2.v2.model.json.view.submodel;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import java.io.IOException;

public class FacetSerializer extends JsonSerializer<Facet> {

    @Override
    public void serialize(Facet facet, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeFieldName(facet.name);
        jsonGenerator.writeObject(facet.fields);
        jsonGenerator.writeEndObject();
    }
}
