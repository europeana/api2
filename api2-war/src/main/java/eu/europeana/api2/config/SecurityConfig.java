package eu.europeana.api2.config;

import eu.europeana.api2.web.security.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.annotation.Resource;

import java.util.Arrays;

import static eu.europeana.corelib.db.util.UserUtils.getPasswordEncoder;

/**
 * @author Willem-Jan Boogerd (www.eledge.net/contact).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Resource(name = "api2_userDetailsService")
    private UserDetailsService userDetailsService;

    @Resource
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .userDetailsService(userDetailsService)
                .passwordEncoder(getPasswordEncoder());
    }

    @Configuration
    @Order(1)
    @ComponentScan(basePackageClasses = UserDetailsServiceImpl.class)

    /**
     * @deprecated 2018-01-09 old MyEuropeana functionality
     */
    @Deprecated
    public static class OAuthLoginConfig extends WebSecurityConfigurerAdapter {

        @Override
        @Bean(name = "api2_oauth2_authenticationManagerBean")
        public AuthenticationManager authenticationManagerBean() throws Exception {
            return super.authenticationManagerBean();
        }

        @Override
        public void configure(WebSecurity web) throws Exception {
            web.ignoring().antMatchers(
                    "/oauth/uncache_approvals",
                    "/oauth/cache_approvals",
                    "/user/activate/**",
                    "/user/password/**"
            );
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {

            // @formatter:off
            http.sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                    .and()
                    .httpBasic().disable()
                    .requestMatchers()
                    .antMatchers("/oauth/authorize","/oauth/token","/oauth/confirm_access","/oauth/error","/oauth/check_token","/oauth/token_key", "/oAuthLogin*")
                    .and()
                    .authorizeRequests()
                    .anyRequest().hasAnyRole("USER")
                    .and()
                    .csrf()
                    .requireCsrfProtectionMatcher(new AntPathRequestMatcher("/oauth/authorize"))
                    .requireCsrfProtectionMatcher(new AntPathRequestMatcher("/oauth/token"))
                    .disable()
                    .formLogin()
                    .loginProcessingUrl("/oAuthLogin.do")
                    .loginPage("/oAuthLogin")
                    .permitAll()
                    .and()
                    .logout()
                    .logoutSuccessUrl("/")
                    .logoutUrl("/logout")
                    .permitAll();

            // @formatter:on
        }
    }

    @Configuration
    public static class BasicLoginConfig extends WebSecurityConfigurerAdapter {

        @Override
        public void configure(WebSecurity web) throws Exception {
            web.ignoring().antMatchers(
                    "/image*",
                    "/v2/**",
                    "/opensearch.rss",
                    "/opensearch.json",
                    "/oauth/uncache_approvals",
                    "/oauth/cache_approvals"
            );

        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            // @formatter:off
            http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                    .and()
                    .authorizeRequests()
                    .antMatchers("/login/**").permitAll()
                    .antMatchers("/mydata", "/mydata/**").hasAnyRole("CLIENT", "ADMIN_CLIENT")
                    .antMatchers("/admin", "/admin/**").hasRole("ADMIN_CLIENT")
                    .and()
                    .httpBasic()
                    .realmName("Europeana API2")
                    .and()
                    .csrf()
                    .disable()
                    .logout()
                    .logoutSuccessUrl("/")
                    .logoutUrl("/logout")
                    .permitAll()
            ;
            // @formatter:on
        }
    }

}
