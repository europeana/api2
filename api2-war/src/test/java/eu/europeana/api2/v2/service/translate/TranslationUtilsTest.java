package eu.europeana.api2.v2.service.translate;

import eu.europeana.api.translation.definitions.language.Language;
import eu.europeana.api2.v2.exceptions.TranslationException;
import eu.europeana.api2.v2.exceptions.TranslationServiceLimitException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;

import static org.junit.Assert.*;
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
    private static final String KEY3 = "key3";

    private static final List<String> ORIGINAL_MAP_1 = Arrays.asList("Calamatta, Luigi   (1801 - 1869)", "Leonardo da Vinci (1452 - 1519)","graveur","voetbal");
    private static final List<String> ORIGINAL_MAP_2 = Arrays.asList("TvB G 3674", "Paris Hilton", "voetbal");
    private static final List<String> ORIGINAL_MAP_3 = Arrays.asList("cheetah", "bread", "umbrella", "rain");

    private static final List<String> TRANSLATED_MAP_TRIM_SPACES = Arrays.asList("Calamatta, Luigi (1801-1869)", "Leonardo da Vinci (1452-1519)","graveur","voetbal");
    private static final List<String> TRANSLATED_MAP_PARTIAL = Arrays.asList("TvB G 3674", "Парис Хилтон", "fútbol");
    private static final List<String> TRANSLATED_MAP = Arrays.asList("leopardo", "pan de molde", "paraguas", "lluvia");

    private static final FieldValuesLanguageMap MAP_TO_TRANSLATE = new FieldValuesLanguageMap(SOURCE_LANG) {{
        put(KEY1, List.of("There is a theory which states that if ever anyone discovers exactly what the Universe is for and why it is here, it will instantly disappear and be replaced by something even more bizarre and inexplicable. There is another theory which states that this has already happened."));
        put(KEY2, List.of(
                "",
                "This is just a text.  ",
                " ",
                "  And here's a second line"));
    }};

    private static final List<String> TRANSLATION1 = List.of("Er is een theorie die stelt dat als iemand ooit ontdekt waar het heelal precies voor dient en waarom het hier is, het onmiddellijk zal verdwijnen en vervangen zal worden door iets dat nog bizarder en onverklaarbaarder is. Er is een andere theorie die stelt dat dit al is gebeurd.");
    private static final List<String> TRANSLATION2 = List.of(
            "",
            "Dit is maar een tekst.",
            "",
            "  En hier is een tweede regel ");

    private static final FieldValuesLanguageMap EXPECTED_MAP_WITH_TRANSLATION = new FieldValuesLanguageMap(TARGET_LANG) {{
        put(KEY1, TRANSLATION1);
        put(KEY2, TRANSLATION2);
    }};

    private static final FieldValuesLanguageMap DEF_MAP = new FieldValuesLanguageMap(Language.DEF) {{
        put(KEY1, ORIGINAL_MAP_1);
        put(KEY2, ORIGINAL_MAP_2);
        put(KEY3, ORIGINAL_MAP_3);
    }};

    private static final FieldValuesLanguageMap TRANSLATED_DEF_MAP = new FieldValuesLanguageMap(Language.DEF) {{
        put(KEY1, TRANSLATED_MAP_TRIM_SPACES);
        put(KEY2, TRANSLATED_MAP_PARTIAL);
        put(KEY3, TRANSLATED_MAP);
    }};

    /**
     * Test if sending a translationmap and putting pack all results under the appropriate keys is working
     */
    @Test
    public void testTranslateMap() throws TranslationException, TranslationServiceLimitException {
        when(translationService.translate(anyList(), eq(TARGET_LANG), eq(SOURCE_LANG))).thenReturn(new ArrayList<>(){{
            addAll(TRANSLATION1);
            addAll(TRANSLATION2);
        }});

        FieldValuesLanguageMap translation = TranslationUtils.translate(translationService, MAP_TO_TRANSLATE, TARGET_LANG, null);

        assertEqual(EXPECTED_MAP_WITH_TRANSLATION, translation);
    }

    @Test
    public void removeIfOriginalIsSameAsTranslated() {
        //1. final map should be null as translated and original map are identical
       assertNull(TranslationUtils.removeIfOriginalIsSameAsTranslated(DEF_MAP, DEF_MAP));

       FieldValuesLanguageMap finalMap = TranslationUtils.removeIfOriginalIsSameAsTranslated(TRANSLATED_DEF_MAP, DEF_MAP);
       assertEquals(Language.DEF, finalMap.getSourceLanguage());
       assertNull(finalMap.get(KEY1));
       // only two as 'TvB G 3674' is same
       assertFalse(finalMap.get(KEY2).contains("TvB G 3674"));
       assertEquals(Arrays.asList("Парис Хилтон", "fútbol"), finalMap.get(KEY2));
       assertEquals(TRANSLATED_MAP, finalMap.get(KEY3)); // all translation present
    }

    @Test
    public void testValueTruncation() {
        String test = "This is a test string of length 34";
        assertEquals(test, TranslationUtils.truncateFieldValue(test,100, 110));
        assertEquals("This is a test string of length...", TranslationUtils.truncateFieldValue(test, 30, 40));
        assertEquals("This is a test str...", TranslationUtils.truncateFieldValue(test, 17, 18));

    }

    /**
     * To pinpoint problems faster we rely on our own check here instead of simply doing an equals
     */
    private void assertEqual(FieldValuesLanguageMap expected, FieldValuesLanguageMap actual) {
        assertEquals(expected.getSourceLanguage(), actual.getSourceLanguage());
        assertEquals(expected.size(), actual.size());
        for (String key : expected.keySet()) {
            assertEquals(expected.get(key), actual.get(key));
        }
    }
}
