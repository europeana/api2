/*
 * Copyright 2007-2012 The Europeana Foundation
 *
 *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved 
 *  by the European Commission;
 *  You may not use this work except in compliance with the Licence.
 *  
 *  You may obtain a copy of the Licence at:
 *  http://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under 
 *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of 
 *  any kind, either express or implied.
 *  See the Licence for the specific language governing permissions and limitations under 
 *  the Licence.
 */

package eu.europeana.api2.v2.web.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.github.jsonldjava.core.JSONLD;
import com.github.jsonldjava.core.JSONLDProcessingError;
import com.github.jsonldjava.core.Options;
import com.github.jsonldjava.impl.JenaRDFParser;
import com.github.jsonldjava.utils.JSONUtils;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import eu.europeana.api2.model.enums.ApiLimitException;
import eu.europeana.api2.model.enums.Profile;
import eu.europeana.api2.model.json.ApiError;
import eu.europeana.api2.model.json.ApiNotImplementedYet;
import eu.europeana.api2.model.json.abstracts.ApiResponse;
import eu.europeana.api2.model.xml.srw.SrwResponse;
import eu.europeana.api2.utils.JsonUtils;
import eu.europeana.api2.v2.model.LimitResponse;
import eu.europeana.api2.v2.model.json.ObjectResult;
import eu.europeana.api2.v2.model.json.view.BriefView;
import eu.europeana.api2.v2.model.json.view.FullDoc;
import eu.europeana.api2.v2.model.json.view.FullView;
import eu.europeana.api2.v2.model.xml.srw.Record;
import eu.europeana.api2.v2.utils.ControllerUtils;
import eu.europeana.api2.v2.web.swagger.SwaggerSelect;
import eu.europeana.corelib.db.entity.enums.RecordType;
import eu.europeana.corelib.db.exception.DatabaseException;
import eu.europeana.corelib.db.exception.LimitReachedException;
import eu.europeana.corelib.db.service.ApiKeyService;
import eu.europeana.corelib.db.service.ApiLogService;
import eu.europeana.corelib.db.service.UserService;
import eu.europeana.corelib.definitions.db.entity.relational.ApiKey;
import eu.europeana.corelib.definitions.edm.beans.BriefBean;
import eu.europeana.corelib.definitions.edm.beans.FullBean;
import eu.europeana.corelib.definitions.exception.ProblemType;
import eu.europeana.corelib.edm.exceptions.EuropeanaQueryException;
import eu.europeana.corelib.edm.exceptions.MongoDBException;
import eu.europeana.corelib.edm.exceptions.SolrTypeException;
import eu.europeana.corelib.edm.utils.EdmUtils;
import eu.europeana.corelib.logging.Log;
import eu.europeana.corelib.logging.Logger;
import eu.europeana.corelib.search.SearchService;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.corelib.utils.EuropeanaUriUtils;
import eu.europeana.corelib.utils.service.OptOutService;
import eu.europeana.corelib.web.service.EuropeanaUrlService;
import eu.europeana.corelib.web.utils.RequestUtils;

/**
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 */
@Controller
@RequestMapping(value = "/v2/record")
@SwaggerSelect
public class ObjectController {

	@Log
	private Logger log;

	@Resource
	private SearchService searchService;

	@Resource
	private ApiLogService apiLogService;

	@Resource
	private ApiKeyService apiService;

//	@Resource
//	private OptOutService optOutService;
	
	@Resource
	private EuropeanaUrlService urlService;
	
	@Resource(name = "corelib_db_userService")
	private UserService userService;

	@Resource
	private ControllerUtils controllerUtils;

	private String similarItemsProfile = "minimal";

