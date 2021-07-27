package eu.europeana.api2.v2.model.translate;

import eu.europeana.api2.v2.service.translate.TranslationMap;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class TranslationMapTest {

    @Test
    public void testMerge() {
        List<String> value1 = List.of("value1");
        List<String> value2 = List.of("exact", "duplicate");
        List<String> value3a = List.of("different", "value");
        List<String> value3b = List.of("different", "value so this should get it's own new key");

        TranslationMap map1 = new TranslationMap("en");
        map1.put("key1", value1);
        map1.put("key2", value2);
        map1.put("key3", value3a);

        TranslationMap map2 = new TranslationMap("en", "key2", value2);
        map2.put("key3", value3b);

        map1.merge(map2);
        assertEquals(4, map1.keySet().size());
        assertEquals(value1, map1.get("key1"));
        assertEquals(value2, map1.get("key2"));
        assertEquals(value3a, map1.get("key3"));
        assertEquals(value3b, map1.get("key3-1124221006"));

        // also test that we properly detect more than 1 duplicate
        TranslationMap map3 = new TranslationMap("en", "key3", value3b);
        map1.merge(map3);
        assertEquals(4, map1.keySet().size());
    }
}
