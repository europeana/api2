package eu.europeana.api2.v2.service.translate;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.api2.v2.exceptions.InvalidParamValueException;
import eu.europeana.api2.v2.exceptions.TranslationException;
import eu.europeana.api2.v2.exceptions.TranslationServiceLimitException;
import eu.europeana.api2.v2.model.translate.Language;
import eu.europeana.api2.v2.utils.MockFullBean;
import eu.europeana.corelib.definitions.edm.beans.FullBean;
import eu.europeana.corelib.definitions.edm.entity.Proxy;
import eu.europeana.corelib.solr.entity.ProxyImpl;
import eu.europeana.corelib.utils.EuropeanaUriUtils;
import org.apache.logging.log4j.LogManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.mongodb.internal.connection.tlschannel.util.Util.assertTrue;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BeanTranslateLanguageTest {

    @Mock
    private TranslationService translationService;

    private static final String SOURCE_LANG_DEF = "def";
    private static final String SOURCE_LANG_DE = "de";
    private static final String SOURCE_LANG_EN = "en";
    private static final String SOURCE_LANG_NO = "no"; // for 1 entity preflabel translation
    private static final String TARGET_LANG = "nl";

    private static final List<String> DC_CREATOR = List.of("Calamatta, Luigi (1801-1869)", "Leonardo da Vinci (1452-1519)", "graveur");
    private static final List<String> DC_DATE = List.of("1821-1869");
    private static final List<String> DC_IDENTIFIER = List.of("TvB G 3674");
    private static final List<String> DC_TITLE = List.of("Mona Lisa");
    private static final List<String> DC_TYPE = List.of("grafiek");
    private static final List<String> DC_TERMS_ALTERNATIVE = List.of("Mona Lisa schilderij");
    private static final List<String> DC_RELATION = List.of("Europese musea", "Parijs");
    private static final List<String> DC_CREATOR_ENTITY_PREFLABEL = List.of("Landbouw");
    private static final List<String> DC_PUBLISHER = List.of("De dagelijkse bugel");

    // manually created using Google Translate
    private static final List<String> MOCK_TRANSLATION_FROM_DEF = new ArrayList<>() {{
        addAll(DC_CREATOR);
        addAll(DC_DATE);
        addAll(DC_IDENTIFIER);
        addAll(DC_TITLE);
    }};
    private static final List<String> MOCK_TRANSLATION_FROM_DE = new ArrayList<>() {{
        addAll(DC_TERMS_ALTERNATIVE);
    }};
    private static final List<String> MOCK_TRANSLATION_FROM_EN_NON_STATIC = new ArrayList<>() {{
        addAll(DC_RELATION);
        addAll(DC_TYPE);
    }};
    private static final List<String> MOCK_TRANSLATION_FROM_EN_STATIC = new ArrayList<>() {{
        addAll(DC_PUBLISHER);
        addAll(DC_RELATION);
        addAll(DC_TYPE);
    }};
    private static final List<String> MOCK_TRANSLATION_FROM_NO = new ArrayList<>() {{
        addAll(DC_CREATOR_ENTITY_PREFLABEL);
    }};

    @Before
    public void setup() {
        when(translationService.translate(anyList(), eq(TARGET_LANG))).thenReturn(MOCK_TRANSLATION_FROM_DEF);
        when(translationService.translate(anyList(), eq(TARGET_LANG), eq(SOURCE_LANG_DEF))).thenReturn(MOCK_TRANSLATION_FROM_DEF);
        // dctermsAlternative is only available in German
        when(translationService.translate(anyList(), eq(TARGET_LANG), eq(SOURCE_LANG_DE))).thenReturn(MOCK_TRANSLATION_FROM_DE);
        // dcType is only available in English
        when(translationService.translate(anyList(), eq(TARGET_LANG), eq(SOURCE_LANG_EN))).thenReturn(MOCK_TRANSLATION_FROM_EN_NON_STATIC);
        when(translationService.translate(anyList(), eq(TARGET_LANG), eq(SOURCE_LANG_NO))).thenReturn(MOCK_TRANSLATION_FROM_NO);
    }

    /**
     * Basic test with our mock bean to see if there are any processing errors.
     * We test all 4 possible cases for proxy fields:
     * <ol>
     *     <li>value found in target language, no translation required</li>
     *     <li>value found in English</li>
     *     <li>value found in 'def' language</li>
     *     <li>value found in other language</li>
     * </ol>
     * Furthermore the dcCreate field contains 1 uri which relates to a concept also in the fullbean (with prefLabel
     * in a different language). The dcType field has 1 uri that doesn't relate to a entity in the fullbean (so
     * value should be removed)
     */
    @Test
    public void testNoStaticTranslation() throws TranslationException, InvalidParamValueException,
            JsonProcessingException, TranslationServiceLimitException {
        FullBean bean = MockFullBean.mock();
        ObjectMapper mapper = new ObjectMapper();
        LogManager.getLogger(BeanFilterLanguageTest.class).info("Original fullbean = {}",
                mapper.writeValueAsString(bean));

        BeanTranslateService translateService = new BeanTranslateService(translationService);
        translateService.translateProxyFields(bean, List.of(Language.validateSingle(TARGET_LANG)));
        LogManager.getLogger(BeanFilterLanguageTest.class).info("Translated fullbean = {}",
                mapper.writeValueAsString(bean));

        Proxy euProxy = bean.getProxies().get(0);
        assertTrue(euProxy.isEuropeanaProxy());

        // dcFormat is already in Dutch, so no translation should be available (test1 in getProxyFieldToTranslate())
        assertNull(euProxy.getDcFormat());

        // dcType should be translated from English (test2 in getProxyFieldToTranslate())
        assertEquals(DC_TYPE, euProxy.getDcType().get(TARGET_LANG));

        // dcIdentifier , dcDate and dcTitle should not be added in the euProxy, as the translated and original value of 'def' are same
        assertNull(euProxy.getDcDate());
        assertNull(euProxy.getDcIdentifier());
        assertNull(euProxy.getDcTitle());

        // dcTerms should be translated from German (test4 in getProxyFieldToTranslate())
        assertEquals(DC_TERMS_ALTERNATIVE, euProxy.getDctermsAlternative().get(TARGET_LANG));

        // dcCreator has an uri that should be resolved by finding and translating the concept's preflabels (from norwegian)
        // other DC_CREATOR for def = ("Calamatta, Luigi (1801-1869)", "Leonardo da Vinci (1452-1519)", "graveur")
        // will not be present, as the translated value and original values are same
        List<String> dcCreatorExpected = new ArrayList<>(DC_CREATOR_ENTITY_PREFLABEL);
        assertEquals(dcCreatorExpected, euProxy.getDcCreator().get(TARGET_LANG));
        // Moreover in the europeanaProxy dcCreator already has values in "def" language, so these should still exists
        assertNotNull(euProxy.getDcCreator().get(SOURCE_LANG_DEF));

        // dcRelation also has an uri, with the entity's preflabel in the same language as the uri (def)
        assertEquals(DC_RELATION, euProxy.getDcRelation().get(TARGET_LANG));

        // Extra check to see if dcCreator doesn't contain uri anymore
         for (String value : euProxy.getDcCreator().get(TARGET_LANG)) {
            assertFalse(EuropeanaUriUtils.isUri(value));
        }
        // Extra check to see if dcCreator doesn't contain uri anymore
        for (String value : euProxy.getDcRelation().get(TARGET_LANG)) {
            assertFalse(EuropeanaUriUtils.isUri(value));
        }
        // check if dcType doesn't contain uri anymore (uri is unresolvable)
        for (String value : euProxy.getDcType().get(TARGET_LANG)) {
            assertFalse(EuropeanaUriUtils.isUri(value));
        }

    }

    @Test
    public void testStaticTranslation() throws TranslationException, InvalidParamValueException,
            JsonProcessingException, TranslationServiceLimitException {
        FullBean bean = MockFullBean.mock();
        BeanTranslateService translateService = new BeanTranslateService(translationService);

        // We modify the bean to have a static translation for fields dcContributor, dcPublisher, dcRights and
        // dcSource
        List<ProxyImpl> proxies = new ArrayList<>();
        proxies.addAll((List<ProxyImpl>) bean.getProxies());
        ProxyImpl aggregatorProxy = new ProxyImpl();
        proxies.add(1, aggregatorProxy);
        bean.setProxies(proxies);

        testHasStaticTranslation(translateService, aggregatorProxy, "dcContributor", false);
        testHasStaticTranslation(translateService, aggregatorProxy, "dcPublisher", false);
        testHasStaticTranslation(translateService, aggregatorProxy, "dcRights", false);
        testHasStaticTranslation(translateService, aggregatorProxy, "dcSource", false);

        // Dutch value already exists, so no translation required
        Map<String, List<String>> dcContributor = new HashMap();
        dcContributor.put(TARGET_LANG, List.of("Bijdrager1", "Bijdrager2"));
        aggregatorProxy.setDcContributor(dcContributor);

        // English value, so request translation
        Map<String, List<String>> dcPublisher = new HashMap();
        dcPublisher.put(SOURCE_LANG_EN, List.of("The Daily Bugle"));
        aggregatorProxy.setDcPublisher(dcPublisher);
        // adjust response to include 1 extra English translation value
        when(translationService.translate(anyList(), eq(TARGET_LANG), eq(SOURCE_LANG_EN))).thenReturn(MOCK_TRANSLATION_FROM_EN_STATIC);

        // URIs and "def" values should be ignored for static translations
        Map<String, List<String>> dcRights = new HashMap();
        dcRights.put(SOURCE_LANG_DEF, List.of("Coverage"));
        aggregatorProxy.setDcRights(dcRights);

        Map<String, List<String>> dcSource = new HashMap();
        dcSource.put(SOURCE_LANG_NO, List.of("https://some.source.no"));
        aggregatorProxy.setDcSource(dcSource);

        testHasStaticTranslation(translateService, aggregatorProxy, "dcContributor", true);
        testHasStaticTranslation(translateService, aggregatorProxy, "dcPublisher", true);
        testHasStaticTranslation(translateService, aggregatorProxy, "dcRights", true);
        testHasStaticTranslation(translateService, aggregatorProxy, "dcSource", true);

        ObjectMapper mapper = new ObjectMapper();
        LogManager.getLogger(BeanFilterLanguageTest.class).info("Original fullbean = {}",
                mapper.writeValueAsString(bean));

        // do actual translation
        translateService.translateProxyFields(bean, List.of(Language.validateSingle(TARGET_LANG)));
        LogManager.getLogger(BeanFilterLanguageTest.class).info("Translated fullbean = {}",
                mapper.writeValueAsString(bean));

        // check outcome
        Proxy euProxy = bean.getProxies().get(0);
        assertTrue(euProxy.isEuropeanaProxy());

        assertNull(euProxy.getDcContributor());

        assertNotNull(euProxy.getDcPublisher());
        assertEquals(DC_PUBLISHER, euProxy.getDcPublisher().get(TARGET_LANG));

        assertNull(euProxy.getDcRights());
        assertNull(euProxy.getDcSource());
    }

    private void testHasStaticTranslation(BeanTranslateService translateService, Proxy aggregatorProxy, String proxyFieldName,
                                       boolean expectedValue) {
        Field proxyField = ReflectionUtils.findField(aggregatorProxy.getClass(), proxyFieldName);
        assertNotNull("Field " + proxyField + " was not found", proxyField);
        if (expectedValue) {
            assertTrue(translateService.hasStaticTranslations(aggregatorProxy, proxyField));
        } else {
            assertFalse(translateService.hasStaticTranslations(aggregatorProxy, proxyField));
        }
    }
}
