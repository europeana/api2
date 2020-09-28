package eu.europeana.api2.utils;


import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.text.ParseException;
import java.util.Locale;

/**
 * Custom deserializer for sanitizing JSON fields against Solr local parameter injection.
 */
public class SolrEscapeDeserializer extends JsonDeserializer<String> {
    private final Logger log = LogManager.getLogger(SolrEscapeDeserializer.class);
    private final SolrLocalParamFormatter formatter = new SolrLocalParamFormatter();


    @Override
    public String deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        try {
            return formatter.parse(jsonParser.getText(), Locale.getDefault());
        } catch ( ParseException e) {
            log.warn("Error sanitizing JSON input: ", e);
            return null;
        }
    }
}
