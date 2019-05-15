/*
 * Copyright 2007-2015 The Europeana Foundation
 *
 * Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 * by the European Commission;
 * You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 * http://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 * any kind, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under
 * the Licence.
 */

package eu.europeana.api2.v2.utils;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Class containing a number of useful controller utilities (mainly for setting headers)
 * 
 */
public final class ControllerUtils {

    private static final String ALLOWED                 = "GET, HEAD, POST";
    private static final String ACCEPT                 = "Accept";

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
        response.addHeader("Allow", ALLOWED);
    }
}
