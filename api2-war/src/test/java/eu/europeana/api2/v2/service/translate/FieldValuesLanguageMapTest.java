package eu.europeana.api2.v2.service.translate;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class FieldValuesLanguageMapTest {

    @Test
    public void testMergeNoDuplicates() {
        FieldValuesLanguageMap map1 = new FieldValuesLanguageMap("en");
        map1.put("key1", List.of("test1"));
        FieldValuesLanguageMap map2 = new FieldValuesLanguageMap("en");
        map2.put("key2", List.of("test1"));

        map1.merge(map2);
        assertEquals(2, map1.keySet().size());
        assertEquals(1, map1.get("key1").size());
        assertEquals(1, map1.get("key2").size());
    }

    @Test
    public void testMergeDuplicates() {
        String key = "key";
        List<String> value1 = List.of("different", "value");
        List<String> value2 = List.of("duplicate", "value");
        List<String> expected_value_merged = List.of("different", "value", "duplicate");

        FieldValuesLanguageMap map1 = new FieldValuesLanguageMap("en");
        map1.put(key, value1);
        FieldValuesLanguageMap map2 = new FieldValuesLanguageMap("en", key, value2);

        map1.merge(map2);
        assertEquals(1, map1.keySet().size());
        assertEquals(expected_value_merged, map1.get(key));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMergeDifferentLanguages() {
        FieldValuesLanguageMap map1 = new FieldValuesLanguageMap("en");
        FieldValuesLanguageMap map2 = new FieldValuesLanguageMap("nl");
        map1.merge(map2);
    }
}
