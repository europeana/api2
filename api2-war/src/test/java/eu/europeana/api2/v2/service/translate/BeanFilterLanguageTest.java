package eu.europeana.api2.v2.service.translate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.api.translation.definitions.language.Language;
import eu.europeana.api2.v2.utils.LanguageFilter;
import eu.europeana.api2.v2.utils.MockBeanConstants;
import eu.europeana.api2.v2.utils.MockFullBean;
import eu.europeana.corelib.definitions.edm.beans.FullBean;
import org.apache.logging.log4j.LogManager;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Test filtering a fullbean for particular languages
 * @author P. Ehlert
 * Created 8 July 2021
 */
public class BeanFilterLanguageTest {

    @Test
    public void testSingleFilter() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        FullBean bean = MockFullBean.mock();
        LogManager.getLogger(BeanFilterLanguageTest.class).info("Unfiltered fullbean = {}",
                mapper.writeValueAsString(bean));

        List<Language> languages = Collections.singletonList(Language.EN);
        LanguageFilter.filter(bean, languages);
        LogManager.getLogger(BeanFilterLanguageTest.class).info("Filtered fullbean = {}",
                mapper.writeValueAsString(bean));

        // first agents.preflabel should now have only 1 language (English, Polish should be filtered out)
        assertEquals(1, bean.getAgents().get(0).getPrefLabel().size());
        assertEquals(MockBeanConstants.AGENT1_PREF_LABEL_EN, bean.getAgents().get(0).getPrefLabel().get(MockBeanConstants.EN).get(0));
        assertNull(bean.getAgents().get(0).getPrefLabel().get(MockBeanConstants.PL));

        // first agent.rdaGr2PlaceOfBirth should have 1 def value and 1 English value
        assertEquals(2, bean.getAgents().get(0).getRdaGr2PlaceOfBirth().size());
        assertEquals(2, bean.getAgents().get(0).getRdaGr2PlaceOfBirth().get(MockBeanConstants.DEF).size());
        assertEquals(1, bean.getAgents().get(0).getRdaGr2PlaceOfBirth().get(MockBeanConstants.EN).size());
        assertEquals(MockBeanConstants.AGENT1_BIRTH_PLACE_EN,
                bean.getAgents().get(0).getRdaGr2PlaceOfBirth().get(MockBeanConstants.EN).get(0));

        // places.altlabel shouldn't have any languages so everything will be displayed
        assertNotNull(bean.getPlaces().get(0).getAltLabel());

        // concepts preflabels and note all values present
        assertNotNull(bean.getConcepts().get(0).getPrefLabel());
        assertNotNull(bean.getConcepts().get(0).getNote());

        // dcDate has non-linguistic content, so should not be translated and still be present in provider proxy
        assertNull(bean.getProxies().get(0).getDcDate());
        assertEquals(MockBeanConstants.DC_DATE, bean.getProxies().get(1).getDcDate().get(MockBeanConstants.ZXX).get(0));

        // dcFormat is a field in the superClass of the superClass of a proxy, so we check if we can find and filter those okay
        assertEquals(1, bean.getProxies().get(1).getDcFormat().size());
        assertEquals(MockBeanConstants.PROXY1_DC_FORMAT1_DEF, bean.getProxies().get(1).getDcFormat().get(MockBeanConstants.DEF).get(0));
        assertEquals(MockBeanConstants.PROXY1_DC_FORMAT2_DEF, bean.getProxies().get(1).getDcFormat().get(MockBeanConstants.DEF).get(1));
        assertNull(bean.getProxies().get(1).getDcFormat().get(MockBeanConstants.NL));

        // dcTermsMedium should have en-GB values - assert to check filtering with locales
        assertEquals(1, bean.getProxies().get(1).getDctermsMedium().size());
        assertEquals(MockBeanConstants.PROXY1_DC_TERMS_MEDIUM_EN, bean.getProxies().get(1).getDctermsMedium().get(MockBeanConstants.EN_GB).get(0));

    }

    @Test
    public void testMultipleFilter() {
        FullBean bean = MockFullBean.mock();
        List<Language> languages = new ArrayList<>(Arrays.asList(Language.PL, Language.IT, Language.BG));
        LanguageFilter.filter(bean, languages);

        // first agents.preflabel should now have only 1 translation (English, Polish one should be filtered out)
        assertEquals(1, bean.getAgents().get(0).getPrefLabel().size());
        assertEquals(MockBeanConstants.AGENT1_PREF_LABEL_PL, bean.getAgents().get(0).getPrefLabel().get(MockBeanConstants.PL).get(0));
        assertNull(bean.getAgents().get(0).getPrefLabel().get(MockBeanConstants.EN));
    }

    @Test
    public void testMultipleFilterWithLocales() {
        FullBean bean = MockFullBean.mock();
        List<Language> languages = new ArrayList<>(Arrays.asList(Language.EN, Language.NL, Language.DE));
        LanguageFilter.filter(bean, languages);

        // dcTermsMedium should have en-GB and nl-NL values - assert to check filtering with locales
        assertEquals(2, bean.getProxies().get(1).getDctermsMedium().size());
        assertEquals(MockBeanConstants.PROXY1_DC_TERMS_MEDIUM_EN, bean.getProxies().get(1).getDctermsMedium().get(MockBeanConstants.EN_GB).get(0));
        assertEquals(MockBeanConstants.PROXY1_DC_TERMS_MEDIUM_NL, bean.getProxies().get(1).getDctermsMedium().get(MockBeanConstants.NL_NL).get(0));
        // 'it' lang should not be present and must have been filtered out
        assertNull(bean.getProxies().get(1).getDctermsMedium().get(MockBeanConstants.IT));

        // dcDescription, both de and 'de-NL' should be present
        assertEquals(2, bean.getProxies().get(1).getDcDescription().size());
        assertEquals(MockBeanConstants.PROXY1_DC_DESCRIPTION_NL, bean.getProxies().get(1).getDcDescription().get(MockBeanConstants.DE_NL).get(0));
        assertEquals(MockBeanConstants.PROXY1_DC_DESCRIPTION_DE, bean.getProxies().get(1).getDcDescription().get(MockBeanConstants.DE).get(0));
    }
}
