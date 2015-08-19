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

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import eu.europeana.api2.ApiLimitException;
import eu.europeana.api2.v2.model.LimitResponse;
import eu.europeana.corelib.db.entity.enums.RecordType;
import eu.europeana.corelib.db.exception.DatabaseException;
import eu.europeana.corelib.db.service.ApiKeyService;
import eu.europeana.corelib.definitions.db.entity.relational.ApiKey;
import eu.europeana.corelib.logging.Logger;

/**
 * Class containing a number of useful controller utilities
 * 
 */
public class ControllerUtils {

  private Logger log = Logger.getLogger(ControllerUtils.class.getCanonicalName());

  @Resource
  private ApiKeyService apiService;

  /**
   * Check the number requests made to the API in the last (rolling) 24 hours. This is a per-user
   * limit (default: 10000) set in the myEuropeana database that is used as a throttling mechanism.
   * <p>
   * NOTE: This functionality has currently been disabled (ticket #1742); this method now always
   * returns the user's apikey and a request number of '999' (unless it's a unregistered user).
   * 
   * @param wskey The user's API web service key
   * @param url The requested URL
   * @param apiCall The REST endpoint of the call
   * @param recordType The type of record, defined by {@link RecordType}
   * @param profile The profile used
   * @return A {@link LimitResponse} consisting of the apiKey and current request number
   * @throws {@link ApiLimitException} if an unregistered or unauthorised key is provided, or if the
   *         daily limit has been reached
   */
  public LimitResponse checkLimit(String wskey, String url, String apiCall, RecordType recordType,
      String profile) throws ApiLimitException {
    ApiKey apiKey = null;
    long requestNumber = 0;
    long t;
    if (wskey == null || "".equalsIgnoreCase(wskey)){
      throw new ApiLimitException(wskey, apiCall, "no API key provided", 0, 401);
    }
    try {
      t = System.currentTimeMillis();
      apiKey = apiService.findByID(wskey);
      if (apiKey == null) {
        throw new ApiLimitException(wskey, apiCall, "Unregistered user", 0, 401);
      }
//       apiKey.getUsageLimit();
      log.info("get apiKey: " + (System.currentTimeMillis() - t));

      requestNumber = 999;
      log.info("setting default request number; (checklimit disabled): " + requestNumber);

      // t = System.currentTimeMillis();
//       requestNumber = apiService.checkReachedLimit(apiKey);
      // log.info("checkReachedLimit: " + (System.currentTimeMillis() - t));
      // t = System.currentTimeMillis();
      // apiLogService.logApiRequest(wskey, url, recordType, profile);
      // log.info("logApiRequest: " + (System.currentTimeMillis() - t));
    } catch (DatabaseException e) {
      // apiLogService.logApiRequest(wskey, url, recordType, profile);
      throw new ApiLimitException(wskey, apiCall, e.getMessage(), requestNumber, 401);
      // } catch (LimitReachedException e) {
      // apiLogService.logApiRequest(wskey, url, RecordType.LIMIT, recordType.toString());
      // throw new ApiLimitException(wskey, apiCall, e.getMessage(), requestNumber, 429);
    }
    return new LimitResponse(apiKey, requestNumber);
  }

  /**
   * Bundling method for adding both {@link ControllerUtils#addCharacterEncoding character encoding}
   * and {@link ControllerUtils#addAccessControlHeaders access control headers} to the response with
   * one call
   * 
   * @param response The response to add the encoding and headers to
   */
  public void addResponseHeaders(HttpServletResponse response) {
    addCharacterEncoding(response);
    addAccessControlHeaders(response);
  }

  /**
   * Add the 'UTF-8' character encoding to the response
   * 
   * @param response The response to add the character encoding to
   */
  public void addCharacterEncoding(HttpServletResponse response) {
    response.setCharacterEncoding("UTF-8");
  }

  /**
   * Add the access control headers to the response, allowing origin '*', methods 'POST' and max age
   * '1000'
   * 
   * @param response The response to add access control headers to
   */
  public void addAccessControlHeaders(HttpServletResponse response) {
    response.addHeader("Access-Control-Allow-Origin", "*");
    response.addHeader("Access-Control-Allow-Methods", "POST");
    response.addHeader("Access-Control-Max-Age", "1000");
  }
}
