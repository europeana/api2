package eu.europeana.api2.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;

import javax.annotation.Resource;

/**
 * @author Willem-Jan Boogerd (www.eledge.net/contact).
 */
@EnableWebSecurity
public class SecurityConfig {

    @Configuration
    @Order(1)
    @ComponentScan("eu.europeana.api2.web.security")
    public static class basicLoginConfig extends WebSecurityConfigurerAdapter {

        @Resource(name = "api2_userDetailsService")
        private UserDetailsService userDetailsService;

        @Resource
        public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
            auth
                    .userDetailsService(userDetailsService)
                    .passwordEncoder(new ShaPasswordEncoder());
        }

        @Override
        public void configure(WebSecurity web) throws Exception {
            web.ignoring().antMatchers(
                    "/image*",
                    "/suggestions.json",
                    "/opensearch.rss",
                    "/opensearch.json",
                    "/search.*",
                    "/record/**"
            );

        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.antMatcher("/v2/mydata/**")
                    .authorizeRequests()
                    .antMatchers("/v2/mydata", "/v2/mydata/**").hasAnyRole("CLIENT", "TRUSTED_CLIENT")
                    .antMatchers("/apikey", "/apikey/**").hasRole("TRUSTED_CLIENT")
                    .and()
                            // FORM LOGIN
                    .formLogin()
                    .loginProcessingUrl("/login.do")
                    .loginPage("/login?form=myData")
                    .and()
                            // LOG OUT
                    .logout()
                    .logoutSuccessUrl("/")
                    .logoutUrl("/logout.do");
        }
    }

}
