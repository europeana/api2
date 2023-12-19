package eu.europeana.api2.v2.utils;

import eu.europeana.api.translation.definitions.language.Language;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

public class MetadataTranslationUtilsTest {

    // multiple language texts
    List<String> texts = new ArrayList<>(Arrays.asList("Isto é uma frase para teste",
            "Traduit aussi cette phrase",
            "2014",
            "एहि वाक्यक सेहो अनुवाद करू"));

    Map<String, String> translateResult = new LinkedHashMap<>();

    List<String> lang = new ArrayList<>(Arrays.asList("pt", "fr" , "zxx" , "na"));

    @Test
    public void test_createTranslateRequestBodyV2() throws JSONException {
       JSONObject object =  MetadataTranslationUtils.createTranslateRequestBody(texts, "en", "es", "", true );
       checkJson(object, true);
    }

    @Test
    public void test_createTranslateRequestBodyV1() throws JSONException {
        JSONObject object =  MetadataTranslationUtils.createTranslateRequestBody(texts, "en", "es", "", false );
        checkJson(object, false);
    }

    @Test
    public void test_createDetectRequestBody() throws JSONException {
        JSONObject object =  MetadataTranslationUtils.createDetectRequestBody(texts, "en",  "" );
        checkJson(object, true);
    }

    @Test
    public void test_getDetectedLangValueMap() {
        Map<String, List<String>> map =  MetadataTranslationUtils.getDetectedLangValueMap(texts, lang );
        Assert.assertNotNull(map);
        Assert.assertEquals(4, map.size());
    }


    @Test
    public void test_getResultsWithoutSorting() {
        translateResult.put(texts.get(0), "This is a test sentence");
        translateResult.put(texts.get(1), "Also translates this sentence");
        translateResult.put(texts.get(2), "2014");
        translateResult.put(texts.get(3), "एहि वाक्यक सेहो अनुवाद करू");

        List<String> results =  MetadataTranslationUtils.getResults(texts, translateResult, false);
        Assert.assertNotNull(results);
        Assert.assertEquals(4, results.size());
    }

    @Test
    public void test_getResultsWithSorting() {
        translateResult.put(texts.get(2), "2014");
        translateResult.put(texts.get(0), "This is a test sentence");
        translateResult.put(texts.get(1), "Also translates this sentence");

        List<String> results =  MetadataTranslationUtils.getResults(texts, translateResult, true);
        Assert.assertNotNull(results);
        Assert.assertEquals(4, results.size());
        Assert.assertEquals("This is a test sentence", results.get(0));
        Assert.assertEquals("2014", results.get(2));
        Assert.assertEquals("एहि वाक्यक सेहो अनुवाद करू", results.get(3));

    }

    @Test
    public void noTranslationRequired() {
        Assert.assertTrue(MetadataTranslationUtils.noTranslationRequired(Language.DEF));
        Assert.assertTrue(MetadataTranslationUtils.noTranslationRequired(Language.NO_LINGUISTIC_CONTENT));
        Assert.assertFalse(MetadataTranslationUtils.noTranslationRequired("es"));
        Assert.assertTrue(MetadataTranslationUtils.noTranslationRequired("en"));
    }

    private void checkJson(JSONObject body, boolean v2OrDetect) {
        Assert.assertNotNull(body);
        if (v2OrDetect) {
            Assert.assertTrue(body.has("apikey"));
            Assert.assertTrue(body.has(MetadataTranslationUtils.MODE));
            Assert.assertTrue(body.has(MetadataTranslationUtils.SOURCE_LANG));
            Assert.assertTrue(body.has(MetadataTranslationUtils.TRANSLATE_SOURCE));

        } else {
            Assert.assertTrue(body.has(MetadataTranslationUtils.TEXT));
            Assert.assertTrue(body.has(MetadataTranslationUtils.TRANSLATE_SOURCE));
            Assert.assertTrue(body.has(MetadataTranslationUtils.TRANSLATE_TARGET));
        }


    }
}
