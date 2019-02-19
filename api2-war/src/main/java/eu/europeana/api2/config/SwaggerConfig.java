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

import eu.europeana.api2.utils.VersionUtils;
import eu.europeana.api2.v2.web.swagger.SwaggerIgnore;
import eu.europeana.api2.v2.web.swagger.SwaggerSelect;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.util.UriComponentsBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.spring.web.paths.Paths;
import springfox.documentation.spring.web.paths.AbstractPathProvider;
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

    private static final String EUROPEANAURL = "http://europeana.eu";

    @Value("${api2.baseUrl}")
    private String apiUrl;

    @Bean
    public Docket customImplementation() {
        System.out.println("+++++ 2222222");
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                // Selects controllers annotated with @SwaggerSelect
                .apis(withClassAnnotation(SwaggerSelect.class)) //Selection by RequestHandler
                .apis(not(or(
                        withMethodAnnotation(SwaggerIgnore.class),
                        withClassAnnotation(SwaggerIgnore.class)
                ))) //Selection by RequestHandler
                .build()
                .host(getHostUrl())
                .pathProvider(new BasePathAwareRelativePathProvider(getApiPath()))
                .apiInfo(apiInfo());
    }

    ApiInfo apiInfo() {
        String version = VersionUtils.getVersion(this.getClass());
        return new ApiInfo(
        "Europeana REST API",
        "This Swagger API console provides an overview of an interface to the Europeana REST API. " +
                "You can build and test anything from the simplest search to a complex query using facetList " +
                "such as dates, geotags and permissions. For more help and information, head to our " +
                "comprehensive <a href=\"https://pro.europeana.eu/page/intro\">online documentation</a>.",
                StringUtils.isNotEmpty(version) ? version : "version unknown",
        "https://www.europeana.eu/portal/en/rights/api.html",
        "https://pro.europeana.eu/page/intro#general",
        "API terms of use",
        "https://www.europeana.eu/portal/en/rights/api.html");
    }

    private String getApiPath(){
        return "/" + (fullApiUrl().toLowerCase().contains("/api") ? "api" : "") ;
    }

    private String getHostUrl(){
        String hostUrl = fullApiUrl();
        return (hostUrl.toLowerCase().contains("/api") ? hostUrl.substring(0, hostUrl.toLowerCase().indexOf("/api")) : hostUrl);
    }

    private String fullApiUrl(){
        return StringUtils.isNotBlank(apiUrl) ? apiUrl : EUROPEANAURL;
    }

    class BasePathAwareRelativePathProvider extends AbstractPathProvider {
        private String basePath;

        public BasePathAwareRelativePathProvider(String basePath) {
            this.basePath = basePath;
        }

        @Override
        protected String applicationPath() {
            return basePath;
        }

        @Override
        protected String getDocumentationPath() {
            return "/";
        }

        @Override
        public String getOperationPath(String operationPath) {
            UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromPath("/");
            return Paths.removeAdjacentForwardSlashes(
                    uriComponentsBuilder.path(operationPath.replaceFirst(basePath, "")).build().toString());
        }
    }
}
