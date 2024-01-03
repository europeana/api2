package eu.europeana.api2.config;

import java.util.Collection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.EndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.PublicMetricsAutoConfiguration;
import org.springframework.boot.actuate.endpoint.InfoEndpoint;
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

    Logger LOG = LogManager.getLogger(ActuatorConfig.class);
    @Bean
    @Autowired
    public EndpointHandlerMappingCustom endpointHandlerMapping(Collection<? extends MvcEndpoint> endpoints) {
        return new EndpointHandlerMappingCustom(endpoints);
    }

    @Bean
    @Autowired
    public EndpointMvcAdapter activateInfoEndPoint(InfoEndpoint delegate) {
        LOG.info("InfoEndpoint bean activation : " + delegate.getId() + " --" +delegate);
        return new EndpointMvcAdapter(delegate);
    }


}
