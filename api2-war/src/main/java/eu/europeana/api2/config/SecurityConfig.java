package eu.europeana.api2.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

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
        @Bean
        public AuthenticationManager authenticationManagerBean() throws Exception {
            return super.authenticationManagerBean();
        }

        @Override
        public void configure(WebSecurity web) throws Exception {
            web.ignoring().antMatchers(
                    "/image*",
                    "/v2/suggestions.json",
                    "/opensearch.rss",
                    "/opensearch.json",
                    "/v2/search.*",
                    "/v2/record/**",
                    "/oauth/uncache_approvals",
                    "/oauth/cache_approvals"
            );

        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            // @formatter:off
            http
//                    .antMatcher("/**")
                .authorizeRequests()
                    .antMatchers("/login*").permitAll()
                    .antMatchers("/mydata", "/mydata/**").hasAnyRole("CLIENT", "TRUSTED_CLIENT")
                    .antMatchers("/admin", "/admin/**").hasRole("TRUSTED_CLIENT")
                    .and()
                .csrf()
                    .requireCsrfProtectionMatcher(new AntPathRequestMatcher("/oauth/authorize"))
                    .disable()
                .httpBasic()
                    .realmName("Europeana API2")
//                    .formLogin()
//                    .loginProcessingUrl("/login.do")
//                    .loginPage("/login?form=myData")
                    .and()
                .logout()
                    .logoutSuccessUrl("/")
                    .logoutUrl("/logout.do");
            // @formatter:on
        }
    }

}
