package eu.europeana.api2.v2.model.translate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.api2.v2.service.translate.BeanFilterLanguage;
import eu.europeana.api2.v2.utils.MockBeanConstants;
import eu.europeana.api2.v2.utils.MockFullBean;
import eu.europeana.corelib.definitions.edm.beans.FullBean;
import org.apache.logging.log4j.LogManager;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Test filtering a fullbean for particular languages
 * @author P. Ehlert
 * Created 8 July 2021
 */
public class BeanFilterLanguageTest {

    @Test
    public void testSingleFilterFields() throws JsonProcessingException {
        testSingleFilter(false);
    }

    @Test
    public void testSingleFilterMethods() throws JsonProcessingException {
        testSingleFilter(true);
    }

    public void testSingleFilter(boolean useReflectiveMethods) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        FullBean bean = MockFullBean.mock();
        LogManager.getLogger(BeanFilterLanguageTest.class).info("Unfiltered fullbean = {}",
                mapper.writeValueAsString(bean));

        List<Language> languages = new ArrayList<>(Arrays.asList(Language.EN));
        BeanFilterLanguage.filter(bean, languages, useReflectiveMethods);
        LogManager.getLogger(BeanFilterLanguageTest.class).info("Filtered fullbean = {}",
                mapper.writeValueAsString(bean));

        // first agents.preflabel should now have only 1 translation (English, Polish should be filtered out)
        assertEquals(1, bean.getAgents().get(0).getPrefLabel().size());
        assertEquals(MockBeanConstants.AGENT1_PREF_LABEL_EN, bean.getAgents().get(0).getPrefLabel().get(MockBeanConstants.EN).get(0));
        assertNull(bean.getAgents().get(0).getPrefLabel().get(MockBeanConstants.PL));

        // first agent.rdaGr2PlaceOfBirth should have 1 def value and 1 english value
        assertEquals(2, bean.getAgents().get(0).getRdaGr2PlaceOfBirth().size());
        assertEquals(2, bean.getAgents().get(0).getRdaGr2PlaceOfBirth().get(MockBeanConstants.DEF).size());
        assertEquals(1, bean.getAgents().get(0).getRdaGr2PlaceOfBirth().get(MockBeanConstants.EN).size());
        assertEquals(MockBeanConstants.AGENT1_BIRTH_PLACE_EN,
                bean.getAgents().get(0).getRdaGr2PlaceOfBirth().get(MockBeanConstants.EN).get(0));

        // places.altlabel should still have 1 translation (Italian)
        assertEquals(1, bean.getPlaces().get(0).getAltLabel().size());
        assertEquals(MockBeanConstants.PLACE_ALT_LABEL, bean.getPlaces().get(0).getAltLabel().get(MockBeanConstants.IT).get(0));

        // concepts preflabels and notes should all be gone
        assertNull(bean.getConcepts().get(0).getPrefLabel());
        assertNull(bean.getConcepts().get(0).getNote());

        // dcFormat is a field in the superClass of the superClass of a proxy, so we check if we can find and filter those okay
        assertEquals(1, bean.getProxies().get(0).getDcFormat().size());
        assertEquals(MockBeanConstants.PROXY1_DC_FORMAT1_DEF, bean.getProxies().get(0).getDcFormat().get(MockBeanConstants.DEF).get(0));
        assertEquals(MockBeanConstants.PROXY1_DC_FORMAT2_DEF, bean.getProxies().get(0).getDcFormat().get(MockBeanConstants.DEF).get(1));
        assertNull(bean.getProxies().get(0).getDcFormat().get(MockBeanConstants.NL));
    }

    @Test
    public void testMultipleFilterFields() throws JsonProcessingException {
        testMultipleFilter(false);
    }

    @Test
    public void testMultipleFilterMethods() throws JsonProcessingException {
        testMultipleFilter(true);
    }

    public void testMultipleFilter(boolean useReflectiveFields) {
        FullBean bean = MockFullBean.mock();
        List<Language> languages = new ArrayList<>(Arrays.asList(Language.PL, Language.IT, Language.BG));
        BeanFilterLanguage.filter(bean, languages, useReflectiveFields);

        // first agents.preflabel should now have only 1 translation (English, Polish one should be filtered out)
        assertEquals(1, bean.getAgents().get(0).getPrefLabel().size());
        assertEquals(MockBeanConstants.AGENT1_PREF_LABEL_PL, bean.getAgents().get(0).getPrefLabel().get(MockBeanConstants.PL).get(0));
        assertNull(bean.getAgents().get(0).getPrefLabel().get(MockBeanConstants.EN));
    }
}
