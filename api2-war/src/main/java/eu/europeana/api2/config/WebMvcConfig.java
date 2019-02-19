package eu.europeana.api2.config;

import eu.europeana.api2.config.viewresolver.Jaxb2MarshallingXmlViewResolver;
import eu.europeana.api2.config.viewresolver.JsonViewResolver;
import eu.europeana.api2.config.viewresolver.JspViewResolver;
import eu.europeana.api2.utils.XmlUtils;
import eu.europeana.api2.v2.model.xml.kml.KmlResponse;
import eu.europeana.api2.v2.model.xml.rss.RssResponse;
import eu.europeana.api2.v2.model.xml.rss.fieldtrip.FieldTripResponse;
import eu.europeana.api2.v2.web.controller.SearchController;
import eu.europeana.api2.web.controller.ExceptionController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.ContentNegotiatingViewResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.xml.MarshallingView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Willem-Jan Boogerd (www.eledge.net/contact).
 */
@Configuration
@EnableWebMvc
@ComponentScan(basePackageClasses = {SearchController.class, ExceptionController.class})
@Import(SwaggerConfig.class) // make sure WebMVC is started before swagger initiates
@EnableAsync
public class WebMvcConfig extends WebMvcConfigurerAdapter {

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer.favorPathExtension(false);
    }

    @Bean
    public ViewResolver contentViewResolver() throws Exception {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/WEB-INF/jsp/");
        viewResolver.setSuffix(".jsp");
        return viewResolver;
    }

//    @Bean
//    public ContentNegotiatingViewResolver contentViewResolver() throws Exception {
//        ContentNegotiationManagerFactoryBean contentNegotiationManager = new ContentNegotiationManagerFactoryBean();
//        contentNegotiationManager.addMediaType("json", MediaType.APPLICATION_JSON);
//
//        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
//        viewResolver.setPrefix("/WEB-INF/jsp/");
//        viewResolver.setSuffix(".jsp");
//
//        MappingJackson2JsonView defaultView = new MappingJackson2JsonView();
//        defaultView.setExtractValueFromSingleKeyModel(true);
//
//        ContentNegotiatingViewResolver contentViewResolver = new ContentNegotiatingViewResolver();
//        contentViewResolver.setContentNegotiationManager(contentNegotiationManager.getObject());
//        contentViewResolver.setViewResolvers(Collections.<ViewResolver>singletonList(viewResolver));
//        contentViewResolver.setDefaultViews(Collections.<View>singletonList(defaultView));
//        return contentViewResolver;
//    }

//    @Override
//    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
//        configurer.ignoreAcceptHeader(true).defaultContentType(
//                MediaType.TEXT_HTML);
//    }
//
//    @Bean
//    public ViewResolver contentNegotiatingViewResolver(ContentNegotiationManager manager) {
//        ContentNegotiatingViewResolver resolver = new ContentNegotiatingViewResolver();
//        resolver.setContentNegotiationManager(manager);
//
//        // Define all possible view resolvers
//        List<ViewResolver> resolvers = new ArrayList<>();
//
//        resolvers.add(jaxb2MarshallingXmlViewResolver());
////        resolvers.add(jsonViewResolver());
//        resolvers.add(jspViewResolver());
//
//        resolver.setViewResolvers(resolvers);
//        return resolver;
//    }

//    @Bean
//    public ViewResolver jaxb2MarshallingXmlViewResolver() {
//        return new Jaxb2MarshallingXmlViewResolver(jaxb2Marshaller());
//    }

//    @Bean
//    public ViewResolver jsonViewResolver() {
//        return new JsonViewResolver();
//    }

//    @Bean
//    public ViewResolver jspViewResolver() {
//        return new JspViewResolver();
//    }

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
}
