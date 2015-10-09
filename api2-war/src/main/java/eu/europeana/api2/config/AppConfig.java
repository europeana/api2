package eu.europeana.api2.config;

import com.mongodb.Mongo;
import eu.europeana.api2.v2.schedule.SugarCRMPollingScheduler;
import eu.europeana.api2.v2.service.SugarCRMCache;
import eu.europeana.api2.v2.service.SugarCRMImporter;
import eu.europeana.api2.v2.utils.ControllerUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.net.UnknownHostException;


/**
 * @author Willem-Jan Boogerd (www.eledge.net/contact).
 */
@Configuration
@ImportResource({
        "classpath:corelib-logging-context.xml",
        "classpath:corelib-db-context.xml",
        "classpath:corelib-solr-context.xml",
        "classpath:corelib-utils-context.xml",
        "classpath:corelib-web-context.xml",
        "classpath:spring-sugarcrmclient.xml"
})
@EnableScheduling
@PropertySource("classpath:europeana.properties")
public class AppConfig {

    @Value("${cachemongodb.host}")
    private String cacheHost;

    @Value("${cachemongodb.port}")
    private int cachePort;

    @Bean
    public static PropertySourcesPlaceholderConfigurer properties() {
        PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();
        propertySourcesPlaceholderConfigurer.setLocation(new ClassPathResource("europeana.properties"));
        return propertySourcesPlaceholderConfigurer;
    }

    @Bean
    public ControllerUtils controllerUtils() {
        return new ControllerUtils();
    }

    @Bean(name = "sugarCRMPoller")
    public SugarCRMPollingScheduler sugarCRMPollingScheduler() {
        return new SugarCRMPollingScheduler();
    }

    @Bean
    public SugarCRMCache sugarCRMCache() {
        return new SugarCRMCache();
    }

    @Bean
    public SugarCRMImporter sugarCRMImporter() {
        return new SugarCRMImporter();
    }

    @Bean(name = "api_db_mongo_cache")
    public Mongo ApiDbMongoCache() throws UnknownHostException {
        return new Mongo(cacheHost, cachePort);
    }

}
