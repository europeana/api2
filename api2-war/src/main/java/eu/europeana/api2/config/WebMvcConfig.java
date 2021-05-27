package eu.europeana.api2.config;

import eu.europeana.api2.utils.SolrEscapeAnnotationFormatterFactory;
import eu.europeana.api2.utils.XmlUtils;
import eu.europeana.api2.v2.model.xml.kml.KmlResponse;
import eu.europeana.api2.v2.model.xml.rss.RssResponse;
import eu.europeana.api2.v2.model.xml.rss.fieldtrip.FieldTripResponse;
import eu.europeana.api2.v2.web.controller.ObjectController;
import eu.europeana.api2.v2.web.controller.SearchController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.HttpHeaders;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.xml.MarshallingView;

import java.util.Collections;

/**
 * @author Willem-Jan Boogerd (www.eledge.net/contact).
 */
@Configuration
@EnableWebMvc
@ComponentScan(basePackageClasses = {ObjectController.class, SearchController.class})
@Import(SwaggerConfig.class) // make sure WebMVC is started before swagger initiates
@EnableAsync
public class WebMvcConfig extends WebMvcConfigurerAdapter {

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer.favorPathExtension(true);
    }

    @Bean
    public ViewResolver contentViewResolver() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/WEB-INF/jsp/");
        viewResolver.setSuffix(".jsp");
        return viewResolver;
    }

    @Bean(name = "api2_mvc_views_jaxbmarshaller")
    public Jaxb2Marshaller jaxb2Marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setClassesToBeBound(RssResponse.class, FieldTripResponse.class, KmlResponse.class);
        marshaller.setMarshallerProperties(Collections.singletonMap("jaxb.formatted.output", Boolean.TRUE));
        return marshaller;
    }

    @Bean
    public MarshallingView marshallingView() {
        return new MarshallingView(jaxb2Marshaller());
    }

    @Bean(name = "api2_mvc_xmlUtils")
    public XmlUtils xmlUtils() {
        return new XmlUtils();
    }



    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }


    /**
     * Configure CORS globally.
     * TODO: Get this working for the Swagger endpoint (/api/api-docs)
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowCredentials(false)
                .allowedOrigins("*")
                .allowedMethods("*")
                .exposedHeaders(HttpHeaders.ALLOW, HttpHeaders.VARY, HttpHeaders.LINK, HttpHeaders.ETAG)
                .maxAge(1000L); // in seconds
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        super.addFormatters(registry);
        registry.addFormatterForFieldAnnotation(new SolrEscapeAnnotationFormatterFactory());
    }
}
