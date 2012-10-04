package eu.europeana.api2.web.controller.v1;

import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import eu.europeana.api2.web.model.json.ApiError;
import eu.europeana.api2.web.model.json.ObjectResult;
import eu.europeana.api2.web.model.json.api1.FullDoc;
import eu.europeana.corelib.db.service.UserService;
import eu.europeana.corelib.definitions.solr.beans.FullBean;
import eu.europeana.corelib.solr.exceptions.SolrTypeException;
import eu.europeana.corelib.solr.service.SearchService;

@Controller
@RequestMapping(value = "/v1/record")
public class ObjectControllerV1 {

	private final Logger log = Logger.getLogger(getClass().getName());

	@Resource(name="corelib_db_userService") private UserService userService;

	@Resource private SearchService searchService;

	@Transactional
	@RequestMapping(value = "/{collectionId}/{recordId}.json", method=RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ModelAndView record(
		@PathVariable String collectionId,
		@PathVariable String recordId,
		@RequestParam(value = "wskey", required = false) String wskey,
		@RequestParam(value = "callback", required = false) String callback,
		HttpServletRequest request
			) throws Exception {
		log.info("====== /v1/record/{collectionId}/{recordId}.json ======");

		Map<String, Object> model = new HashMap<String, Object>();
		Api1Utils utils = new Api1Utils();

		if (StringUtils.isBlank(wskey) || userService.findByApiKey(wskey) == null) {
			// error handling here
			model.put("error", utils.toJson(new Exception("No API authorisation key, or the key is not recognised.").getMessage()));
		} else {
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
					model.put("error", utils.toJson(new ApiError(null, "record.json", "not found error")));
				}
			} catch (SolrTypeException e) {
				// return new ApiError(principal.getName(), "record.json", e.getMessage());
				model.put("error", utils.toJson(e.getMessage()));
			}
		}

		ModelAndView page = new ModelAndView("search", model);
		return page;
	}
}
