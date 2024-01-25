package eu.europeana.api2.v2.service.translate;

import eu.europeana.api2.v2.model.translate.TranslationMap;
import org.junit.Test;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Test the TranslationsMap object
 *
 * @author Srishti singh
 * Created 9 Jan 2024
 */
public class TranslationsMapTest {

    @Test
    public void testAddField() {
        String fieldName = "Field1";
        String value1 = "value1";
        String value2 = "value2";
        TranslationMap map = new TranslationMap("en");

        // we add 2 values in the same language and same field name (merge values)
        map.put(fieldName, List.of(value1));
        map.put(fieldName, List.of(value2));
        assertEquals(1, map.keySet().size());
    }

}