	@RequestMapping(value = "/{collectionId}/{recordId}.json", method = RequestMethod.GET, 
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ModelAndView record(
			@PathVariable String collectionId,
			@PathVariable String recordId,
			@RequestParam(value = "profile", required = false, defaultValue = "full") String profile,
			@RequestParam(value = "wskey", required = true) String wskey,
			@RequestParam(value = "callback", required = false) String callback,
			HttpServletRequest request,
			HttpServletResponse response) {
		controllerUtils.addResponseHeaders(response);

		LimitResponse limitResponse = null;
		try {
			limitResponse = controllerUtils.checkLimit(wskey, request.getRequestURL().toString(),
					"record.json", RecordType.OBJECT, profile);
		} catch (ApiLimitException e) {
			response.setStatus(e.getHttpStatus());
			return JsonUtils.toJson(new ApiError(e), callback);
		}

		log.info("record");
		ObjectResult objectResult = new ObjectResult(wskey, "record.json", limitResponse.getRequestNumber());
		if (StringUtils.containsIgnoreCase(profile, "params")) {
			objectResult.addParams(RequestUtils.getParameterMap(request), "wskey");
			objectResult.addParam("profile", profile);
		}

		String europeanaObjectId = EuropeanaUriUtils.createResolveEuropeanaId(collectionId, recordId);
		try {
			long t0 = (new Date()).getTime();
			FullBean bean = searchService.findById(europeanaObjectId, false);
			if (bean == null) {
                                europeanaObjectId= searchService.resolveId(europeanaObjectId);
				bean = searchService.findById(europeanaObjectId, false);
			}
			if(bean!=null && bean.isOptedOut()){
				bean.getAggregations().get(0).setEdmObject("");
			}
			if (bean == null) {
				return JsonUtils.toJson(new ApiError(wskey, "record.json", "Invalid record identifier: "
						+ europeanaObjectId, limitResponse.getRequestNumber()), callback);
			}

			if (StringUtils.containsIgnoreCase(profile, Profile.SIMILAR.getName())) {
				List<BriefBean> similarItems;
				List<BriefView> beans = new ArrayList<BriefView>();
				try {
					similarItems = searchService.findMoreLikeThis(europeanaObjectId);
					for (BriefBean b : similarItems) {
						Boolean optOut = b.getPreviewNoDistribute();
						BriefView view = new BriefView(b, similarItemsProfile, wskey, limitResponse.getApiKey().getUser().getId(), optOut==null?false:optOut);
						beans.add(view);
					}
				} catch (SolrServerException e) {
					log.error("Error during getting similar items: " + e.getLocalizedMessage(),e);
				}
				objectResult.similarItems = beans;
			}
			Boolean optOut = bean.getAggregations().get(0).getEdmPreviewNoDistribute();
			objectResult.object = new FullView(bean, profile, limitResponse.getApiKey().getUser().getId(), optOut==null?false:optOut);
			long t1 = (new Date()).getTime();
			objectResult.statsDuration = (t1 - t0);
//		} catch (SolrTypeException e) {
//			return JsonUtils.toJson(new ApiError(wskey, "record.json", e.getMessage(), limitResponse.getRequestNumber()), callback);
		} catch (MongoDBException e) {
			return JsonUtils.toJson(new ApiError(wskey, "record.json", e.getMessage(), limitResponse.getRequestNumber()), callback);
		}

		return JsonUtils.toJson(objectResult, callback);
	}

	@SuppressWarnings("unused")
	@RequestMapping(value = "/{collectionId}/{recordId}.kml", produces = "application/vnd.google-earth.kml+xml")
	public @ResponseBody ApiResponse searchKml(
			@PathVariable String collectionId, @PathVariable String recordId,
			@RequestParam(value = "apikey", required = true) String apiKey,
			@RequestParam(value = "sessionhash", required = true) String sessionHash) {
		return new ApiNotImplementedYet(apiKey, "record.kml");
	}

	@RequestMapping(value = { "/context.jsonld", "/context.json-ld" }, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ModelAndView contextJSONLD(
			@RequestParam(value = "callback", required = false) String callback 
			) {
		String jsonld = JSONUtils.toString(getJsonContext());
		return JsonUtils.toJson(jsonld, callback);
	}

	@RequestMapping(value = { "/{collectionId}/{recordId}.jsonld", "/{collectionId}/{recordId}.json-ld" }, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ModelAndView recordJSONLD(
			@PathVariable String collectionId, 
			@PathVariable String recordId,
			@RequestParam(value = "wskey", required = true) String wskey,
			@RequestParam(value = "format", required = false, defaultValue="compacted") String format,
			@RequestParam(value = "callback", required = false) String callback, 
			HttpServletRequest request, HttpServletResponse response) {
		response.setCharacterEncoding("UTF-8");

		LimitResponse limitResponse = null;
		try {
			limitResponse = controllerUtils.checkLimit(wskey, request.getRequestURL().toString(),
					"record.jsonld", RecordType.OBJECT_JSONLD, null);
		} catch (ApiLimitException e) {
			response.setStatus(e.getHttpStatus());
			return JsonUtils.toJson(new ApiError(e), callback);
		}

		String europeanaObjectId = "/" + collectionId + "/" + recordId;

		String jsonld = null;

		FullBeanImpl bean = null;
		try {
			bean = (FullBeanImpl) searchService.findById(europeanaObjectId, false);
			if (bean == null) {
				bean = (FullBeanImpl) searchService.resolve(europeanaObjectId, false);
			}
		} catch (SolrTypeException e) {
			log.error(ExceptionUtils.getFullStackTrace(e));
		} catch (MongoDBException e) {
			log.error(ExceptionUtils.getFullStackTrace(e));
		}
		
		if (bean != null) {
			Boolean optOut = bean.getAggregations().get(0).getEdmPreviewNoDistribute();
			if(optOut!=null&&optOut==true){
				bean.getAggregations().get(0).setEdmObject("");
				
			}
			String rdf = EdmUtils.toEDM(bean,false);
			try {
				Model modelResult = ModelFactory.createDefaultModel().read(IOUtils.toInputStream(rdf), "", "RDF/XML");
				JenaRDFParser parser = new JenaRDFParser();
				Object raw = JSONLD.fromRDF(modelResult, parser);
				if (StringUtils.equalsIgnoreCase(format, "compacted")) {
					raw = JSONLD.compact(raw, getJsonContext(), new Options());
				} else if (StringUtils.equalsIgnoreCase(format, "flattened")) {
					raw = JSONLD.flatten(raw);
				} else if (StringUtils.equalsIgnoreCase(format, "normalized")) {
					raw = JSONLD.normalize(raw);
				}
				jsonld = JSONUtils.toString(raw);
			} catch (JSONLDProcessingError e) {
				log.error(e.getMessage(), e);
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		} else {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		}

		return JsonUtils.toJson(jsonld, callback);
	}

	@RequestMapping(value = "/{collectionId}/{recordId}.rdf", produces = "application/rdf+xml")
	public ModelAndView recordRdf(@PathVariable String collectionId, @PathVariable String recordId,
			@RequestParam(value = "wskey", required = true) String wskey, HttpServletResponse response) {
		response.setCharacterEncoding("UTF-8");

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("error", "");

		String europeanaObjectId = "/" + collectionId + "/" + recordId;
		String requestUri = europeanaObjectId + ".rdf";
		String profile = "full";

		ApiKey apiKey;
		try {
			apiKey = apiService.findByID(wskey);
			if (apiKey == null) {
				response.setStatus(401);
				model.put("error", "Unregistered user");
				return new ModelAndView("rdf", model);
			}
			apiKey.getUsageLimit();
			apiService.checkReachedLimit(apiKey);
		} catch (DatabaseException e) {
          // Disabled while awaiting better implementation (ticket #1742)
		  // apiLogService.logApiRequest(wskey, requestUri, RecordType.OBJECT_RDF, profile);
			model.put("error", e.getMessage());
			response.setStatus(401);
			return new ModelAndView("rdf", model);
			// return JsonUtils.toJson(new ApiError(wskey, "record.json", e.getMessage(), requestNumber));
		} catch (LimitReachedException e) {
	       // Disabled while awaiting better implementation (ticket #1742)
    	   // apiLogService.logApiRequest(wskey, requestUri, RecordType.LIMIT, profile);
			log.error(e.getMessage());
			model.put("error", e.getMessage());
			response.setStatus(429);
			return new ModelAndView("rdf", model);
			// return JsonUtils.toJson(new ApiError(wskey, "record.json", e.getMessage(), e.getRequested()));
		}

		FullBeanImpl bean = null;
		try {
			bean = (FullBeanImpl) searchService.findById(europeanaObjectId, false);
			if (bean == null) {
				bean = (FullBeanImpl) searchService.resolve(europeanaObjectId, false);
			}
		} catch (SolrTypeException e) {
			log.error(ExceptionUtils.getFullStackTrace(e));
		} catch (MongoDBException e) {
			log.error(ExceptionUtils.getFullStackTrace(e));
		}

		if (bean != null) {
			Boolean optOut = bean.getAggregations().get(0).getEdmPreviewNoDistribute();
			if(optOut!=null&&optOut==true){
				bean.getAggregations().get(0).setEdmObject("");
				
			}
			model.put("record", EdmUtils.toEDM(bean, false));
		} else {
			response.setStatus(404);
			model.put("error", "Non-existing record identifier");
		}

        // Disabled while awaiting better implementation (ticket #1742)
		// apiLogService.logApiRequest(wskey, requestUri, RecordType.OBJECT_RDF, profile);
		return new ModelAndView("rdf", model);
	}

	private Object getJsonContext() {
		InputStream in = this.getClass().getResourceAsStream("/jsonld/context.jsonld");
		try {
			return JSONUtils.fromInputStream(in);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(in);
		}
		return null;
	}
	
	@RequestMapping(value = "/{collectionId}/{recordId}.srw", produces = MediaType.TEXT_XML_VALUE)
	public @ResponseBody
	SrwResponse recordSrw(@PathVariable String collectionId, @PathVariable String recordId,
			@RequestParam(value = "wskey", required = false) String wskey, HttpServletResponse response)
			throws Exception {
		log.info("====== /v2/record/{collectionId}/{recordId}.srw ======");

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
				Boolean optOut = bean.getAggregations().get(0).getEdmPreviewNoDistribute();
				if(optOut!=null&&optOut==true){
					bean.getAggregations().get(0).setEdmObject("");
					
				}
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