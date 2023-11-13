package eu.europeana.api2.v2.utils;

import eu.europeana.api.commons.exception.ApiKeyExtractionException;
import eu.europeana.api.commons.exception.AuthorizationExtractionException;
import eu.europeana.api.commons.oauth2.utils.OAuthUtils;
import eu.europeana.api.commons.web.exception.ApplicationAuthenticationException;
import eu.europeana.api2.ApiKeyException;
import eu.europeana.api2.model.utils.Api2UrlService;
import eu.europeana.api2.v2.service.ApiAuthorizationService;
import eu.europeana.corelib.web.exception.ProblemType;
import java.io.IOException;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility class for checking API client keys
 *
 * @author Patrick Ehlert on 13-6-17.
 */
public class ApiKeyUtils{
    private static final Logger LOG                = LogManager.getLogger(ApiKeyUtils.class);

    /** Method to fetch ApiKey Either from request header or request parameter
     * @param request HttpServletRequest
     * @return apikey String
     */
    public static String extractApiKeyFromRequest(HttpServletRequest request) {
        try {
            return OAuthUtils.extractApiKey(request);
        } catch (ApiKeyExtractionException | AuthorizationExtractionException e) {
            LOG.info("Missing Apikey in request!!");
            return null;
        }
    }

}
