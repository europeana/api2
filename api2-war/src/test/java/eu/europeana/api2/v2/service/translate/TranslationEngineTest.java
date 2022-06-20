package eu.europeana.api2.v2.service.translate;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TranslationEngineTest {

    @Test
    public void fromStringTest() {
        assertEquals(TranslationEngine.GOOGLE, TranslationEngine.fromString("google"));
        assertEquals(TranslationEngine.PANGEANIC, TranslationEngine.fromString(" pangeanIC "));
        assertEquals(TranslationEngine.NONE, TranslationEngine.fromString("bla"));
        assertEquals(TranslationEngine.NONE, TranslationEngine.fromString(null));
    }
}
