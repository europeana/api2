package eu.europeana.api2.v2.utils;

import eu.europeana.api2.ApiKeyException;
import eu.europeana.api2.model.utils.Api2UrlService;
import eu.europeana.api2.v2.model.LimitResponse;
import eu.europeana.corelib.db.entity.relational.ApiKeyImpl;
import eu.europeana.corelib.db.exception.DatabaseException;
import eu.europeana.corelib.db.service.ApiKeyService;
import eu.europeana.corelib.definitions.db.entity.relational.ApiKey;
import eu.europeana.corelib.definitions.db.entity.relational.enums.ApiClientLevel;
import eu.europeana.corelib.web.exception.ProblemType;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
//import org.springframework.http.HttpStatus;
import org.apache.http.HttpStatus;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * Utility class for checking API client keys
 *
 * @author Patrick Ehlert on 13-6-17.
 */
public class ApiKeyUtils{

    private static final Logger LOG              = LogManager.getLogger(ApiKeyUtils.class);
    private static final String AUTHORIZATION    = "Authorization";
    private static final String UNABLETOVALIDATE = "Unable to validate apikey";
    private static final String ERRORRETRIEVING  = "Error retrieving apikey";
    private static final String UNEXPECTED       = "An unexpected error occurred during apikey validation";
    private static final String TEMPKEY          = "Temporary apikey";
    private static final String NOTHING          = "nothing";
    private static final int    MAXCONNTOTAL     = 200;
    private static final int    MAXCONNPERROUTE  = 100;
    private static final int    FIXEDREQUESTNR   = 999;

    @Resource
    private ApiKeyService apiService;

    @Resource
    private Api2UrlService urlService;

    private CloseableHttpClient httpClient;

    public ApiKeyUtils(){
        // configure http client
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(MAXCONNTOTAL);
        cm.setDefaultMaxPerRoute(MAXCONNPERROUTE);

        //configure for requests to APIkey service
        httpClient = HttpClients.custom().setConnectionManager(cm).build();
    }

    /**
     * EA-1826 Validates the supplied apikey using the apikey service if configured; if not it falls back to the
     * old apikey validation method.
     * NOTE: for the time being, this method mimics the user response of the old checkLimit() method with regards to
     * the JSON format of the output, the returned requestnumber and messages and format of possible error conditions,
     * including providing a temporary validated apikey if the apikey service cannot be reached
     *
     * @param apikey The user's public apikey
     * @return A {@link LimitResponse} consisting of the apiKey and current request number
     * @throws ApiKeyException {@link ApiKeyException} if an unregistered or unauthorised apikey is provided
     */
    public LimitResponse validateApiKey(String apikey) throws ApiKeyException {
        if (StringUtils.equalsIgnoreCase(urlService.getApikeyValidateUrl(), NOTHING)) {
            return checkLimit(apikey);
        } else {
            return validate(apikey);
        }
    }

    /**
     * Check the number requests made to the API in the last (rolling) 24 hours. This is a per-user
     * limit (default: 10000) set in the myEuropeana database that is used as a throttling mechanism.
     *
     * NOTE: This functionality has currently been disabled (ticket #1742); this method now always
     * returns the user's apikey and a request number of '999' (unless it's a unregistered user).
     *
     * @param apikey The user's API web service apikey
     * @return A {@link LimitResponse} consisting of the apiKey and current request number
     * @throws ApiKeyException {@link ApiKeyException} if an unregistered or unauthorised apikey is provided, or if the
     *                         daily limit has been reached
     * @Deprecated Only checking if a apikey exists is used at the moment (not the limit)
     * All functionality will be moved to the new apikey project
     */
    private LimitResponse checkLimit(String apikey) throws ApiKeyException {
        if (StringUtils.isBlank(apikey)) {
            throw new ApiKeyException(ProblemType.APIKEY_MISSING, null);
        }

        ApiKey apiKey;
        long   requestNumber;
        long   t;
        try {
            t      = System.currentTimeMillis();
            apiKey = apiService.findByID(apikey);
            if (apiKey == null) {
                throw new ApiKeyException(ProblemType.APIKEY_INVALID, apikey);
            }
            LOG.debug("Get apiKey took {} ms", (System.currentTimeMillis() - t));

            requestNumber = FIXEDREQUESTNR;
            LOG.debug("Setting default request number; (checklimit disabled): {} ", requestNumber);

        } catch (DatabaseException e) {
            LOG.error(ERRORRETRIEVING, e);
            ApiKeyException ex = new ApiKeyException(ProblemType.APIKEY_INVALID, apikey);
            ex.initCause(e);
            throw ex;
        } catch (org.hibernate.exception.JDBCConnectionException | org.springframework.transaction.CannotCreateTransactionException ex) {
            // EA-1537 we sometimes have connection problems with the database, so we simply log and do not validate
            // keys when that happens
            LOG.error(UNABLETOVALIDATE, ex);
            requestNumber = FIXEDREQUESTNR - 1;
            apiKey        = createTempApiKey(apikey);
        }
        return new LimitResponse(apiKey, requestNumber);
    }

    private LimitResponse validate(String apikey) throws ApiKeyException {
        if (StringUtils.isBlank(apikey)) {
            throw new ApiKeyException(ProblemType.APIKEY_MISSING, null, HttpStatus.SC_BAD_REQUEST);
        }
        ApiKey apiKey;
        long   requestNumber;
        try {
            HttpPost httpPost = new HttpPost(urlService.getApikeyValidateUrl());
            httpPost.setHeader(AUTHORIZATION, "APIKEY " + apikey);
            long                  t        = System.currentTimeMillis();
            CloseableHttpResponse response = httpClient.execute(httpPost);

            LOG.debug("Post request to validate apiKey took {} ms", (System.currentTimeMillis() - t));

            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_NO_CONTENT) {
                requestNumber = FIXEDREQUESTNR;
                apiKey        = createApiKey(apikey);
                apiKey.setApiKey(apikey);
            } else if (response.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
                throw new ApiKeyException(ProblemType.APIKEY_NOT_REGISTERED,
                                          apikey,
                                          response.getStatusLine().getStatusCode());
            } else if (response.getStatusLine().getStatusCode() == HttpStatus.SC_GONE) {
                throw new ApiKeyException(ProblemType.APIKEY_DEPRECATED,
                                          apikey,
                                          response.getStatusLine().getStatusCode());
            } else if (response.getStatusLine().getStatusCode() == HttpStatus.SC_BAD_REQUEST) {
                throw new ApiKeyException(ProblemType.APIKEY_MISSING, apikey, response.getStatusLine().getStatusCode());
            } else {
                throw new ApiKeyException(ProblemType.APIKEY_OTHER, apikey, response.getStatusLine().getStatusCode());
            }

        } catch (IOException e) {
            // similar to how this is handled in the old situation (see above), assign a temporary ApiKey if there
            // is a communication problem
            LOG.error(UNABLETOVALIDATE, e);
            requestNumber = FIXEDREQUESTNR - 1;
            apiKey        = createTempApiKey(apikey);
        }
        return new LimitResponse(apiKey, requestNumber);
    }

    private ApiKey createTempApiKey(String key) {
        ApiKey apiKey = createApiKey(key);
        apiKey.setDescription(TEMPKEY);
        return apiKey;
    }

    private ApiKey createApiKey(String key) {
        ApiKey apiKey = new ApiKeyImpl();
        apiKey.setApiKey(key);
        apiKey.setLevel(ApiClientLevel.CLIENT);
        return apiKey;
    }
}
