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
import springfox.documentation.service.Tag;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.paths.AbstractPathProvider;
import springfox.documentation.spring.web.paths.Paths;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import static com.google.common.base.Predicates.not;
import static com.google.common.base.Predicates.or;
import static springfox.documentation.builders.PathSelectors.ant;
import static springfox.documentation.builders.RequestHandlerSelectors.withClassAnnotation;
import static springfox.documentation.builders.RequestHandlerSelectors.withMethodAnnotation;

/**
 * @author luthien
 */
@EnableSwagger2 //Loads the spring beans required by the framework
@PropertySource("classpath:swagger.properties")
public class SwaggerConfig {

    public static final String SEARCH_TAG = "Search";
    public static final String RECORD_TAG = "Record";

    private static final String API_PATH     = "/";

    private String getApiBaseUrl(){
        return StringUtils.substringAfter(
                // empty route argument used to return default values
                Api2UrlService.getBeanInstance().getApi2BaseUrl(""),
                "://");
    }

    @Bean
    public Docket apiDocumentationConfig() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                // Selects controllers annotated with @SwaggerSelect
                .apis(withClassAnnotation(SwaggerSelect.class)) //Selection by RequestHandler
                .apis(not(or(
                        withMethodAnnotation(SwaggerIgnore.class),
                        withClassAnnotation(SwaggerIgnore.class)
                )))
                //EA-2447: only document /record/v2 pattern
                .paths(ant("/record/v2/**"))
                .build()

                .host(getApiBaseUrl())
                .pathProvider(new ApiPathProvider(API_PATH))
                .apiInfo(apiInfo())
                // override default descriptions
                // see: https://github.com/swagger-api/swagger-core/issues/1476#issuecomment-555017788
                .tags(new Tag(SEARCH_TAG, ""))
                .tags(new Tag(RECORD_TAG, ""));
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
