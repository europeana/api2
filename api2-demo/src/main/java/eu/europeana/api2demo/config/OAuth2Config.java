package eu.europeana.api2demo.config;

import eu.europeana.api2demo.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;

import java.util.Arrays;

@Configuration
@EnableOAuth2Client
@PropertySource("classpath:api2demo.properties")
@ComponentScan("eu.europeana.api2demo.web.service")
public class OAuth2Config {

    @Value("${api2.key}")
    private String clientId;

    @Value("${api2.secret}")
    private String clientSecret;

    @Value("${api2.url.oauth.token}")
    private String accessTokenUri;

    @Value("${api2.url.oauth.authorize}")
    private String userAuthorizationUri;

    @Bean
    public OAuth2ProtectedResourceDetails myEuropeana() {
        AuthorizationCodeResourceDetails bean = new AuthorizationCodeResourceDetails();
        bean.setId("myEuropeana");
        bean.setClientId(clientId);
        bean.setClientSecret(clientSecret);
        bean.setAccessTokenUri(accessTokenUri);
        bean.setUserAuthorizationUri(userAuthorizationUri);
        bean.setScope(Arrays.asList("read", "write"));
        return bean;
    }

    @Bean
    public OAuth2RestTemplate myEuropeanaRestTemplate(OAuth2ClientContext clientContext) {
        return new OAuth2RestTemplate(myEuropeana(), clientContext);
    }


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


}
