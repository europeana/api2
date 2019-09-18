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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Resource;

/**
 * Utility class for checking API client keys
 * @author Patrick Ehlert on 13-6-17.
 */
public class ApiKeyUtils {

    private static final Logger LOG = LogManager.getLogger(ApiKeyUtils.class);

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
     * @param recordType The type of record, defined by {@link RecordType}
     * @param profile The profile used
     * @return A {@link LimitResponse} consisting of the apiKey and current request number
     * @throws ApiKeyException {@link ApiKeyException} if an unregistered or unauthorised key is provided, or if the
     *         daily limit has been reached
     *
     * @Deprecated Only checking if a key exists is used at the moment (not the limit)
     *    All functionality will be moved to the new apikey project
     */
    public LimitResponse checkLimit(String wskey, String url, RecordType recordType,
                                    String profile) throws ApiKeyException {
        if (StringUtils.isBlank(wskey)) {
            throw new ApiKeyException(ProblemType.APIKEY_MISSING, null);
        }

        ApiKey apiKey;
        long requestNumber = 0;
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
            LOG.error("Error retrieving apikey", e);
            ApiKeyException ex = new ApiKeyException(ProblemType.APIKEY_INVALID, wskey);
            ex.initCause(e);
            throw ex;
        } catch (org.hibernate.exception.JDBCConnectionException |
                 org.springframework.transaction.CannotCreateTransactionException ex) {
            // EA-1537 we sometimes have connection problems with the database, so we simply log and do not validate
            // keys when that happens
            LOG.error("Unable to validate apikey", ex);
            requestNumber = 998;
            apiKey = new ApiKeyImpl();
            apiKey.setApiKey(wskey);
            apiKey.setDescription("Temporary key");
            apiKey.setLevel(ApiClientLevel.CLIENT);
        }
        return new LimitResponse(apiKey, requestNumber);
    }
}
