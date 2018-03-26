package eu.europeana.api2.config;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.EndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.PublicMetricsAutoConfiguration;
import org.springframework.boot.actuate.endpoint.InfoEndpoint;
import org.springframework.boot.actuate.endpoint.mvc.EndpointHandlerMapping;
import org.springframework.boot.actuate.endpoint.mvc.EndpointMvcAdapter;
import org.springframework.boot.actuate.endpoint.mvc.MvcEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

/**
 * Configuration to add Spring-Boot Actuator to an Spring MVC application.
 * For the moment we only activate the /info endpoint, which displays the information in the build.properties file
 * @author Patrick Ehlert
 * Created on 25-03-2018
 */
@Configuration
@PropertySource(value = "classpath:build.properties", ignoreResourceNotFound = true)
@Import({ EndpointAutoConfiguration.class, PublicMetricsAutoConfiguration.class })
public class ActuatorConfig {

    @Bean
    @Autowired
    public EndpointHandlerMapping endpointHandlerMapping(Collection<? extends MvcEndpoint> endpoints) {
        return new EndpointHandlerMapping(endpoints);
    }

    @Bean
    @Autowired
    public EndpointMvcAdapter activateInfoEndPoint(InfoEndpoint delegate) {
        return new EndpointMvcAdapter(delegate);
    }

}
