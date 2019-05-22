package eu.europeana.api2.v2.utils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Class containing a number of useful controller utilities (mainly for setting headers)
 * 
 */
public final class ControllerUtils {

    private static final String ALLOWED                 = "GET, HEAD, POST";

    private ControllerUtils() {
        // to avoid instantiating this class
    }

    /**
     * Extracts the format type from a request URL, e.g. JSON, JSON-LD, RDF, XML, etc.
     * @param request
     * @return String with request format, or null if no format information was found in the URL
     */
    public static String getRequestFormat(HttpServletRequest request) {
        String result = null;
        String uri = request.getRequestURI();
        if (uri.contains(".")) {
            result = uri.substring(uri.lastIndexOf('.')+1, uri.length());
        }
        return result;
    }

    /**
    * Add the 'UTF-8' character encoding to the response
    *
    * @param response The response to add the encoding and headers to
    */
    public static void addResponseHeaders(HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");
        response.addHeader("Allow", ALLOWED);
    }
}
