package eu.europeana.api2.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SolrEscapeDeserializerTest {
    
    @Test
    public void shouldEscapeInputString() throws Exception {
        String json = "{\"query\":\"{! field=test}\"}";

        String escapedQuery = "\\{\\! field=test\\}";

        TestObject obj = new ObjectMapper().readValue(json, TestObject.class);
        assertEquals(escapedQuery, obj.query);
    }


    private static final class TestObject {
        @JsonDeserialize(using = SolrEscapeDeserializer.class)
        String query;
    }
}