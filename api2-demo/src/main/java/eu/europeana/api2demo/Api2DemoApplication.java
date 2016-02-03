package eu.europeana.api2demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;

/**
 * @author Willem-Jan Boogerd (www.eledge.net/contact).
 */
@SpringBootApplication
public class Api2DemoApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(Api2DemoApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(Api2DemoApplication.class);
    }

    //    @Override
//    public void onStartup(ServletContext container) throws ServletException {
//        super.onStartup(container);
//        registerProxyFilter(container, "springSecurityFilterChain");
//        registerProxyFilter(container, "oauth2ClientContextFilter");
//    }
//
//    @Override
//    protected WebApplicationContext createRootApplicationContext() {
//        return null;
//    }
//
//    @Override
//    protected WebApplicationContext createServletApplicationContext() {
//        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
//        context.register(WebSecurityConfig.class, WebMvcConfig.class);
//        return context;
//    }
//
//    @Override
//    protected String[] getServletMappings() {
//        return new String[]{"/"};
//    }
//
//    private void registerProxyFilter(ServletContext servletContext, String name) {
//        DelegatingFilterProxy filter = new DelegatingFilterProxy(name);
//        filter.setContextAttribute("org.springframework.web.servlet.FrameworkServlet.CONTEXT.dispatcher");
//        servletContext.addFilter(name, filter).addMappingForUrlPatterns(null, false, "/*");
//    }
}
