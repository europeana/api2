/*
 * Copyright 2007-2015 The Europeana Foundation
 *
 * Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 * by the European Commission;
 * You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 * http://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 * any kind, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under
 * the Licence.
 */

package eu.europeana.api2.config;

import eu.europeana.api2.v2.web.swagger.SwaggerIgnore;
import eu.europeana.api2.v2.web.swagger.SwaggerSelect;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import static com.google.common.base.Predicates.not;
import static com.google.common.base.Predicates.or;
import static springfox.documentation.builders.RequestHandlerSelectors.withClassAnnotation;
import static springfox.documentation.builders.RequestHandlerSelectors.withMethodAnnotation;

/**
 * @author luthien
 */
@EnableSwagger2 //Loads the spring beans required by the framework
@PropertySource("classpath:swagger.properties")
public class SwaggerConfig {

    @Value("${api2.url}")
    private String apiUrl;

    @Value("${api2.canonical.url}")
    private String apiCanonicalUrl;

    @Bean
    public Docket customImplementation() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                // Selects controllers annotated with @SwaggerSelect
                .apis(withClassAnnotation(SwaggerSelect.class)) //Selection by RequestHandler
                .apis(not(or(
                        withMethodAnnotation(SwaggerIgnore.class),
                        withClassAnnotation(SwaggerIgnore.class)
                ))) //Selection by RequestHandler
                .build()
                .host(hostUrl())
                .apiInfo(apiInfo());
    }
    
    ApiInfo apiInfo() {
        return new ApiInfo(
        "Europeana REST API",
        "This Swagger API console provides an overview of and interface to the Europeana REST API. " +
                "You can build and test anything from the simplest search to a complex query using facets " +
                "such as dates, geotags and permissions. For more help and information, head to our " +
                "comprehensive <a href=\"http://labs.europeana.api/api/\">online documentation</a>.",
        "2.0.14",
        "Contact Email",
        "development-core@europeanalabs.eu",
        "Terms of use",
        "http://www.europeana.eu/portal/rights/api-terms-of-use.html" );
    }

    private String hostUrl(){
        String hostUrl = StringUtils.isNotBlank(apiUrl) ? apiUrl : (
                StringUtils.isNotBlank(apiCanonicalUrl) ? apiCanonicalUrl : "http://europeana.eu");
        if (hostUrl.toLowerCase().contains("/api")){
            return hostUrl.substring(0, hostUrl.toLowerCase().indexOf("/api"));
        } else {
            return hostUrl;
        }
    }
}