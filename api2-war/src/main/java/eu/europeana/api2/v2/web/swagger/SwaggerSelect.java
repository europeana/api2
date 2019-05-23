package eu.europeana.api2.v2.web.swagger;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.TYPE;



/**
 * Used to annotate classes to be included in the Swagger output
 * @author luthien
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value=TYPE)
@Documented
public @interface SwaggerSelect {
    String value() default "";
}
