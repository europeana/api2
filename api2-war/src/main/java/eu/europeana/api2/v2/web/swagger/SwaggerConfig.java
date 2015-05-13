/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.europeana.api2.v2.web.swagger;

import com.google.common.base.Predicate;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;


import static com.google.common.base.Predicates.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import static springfox.documentation.builders.PathSelectors.*;
import static springfox.documentation.builders.RequestHandlerSelectors.withClassAnnotation;
import springfox.documentation.service.ApiInfo;


/**
 *
 * @author luthien
 */

@Configuration
//@ComponentScan(basePackages = "io.swagger.api")
@ComponentScan(basePackages = "eu.europeana.api2.v2.web.controller")
@EnableWebMvc
@EnableSwagger2 //Loads the spring beans required by the framework
@PropertySource("classpath:swagger.properties")
public class SwaggerConfig {
    
    @Bean
    public Docket customImplementation(){
        return new Docket(DocumentationType.SWAGGER_2)
            .select() 
                // Selects controllers annotated with @SwaggerSelect
                .apis(withClassAnnotation(SwaggerSelect.class)) //Selection by RequestHandler
                .build()
            .apiInfo(apiInfo());
    }
    
    ApiInfo apiInfo() {
        ApiInfo apiInfo = new ApiInfo(
        "Europeana API",
        "TBD Api Description",
        "1.0",
        "Contact Email",
        "development-core@europeanalabs.eu",
        "TBD Licence Type",
        "http://www.europeana.eu/portal/rights/api-terms-of-use.html" );
        return apiInfo;
    }

}