package eu.europeana.api2demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    final static String ROLE_USER = "USER";

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
                .withUser("demo1").password("demo1").roles(ROLE_USER).and()
                .withUser("demo2").password("demo2").roles(ROLE_USER).and()
                .withUser("demo3").password("demo3").roles(ROLE_USER).and()
                .withUser("demo4").password("demo4").roles(ROLE_USER).and()
                .withUser("demo5").password("demo5").roles(ROLE_USER);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // @formatter:off
        http
            .authorizeRequests()
                .antMatchers("/user/**").hasRole(ROLE_USER)
                .anyRequest().permitAll()
                .and()
            .logout()
                .logoutSuccessUrl("/")
                .logoutUrl("/logout.do")
                .permitAll()
                .and()
            .httpBasic()
                .realmName("Api2 DEMO application");
//                .and()
//            .anonymous();
        // @formatter:on

    }
}
