package eu.europeana.api2.v2.web.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import eu.europeana.api2.model.enums.ApiLimitException;
import eu.europeana.api2.model.json.ApiError;
import eu.europeana.api2.utils.JsonUtils;
import eu.europeana.api2.v2.model.LimitResponse;
import eu.europeana.api2.v2.model.json.CrfMetadataResult;
import eu.europeana.api2.v2.utils.ControllerUtils;
import eu.europeana.corelib.db.entity.enums.RecordType;
import eu.europeana.corelib.db.service.ApiKeyService;
import eu.europeana.corelib.db.service.ApiLogService;
import eu.europeana.corelib.logging.Log;
import eu.europeana.corelib.logging.Logger;
import eu.europeana.corelib.search.SearchService;
import eu.europeana.corelib.web.service.ContentReuseFrameworkService;
import eu.europeana.corelib.web.service.EuropeanaUrlService;
import eu.europeana.harvester.domain.SourceDocumentReferenceMetaInfo;

@Controller
public class ContentReuseFrameworkController {

	@Log
	private Logger log;

	@Resource
	private SearchService searchService;

	@Resource(name = "corelib_web_contentReuseFrameworkService")
	private ContentReuseFrameworkService crfService;

	@Resource
	private ApiLogService apiLogService;

	@Resource
	private ApiKeyService apiService;

	@Resource
	private EuropeanaUrlService urlService;

	@Resource
	private ControllerUtils controllerUtils;

	@RequestMapping(value = "/v2/metadata-by-url.json", method = RequestMethod.GET, 
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ModelAndView metadataByUrl(
			@RequestParam(value = "url", required = true) String url,
			@RequestParam(value = "wskey", required = true) String wskey,
			@RequestParam(value = "callback", required = false) String callback,
			HttpServletRequest request,
			HttpServletResponse response) {
		long t0 = System.currentTimeMillis();
		controllerUtils.addResponseHeaders(response);
		LimitResponse limitResponse = null;
		try {
			limitResponse = controllerUtils.checkLimit(wskey, request.getRequestURL().toString(),
					"record.json", RecordType.OBJECT, null);
		} catch (ApiLimitException e) {
			response.setStatus(e.getHttpStatus());
			return JsonUtils.toJson(new ApiError(e), callback);
		}

		CrfMetadataResult result = new CrfMetadataResult(wskey, "metadata-by-url.json", limitResponse.getRequestNumber());
		SourceDocumentReferenceMetaInfo info = crfService.getMetadata(url);
		if (info != null) {
			result.imageMetaInfo = info.getImageMetaInfo();
		}
		result.statsDuration = (System.currentTimeMillis() - t0);
		return JsonUtils.toJson(result, callback);
	}
}
