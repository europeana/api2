package eu.europeana.api2.v2.utils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.web.servlet.ModelAndView;

import eu.europeana.api2.model.json.ApiError;
import eu.europeana.api2.utils.JsonUtils;
import eu.europeana.corelib.db.entity.enums.RecordType;
import eu.europeana.corelib.db.exception.DatabaseException;
import eu.europeana.corelib.db.exception.LimitReachedException;
import eu.europeana.corelib.db.service.ApiKeyService;
import eu.europeana.corelib.db.service.ApiLogService;
import eu.europeana.corelib.definitions.db.entity.relational.ApiKey;

public class ControllerUtils {

	@Resource
	private ApiKeyService apiService;

	@Resource
	private ApiLogService apiLogService;

	public ModelAndView checkLimit(LimitRequest req) {
		ApiKey apiKey;
		long requestNumber = 0;
		String wskey = req.getWskey();
		try {
			apiKey = apiService.findByID(wskey);
			if (apiKey == null) {
				return JsonUtils.toJson(new ApiError(wskey, req.getApiCall(), "Unregistered user"), req.getCallback());
			}
			apiKey.getUsageLimit();
			requestNumber = apiService.checkReachedLimit(apiKey);
			req.setApiKey(apiKey);
			req.setRequestNumber(requestNumber);
			apiLogService.logApiRequest(wskey, req.getUrl(), req.getRecordType(), req.getProfile());
		} catch (DatabaseException e) {
			apiLogService.logApiRequest(wskey, req.getUrl(), req.getRecordType(), req.getProfile());
			return JsonUtils.toJson(new ApiError(wskey, req.getApiCall(), e.getMessage(), requestNumber), req.getCallback());
		} catch (LimitReachedException e) {
			apiLogService.logApiRequest(wskey, req.getUrl(), RecordType.LIMIT, null);
			return JsonUtils.toJson(new ApiError(wskey, req.getApiCall(), e.getMessage(), e.getRequested()), req.getCallback());
		}
		return null;
	}
}
