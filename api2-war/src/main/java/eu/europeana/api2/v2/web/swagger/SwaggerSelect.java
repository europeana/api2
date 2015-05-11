/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.europeana.api2.v2.web.swagger;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Target;
import java.lang.annotation.Documented;

@Retention(RetentionPolicy.RUNTIME)
@Target(value=TYPE)
@Documented

/**
 * Used to annotate classes to be included in the Swagger output
 * @author luthien
 */
public @interface SwaggerSelect {
    String value() default "";
}
