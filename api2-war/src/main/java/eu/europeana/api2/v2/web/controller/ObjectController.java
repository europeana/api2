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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.slf4j.Logger;
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
import com.github.jsonldjava.impl.JenaRDFParser;
import com.github.jsonldjava.utils.JSONUtils;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import eu.europeana.api2.model.enums.Profile;
import eu.europeana.api2.model.json.ApiError;
import eu.europeana.api2.model.json.ApiNotImplementedYet;
import eu.europeana.api2.model.json.abstracts.ApiResponse;
import eu.europeana.api2.utils.JsonUtils;
import eu.europeana.api2.v2.model.json.ObjectResult;
import eu.europeana.api2.v2.model.json.view.BriefView;
import eu.europeana.api2.v2.model.json.view.FullView;
import eu.europeana.corelib.db.entity.enums.RecordType;
import eu.europeana.corelib.db.exception.DatabaseException;
import eu.europeana.corelib.db.exception.LimitReachedException;
import eu.europeana.corelib.db.service.ApiKeyService;
import eu.europeana.corelib.db.service.ApiLogService;
import eu.europeana.corelib.definitions.db.entity.relational.ApiKey;
import eu.europeana.corelib.definitions.solr.beans.BriefBean;
import eu.europeana.corelib.definitions.solr.beans.FullBean;
import eu.europeana.corelib.logging.Log;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.corelib.solr.exceptions.MongoDBException;
import eu.europeana.corelib.solr.exceptions.SolrTypeException;
import eu.europeana.corelib.solr.service.SearchService;
import eu.europeana.corelib.solr.utils.EDMUtils;
import eu.europeana.corelib.utils.service.OptOutService;
import eu.europeana.corelib.web.service.EuropeanaUrlService;
import eu.europeana.corelib.web.utils.RequestUtils;

/**
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 */
@Controller
@RequestMapping(value = "/v2/record")
public class ObjectController {

	@Log
	private Logger log;

	@Resource
	private SearchService searchService;

	@Resource
	private ApiLogService apiLogService;

	@Resource
	private ApiKeyService apiService;

	@Resource
	private OptOutService optOutService;
	
	@Resource
	private EuropeanaUrlService urlService;

	private String similarItemsProfile = "minimal";

