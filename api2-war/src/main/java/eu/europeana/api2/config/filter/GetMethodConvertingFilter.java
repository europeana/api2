package eu.europeana.api2.config.filter;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;

/**
 * Created by Jeroen Jeurissen on 16-9-16.
 * Fixes tomcat8 put issue (HTTP Status 405 - JSPs only permit GET POST or HEAD)
 * See http://stackoverflow.com/questions/24673041/405-jsp-error-with-put-method for more information
 */
@WebFilter(filterName = "methodConvertingFilter", urlPatterns = {"/*"}, dispatcherTypes = {DispatcherType.FORWARD})
public class GetMethodConvertingFilter implements Filter {

    @Override
    public void init(FilterConfig config) throws ServletException {
        // do nothing
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
            HttpServletRequest servletRequest = (HttpServletRequest)request;
            // pass the request along the filter chain
            chain.doFilter(wrapRequest(servletRequest), response);
    }

    @Override
    public void destroy() {
        // do nothing
    }

    private static HttpServletRequestWrapper wrapRequest(HttpServletRequest request) {
        return new HttpServletRequestWrapper(request) {
            @Override
            public String getMethod() {
                return "GET";
            }
        };
    }
}

