package eu.europeana.api2.v2.utils;

import eu.europeana.api2.v2.model.enums.Profile;
import java.io.ByteArrayInputStream;
import java.util.Set;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.solr.common.util.Base64;
import org.apache.solr.common.util.JavaBinCodec;
import org.springframework.http.HttpStatus;

/**
 * Class containing a number of useful controller utilities (mainly for setting headers)
 *
 */
public final class ControllerUtils {

    public static final String ALLOWED_GET_HEAD       = "GET, HEAD";
    public static final String ALLOWED_GET_HEAD_POST  = "GET, HEAD, POST";
    private static final String ACCEPT                = "Accept";
    private static final String PROFILE_PATTERN = "[&?]profile.*?(?=&|\\?|$)";
    private static final String LANG_PATTERN    = "[&?]lang.*?(?=&|\\?|$)";
    private static final String PARAM_SEPERATOR    = "&";

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
     * And 'lang' param from query
     *
     * The redirect should not be translated or filtered
     *
     *
     * @param request
     * @param response
     */
    public static void redirectForTranslationsLimitException(HttpServletRequest request, HttpServletResponse response, Set<Profile> profiles) {
        String queryStringWithoutTranslate = getQueryStringWithoutTranslate(request.getQueryString(), profiles);
        String location = removeRequestMapping(request.getRequestURI()) + "?" +queryStringWithoutTranslate;
        response.setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT);
        response.setHeader(HttpHeaders.LOCATION, location);
    }

    /**
     * The redirects will automatically append the ServletMapping and
     * request URI.
     *
     * The idea is to send the data after the last "/" present in the request URI
     * For records mappings -  "/api/v2/record/x/y.json",  "/v2/record/x/y.json", "/record/v2/x/y.json",
     *         "/record/x/y.json" -> Only 'y.json' should be sent
     *     search -
     *        For these mappings -  '/record/search.json', '/api/v2/search.json', OR '/record/v2/search.json'
     *          -> Only 'search.json' should be sent
     *
     * @param request
     * @return
     */
    public static String removeRequestMapping(String request) {
        return StringUtils.substringAfterLast(request, "/");
    }

    /**
     * Removes 'profile=translate' OR 'translate' from the query string
     * @param query
     * @param profiles
     * @return
     */
    public static String getQueryStringWithoutTranslate(String query, Set<Profile> profiles) {
        // remove profile and lang param from request
        String queryString = removeProfileFromRequest(removeLangFromRequest(query));

        profiles.remove(Profile.TRANSLATE); // translate profile is always present in this case
        if (!profiles.isEmpty()) {
            if (StringUtils.startsWith(queryString, PARAM_SEPERATOR)) {
                queryString = StringUtils.removeStart(queryString, PARAM_SEPERATOR);
            }
            queryString = queryString + "&profile=" + profiles.stream().map(Profile::getName).collect(Collectors.joining(","));
        }
        return queryString;
    }

    /**
     * Removes the lang param from the query string
     */
    private static String removeLangFromRequest(String query) {
        if (StringUtils.startsWith(query , "lang")) {
            query = "?" + query;
        }
        return query.replaceAll(LANG_PATTERN, "");
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


    /**
     * Return true is status code is 5xx
     * @param httpStatusCode
     * @return
     */
    public static boolean is5xxError(int httpStatusCode) {
        HttpStatus status = HttpStatus.valueOf(httpStatusCode);
        return status.is5xxServerError();
    }

    public static boolean isBase64Encoded(String cursorMark) {
        try {
            byte[] buf =
                Base64.base64ToByteArray(cursorMark);
            try (JavaBinCodec jbc = new JavaBinCodec();
                ByteArrayInputStream in = new ByteArrayInputStream(buf)) {
                jbc.unmarshal(in);
                return true;
            }
        }
        catch (Exception e) {
            return false;
        }
    }
}
