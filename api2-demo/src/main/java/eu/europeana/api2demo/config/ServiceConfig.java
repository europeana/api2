package eu.europeana.api2demo.config;

import eu.europeana.api2demo.Config;
import eu.europeana.api2demo.web.service.Api2UserService;
import eu.europeana.api2demo.web.service.impl.Api2UserServiceImpl;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.client.RestOperations;

@Configuration
@Import(OAuth2Config.class)
public class ServiceConfig {

    @Bean
    public static PropertySourcesPlaceholderConfigurer properties() {
        PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();
        propertySourcesPlaceholderConfigurer.setLocation(new ClassPathResource("api2demo.properties"));
        return propertySourcesPlaceholderConfigurer;
    }

    @Bean
    public Config config() {
        return new Config();
    }

    @Bean
    public Api2UserService userService(
            @Qualifier("myEuropeanaRestTemplate") RestOperations restTemplate) {
        Api2UserServiceImpl bean = new Api2UserServiceImpl();
        bean.setRestTemplate(restTemplate);
        return bean;
    }
}
