package eu.europeana.api2.config;

import eu.europeana.api2.config.filter.GetMethodConvertingFilter;
import eu.europeana.corelib.web.context.VcapPropertyLoader;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.support.AbstractDispatcherServletInitializer;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * Bootstrap the API application
 * @author Willem-Jan Boogerd (www.eledge.net/contact).
 */
@ComponentScan(basePackageClasses = {GetMethodConvertingFilter.class})
public class ServletInitializer extends AbstractDispatcherServletInitializer {

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        super.onStartup(servletContext);
        registerProxyFilter(servletContext, "springSecurityFilterChain");
    }

    private void registerProxyFilter(ServletContext servletContext, String name) {
        DelegatingFilterProxy filter = new DelegatingFilterProxy(name);
        filter.setContextAttribute("org.springframework.web.servlet.FrameworkServlet.CONTEXT.dispatcher");
        servletContext.addFilter(name, filter).addMappingForUrlPatterns(null, false, "/*");
    }

    @Override
    protected WebApplicationContext createRootApplicationContext() {
        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.register(AppConfig.class, OAuth2ServerConfig.class, SecurityConfig.class);
        new VcapPropertyLoader();
        return context;
    }

    @Override
    protected WebApplicationContext createServletApplicationContext() {
        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.register(SwaggerConfig.class, WebMvcConfig.class, ActuatorConfig.class);
        //context.addApplicationListener(new VcapPropertyLoaderListener());
        return context;
    }

    @Override
    protected String[] getServletMappings() {
        return new String[]{"/"};
    }

}
