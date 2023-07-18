package eu.europeana.api2.v2.utils;

import eu.europeana.api2.v2.model.enums.Profile;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Class containing a number of useful controller utilities (mainly for setting headers)
 *
 */
public final class ControllerUtils {

    public static final String ALLOWED_GET_HEAD       = "GET, HEAD";
    public static final String ALLOWED_GET_HEAD_POST  = "GET, HEAD, POST";
    private static final String ACCEPT                = "Accept";
    private static final String PROFILE_PATTERN = "[&?]profile.*?(?=&|\\?|$)";

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
     * Build a Redirect response for Translation limit (Resource exhausted) exception
     * Removes the 'translate' profile from the query string
     *
     *
     * @param request
     * @param response
     */
    public static void redirectForTranslationsLimitException(HttpServletRequest request, HttpServletResponse response, Set<Profile> profiles) {
        String queryStringWithoutTranslate = getQueryStringWithoutTranslate(request.getQueryString(), profiles);

        final String location = ServletUriComponentsBuilder
                .fromCurrentServletMapping()
                .path(request.getRequestURI())
                .query(queryStringWithoutTranslate)
                .build().toUriString();

        response.setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT);
        response.setHeader(HttpHeaders.LOCATION, location);
    }


    /**
     * Removes 'profile=translate' OR 'translate' from the query string
     * @param query
     * @param profiles
     * @return
     */
    public static String getQueryStringWithoutTranslate(String query, Set<Profile> profiles) {
        String queryString = removeProfileFromRequest(query);
        profiles.remove(Profile.TRANSLATE); // translate profile is always present in this case
        if (!profiles.isEmpty()) {
            queryString = queryString + "&profile=" + profiles.stream().map(Profile::getName).collect(Collectors.joining(","));
        }
        return queryString;
    }

    /**
     * Cleans the query string by removing the profile param
     * There are high chances of profile param to ve present multiple time,
     * also in middle or in the start of the query. (As seen in production requests)
     *
     * "?" is added only if the query string starts with profile param
     *
     * @param query
     * @return
     */
    private static String removeProfileFromRequest(String query) {
        if (StringUtils.startsWith(query , "profile")) {
            query = "?" + query;
        }
        return query.replaceAll(PROFILE_PATTERN, "");
    }

}
