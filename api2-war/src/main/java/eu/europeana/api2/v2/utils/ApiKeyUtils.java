package eu.europeana.api2.v2.utils;

import eu.europeana.api.commons.web.exception.ApplicationAuthenticationException;
import eu.europeana.api2.ApiKeyException;
import eu.europeana.api2.model.utils.Api2UrlService;
import eu.europeana.api2.v2.service.SearchAuthorizationService;
import eu.europeana.corelib.web.exception.ProblemType;
import java.io.IOException;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
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
    private static final String AUTHORIZATION      = "Authorization";
    private static final String APIKEYSERVICEERROR = "Problem connecting to the apikey service";
    private static final int    MAXCONNTOTAL       = 200;
    private static final int    MAXCONNPERROUTE    = 100;
    public static final String X_API_KEY = "X-Api-Key";
    public static final String WSKEY = "wskey";

    @Resource
    private Api2UrlService urlService;

    private CloseableHttpClient httpClient;

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


    /**
     * This method uses the Apikey service to validate API keys. It responds like this:
     *
     * - HTTP 400 and APIKEY_MISSING if no apikey is provided OR the Apikey service returns HTTP 400
     * - HTTP 401 and APIKEY_DOES_NOT_EXIST if the Apikey service returns HTTP 401 (apikey not found)
     * - HTTP 410 and APIKEY_DEPRECATED if the Apikey service returns HTTP 410 (apikey has past deprecationdate)
     *
     * In all other cases the API key is (quietly) suspended in order to not let possible issues with the Apikey
     * service interfere with the Api functionality.
     * However, an error is logged if there was a problem connecting to the Apikey service.
     *
     * @param apikey The user's API web service apikey
     * @throws ApiKeyException exception
     *
     */
    @Deprecated(since = "Oct 2023")
    public void validateApiKey(String apikey) throws ApiKeyException {
        if (StringUtils.isBlank(urlService.getApikeyValidateUrl())) {
            LOG.debug("API Key validation disabled");
            return;
        }

        if (StringUtils.isBlank(apikey)) {
            throw new ApiKeyException(ProblemType.APIKEY_MISSING, null, HttpStatus.SC_BAD_REQUEST);
        }
        long t = System.currentTimeMillis();

        HttpPost httpPost = new HttpPost(urlService.getApikeyValidateUrl());
        httpPost.setHeader(AUTHORIZATION, "APIKEY " + apikey);

        try (CloseableHttpResponse response = httpClient.execute(httpPost)){
            int statusCode = response.getStatusLine().getStatusCode();
            LOG.trace("Validating API key {}, response is {}", apikey, statusCode);
            switch (statusCode) {
                case HttpStatus.SC_OK: break; // everything is fine, do nothing
                case HttpStatus.SC_NO_CONTENT: break; 
                case HttpStatus.SC_UNAUTHORIZED: throw new ApiKeyException(ProblemType.APIKEY_DOES_NOT_EXIST, apikey,
                        response.getStatusLine().getStatusCode());
                case HttpStatus.SC_GONE: throw new ApiKeyException(ProblemType.APIKEY_DEPRECATED, apikey,
                        response.getStatusLine().getStatusCode());
                default: LOG.error("{}: {} ({})", "Unexpected API key service response", statusCode,
                        response.getStatusLine().getReasonPhrase());
            }
        } catch (IOException e) {
            // similar to how this is handled in the old situation (see above), log the error and carry on
            LOG.error(APIKEYSERVICEERROR, e);
        }
        LOG.debug("Post request to validate apiKey took {} ms", (System.currentTimeMillis() - t));
    }


    /** Method uses authentication from api-commons for validating the read access to the API.
     * @param servletRequest Request Object
     * @throws ApiKeyException exception if not authorised
     */
    public void authorizeReadAccess(HttpServletRequest servletRequest)
        throws  ApiKeyException {
        long startTime = System.currentTimeMillis();
        try {
            checkApiKey(servletRequest);
            SearchAuthorizationService authService = new SearchAuthorizationService();
            authService.authorizeReadAccess(servletRequest);
        } catch (ApplicationAuthenticationException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(e);
            }
            int statusCode = e.getStatus() != null ? e.getStatus().value() : HttpStatus.SC_UNAUTHORIZED;
            throw new ApiKeyException(ProblemType.APIKEY_DOES_NOT_EXIST, servletRequest.getHeader(X_API_KEY),statusCode);
        }finally {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Validation of apiKey took {} ms", (System.currentTimeMillis() - startTime));
            }
        }

    }

    private void checkApiKey(HttpServletRequest servletRequest) throws ApiKeyException {
        if (StringUtils.isBlank(servletRequest.getParameter(WSKEY)) &&
            StringUtils.isBlank(servletRequest.getHeader(X_API_KEY))) {
            throw new ApiKeyException(ProblemType.APIKEY_MISSING, null, HttpStatus.SC_BAD_REQUEST);
        }
    }


}
