package eu.europeana.api2.config;

import eu.europeana.api2.model.utils.Api2UrlService;
import eu.europeana.api2.utils.VersionUtils;
import eu.europeana.api2.v2.web.swagger.SwaggerIgnore;
import eu.europeana.api2.v2.web.swagger.SwaggerSelect;
import org.apache.commons.lang3.StringUtils;
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

    private static final String API_PATH     = "/api";

    private String getApiBaseUrl(){
        return StringUtils.substringAfter(
                Api2UrlService.getBeanInstance().getApi2BaseUrl(),
                "://");
    }

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
                .host(getApiBaseUrl())
                .pathProvider(new ApiPathProvider(API_PATH))
                .apiInfo(apiInfo());
    }

    private ApiInfo apiInfo() {
        String version = VersionUtils.getVersion(this.getClass());
        return new ApiInfo(
        "Europeana Search & Record API",
        "This Swagger API console provides an overview of the Europeana Search & Record API. " +
                "You can build and test anything from the simplest search to a complex query using facetList " +
                "such as dates, geotags and permissions. For more help and information, head to our " +
                "comprehensive <a href=\"https://pro.europeana.eu/page/intro\">online documentation</a>.",
                StringUtils.isNotEmpty(version) ? version : "version unknown",
        "https://www.europeana.eu/en/rights/api-terms-of-use",
        "https://pro.europeana.eu/page/intro#general",
        "API terms of use",
        "https://www.europeana.eu/en/rights/api-terms-of-use");
    }

    class ApiPathProvider extends AbstractPathProvider {
        private String apiPath;

        ApiPathProvider(String basePath) {
            this.apiPath = basePath;
        }

        @Override
        protected String applicationPath() {
            return apiPath;
        }

        @Override
        protected String getDocumentationPath() {
            return "/";
        }

        @Override
        public String getOperationPath(String operationPath) {
            UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromPath("/");
            return Paths.removeAdjacentForwardSlashes(
                    uriComponentsBuilder.path(operationPath.replaceFirst(apiPath, "")).build().toString());
        }
    }
}
