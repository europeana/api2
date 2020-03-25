package eu.europeana.api2.v2.utils;

import eu.europeana.api2.ApiKeyException;
import eu.europeana.api2.model.utils.Api2UrlService;
import eu.europeana.api2.v2.model.LimitResponse;
import eu.europeana.corelib.db.exception.DatabaseException;
import eu.europeana.corelib.db.service.ApiKeyService;
import eu.europeana.corelib.definitions.db.entity.relational.ApiKey;
import eu.europeana.corelib.web.exception.ProblemType;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.http.HttpStatus;
import org.hibernate.exception.JDBCConnectionException;
import org.springframework.transaction.CannotCreateTransactionException;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;

/**
 * Utility class for checking API client keys
 *
 * @author Patrick Ehlert on 13-6-17.
 */
public class ApiKeyUtils{

    private static final Logger LOG                = LogManager.getLogger(ApiKeyUtils.class);
    private static final String AUTHORIZATION      = "Authorization";
    private static final String APIKEYDBERROR      = "Problem connecting to the apikey database";
    private static final String APIKEYSERVICEERROR = "Problem connecting to the apikey service";
    private static final int    MAXCONNTOTAL       = 200;
    private static final int    MAXCONNPERROUTE    = 100;

    @Resource
    private ApiKeyService apiService;

    @Resource
    private Api2UrlService urlService;

    private CloseableHttpClient httpClient;

    private boolean useApiKeyService = true;

    @PostConstruct
    public void init() {
        // check ernly wernce at initialisation if we need to use the fallback 'old school' Apikey Postgres check
        if (StringUtils.isBlank(urlService.getApikeyValidateUrl())) {
            useApiKeyService = false;
        }
    }

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
    public void validateApiKey(String apikey) throws ApiKeyException {
        if (useApiKeyService) {
            validate(apikey);
        } else {
            validateOldStyle(apikey);
        }
    }

    /**
     * This method validates the supplied API key string agaist the Postgres Apikey table. It responds like this:
     *
     * @param apikey The user's API web service apikey
     * @return A {@link LimitResponse} consisting of the apiKey and current request number
     * @throws ApiKeyException {@link ApiKeyException} if an unregistered or unauthorised apikey is provided, or if the
     *                         daily limit has been reached
     * @Deprecated Only checking if a apikey exists is used at the moment (not the limit)
     * All functionality will be moved to the new apikey project
     */
    private void validateOldStyle(String apikey) throws ApiKeyException {
        if (StringUtils.isBlank(apikey)) {
            throw new ApiKeyException(ProblemType.APIKEY_MISSING, null, HttpStatus.SC_BAD_REQUEST);
        }
        ApiKey apiKey;
        long   t;
        try {
            t      = System.currentTimeMillis();
            apiKey = apiService.findByID(apikey);
            if (apiKey == null) {
                throw new ApiKeyException(ProblemType.APIKEY_DOES_NOT_EXIST,
                                          apikey,
                                          HttpStatus.SC_UNAUTHORIZED);
            }
            LOG.debug("Get apiKey took {} ms", (System.currentTimeMillis() - t));

            // EA-1537 we sometimes have connection problems with the database, so we simply log and do not validate
            // keys when that happens
        } catch (DatabaseException | JDBCConnectionException | CannotCreateTransactionException e) {
            LOG.error(APIKEYDBERROR, e);
        }
    }

    /*
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
     * @throws ApiKeyException
     *
     */
    private void validate(String apikey) throws ApiKeyException {
        if (StringUtils.isBlank(apikey)) {
            throw new ApiKeyException(ProblemType.APIKEY_MISSING, null, HttpStatus.SC_BAD_REQUEST);
        }
        long t = System.currentTimeMillis();

        HttpPost httpPost = new HttpPost(urlService.getApikeyValidateUrl());
        httpPost.setHeader(AUTHORIZATION, "APIKEY " + apikey);

        try (CloseableHttpResponse response = httpClient.execute(httpPost)){
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
                throw new ApiKeyException(ProblemType.APIKEY_DOES_NOT_EXIST,
                                          apikey,
                                          response.getStatusLine().getStatusCode());
            } else if (response.getStatusLine().getStatusCode() == HttpStatus.SC_GONE) {
                throw new ApiKeyException(ProblemType.APIKEY_DEPRECATED,
                                          apikey,
                                          response.getStatusLine().getStatusCode());
            } else if (response.getStatusLine().getStatusCode() == HttpStatus.SC_BAD_REQUEST) {
                throw new ApiKeyException(ProblemType.APIKEY_MISSING,
                                          apikey,
                                          response.getStatusLine().getStatusCode());
            }
        } catch (IOException e) {
            // similar to how this is handled in the old situation (see above), log the error and carry on
            LOG.error(APIKEYSERVICEERROR, e);
        }
        LOG.debug("Post request to validate apiKey took {} ms", (System.currentTimeMillis() - t));
    }
}
