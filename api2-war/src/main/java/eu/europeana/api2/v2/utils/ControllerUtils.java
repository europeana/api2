package eu.europeana.api2.v2.utils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.web.servlet.ModelAndView;

import eu.europeana.api2.model.enums.ApiLimitException;
import eu.europeana.api2.model.json.ApiError;
import eu.europeana.api2.utils.JsonUtils;
import eu.europeana.corelib.db.entity.enums.RecordType;
import eu.europeana.corelib.db.exception.DatabaseException;
import eu.europeana.corelib.db.exception.LimitReachedException;
import eu.europeana.corelib.db.service.ApiKeyService;
import eu.europeana.corelib.db.service.ApiLogService;
import eu.europeana.corelib.definitions.db.entity.relational.ApiKey;
import eu.europeana.corelib.definitions.exception.ProblemType;

public class ControllerUtils {

	@Resource
	private ApiKeyService apiService;

	@Resource
	private ApiLogService apiLogService;

	public long checkLimit(String wskey, String url, String apiCall, RecordType recordType, String profile) 
			throws ApiLimitException {
		ApiKey apiKey;
		long requestNumber = 0;
		try {
			apiKey = apiService.findByID(wskey);
			if (apiKey == null) {
				throw new ApiLimitException(wskey, apiCall, "Unregistered user");
			}
			apiKey.getUsageLimit();
			requestNumber = apiService.checkReachedLimit(apiKey);
			apiLogService.logApiRequest(wskey, url, recordType, profile);
		} catch (DatabaseException e) {
			apiLogService.logApiRequest(wskey, url, recordType, profile);
			throw new ApiLimitException(wskey, apiCall, e.getMessage(), requestNumber);
		} catch (LimitReachedException e) {
			apiLogService.logApiRequest(wskey, url, RecordType.LIMIT, recordType.toString());
			throw new ApiLimitException(wskey, apiCall, e.getMessage(), requestNumber);
		}
		return requestNumber;
	}
}
