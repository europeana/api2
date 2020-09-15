package eu.europeana.api2.utils;

import org.junit.Test;

import java.text.ParseException;
import java.util.Locale;

import static org.junit.Assert.*;

public class SolrLocalParamFormatterTest {
    SolrLocalParamFormatter formatter = new SolrLocalParamFormatter();

    @Test
    public void shouldEscapeInputString() throws ParseException {
        String input = "{! field=test}";
        String expected = "\\{\\! field=test\\}";
        assertEquals(expected, formatter.parse(input, Locale.getDefault()));
    }
}