	@RequestMapping(value = "/{collectionId}/{recordId}.json", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ModelAndView record(@PathVariable String collectionId, @PathVariable String recordId,
			@RequestParam(value = "profile", required = false, defaultValue = "full") String profile,
			@RequestParam(value = "wskey", required = true) String wskey,
			@RequestParam(value = "callback", required = false) String callback, HttpServletRequest request,
			HttpServletResponse response) {
		response.setCharacterEncoding("UTF-8");

		String europeanaObjectId = "/" + collectionId + "/" + recordId;
		String requestUri = europeanaObjectId + ".json";
		ApiKey apiKey;
		long requestNumber = 0;
		try {
			apiKey = apiService.findByID(wskey);
			if (apiKey == null) {
				return JsonUtils.toJson(new ApiError(wskey, "record.json", "Unregistered user"), callback);
			}
			apiKey.getUsageLimit();
			requestNumber = apiService.checkReachedLimit(apiKey);
		} catch (DatabaseException e) {
			apiLogService.logApiRequest(wskey, requestUri, RecordType.OBJECT, profile);
			return JsonUtils.toJson(new ApiError(wskey, "record.json", e.getMessage(), requestNumber), callback);
		} catch (LimitReachedException e) {
			apiLogService.logApiRequest(wskey, requestUri, RecordType.LIMIT, profile);
			return JsonUtils.toJson(new ApiError(wskey, "record.json", e.getMessage(), e.getRequested()), callback);
		}
		log.info("record");
		ObjectResult objectResult = new ObjectResult(wskey, "record.json", requestNumber);
		if (StringUtils.containsIgnoreCase(profile, "params")) {
			objectResult.addParams(RequestUtils.getParameterMap(request), "wskey");
			objectResult.addParam("profile", profile);
		}

		try {
			long t0 = (new Date()).getTime();
			FullBean bean = searchService.findById(europeanaObjectId, true);
			if (bean == null) {
				bean = searchService.resolve(europeanaObjectId, true);
			}

			if (bean == null) {
				apiLogService.logApiRequest(wskey, requestUri, RecordType.LIMIT, profile);
				return JsonUtils.toJson(new ApiError(wskey, "record.json", "Invalid record identifier: "
						+ europeanaObjectId, requestNumber), callback);
			}

			if (StringUtils.containsIgnoreCase(profile, Profile.SIMILAR.getName())) {
				List<BriefBean> similarItems;
				List<BriefView> beans = new ArrayList<BriefView>();
				try {
					similarItems = searchService.findMoreLikeThis(europeanaObjectId);
					for (BriefBean b : similarItems) {
						BriefView view = new BriefView(b, similarItemsProfile, wskey, optOutService.check(b.getId()));
						beans.add(view);
					}
				} catch (SolrServerException e) {
					log.error("Error during getting similar items: " + e.getLocalizedMessage());
					log.error(ExceptionUtils.getMessage(e));
				}
				objectResult.similarItems = beans;
			}
			FullView.setApiUrl(urlService.getApi2Home(wskey));
			FullView.setPortalUrl(urlService.getPortalHome(false));
			objectResult.object = new FullView(bean, profile, apiKey.getUser().getId(), optOutService.check(bean
					.getId()));
			long t1 = (new Date()).getTime();
			objectResult.statsDuration = (t1 - t0);
			apiLogService.logApiRequest(wskey, requestUri, RecordType.OBJECT, profile);
		} catch (SolrTypeException e) {
			return JsonUtils.toJson(new ApiError(wskey, "record.json", e.getMessage(), requestNumber), callback);
		} catch (MongoDBException e) {
			return JsonUtils.toJson(new ApiError(wskey, "record.json", e.getMessage(), requestNumber), callback);
		}

		return JsonUtils.toJson(objectResult, callback);
	}

	@RequestMapping(value = "/{collectionId}/{recordId}.kml", produces = "application/vnd.google-earth.kml+xml")
	public @ResponseBody
	ApiResponse searchKml(@PathVariable String collectionId, @PathVariable String recordId,
			@RequestParam(value = "apikey", required = true) String apiKey,
			@RequestParam(value = "sessionhash", required = true) String sessionHash) {
		return new ApiNotImplementedYet(apiKey, "record.kml");
	}

	@RequestMapping(value = { "/{collectionId}/{recordId}.jsonld", "/{collectionId}/{recordId}.json-ld" }, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ModelAndView recordJSONLD(@PathVariable String collectionId, @PathVariable String recordId,
			@RequestParam(value = "wskey", required = true) String wskey,
			@RequestParam(value = "callback", required = false) String callback, HttpServletResponse response) {
		response.setCharacterEncoding("UTF-8");

		ApiKey apiKey;
		long requestNumber = 0;
		try {
			apiKey = apiService.findByID(wskey);
			if (apiKey == null) {
				return JsonUtils.toJson(new ApiError(wskey, "record.json", "Unregistered user"), callback);
			}
			apiKey.getUsageLimit();
			requestNumber = apiService.checkReachedLimit(apiKey);
		} catch (DatabaseException e) {
			apiLogService.logApiRequest(wskey, requestUri, RecordType.OBJECT, profile);
			return JsonUtils.toJson(new ApiError(wskey, "record.json", e.getMessage(), requestNumber), callback);
		} catch (LimitReachedException e) {
			apiLogService.logApiRequest(wskey, requestUri, RecordType.LIMIT, profile);
			return JsonUtils.toJson(new ApiError(wskey, "record.json", e.getMessage(), e.getRequested()), callback);
		}
		
		String europeanaObjectId = "/" + collectionId + "/" + recordId;

		String jsonld = null;

		FullBeanImpl bean = null;
		try {
			bean = (FullBeanImpl) searchService.findById(europeanaObjectId, true);
			if (bean == null) {
				bean = (FullBeanImpl) searchService.resolve(europeanaObjectId, true);
			}
		} catch (SolrTypeException e) {
			log.error(ExceptionUtils.getFullStackTrace(e));
		} catch (MongoDBException e) {
			log.error(ExceptionUtils.getFullStackTrace(e));
		}

		if (bean != null) {
			String rdf = EDMUtils.toEDM(bean);
			try {
				Model modelResult = ModelFactory.createDefaultModel().read(IOUtils.toInputStream(rdf), "", "RDF/XML");
				JenaRDFParser parser = new JenaRDFParser();
				jsonld = JSONUtils.toString(JSONLD.fromRDF(modelResult, parser));
			} catch (JSONLDProcessingError e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			response.setStatus(404);
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
			apiLogService.logApiRequest(wskey, requestUri, RecordType.OBJECT, profile);
			model.put("error", e.getMessage());
			response.setStatus(401);
			return new ModelAndView("rdf", model);
			// return JsonUtils.toJson(new ApiError(wskey, "record.json", e.getMessage(), requestNumber));
		} catch (LimitReachedException e) {
			apiLogService.logApiRequest(wskey, requestUri, RecordType.LIMIT, profile);
			log.error(e.getMessage());
			model.put("error", e.getMessage());
			response.setStatus(429);
			return new ModelAndView("rdf", model);
			// return JsonUtils.toJson(new ApiError(wskey, "record.json", e.getMessage(), e.getRequested()));
		}

		FullBeanImpl bean = null;
		try {
			bean = (FullBeanImpl) searchService.findById(europeanaObjectId, true);
			if (bean == null) {
				bean = (FullBeanImpl) searchService.resolve(europeanaObjectId, true);
			}
		} catch (SolrTypeException e) {
			log.error(ExceptionUtils.getFullStackTrace(e));
		} catch (MongoDBException e) {
			log.error(ExceptionUtils.getFullStackTrace(e));
		}

		if (bean != null) {
			model.put("record", EDMUtils.toEDM(bean));
		} else {
			response.setStatus(404);
			model.put("error", "Non-existing record identifier");
		}

		apiLogService.logApiRequest(wskey, requestUri, RecordType.OBJECT, profile);
		return new ModelAndView("rdf", model);
	}
}