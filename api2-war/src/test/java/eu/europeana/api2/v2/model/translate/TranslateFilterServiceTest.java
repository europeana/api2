package eu.europeana.api2.v2.model.translate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.api2.v2.service.translate.TranslateFilterService;
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
public class TranslateFilterServiceTest {

    private static final TranslateFilterService FILTER_SERVICE = new TranslateFilterService(null);

    @Test
    public void testFilterFullBeanSingle() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        FullBean bean = MockFullBean.mock();
        LogManager.getLogger(TranslateFilterServiceTest.class).info("Unfiltered fullbean = {}",
                mapper.writeValueAsString(bean));

        List<Language> languages = new ArrayList<>(Arrays.asList(Language.EN));
        FILTER_SERVICE.filter(bean, languages);
        LogManager.getLogger(TranslateFilterServiceTest.class).info("Filtered fullbean = {}",
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

        // concepts should have still the first preflabel (no) and not the second anymore (de)
        assertEquals(1, bean.getConcepts().get(0).getPrefLabel().size());
        assertEquals(MockBeanConstants.CONCEPT_PREF_LABEL_NO, bean.getConcepts().get(0).getPrefLabel().get(MockBeanConstants.NO).get(0));
        assertNull(bean.getConcepts().get(0).getPrefLabel().get(MockBeanConstants.DE));
    }

    @Test
    public void testFilterFullBeanMultiple() {
        FullBean bean = MockFullBean.mock();
        List<Language> languages = new ArrayList<>(Arrays.asList(Language.PL, Language.IT, Language.BG));
        FILTER_SERVICE.filter(bean, languages);

        // first agents.preflabel should now have only 1 translation (English, Polish one should be filtered out)
        assertEquals(1, bean.getAgents().get(0).getPrefLabel().size());
        assertEquals(MockBeanConstants.AGENT1_PREF_LABEL_PL, bean.getAgents().get(0).getPrefLabel().get(MockBeanConstants.PL).get(0));
        assertNull(bean.getAgents().get(0).getPrefLabel().get(MockBeanConstants.EN));
    }
}
