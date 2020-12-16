package eu.europeana.api2.utils;

import java.lang.annotation.*;


/**
 * Annotation used in Spring controller request params.
 * Parameters with this annotation are handled in {@link SolrEscapeAnnotationFormatterFactory}
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
public @interface SolrEscape {
}
