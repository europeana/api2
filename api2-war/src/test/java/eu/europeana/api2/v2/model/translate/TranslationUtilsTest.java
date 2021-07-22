package eu.europeana.api2.v2.model.translate;

import eu.europeana.api2.v2.service.translate.TranslationService;
import eu.europeana.api2.v2.service.translate.TranslationUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TranslationUtilsTest {

    @Mock
    private TranslationService translationService;

    private static final String TARGET_LANG = "nl";
    private static final String SOURCE_LANG = "en";

    private static final String KEY1 = "key1";
    private static final String KEY2 = "key2";

    private static final List<String> VALUE1 = List.of("There is a theory which states that if ever anyone discovers exactly what the Universe is for and why it is here, it will instantly disappear and be replaced by something even more bizarre and inexplicable. There is another theory which states that this has already happened.");
    private static final List<String> VALUE2 = List.of(
            "",
            "This is just a text.  ",
            " ",
            "  And here's a second line");
    private static final Map<String, List<String>> MAP_TO_TRANSLATE = new LinkedHashMap<>() {{
        put(KEY1, VALUE1);
        put(KEY2, VALUE2);
    }};

    private static final List<String> TRANSLATION1 = List.of("Er is een theorie die stelt dat als iemand ooit ontdekt waar het heelal precies voor dient en waarom het hier is, het onmiddellijk zal verdwijnen en vervangen zal worden door iets dat nog bizarder en onverklaarbaarder is. Er is een andere theorie die stelt dat dit al is gebeurd.");
    private static final List<String> TRANSLATION2 = List.of(
            "",
            "Dit is maar een tekst.",
            "",
            "  En hier is een tweede regel ");

    private static final List<String> TRANSLATION = new ArrayList<>(){{
        addAll(TRANSLATION1);
        addAll(TRANSLATION2);
    }};
    private static final Map<String, List<String>> EXPECTED_MAP_WITH_TRANSLATION = new LinkedHashMap<>() {{
        put(KEY1, TRANSLATION1);
        put(KEY2, TRANSLATION2);
    }};

    @Test
    public void testTranslateMap() {
        when(translationService.translate(anyList(), eq(TARGET_LANG), eq(SOURCE_LANG))).thenReturn(TRANSLATION);

        Map<String, List<String>> translation = TranslationUtils.translate(translationService, MAP_TO_TRANSLATE, TARGET_LANG , SOURCE_LANG);

        this.assertEqual(EXPECTED_MAP_WITH_TRANSLATION, translation);

    }

    public void assertEqual(Map<String, List<String>> expected, Map<String, List<String>> actual) {
        assertEquals(expected.size(), actual.size());
        for (String key : expected.keySet()) {
            assertEquals(expected.get(key), actual.get(key));
        }
    }
}
