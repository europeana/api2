package eu.europeana.api2.v2.utils;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class containing a number of useful controller utilities (mainly for setting headers)
 *
 */
public final class ControllerUtils {

    public static final String ALLOWED_GET_HEAD       = "GET, HEAD";
    public static final String ALLOWED_GET_HEAD_POST  = "GET, HEAD, POST";
    private static final String ACCEPT                = "Accept";
    private static final String TRANSLATE                 = "translate";

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
     * Extracts the value of the Accept header from the request URL, e.g. application/graphql
     * @param request
     * @return String with Accept header contents, or null if not found
     */
    public static String getRequestedMediaType(HttpServletRequest request) {
        if (StringUtils.isNotBlank(request.getHeader(ACCEPT))){
            return request.getHeader(ACCEPT);
        }
        return null;
    }

    public static String getRequestedContentType(HttpServletRequest request) {
        if (StringUtils.isNotBlank(request.getContentType())){
            return request.getContentType();
        }
        return null;
    }

    /**
     * Add the 'UTF-8' character encoding to the response
     *
     * @param response The response to add the encoding and headers to
     */
    public static void addResponseHeaders(HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");
        response.addHeader("Allow", ALLOWED_GET_HEAD);
    }

    /**
     * Removes 'profile=translate' OR 'translate' from the query string
     * @param query
     * @param profile
     * @return
     */
    public static String getQueryStringWithoutTranslate(String query, String profile) {
        String queryString;
        if (StringUtils.contains(profile, ",")) {
            query = StringUtils.remove(query, "profile="+profile);

            List<String> profileList = Arrays.stream(profile.split(","))
                    .map(String::trim)
                    .collect(Collectors.toList());
            profileList.remove(TRANSLATE);

            queryString = query + "&profile="+profileList.stream().collect(Collectors.joining(","));
        } else {
            queryString = StringUtils.remove(query, "profile=translate");
        }
        return queryString;
    }
}
