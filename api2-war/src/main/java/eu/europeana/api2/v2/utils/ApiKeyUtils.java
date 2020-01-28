package eu.europeana.api2.v2.utils;

import eu.europeana.api2.ApiKeyException;
import eu.europeana.api2.v2.model.LimitResponse;
import eu.europeana.corelib.db.entity.enums.RecordType;
import eu.europeana.corelib.db.entity.relational.ApiKeyImpl;
import eu.europeana.corelib.db.exception.DatabaseException;
import eu.europeana.corelib.db.service.ApiKeyService;
import eu.europeana.corelib.definitions.db.entity.relational.ApiKey;
import eu.europeana.corelib.definitions.db.entity.relational.enums.ApiClientLevel;
import eu.europeana.corelib.web.exception.ProblemType;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
//import org.springframework.http.HttpStatus;
import org.apache.http.HttpStatus;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * Utility class for checking API client keys
 * @author Patrick Ehlert on 13-6-17.
 */
public class ApiKeyUtils {

    private static final Logger LOG = LogManager.getLogger(ApiKeyUtils.class);
    private static final String AUTHORIZATION = "Authorization";
    private static final String UNABLETOVALIDATE = "Unable to validate apikey";
    private static final String ERRORRETRIEVING = "Error retrieving apikey";
    private static final String TEMPKEY = "Temporary key";

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
     * @return A {@link LimitResponse} consisting of the apiKey and current request number
     * @throws ApiKeyException {@link ApiKeyException} if an unregistered or unauthorised key is provided, or if the
     *         daily limit has been reached
     *
     * @Deprecated Only checking if a key exists is used at the moment (not the limit)
     *    All functionality will be moved to the new apikey project
     */
    public LimitResponse checkLimit(String wskey) throws ApiKeyException {
        if (StringUtils.isBlank(wskey)) {
            throw new ApiKeyException(ProblemType.APIKEY_MISSING, null);
        }

        ApiKey apiKey;
        long requestNumber;
        long t;
        try {
            t = System.currentTimeMillis();
            apiKey = apiService.findByID(wskey);
            if (apiKey == null) {
                throw new ApiKeyException(ProblemType.APIKEY_INVALID, wskey);
            }
            LOG.debug("Get apiKey took {} ms",(System.currentTimeMillis() - t));

            //       apiKey.getUsageLimit();
            requestNumber = 999;
            LOG.debug("Setting default request number; (checklimit disabled): {} ", requestNumber);

        } catch (DatabaseException e) {
            LOG.error(ERRORRETRIEVING, e);
            ApiKeyException ex = new ApiKeyException(ProblemType.APIKEY_INVALID, wskey);
            ex.initCause(e);
            throw ex;
        } catch (org.hibernate.exception.JDBCConnectionException |
                 org.springframework.transaction.CannotCreateTransactionException ex) {
            // EA-1537 we sometimes have connection problems with the database, so we simply log and do not validate
            // keys when that happens
            LOG.error(UNABLETOVALIDATE, ex);
            requestNumber = 998;
            apiKey = new ApiKeyImpl();
            apiKey.setApiKey(wskey);
            apiKey.setDescription(TEMPKEY);
            apiKey.setLevel(ApiClientLevel.CLIENT);
        }
        return new LimitResponse(apiKey, requestNumber);
    }

    /**
     * Validates the supplied apikey using the apikey service
     * <p>
     * NOTE: for the time being, this method mimics the user response of the old checkLimit() method with regards to
     * the JSON format of the output, the returned requestnumber and messages and format of possible error conditions,
     * including providing a temporary validated key if the apikey service cannot be reached
     *
     * @param key The user's public apikey
     * @return A {@link LimitResponse} consisting of the apiKey and current request number
     * @throws ApiKeyException {@link ApiKeyException} if an unregistered or unauthorised key is provided
     *
     */
    public LimitResponse validateApiKey(String key) throws ApiKeyException {
        if (StringUtils.isBlank(key)) {
            throw new ApiKeyException(ProblemType.APIKEY_MISSING, null);
        }

        ApiKey apiKey;
        long requestNumber;

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost            httpPost = new HttpPost("http://www.example.com");
            httpPost.setHeader(AUTHORIZATION, "APIKEY " + key);
            long                  t        = System.currentTimeMillis();
            CloseableHttpResponse response = client.execute(httpPost);
            LOG.debug("Post request to validate apiKey took {} ms", (System.currentTimeMillis() - t));

            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_NO_CONTENT) {
                LOG.debug("Apikey validated");
                requestNumber = 999;
                apiKey = createApiKey(key);
                apiKey.setApiKey(key);
            } else if (response.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
                LOG.error("Apikey is not registered");
                ApiKeyException e = new ApiKeyException(ProblemType.APIKEY_NOT_REGISTERED, key);
                e.initCause(e);
                throw e;
            } else if (response.getStatusLine().getStatusCode() == HttpStatus.SC_GONE) {
                LOG.error("Apikey is deprecated");
                ApiKeyException e = new ApiKeyException(ProblemType.APIKEY_DEPRECATED, key);
                e.initCause(e);
                throw e;
            } else if (response.getStatusLine().getStatusCode() == HttpStatus.SC_BAD_REQUEST) {
                LOG.error("No Apikey provided");
                ApiKeyException e = new ApiKeyException(ProblemType.APIKEY_MISSING, key);
                e.initCause(e);
                throw e;
            } else {
                LOG.error(ERRORRETRIEVING + ": Apikey service returned HTTP status {}", response.getStatusLine().getStatusCode());
                ApiKeyException ex = new ApiKeyException(ProblemType.APIKEY_INVALID, key);
                ex.initCause(e);
                throw ex;
            }

        } catch (IOException e) {
            LOG.error(UNABLETOVALIDATE, e);
            requestNumber = 998;
            apiKey = createTempApiKey(key);
        }
        return new LimitResponse(apiKey, requestNumber);
    }

    private ApiKey createTempApiKey(String key){
        ApiKey apiKey = createApiKey(key);
        apiKey.setDescription(TEMPKEY);
    }

    private ApiKey createApiKey (String key){
        ApiKey apiKey = new ApiKeyImpl();
        apiKey.setApiKey(key);
        apiKey.setLevel(ApiClientLevel.CLIENT);
    }


}
