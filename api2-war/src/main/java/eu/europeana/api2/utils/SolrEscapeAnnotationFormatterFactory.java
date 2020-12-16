package eu.europeana.api2.utils;

import org.springframework.context.support.EmbeddedValueResolutionSupport;
import org.springframework.format.AnnotationFormatterFactory;
import org.springframework.format.Parser;
import org.springframework.format.Printer;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Formats RequestParams annotated with {@link SolrEscape}
 */
public class SolrEscapeAnnotationFormatterFactory extends EmbeddedValueResolutionSupport
        implements AnnotationFormatterFactory<SolrEscape> {
    @Override
    public Set<Class<?>> getFieldTypes() {
        Set<Class<?>> fieldTypes = new HashSet<>();
        fieldTypes.add(String.class);
        return Collections.unmodifiableSet(fieldTypes);
    }

    @Override
    public Printer<String> getPrinter(SolrEscape annotation, Class<?> aClass) {
        return new SolrLocalParamFormatter();
    }

    @Override
    public Parser<String> getParser(SolrEscape annotation, Class<?> aClass) {
        return new SolrLocalParamFormatter();
    }
}
