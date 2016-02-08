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
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.annotation.Resource;

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
                    "/oauth/cache_approvals"
            );
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            // @formatter:off
            http
                .requestMatchers()
                    .antMatchers("/user/**","/oauth/**","/oAuthLogin*")
                    .and()
                .authorizeRequests()
                    .antMatchers("/oAuthLogin").permitAll()
                    .anyRequest().hasRole("USER")
                    .and()
                .csrf()
//                    .requireCsrfProtectionMatcher(new AntPathRequestMatcher("/oauth/authorize"))
                    .disable()
                .formLogin()
                    .loginProcessingUrl("/oAuthLogin.do")
                    .loginPage("/oAuthLogin")
                    .and()
                .logout()
                    .logoutSuccessUrl("/")
                    .logoutUrl("/logout")
            ;
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
            http
                .authorizeRequests()
                    .antMatchers("/login/**").permitAll()
                    .antMatchers("/mydata", "/mydata/**").hasAnyRole("CLIENT", "ADMIN_CLIENT")
                    .antMatchers("/admin", "/admin/**").hasRole("ADMIN_CLIENT")
                    .and()
                .httpBasic()
                    .realmName("Europeana API2")
                    .and()
                .logout()
                    .logoutSuccessUrl("/")
                    .logoutUrl("/logout")
            ;
            // @formatter:on
        }
    }

}
