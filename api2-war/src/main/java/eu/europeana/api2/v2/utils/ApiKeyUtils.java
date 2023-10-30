package eu.europeana.api2.v2.utils;

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
    private static final int    MAXCONNTOTAL       = 200;
    private static final int    MAXCONNPERROUTE    = 100;

    @Resource
    private Api2UrlService urlService;

    private CloseableHttpClient httpClient;

    @Resource
    ApiAuthorizationService authorizationService;

    /**
     * Constructor for ApiKeyUtils
     */
    public ApiKeyUtils(){
        // configure http client
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(MAXCONNTOTAL);
        cm.setDefaultMaxPerRoute(MAXCONNPERROUTE);

        //configure for requests to APIkey service
        httpClient = HttpClients.custom().setConnectionManager(cm).build();
    }

    @PreDestroy
    public void close() throws IOException {
        if (httpClient != null) {
            httpClient.close();
        }
    }

    /** Method uses authentication from api-commons for validating the read access to the API.
     * @param servletRequest Request Object
     * @throws ApiKeyException exception if not authorised
     */
    public void authorizeReadAccess(HttpServletRequest servletRequest)
        throws  ApiKeyException {
        long startTime = System.currentTimeMillis();
        try {
            if (StringUtils.isBlank(extractApiKeyFromRequest(servletRequest))) {
                throw new ApiKeyException(ProblemType.APIKEY_MISSING, null, HttpStatus.SC_BAD_REQUEST);
            }
            performReadAccessAuthorization(servletRequest);
        } catch (ApplicationAuthenticationException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(e);
            }
            int statusCode = e.getStatus() != null ? e.getStatus().value() : HttpStatus.SC_UNAUTHORIZED;
             throw new ApiKeyException(ProblemType.APIKEY_DOES_NOT_EXIST, servletRequest.getHeader(
                 ApiConstants.X_API_KEY),statusCode);
        }finally {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Validation of apiKey took {} ms", (System.currentTimeMillis() - startTime));
            }
        }

    }

    private void performReadAccessAuthorization(HttpServletRequest servletRequest)
        throws ApplicationAuthenticationException {
        if(StringUtils.isBlank(urlService.getApikeyServiceUrl())){
            LOG.warn("API Key Service URL not defined ,validation disabled!!!");
            return;
        }
        authorizationService.authorizeReadAccess(servletRequest);
    }


    /** Method to fetch ApiKey Either from request header or request parameter
     * @param request HttpServletRequest
     * @return apikey String
     */
    public static String extractApiKeyFromRequest(HttpServletRequest request){
      return (request.getHeader(ApiConstants.X_API_KEY) != null ? request.getHeader(ApiConstants.X_API_KEY)
          : request.getParameter(ApiConstants.WSKEY));
    }


}
