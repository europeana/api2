package eu.europeana.api2.v2.service.translate;

import eu.europeana.corelib.web.exception.EuropeanaException;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class SearchResultTranslateServiceTest {

    @Test
    public void testGetNonUriValueFromMap() {
        SearchResultTranslateService service = new SearchResultTranslateService(null);
        String dummyFieldName = "testFieldName";
        String testValue1 = "This is a value";
        String testValue2 = "http://this-is.an-uri.nl";
        String testValue3 = "Just some more text";

        Map<String, List<String>> langMap = new LinkedHashMap<>();
        langMap.put("en", List.of(testValue1, testValue2, testValue3));
        FieldValuesLanguageMap result = service.getNonUriValuesFromLanguageMap(langMap, dummyFieldName, "en");

        assertEquals("en", result.getSourceLanguage());
        assertEquals(1, result.keySet().size());
        assertTrue(result.containsKey(dummyFieldName));
        List<String> values = result.get(dummyFieldName);
        assertNotNull(values);
        assertEquals(2, values.size());
        assertTrue(values.contains(testValue1));
        assertFalse(values.contains(testValue2));
        assertTrue(values.contains(testValue3));
    }
}
