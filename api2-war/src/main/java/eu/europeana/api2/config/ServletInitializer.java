package eu.europeana.api2.config;

import eu.europeana.corelib.web.context.VcapPropertyLoaderListener;
import org.springframework.util.ClassUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.support.AbstractDispatcherServletInitializer;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * @author Willem-Jan Boogerd (www.eledge.net/contact).
 */
public class ServletInitializer extends AbstractDispatcherServletInitializer {

    @Override
    protected WebApplicationContext createServletApplicationContext() {
        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.register(SwaggerConfig.class,WebMvcConfig.class);
        context.addApplicationListener(new VcapPropertyLoaderListener());
        return context;
    }

    @Override
    protected String[] getServletMappings() {
        return new String[]{"/"};
    }

    @Override
    protected WebApplicationContext createRootApplicationContext() {
        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.register(AppConfig.class,OAuth2ServerConfig.class,SecurityConfig.class);
        context.addApplicationListener(new VcapPropertyLoaderListener());
        return context;
    }

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

}
