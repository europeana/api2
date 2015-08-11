package eu.europeana.api2demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages = {"eu.europeana.api2demo.web.controller"})
@Import(ServiceConfig.class)
public class WebMvcConfig extends WebMvcConfigurerAdapter {

    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }

//    @Bean
//    public ContentNegotiatingViewResolver contentViewResolver() throws Exception {
//        ContentNegotiatingViewResolver contentViewResolver = new ContentNegotiatingViewResolver();
//        ContentNegotiationManagerFactoryBean contentNegotiationManager = new ContentNegotiationManagerFactoryBean();
//        contentNegotiationManager.addMediaType("json", MediaType.APPLICATION_JSON);
//        contentViewResolver.setContentNegotiationManager(contentNegotiationManager.getObject());
//        contentViewResolver.setDefaultViews(Collections.singletonList((View) new MappingJacksonJsonView()));
//        return contentViewResolver;
//    }
//

    @Bean
    public ViewResolver jspViewResolver() {
        InternalResourceViewResolver bean = new InternalResourceViewResolver();
        bean.setViewClass(JstlView.class);
        bean.setPrefix("/WEB-INF/views/");
        bean.setSuffix(".jsp");
        return bean;
    }
}
