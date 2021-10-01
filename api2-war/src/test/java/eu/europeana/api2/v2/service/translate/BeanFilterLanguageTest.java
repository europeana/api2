package eu.europeana.api2.v2.service.translate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.api2.v2.model.translate.Language;
import eu.europeana.api2.v2.utils.MockBeanConstants;
import eu.europeana.api2.v2.utils.MockFullBean;
import eu.europeana.corelib.definitions.edm.beans.FullBean;
import org.apache.logging.log4j.LogManager;
import org.junit.Test;

import java.util.*;

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
        BeanFilterLanguage.filter(bean, languages);
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

        // places.altlabel shouldn't have any languages so be removed entirely
        assertNull(bean.getPlaces().get(0).getAltLabel());

        // concepts preflabels and notes should all be gone
        assertNull(bean.getConcepts().get(0).getPrefLabel());
        assertNull(bean.getConcepts().get(0).getNote());

        // dcFormat is a field in the superClass of the superClass of a proxy, so we check if we can find and filter those okay
        assertEquals(1, bean.getProxies().get(1).getDcFormat().size());
        assertEquals(MockBeanConstants.PROXY1_DC_FORMAT1_DEF, bean.getProxies().get(1).getDcFormat().get(MockBeanConstants.DEF).get(0));
        assertEquals(MockBeanConstants.PROXY1_DC_FORMAT2_DEF, bean.getProxies().get(1).getDcFormat().get(MockBeanConstants.DEF).get(1));
        assertNull(bean.getProxies().get(1).getDcFormat().get(MockBeanConstants.NL));
    }

    @Test
    public void testMultipleFilter() {
        FullBean bean = MockFullBean.mock();
        List<Language> languages = new ArrayList<>(Arrays.asList(Language.PL, Language.IT, Language.BG));
        BeanFilterLanguage.filter(bean, languages);

        // first agents.preflabel should now have only 1 translation (English, Polish one should be filtered out)
        assertEquals(1, bean.getAgents().get(0).getPrefLabel().size());
        assertEquals(MockBeanConstants.AGENT1_PREF_LABEL_PL, bean.getAgents().get(0).getPrefLabel().get(MockBeanConstants.PL).get(0));
        assertNull(bean.getAgents().get(0).getPrefLabel().get(MockBeanConstants.EN));
    }

    @Test
    public void testEmptyMapValues() {
        FullBean bean = MockFullBean.mock();
        // add a empty map {def=[]} for DcIdentifier
        bean.getProxies().get(1).getDcIdentifier().get(MockBeanConstants.DEF).clear();
        //add a empty map {pl=[]} for RdaGr2BiographicalInformation
        bean.getAgents().get(0).getRdaGr2BiographicalInformation().get(MockBeanConstants.PL).clear();

        // check the empty map exist before filtering
        assertNotNull(bean.getProxies().get(1).getDcIdentifier());
        assertNotNull(bean.getAgents().get(0).getRdaGr2BiographicalInformation().get(MockBeanConstants.PL));

        List<Language> languages = new ArrayList<>(Arrays.asList(Language.PL, Language.EN));
        BeanFilterLanguage.filter(bean, languages);

        assertNull(bean.getProxies().get(1).getDcIdentifier());
        // should only contain EN - translation. As PL is empty so it must be removed.
        assertNull(bean.getAgents().get(0).getRdaGr2BiographicalInformation().get(MockBeanConstants.PL));
        assertNotNull(bean.getAgents().get(0).getRdaGr2BiographicalInformation());
        assertNotNull(bean.getAgents().get(0).getRdaGr2BiographicalInformation().get(MockBeanConstants.EN));
    }
}
