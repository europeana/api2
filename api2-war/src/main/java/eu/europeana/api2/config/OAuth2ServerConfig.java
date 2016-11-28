package eu.europeana.api2.config;

import eu.europeana.api2.web.security.oauth2.ApiApprovalHandler;
import eu.europeana.api2.web.security.oauth2.ApiTokenStore;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.approval.ApprovalStore;
import org.springframework.security.oauth2.provider.approval.TokenApprovalStore;
import org.springframework.security.oauth2.provider.approval.UserApprovalHandler;
import org.springframework.security.oauth2.provider.request.DefaultOAuth2RequestFactory;
import org.springframework.security.oauth2.provider.token.TokenStore;

import javax.annotation.Resource;

import static org.springframework.http.HttpMethod.*;
import static org.springframework.security.config.http.SessionCreationPolicy.IF_REQUIRED;


/**
 * @author Willem-Jan Boogerd (www.eledge.net/contact).
 */
@Configuration
public class OAuth2ServerConfig {

    @Configuration
    @EnableResourceServer
    protected static class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {

        private static final String EUROPEANA_RESOURCE_ID = "Europeana API2";

        @Override
        public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
            resources.resourceId(EUROPEANA_RESOURCE_ID).stateless(true);
        }

        @Override
        public void configure(HttpSecurity http) throws Exception {
            // @formatter:off
            http
                    .sessionManagement()
                    .sessionCreationPolicy(IF_REQUIRED)
                    .and()
                    .requestMatchers()
                    .antMatchers("/user/**", "/oauth/users/**", "/oauth/clients/**")
                    .and()
                    .csrf()
                    .disable()
                    .authorizeRequests()
                    .antMatchers(GET, "/user/profile").hasRole("USER").and()
                    .authorizeRequests()
                    .antMatchers(GET, "/user/**").access("#oauth2.hasScope('read')")
                    .antMatchers(POST, "/user/**").access("#oauth2.hasScope('write')")
                    .antMatchers(PUT, "/user/**").access("#oauth2.hasScope('write')")
                    .antMatchers(DELETE, "/user/**").access("#oauth2.hasScope('write')")
                    // Authentication,
                    .regexMatchers(DELETE, "/oauth/users/([^/].*?)/tokens/.*")
                    .access("#oauth2.clientHasRole('CLIENT') and (#oauth2.hasRole('USER') or #oauth2.isClient()) and #oauth2.hasScope('write')")
                    .regexMatchers(GET, "/oauth/clients/([^/].*?)/users/.*")
                    .access("#oauth2.clientHasRole('CLIENT') and (#oauth2.hasRole('USER') or #oauth2.isClient()) and #oauth2.hasScope('read')")
                    .regexMatchers(GET, "/oauth/clients/.*")
                    .access("#oauth2.clientHasRole('CLIENT') and #oauth2.isClient() and #oauth2.hasScope('read')")

            ;
            // @formatter:on
        }
    }

    @Configuration
    @EnableAuthorizationServer
    protected static class AuthorizationServerConfiguration extends AuthorizationServerConfigurerAdapter {

        @Resource
        private UserApprovalHandler userApprovalHandler;

        @Resource(name = "api2_oauth2_clientDetailsService")
        private ClientDetailsService clientDetailsService;

        @Resource(name = "api2_oauth2_authenticationManagerBean")
        private AuthenticationManager authenticationManager;

        @Override
        public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
            clients
                    .withClientDetails(clientDetailsService);
        }

        @Override
        public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
            endpoints
                    .tokenStore(tokenStore())
                    .userApprovalHandler(userApprovalHandler)
                    .authenticationManager(authenticationManager);
        }

        @Override
        public void configure(AuthorizationServerSecurityConfigurer oauthServer) throws Exception {
            oauthServer.realm("europeana/client");
        }

        @Bean
        public TokenStore tokenStore() {
            return new ApiTokenStore();
        }

//        @Bean(name = "api2_oauth2_clientDetailsService")
//        public ClientDetailsService clientDetailsService() {
//            return new OAuth2ClientDetailsService();
//        }
    }

    @Configuration
    protected static class Stuff {

        @Resource(name = "api2_oauth2_clientDetailsService")
        private ClientDetailsService clientDetailsService;

        @Resource
        private TokenStore tokenStore;

        @Bean
        public ApprovalStore approvalStore() throws Exception {
            TokenApprovalStore store = new TokenApprovalStore();
            store.setTokenStore(tokenStore);
            return store;
        }

        @Bean
        @Lazy
        @Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
        public ApiApprovalHandler userApprovalHandler() throws Exception {
            ApiApprovalHandler handler = new ApiApprovalHandler();
            handler.setApprovalStore(approvalStore());
            handler.setRequestFactory(new DefaultOAuth2RequestFactory(clientDetailsService));
            handler.setClientDetailsService(clientDetailsService);
            handler.setUseApprovalStore(true);

            return handler;
        }
    }

}
