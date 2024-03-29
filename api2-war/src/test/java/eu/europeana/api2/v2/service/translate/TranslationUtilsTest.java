package eu.europeana.api2.v2.service.translate;

import eu.europeana.api.translation.definitions.model.TranslationObj;
import eu.europeana.api2.v2.model.translate.TranslationMap;
import eu.europeana.api2.v2.utils.MockFullBean;
import eu.europeana.corelib.definitions.edm.beans.FullBean;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;

/**
 * Test the TranslationUtils
 *
 * @author Srishti singh
 * Created 9 Jan 2024
 */
@RunWith(MockitoJUnitRunner.class)
public class TranslationUtilsTest {

    private static final Map<String, List<String>> map = new HashMap<>();
    private static final FullBean bean = MockFullBean.mock();

    private static final String TARGET_LANG = "nl";

    private static final String KEY1 = "de";
    private static final String KEY2 = "no";
    private static final String KEY3 = "fr";

    // non -eligible case scenario for value "?", is now handled by Translation API
    private static final List<String> VALUES_1 = Arrays.asList("Calamatta, Luigi (1801 - 1869)", "Leonardo da Vinci (1452 - 1519)",
                                                            "graveur","voetbal", "http://data.europeana.eu/agent/base/6", "?");

    private static final List<String> VALUES_2 = Arrays.asList("TvB G 3674", "Paris Hilton", "voetbal", "http://data.europeana.eu/concept/base/190", "Paris Hilton");

    private static final List<String> VALUES_FOR_TRANSLATION_1 = Arrays.asList("Calamatta, Luigi (1801 - 1869)", "Leonardo da Vinci (1452 - 1519)",
            "graveur","voetbal", "?");
    private static final List<String> VALUES_FOR_TRANSLATION_2 = Arrays.asList("TvB G 3674", "Paris Hilton", "voetbal", "Landbruk");

    @Before
    public void setup() {
        map.put(KEY1, VALUES_1);
        map.put(KEY2, VALUES_2);
    }

    @Test
    public void Test_ifValuesShouldBePickedForTranslation() {
        Map<String,  List<String>> map = new HashMap<>();

        Assert.assertFalse(TranslationUtils.ifValuesShouldBePickedForTranslation(map, "nl", "en")); // map is empty

        map.put("nl", Arrays.asList("Hallo", "Nederlands"));
        map.put("fr", Arrays.asList("Bonjour", "France"));
        Assert.assertTrue(TranslationUtils.ifValuesShouldBePickedForTranslation(map, "nl", "en"));
        Assert.assertTrue(TranslationUtils.ifValuesShouldBePickedForTranslation(map, "fr", "en"));
        Assert.assertFalse(TranslationUtils.ifValuesShouldBePickedForTranslation(map, "de", "en")); // doesn't contain this lang

        // add region codes
        map.put("de-NL", Arrays.asList("region codes test", "test"));
        Assert.assertTrue(TranslationUtils.ifValuesShouldBePickedForTranslation(map, "de", "en"));

        // add pivot language
        map.put("en", Arrays.asList("english value present already"));
        // en already present in the map but this not a ingestion process, But there is already value in target lang
        Assert.assertFalse(TranslationUtils.ifValuesShouldBePickedForTranslation(map, "de", "en"));
    }

    @Test
    public void Test_createTranslationRequest() {
        List<String> text = Arrays.asList("Hallo", "Nederlands");
        List<TranslationObj> request = TranslationUtils.createTranslationRequest(text, "nl", "en");
        Assert.assertEquals("en", request.get(0).getSourceLang());
        Assert.assertEquals("nl", request.get(0).getTargetLang());
        Assert.assertEquals(2 , request.size());
    }

    @Test
    public void Test_getValuesToTranslate_Key1() {
        // the preflabel for agent already has en tag value
        List<String> valuesToTranslate = TranslationUtils.getValuesToTranslate(map, KEY1, TARGET_LANG, bean, false, null, null);
        Assert.assertEquals(5, valuesToTranslate.size());
        Assert.assertEquals(VALUES_FOR_TRANSLATION_1, valuesToTranslate);
    }

    @Test
    public void Test_getValuesToTranslate_Key2() {
        // will eliminate duplicate value and add the preflabel for concept
        List<String> valuesToTranslate = TranslationUtils.getValuesToTranslate(map, KEY2, TARGET_LANG,  bean, false, null, null);
        Assert.assertEquals(4, valuesToTranslate.size());
        Assert.assertEquals(VALUES_FOR_TRANSLATION_2, valuesToTranslate);

        // only literals
        valuesToTranslate.clear();
        valuesToTranslate = TranslationUtils.getValuesToTranslate(map, KEY2, TARGET_LANG, bean, true, null, null);
        Assert.assertEquals(3, valuesToTranslate.size());
        Assert.assertFalse(valuesToTranslate.contains("Landbruk"));
    }

    @Test
    public void Test_Optimisation() {
        TranslationMap prefLabelAcrossProxy = new TranslationMap(KEY1);
        TranslationMap textToTranslate = new TranslationMap(KEY1);

        prefLabelAcrossProxy.add("dcSubject", new ArrayList<>(Arrays.asList("Großbritannien", "Irland", "Guernsey", "Jersey", "Isle of Man", "Kanalinseln", "London", "Cricket", "Erster Weltkrieg", "Irland", "England", "Erster Weltkrieg")));
        prefLabelAcrossProxy.add("dcType", new ArrayList<>(Arrays.asList("Film")));
        prefLabelAcrossProxy.add("dctermsSpatial", new ArrayList<>(Arrays.asList("Großbritannien", "Britische Inseln", "Vereinigtes Königreich", "Irland", "Guernsey", "Jersey", "Isle of Man", "Kanalinseln", "London", "Cricket", "Erster Weltkrieg", "England", "Königreich der Niederland")));

        textToTranslate.add("dcDescription", new ArrayList<>(Arrays.asList("Wochenschau mit Artikeln über England während des Ersten Weltkriegs")));
        textToTranslate.add("dcSubject", new ArrayList<>(Arrays.asList("Großbritannien", "London", "Kricket", "Monarchie")));
        textToTranslate.add("dcTitle", new ArrayList<>(Arrays.asList("Wochenschau mit Artikeln über England während des Ersten Weltkriegs")));
        textToTranslate.add("dctermsSpatial", new ArrayList<>(Arrays.asList("großbritannien", "london", "cricket"))); // case sensitive values

        TranslationUtils.optimisation(prefLabelAcrossProxy, textToTranslate);

        Assert.assertFalse(textToTranslate.isEmpty());
        Assert.assertTrue(textToTranslate.containsKey("dcDescription"));
        // two values are removed
        Assert.assertTrue(textToTranslate.containsKey("dcSubject"));
        Assert.assertEquals(2, textToTranslate.get("dcSubject").size());

        Assert.assertTrue(textToTranslate.containsKey("dcTitle"));
        // all values removed
        Assert.assertFalse(textToTranslate.containsKey("dctermsSpatial"));
    }
}
