package eu.europeana.api2.v1.web.controller;

import java.io.StringWriter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.lang.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import eu.europeana.api2.model.json.ApiError;
import eu.europeana.api2.model.xml.srw.SrwResponse;
import eu.europeana.api2.utils.JsonUtils;
import eu.europeana.api2.v1.model.json.view.FullDoc;
import eu.europeana.api2.v2.model.xml.srw.Record;
import eu.europeana.corelib.db.service.ApiKeyService;
import eu.europeana.corelib.db.service.UserService;
import eu.europeana.corelib.definitions.exception.ProblemType;
import eu.europeana.corelib.definitions.solr.beans.FullBean;
import eu.europeana.corelib.logging.Log;
import eu.europeana.corelib.logging.Logger;
import eu.europeana.corelib.solr.exceptions.EuropeanaQueryException;
import eu.europeana.corelib.solr.service.SearchService;
import eu.europeana.corelib.web.service.EuropeanaUrlService;

@Controller
@RequestMapping(value = "/v1/record")
public class ObjectController1 {

	@Log
	private Logger log;

	@Resource(name = "corelib_db_userService")
	private UserService userService;

	@Resource
	private ApiKeyService apiService;

	@Resource
	private SearchService searchService;

	@Resource
	private EuropeanaUrlService urlService;

	static String portalPath;

	@Transactional
	@RequestMapping(value = "/{collectionId}/{recordId}.json", produces = MediaType.APPLICATION_JSON_VALUE)
	// method=RequestMethod.GET,
	public ModelAndView recordJson(
			@PathVariable String collectionId,
			@PathVariable String recordId,
			@RequestParam(value = "wskey", required = false) String wskey,
			@RequestParam(value = "callback", required = false) String callback,
			HttpServletResponse response)
					throws Exception {
		log.info("====== /v1/record/{collectionId}/{recordId}.json ======");

		if (StringUtils.isBlank(wskey)) {
			response.setStatus(401);
			return JsonUtils.toJson(new ApiError(wskey, "record.json", "No API authorisation key."), callback);
		}

		if (userService.findByApiKey(wskey) == null && apiService.findByID(wskey) == null) {
			response.setStatus(401);
			return JsonUtils.toJson(new ApiError(wskey, "record.json", "Unregistered user"), callback);
		}

		String europeanaObjectId = "/" + collectionId + "/" + recordId;
		FullBean bean = searchService.findById(europeanaObjectId, true);
		if (bean == null) {
			bean = searchService.resolve(europeanaObjectId, true);
		}

		if (bean != null) {
			return JsonUtils.toJson(new FullDoc(bean).asMap(), callback);
		} else {
			response.setStatus(404);
			return JsonUtils.toJson(new ApiError(wskey, "record.json", "not found error"), callback);
		}
	}

	@Transactional
	@RequestMapping(value = "/{collectionId}/{recordId}.srw", produces = MediaType.TEXT_XML_VALUE)
	// method=RequestMethod.GET
	public @ResponseBody
	SrwResponse recordSrw(@PathVariable String collectionId, @PathVariable String recordId,
			@RequestParam(value = "wskey", required = false) String wskey, HttpServletResponse response)
			throws Exception {
		log.info("====== /v1/record/{collectionId}/{recordId}.srw ======");

		boolean hasResult = false;
		if (!hasResult && StringUtils.isBlank(wskey)) {
			// model.put("json", utils.toJson(new ApiError(wskey, "search.json", "No API authorisation key.")));
			throw new EuropeanaQueryException(ProblemType.NO_PASSWORD);
		}

		if (!hasResult && (userService.findByApiKey(wskey) == null && apiService.findByID(wskey) == null)) {
			// model.put("json", utils.toJson(new ApiError(wskey, "search.json", "Unregistered user")));
			throw new EuropeanaQueryException(ProblemType.NO_PASSWORD);
			// hasResult = true;
		}

		if (!hasResult) {
			String europeanaObjectId = "/" + collectionId + "/" + recordId;
			FullBean bean = searchService.findById(europeanaObjectId, true);
			if (bean == null) {
				bean = searchService.resolve(europeanaObjectId, true);
			}

			SrwResponse srwResponse = new SrwResponse();
			FullDoc doc = null;
			if (bean != null) {
				doc = new FullDoc(bean);
				Record record = new Record();
				record.recordData.dc = doc;
				srwResponse.records.record.add(record);
				log.info("record added");
			} else {
				String url = urlService.getPortalRecord(true, collectionId, recordId).toString();
				response.setStatus(302);
				response.setHeader("Location", url);
				return null;
			}
			createXml(srwResponse);
			log.info("xml created");
			return srwResponse;
		}

		// ModelAndView page = new ModelAndView("json", model);
		return null;
	}

	private void createXml(SrwResponse response) {
		try {
			final JAXBContext context = JAXBContext.newInstance(SrwResponse.class);
			final Marshaller marshaller = context.createMarshaller();
			final StringWriter stringWriter = new StringWriter();
			marshaller.marshal(response, stringWriter);
			if (log.isInfoEnabled()) {
				log.info("result: " + stringWriter.toString());
			}
		} catch (JAXBException e) {
			if (log.isErrorEnabled()) {
				log.error("JAXBException: " + e.getMessage() + ", " + e.getCause().getMessage(), e);
			}
		}
	}
}
