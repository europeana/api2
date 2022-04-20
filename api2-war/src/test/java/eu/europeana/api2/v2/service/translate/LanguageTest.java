package eu.europeana.api2.v2.service.translate;

import eu.europeana.api2.v2.exceptions.InvalidParamValueException;
import eu.europeana.api2.v2.model.translate.Language;
import eu.europeana.corelib.web.exception.EuropeanaException;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Test validating supported languages coming in from the lang request parameter
 *
 * @author P. Ehlert
 * Created 6 July 2021
 */
public class LanguageTest {

    @Test
    public void testSingleLanguageValid() throws EuropeanaException {
        Language lang = Language.validateSingle("en");
        assertNotNull(lang);
        assertEquals(Language.EN, lang);
    }

    @Test(expected = InvalidParamValueException.class)
    public void testSingleLanguageEmpty() throws EuropeanaException {
        Language.validateSingle("");
    }

    @Test(expected = InvalidParamValueException.class)
    public void testSingleLanguageInvalid() throws EuropeanaException {
        Language.validateSingle("dk");
    }

    @Test
    public void testMultipleLanguageValid()  throws EuropeanaException {
        List<Language> langs = Language.validateMultiple("fr, De , NL");
        assertNotNull(langs);
        assertEquals(3, langs.size());
        assertEquals(Language.FR, langs.get(0));
        assertEquals(Language.DE, langs.get(1));
        assertEquals(Language.NL, langs.get(2));
    }

    @Test(expected = InvalidParamValueException.class)
    public void testMultipleLanguageEmpty()  throws EuropeanaException {
        Language.validateMultiple(null);
    }

    @Test(expected = InvalidParamValueException.class)
    public void testMultipleLanguageInvalid()  throws EuropeanaException {
        Language.validateMultiple("es,it,xx");
    }

    @Test(expected = InvalidParamValueException.class)
    public void testMultipleLanguageInvalid2()  throws EuropeanaException {
        Language.validateMultiple(",");
    }

    @Test
    public void testIsSupported() {
        assertTrue(Language.isSupported("fi"));
        assertTrue(Language.isSupported("Da"));
        assertFalse(Language.isSupported(""));
        assertFalse(Language.isSupported("zz"));
        // with regions
        assertTrue(Language.isSupported("en-GB"));
        assertTrue(Language.isSupported("de-NL"));
        assertFalse(Language.isSupported(""));
        assertFalse(Language.isSupported("zz"));
    }

    @Test
    public void testGetLanguage() {
        assertEquals(Language.FI, Language.getLanguage("fi"));
        assertEquals(Language.EN, Language.getLanguage("en-GB"));
        assertEquals(Language.DE, Language.getLanguage("de-NL"));
        assertEquals(Language.ES, Language.getLanguage("es"));
    }

}
