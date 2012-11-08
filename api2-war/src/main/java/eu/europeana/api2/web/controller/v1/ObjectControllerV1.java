package eu.europeana.api2.web.controller.v1;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.ctc.wstx.util.ExceptionUtil;

import eu.europeana.api2.web.model.json.ApiError;
import eu.europeana.api2.web.model.json.api1.FullDoc;
import eu.europeana.api2.web.model.xml.rss.RssResponse;
import eu.europeana.api2.web.model.xml.srw.Record;
import eu.europeana.api2.web.model.xml.srw.SrwResponse;
import eu.europeana.corelib.db.service.ApiKeyService;
import eu.europeana.corelib.db.service.UserService;
import eu.europeana.corelib.definitions.exception.EuropeanaException;
import eu.europeana.corelib.definitions.exception.ProblemType;
import eu.europeana.corelib.definitions.solr.beans.FullBean;
import eu.europeana.corelib.solr.exceptions.EuropeanaQueryException;
import eu.europeana.corelib.solr.exceptions.SolrTypeException;
import eu.europeana.corelib.solr.service.SearchService;

@Controller
@RequestMapping(value = "/v1/record")
public class ObjectControllerV1 {

	private final Logger log = Logger.getLogger(getClass().getName());
	
	private final static String EXT_HTML = ".html";

	@Resource(name="corelib_db_userService") private UserService userService;

	@Resource private ApiKeyService apiService;

	@Resource private SearchService searchService;

	@Value("#{europeanaProperties['portal.server']}")
	private String portalServer;

	@Value("#{europeanaProperties['portal.name']}")
	private String portalName;

	static String portalPath;

	@Transactional
	@RequestMapping(value = "/{collectionId}/{recordId}.json", method=RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ModelAndView recordJson(
		@PathVariable String collectionId,
		@PathVariable String recordId,
		@RequestParam(value = "wskey", required = false) String wskey,
		@RequestParam(value = "callback", required = false) String callback,
		HttpServletRequest request,
		HttpServletResponse response
			) throws Exception {
		log.info("====== /v1/record/{collectionId}/{recordId}.json ======");

		Map<String, Object> model = new HashMap<String, Object>();
		Api1Utils utils = new Api1Utils();

		boolean hasResult = false;
		if (!hasResult && StringUtils.isBlank(wskey)) {
			model.put("json", utils.toJson(new ApiError(wskey, "search.json", "No API authorisation key.")));
			hasResult = true;
			response.setStatus(401);
		}

		if (!hasResult && (userService.findByApiKey(wskey) == null && apiService.findByID(wskey) == null)) {
			model.put("json", utils.toJson(new ApiError(wskey, "search.json", "Unregistered user")));
			hasResult = true;
			response.setStatus(401);
		}

		if (!hasResult) {
			try {
				FullBean bean = searchService.findById(collectionId, recordId);
				FullDoc doc = null;
				if (bean != null) {
					doc = new FullDoc(bean);
				}
				String json = null;
				if (doc != null) {
					json = utils.toJson(doc.asMap());
					model.put("json", json);
				} else {
					model.put("json", utils.toJson(new ApiError(wskey, "record.json", "not found error")));
					response.setStatus(404);
				}
			} catch (SolrTypeException e) {
				model.put("json", utils.toJson(new ApiError(wskey, "record.json", e.getMessage())));
				response.setStatus(500);
			}
		}

		ModelAndView page = new ModelAndView("search", model);
		return page;
	}

	@Transactional
	@RequestMapping(value = "/{collectionId}/{recordId}.srw", method=RequestMethod.GET, produces = MediaType.TEXT_XML_VALUE)
	public @ResponseBody SrwResponse recordSrw(
		@PathVariable String collectionId,
		@PathVariable String recordId,
		@RequestParam(value = "wskey", required = false) String wskey,
		@RequestParam(value = "callback", required = false) String callback,
		HttpServletRequest request,
		HttpServletResponse response
			) throws Exception {
		log.info("====== /v1/record/{collectionId}/{recordId}.srw ======");

		Map<String, Object> model = new HashMap<String, Object>();
		Api1Utils utils = new Api1Utils();

		boolean hasResult = false;
		if (!hasResult && StringUtils.isBlank(wskey)) {
			model.put("json", utils.toJson(new ApiError(wskey, "search.json", "No API authorisation key.")));
			throw new EuropeanaQueryException(ProblemType.NO_PASSWORD);
		}

		if (!hasResult && (userService.findByApiKey(wskey) == null && apiService.findByID(wskey) == null)) {
			model.put("json", utils.toJson(new ApiError(wskey, "search.json", "Unregistered user")));
			throw new EuropeanaQueryException(ProblemType.NO_PASSWORD);
			// hasResult = true;
		}

		if (!hasResult) {
			try {
				FullBean bean = searchService.findById(collectionId, recordId);
				SrwResponse srwResponse = new SrwResponse();
				FullDoc doc = null;
				if (bean != null) {
					doc = new FullDoc(bean);
					Record record = new Record();
					record.recordData.dc = doc;
					srwResponse.records.record.add(record);
					log.info("record added");
				} else {
					StringBuilder sb = new StringBuilder(getPortalPath());
					sb.append(collectionId).append("/").append(recordId).append(EXT_HTML);

					response.setStatus(302);
					response.setHeader("Location", sb.toString());
					return null; //"redirect:" + sb.toString();
				}
				createXml(srwResponse);
				log.info("xml created");
				return srwResponse;
			} catch (SolrTypeException e) {
				model.put("json", utils.toJson(new ApiError(wskey, "record.json", e.getMessage())));
				response.setStatus(500);
				return null;
			}
		}

		// ModelAndView page = new ModelAndView("search", model);
		return null;
	}

	public String getPortalPath() {
		if (portalPath == null) {
			if (portalServer.endsWith("/")) {
				portalServer = portalServer.substring(0, portalServer.length()-1);
			}
			if (portalName.startsWith("/")) {
				portalName = portalName.substring(1);
			}
			if (portalName.endsWith("/")) {
				portalName = portalName.substring(0, portalName.length()-1);
			}
			portalPath = portalServer + "/" + portalName + "/record/";
		}
		return portalPath;
	}

	private void createXml(SrwResponse response) {
		try {
			final JAXBContext context = JAXBContext.newInstance(SrwResponse.class);
			final Marshaller marshaller = context.createMarshaller();
			final StringWriter stringWriter = new StringWriter();
			marshaller.marshal(response, stringWriter);
			log.info("result: " + stringWriter.toString());
		} catch (JAXBException e) {
			log.severe("JAXBException: " + e.getMessage() + ", " + e.getCause().getMessage());
			log.severe(ExceptionUtils.getFullStackTrace(e));
			
			StringBuilder sb = new StringBuilder();
			for (StackTraceElement t : e.getStackTrace()) {
				sb.append(t.toString()).append("\n");
			}
			log.severe(sb.toString());
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
