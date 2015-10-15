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
@Configuration
@EnableWebSecurity
@ComponentScan("eu.europeana.api2.web.security")
public class SecurityConfig {

    @Resource(name = "api2_userDetailsService")
    private UserDetailsService userDetailsService;

    @Resource
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .userDetailsService(userDetailsService)
                .passwordEncoder(new ShaPasswordEncoder());
    }

    @Configuration
    @Order(1)
    public static class BasicLoginConfig extends WebSecurityConfigurerAdapter {

        @Override
        public void configure(WebSecurity web) throws Exception {
            web.ignoring().antMatchers(
                    "/image*",
                    "/v2/suggestions.json",
                    "/opensearch.rss",
                    "/opensearch.json",
                    "/v2/search.*",
                    "/v2/record/**"
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
