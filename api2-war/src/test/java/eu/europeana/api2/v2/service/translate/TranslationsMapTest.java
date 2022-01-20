package eu.europeana.api2.v2.service.translate;

import org.junit.Test;

import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Test the TranslationsMap object
 *
 * @author P. Ehlert
 * Created 21 Sep 2021
 */
public class TranslationsMapTest {

    @Test
    public void testAddField() {
        String fieldName = "Field1";
        String value1 = "value1";
        String value2 = "value2";

        // we add 2 values in the same language and same field name (merge values)
        FieldValuesLanguageMap langMap1 = new FieldValuesLanguageMap("nl");
        langMap1.put(fieldName, List.of(value1));
        FieldValuesLanguageMap langMap2 = new FieldValuesLanguageMap("nl");
        langMap2.put(fieldName, List.of(value2));
        // we also add the same field but in a different language (should not be merged)
        FieldValuesLanguageMap langMap3 = new FieldValuesLanguageMap("en");
        langMap3.put(fieldName, List.of("some value"));

        TranslationsMap map = new TranslationsMap(List.of(langMap1, langMap2, langMap3));

        assertEquals(2, map.keySet().size());
        Iterator<String> it = map.keySet().iterator();
        assertEquals("nl", it.next());
        assertEquals("en", it.next());

        FieldValuesLanguageMap nlMap = map.get("nl");
        FieldValuesLanguageMap enMap = map.get("en");
        assertEquals(1, nlMap.keySet().size());
        assertEquals(List.of("value1", "value2"), nlMap.get(fieldName));
        assertEquals(1, enMap.keySet().size());
        assertEquals(List.of("some value"), enMap.get(fieldName));
    }

}
