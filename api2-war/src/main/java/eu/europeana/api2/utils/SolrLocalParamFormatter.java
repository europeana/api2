package eu.europeana.api2.utils;

import org.springframework.format.Parser;
import org.springframework.format.Printer;

import java.text.ParseException;
import java.util.Locale;


public class SolrLocalParamFormatter implements Printer<String>, Parser<String> {
    @Override
    public String print(String str, Locale locale) {
        return str;
    }

    /**
     * Escapes Solr LocalParams from the input string.
     * LocalParams begin with "{!"
     *
     * @param str    input string
     * @param locale locale.
     * @return Formatted string
     * @throws ParseException on exception
     */
    @Override
    public String parse(String str, Locale locale) throws ParseException {
        return str.replace("{!", "\\{\\!").replace("}", "\\}");
    }
}
