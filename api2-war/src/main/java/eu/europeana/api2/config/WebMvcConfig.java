package eu.europeana.api2.config;

import eu.europeana.api2.utils.XmlUtils;
import eu.europeana.api2.v2.model.xml.kml.KmlResponse;
import eu.europeana.api2.v2.model.xml.rss.RssResponse;
import eu.europeana.api2.v2.model.xml.rss.fieldtrip.FieldTripResponse;
import eu.europeana.api2.v2.web.controller.SearchController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.xml.MarshallingView;

import java.util.Collections;

/**
 * @author Willem-Jan Boogerd (www.eledge.net/contact).
 */
@Configuration
@ComponentScan(basePackages = {"eu.europeana.api2.web.controller, eu.europeana.api2.v2.web.controller"})
@EnableWebMvc
@Import(SwaggerConfig.class)
public class WebMvcConfig extends WebMvcConfigurerAdapter {

    @Bean
    public ViewResolver contentViewResolver() throws Exception {
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
    public SearchController searchController() {
        return new SearchController();
    }

    @Bean
    public MarshallingView marshallingView() {
        return new MarshallingView(jaxb2Marshaller());
    }

    @Bean
    public XmlUtils xmlUtils() {
        return new XmlUtils();
    }

    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }
}
