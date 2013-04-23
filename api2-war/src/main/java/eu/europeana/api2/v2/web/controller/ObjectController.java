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
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.europeana.api2.exceptions.LimitReachedException;
import eu.europeana.api2.model.enums.Profile;
import eu.europeana.api2.model.json.ApiError;
import eu.europeana.api2.model.json.ApiNotImplementedYet;
import eu.europeana.api2.model.json.abstracts.ApiResponse;
import eu.europeana.api2.v2.model.json.ObjectResult;
import eu.europeana.api2.v2.model.json.view.BriefView;
import eu.europeana.api2.v2.model.json.view.FullView;
import eu.europeana.corelib.db.entity.enums.RecordType;
import eu.europeana.corelib.db.exception.DatabaseException;
import eu.europeana.corelib.db.service.ApiKeyService;
import eu.europeana.corelib.db.service.ApiLogService;
import eu.europeana.corelib.definitions.db.entity.relational.ApiKey;
import eu.europeana.corelib.definitions.solr.beans.BriefBean;
import eu.europeana.corelib.definitions.solr.beans.FullBean;
import eu.europeana.corelib.solr.exceptions.SolrTypeException;
import eu.europeana.corelib.solr.service.SearchService;
import eu.europeana.corelib.utils.service.OptOutService;

/**
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 */
@Controller
@RequestMapping(value = "/v2/record")
public class ObjectController {

	private final Logger log = Logger.getLogger(getClass().getName());

	@Resource
	private SearchService searchService;

	@Resource
	private ApiLogService apiLogService;

	@Resource
	private ApiKeyService apiService;
	
	@Resource
	private OptOutService optOutService;

	@Value("#{europeanaProperties['portal.server']}")
	private String portalServer;

	@Value("#{europeanaProperties['portal.name']}")
	private String portalName;

	@Value("#{europeanaProperties['api2.url']}")
	private String apiUrl;

	private static String portalUrl;

	private String similarItemsProfile = "minimal";

	private final static String RESOLVE_PREFIX = "http://www.europeana.eu/resolve/record";

	// @Transactional
	@RequestMapping(value = "/{collectionId}/{recordId}.json", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	ApiResponse record(
			@PathVariable String collectionId,
			@PathVariable String recordId,
			@RequestParam(value = "profile", required = false, defaultValue = "full") String profile,
			@RequestParam(value = "wskey", required = true) String wskey,
			HttpServletRequest request, HttpServletResponse response) {
		response.setCharacterEncoding("UTF-8");

		String europeanaObjectId = "/" + collectionId + "/" + recordId;
		String requestUri = europeanaObjectId + ".json";
		long usageLimit = 0;
		ApiKey apiKey;
		long requestNumber = 0;
		try {
			apiKey = apiService.findByID(wskey);
			if (apiKey == null) {
				return new ApiError(wskey, "record.json", "Unregistered user");
			}
			if (apiService.checkReachedLimit(apiKey)) {
				throw new LimitReachedException();
			}
		} catch (DatabaseException e) {
			apiLogService.logApiRequest(wskey, requestUri, RecordType.OBJECT,
					profile);
			return new ApiError(wskey, "record.json", e.getMessage(),
					requestNumber);
		} catch (LimitReachedException e) {
			apiLogService.logApiRequest(wskey, requestUri, RecordType.LIMIT,
					profile);
			return new ApiError(wskey, "record.json", "Rate limit exceeded. "
					+ usageLimit, requestNumber);
		}
		log.info("record");
		ObjectResult objectResult = new ObjectResult(wskey, "record.json",
				requestNumber);
		try {
			long t0 = (new Date()).getTime();
			FullBean bean = searchService.findById(europeanaObjectId);
			if (bean == null) {
				bean = searchService
						.resolve(RESOLVE_PREFIX + europeanaObjectId);
			}

			if (bean == null) {
				apiLogService.logApiRequest(wskey, requestUri, RecordType.LIMIT,
						profile);
				return new ApiError(wskey, "record.json",
						"Invalid record identifier: " + europeanaObjectId,
						requestNumber);
			}

			if (profile.equals(Profile.SIMILAR.getName())) {
				BriefView.setApiUrl(apiUrl);
				BriefView.setPortalUrl(getPortalUrl());

				List<BriefView> beans = new ArrayList<BriefView>();
				for (BriefBean b : bean.getSimilarItems()) {
					BriefView view = new BriefView(b, similarItemsProfile,
							wskey, optOutService.check(b.getId()));
					beans.add(view);
				}
				objectResult.similarItems = beans;
			}
			FullView.setApiUrl(apiUrl);
			FullView.setPortalUrl(getPortalUrl());
			objectResult.object = new FullView(bean, profile, apiKey.getUser()
					.getId(), optOutService.check(bean.getId()));
			long t1 = (new Date()).getTime();
			objectResult.statsDuration = (t1 - t0);
			apiLogService.logApiRequest(wskey, requestUri, RecordType.OBJECT,
					profile);
		} catch (SolrTypeException e) {
			return new ApiError(wskey, "record.json", e.getMessage(),
					requestNumber);
		}

		return objectResult;
	}

	@RequestMapping(value = "/{collectionId}/{recordId}.kml", produces = "application/vnd.google-earth.kml+xml")
	public @ResponseBody
	ApiResponse searchKml(
			@PathVariable String collectionId,
			@PathVariable String recordId,
			@RequestParam(value = "apikey", required = true) String apiKey,
			@RequestParam(value = "sessionhash", required = true) String sessionHash) {
		return new ApiNotImplementedYet(apiKey, "record.kml");
	}

	private String getPortalUrl() {
		if (portalUrl == null) {
			StringBuilder sb = new StringBuilder(portalServer);
			if (!portalServer.endsWith("/") && !portalName.startsWith("/")) {
				sb.append("/");
			}
			sb.append(portalName);
			portalUrl = sb.toString();
		}
		return portalUrl;
	}
}