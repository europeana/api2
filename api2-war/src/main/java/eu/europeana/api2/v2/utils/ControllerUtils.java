package eu.europeana.api2.v2.utils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.util.log.Log;

import eu.europeana.api2.model.enums.ApiLimitException;
import eu.europeana.api2.v2.model.LimitResponse;
import eu.europeana.corelib.db.entity.enums.RecordType;
import eu.europeana.corelib.db.exception.DatabaseException;
import eu.europeana.corelib.db.exception.LimitReachedException;
import eu.europeana.corelib.db.service.ApiKeyService;
import eu.europeana.corelib.db.service.ApiLogService;
import eu.europeana.corelib.definitions.db.entity.relational.ApiKey;
import eu.europeana.corelib.logging.Logger;

public class ControllerUtils {

	private Logger log = Logger.getLogger(ControllerUtils.class.getCanonicalName());

	@Resource
	private ApiKeyService apiService;

	@Resource
	private ApiLogService apiLogService;

	public LimitResponse checkLimit(String wskey, String url, String apiCall, RecordType recordType, String profile) 
			throws ApiLimitException {
		ApiKey apiKey = null;
		long requestNumber = 0;
		long t;
		try {
			t = System.currentTimeMillis();
			apiKey = apiService.findByID(wskey);
			if (apiKey == null) {
				throw new ApiLimitException(wskey, apiCall, "Unregistered user", 0, 401);
			}
			// apiKey.getUsageLimit();
			log.info("get apiKey: " + (System.currentTimeMillis() - t));
			t = System.currentTimeMillis();
			requestNumber = apiService.checkReachedLimit(apiKey);
			log.info("checkReachedLimit: " + (System.currentTimeMillis() - t));
			t = System.currentTimeMillis();
			apiLogService.logApiRequest(wskey, url, recordType, profile);
			log.info("logApiRequest: " + (System.currentTimeMillis() - t));
		} catch (DatabaseException e) {
			apiLogService.logApiRequest(wskey, url, recordType, profile);
			throw new ApiLimitException(wskey, apiCall, e.getMessage(), requestNumber, 401);
		} catch (LimitReachedException e) {
			apiLogService.logApiRequest(wskey, url, RecordType.LIMIT, recordType.toString());
			throw new ApiLimitException(wskey, apiCall, e.getMessage(), requestNumber, 429);
		}
		return new LimitResponse(apiKey, requestNumber);
	}

	public void addResponseHeaders(HttpServletResponse response) {
		addCharacterEncoding(response);
		addAccessControlHeaders(response);
	}

	public void addCharacterEncoding(HttpServletResponse response) {
		response.setCharacterEncoding("UTF-8");
	}

	public void addAccessControlHeaders(HttpServletResponse response) {
		response.addHeader("Access-Control-Allow-Origin", "*");
		response.addHeader("Access-Control-Allow-Methods", "POST");
		response.addHeader("Access-Control-Max-Age", "1000");
	}
}
