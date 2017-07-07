package eu.europeana.api2.v2.utils;

import eu.europeana.api2.ApiLimitException;
import eu.europeana.api2.v2.model.LimitResponse;
import eu.europeana.corelib.db.entity.enums.RecordType;
import eu.europeana.corelib.db.exception.DatabaseException;
import eu.europeana.corelib.db.service.ApiKeyService;
import eu.europeana.corelib.definitions.db.entity.relational.ApiKey;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;

import javax.annotation.Resource;

/**
 * Utility class for checking API client keys
 * Created by patrick on 13-6-17.
 */
public class ApiKeyUtils {

    private static final Logger LOG = Logger.getLogger(ApiKeyUtils.class);

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
     * @throws {@link ApiLimitException} if an unregistered or unauthorised key is provided, or if the
     *         daily limit has been reached
     *
     * @Deprecated Only checking if a key exists is used at the moment (not the limit)
     *    All functionality will be moved to the new apikey project
     */
    public LimitResponse checkLimit(String wskey, String url, RecordType recordType,
                                    String profile) throws ApiLimitException {
        ApiKey apiKey;
        long requestNumber = 0;
        long t;
        if (StringUtils.isBlank(wskey)) {
            throw new ApiLimitException(wskey, "No API key provided", 0, HttpStatus.UNAUTHORIZED.value());
        }
        try {
            t = System.currentTimeMillis();
            apiKey = apiService.findByID(wskey);
            if (apiKey == null) {
                throw new ApiLimitException(wskey, "Invalid API key", 0, HttpStatus.UNAUTHORIZED.value());
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("Get apiKey took " + (System.currentTimeMillis() - t) +" ms");
            }

            //       apiKey.getUsageLimit();
            requestNumber = 999;
            if (LOG.isDebugEnabled()) {
                LOG.debug("Setting default request number; (checklimit disabled): " + requestNumber);
            }

            // t = System.currentTimeMillis();
//       requestNumber = apiService.checkReachedLimit(apiKey);
            // log.info("checkReachedLimit: " + (System.currentTimeMillis() - t));
            // t = System.currentTimeMillis();
            // apiLogService.logApiRequest(wskey, url, recordType, profile);
            // log.info("logApiRequest: " + (System.currentTimeMillis() - t));
        } catch (DatabaseException e) {
            LOG.error("Error retrieving apikey", e);
            // apiLogService.logApiRequest(wskey, url, recordType, profile);
            throw new ApiLimitException(wskey, e.getMessage(), requestNumber, HttpStatus.UNAUTHORIZED.value());
            // } catch (LimitReachedException e) {
            // apiLogService.logApiRequest(wskey, url, RecordType.LIMIT, recordType.toString());
            // throw new ApiLimitException(wskey, apiCall, e.getMessage(), requestNumber, 429);
        }
        return new LimitResponse(apiKey, requestNumber);
    }
}
