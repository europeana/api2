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

import javax.servlet.http.HttpServletResponse;

/**
 * Class containing a number of useful controller utilities (mainly for setting headers)
 * 
 */
public class ControllerUtils {

  private ControllerUtils() {
    // to avoid instantiating this class
  }

  /**
   * Bundling method for adding both {@link ControllerUtils#addCharacterEncoding character encoding}
   * and {@link ControllerUtils#addAccessControlHeaders access control headers} to the response with
   * one call
   * 
   * @param response The response to add the encoding and headers to
   */
  public static void addResponseHeaders(HttpServletResponse response) {
    addCharacterEncoding(response);
    addAccessControlHeaders(response);
  }

  /**
   * Add the 'UTF-8' character encoding to the response
   * 
   * @param response The response to add the character encoding to
   */
  public static void addCharacterEncoding(HttpServletResponse response) {
    response.setCharacterEncoding("UTF-8");
  }

  /**
   * Add the access control headers to the response, allowing origin '*', methods 'POST' and max age
   * '1000'
   * 
   * @param response The response to add access control headers to
   */
  public static void addAccessControlHeaders(HttpServletResponse response) {
    response.addHeader("Access-Control-Allow-Origin", "*");
    response.addHeader("Access-Control-Allow-Methods", "POST");
    response.addHeader("Access-Control-Max-Age", "1000");
  }
}
