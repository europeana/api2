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

package eu.europeana.api2.web.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mongodb.Mongo;

import eu.europeana.api2.exceptions.LimitReachedException;
import eu.europeana.api2.web.model.ModelUtils;
import eu.europeana.api2.web.model.json.ApiError;
import eu.europeana.api2.web.model.json.ApiView;
import eu.europeana.api2.web.model.json.BriefView;
import eu.europeana.api2.web.model.json.SearchResults;
import eu.europeana.api2.web.model.json.Suggestions;
import eu.europeana.api2.web.model.json.abstracts.ApiResponse;
import eu.europeana.api2.web.model.xml.kml.KmlResponse;
import eu.europeana.api2.web.model.xml.rss.Channel;
import eu.europeana.api2.web.model.xml.rss.Item;
import eu.europeana.api2.web.model.xml.rss.RssResponse;
import eu.europeana.corelib.db.exception.DatabaseException;
import eu.europeana.corelib.db.logging.api.ApiLogger;
import eu.europeana.corelib.db.logging.api.enums.RecordType;
import eu.europeana.corelib.db.service.ApiKeyService;
import eu.europeana.corelib.db.service.UserService;
import eu.europeana.corelib.definitions.db.entity.relational.ApiKey;
import eu.europeana.corelib.definitions.solr.beans.ApiBean;
import eu.europeana.corelib.definitions.solr.beans.BriefBean;
import eu.europeana.corelib.definitions.solr.beans.IdBean;
import eu.europeana.corelib.definitions.solr.model.Query;
import eu.europeana.corelib.solr.exceptions.SolrTypeException;
import eu.europeana.corelib.solr.model.ResultSet;
import eu.europeana.corelib.solr.service.SearchService;
import eu.europeana.corelib.solr.utils.SolrUtils;
import eu.europeana.corelib.utils.OptOutDatasetsUtil;
import eu.europeana.corelib.web.utils.NavigationUtils;

/**
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 */
@Controller
public class SearchController {

	private final Logger log = Logger.getLogger(getClass().getName());

	@Resource(name = "corelib_db_mongo") private Mongo mongo;

	@Resource private SearchService searchService;

	@Resource private ApiKeyService apiService;

	@Resource private UserService userService;

	@Value("#{europeanaProperties['api.rowLimit']}")
	private String rowLimit = "96";

	@Value("#{europeanaProperties['portal.server']}")
	private String portalServer;

	@Value("#{europeanaProperties['portal.name']}")
	private String portalName;

	@Value("#{europeanaProperties['api2.url']}")
	private String apiUrl;

	@Value("#{europeanaProperties['api.optOutList']}")
	private String optOutList;

	@Resource private ApiLogger apiLogger;

	private static String portalUrl;

	private static int maxRows = -1;

	@RequestMapping(value = "/v2/search.json", method=RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ApiResponse searchJson(
		@RequestParam(value = "query", required = true) String queryString,
		@RequestParam(value = "qf", required = false) String[] refinements,
		@RequestParam(value = "profile", required = false, defaultValue="standard") String profile,
		@RequestParam(value = "start", required = false, defaultValue="1") int start,
		@RequestParam(value = "rows", required = false, defaultValue="12") int rows,
		@RequestParam(value = "sort", required = false) String sort,
		@RequestParam(value = "wskey", required = false) String wskey,
		HttpServletRequest request, HttpServletResponse response
			) {

		// workaround of a Spring issue (https://jira.springsource.org/browse/SPR-7963)
		String[] _qf = (String[]) request.getParameterMap().get("qf");
		if (_qf != null && _qf.length != refinements.length) {
			refinements = _qf;
		}

		response.setCharacterEncoding("UTF-8");
		if (maxRows == -1) {
			maxRows = Integer.parseInt(rowLimit);
		}
		rows = Math.min(rows, maxRows);
		log.info("=== search.json: " + rows);
		OptOutDatasetsUtil.setOptOutDatasets(optOutList);

		Query query = new Query(SolrUtils.translateQuery(queryString))
							.setApiQuery(true)
							.setRefinements(refinements)
							.setPageSize(rows)
							.setStart(start - 1)
							.setParameter("facet.mincount", "1")
							.setAllowSpellcheck(false)
							.setAllowFacets(false);
		if (profile.equals("portal") || profile.equals("spelling")) {
			query.setAllowSpellcheck(true);
		}
		if (profile.equals("portal") || profile.equals("facets")) {
			query.setAllowFacets(true);
		}
		long usageLimit = 0;
		ApiKey apiKey;
		long requestNumber = 0;
		try {
			apiKey = apiService.findByID(wskey);
			if (apiKey == null) {
				response.setStatus(401);
				return new ApiError(wskey, "search.json", "Unregistered user");
			}
			usageLimit = apiKey.getUsageLimit();
			requestNumber = apiLogger.getRequestNumber(wskey);
			if (requestNumber > usageLimit) {
				throw new LimitReachedException();
			}
		} catch (DatabaseException e){
			apiLogger.saveApiRequest(wskey, query.getQuery(), RecordType.SEARCH, profile);
			response.setStatus(401);
			return new ApiError(wskey, "search.json", e.getMessage(), requestNumber);
		} catch (LimitReachedException e){
			apiLogger.saveApiRequest(wskey, query.getQuery(), RecordType.LIMIT, profile);
			response.setStatus(429);
			return new ApiError(wskey, "search.json", "Rate limit exceeded. " + usageLimit, requestNumber);
		}

		Class<? extends IdBean> clazz;
		if (StringUtils.containsIgnoreCase(profile, "minimal")) {
			clazz = BriefBean.class;
		} else {
			clazz = ApiBean.class;
		}

		try {
			SearchResults<? extends IdBean> result = createResults(wskey, profile, query, clazz);
			result.requestNumber = requestNumber;
			log.info("got response " + result.items.size());
			apiLogger.saveApiRequest(wskey, query.getQuery(), RecordType.SEARCH, profile);
			return result;
		} catch (SolrTypeException e) {
			log.severe(wskey + " [search.json] " + e.getMessage());
			response.setStatus(500);
			return new ApiError(wskey, "search.json", e.getMessage());
		} catch (Exception e) {
			log.severe(wskey + " [search.json] " + e.getClass().getSimpleName() + " " + e.getMessage());
			response.setStatus(500);
			return new ApiError(wskey, "search.json", e.getMessage());
		}
	}

	private <T extends IdBean> SearchResults<T> createResults(String apiKey, String profile, Query query, Class<T> clazz) 
			throws SolrTypeException {
		SearchResults<T> response = new SearchResults<T>(apiKey, "search.json");
		ResultSet<T> resultSet = searchService.search(clazz, query);
		response.totalResults = resultSet.getResultSize();
		response.itemsCount = resultSet.getResults().size();
		response.items = resultSet.getResults();

		BriefView.setApiUrl(apiUrl);
		BriefView.setPortalUrl(getPortalUrl());

		List<T> beans = new ArrayList<T>();
		for (T b : resultSet.getResults()) {
			if (b instanceof ApiBean) {
				ApiBean bean = (ApiBean)b;
				ApiView view = new ApiView(bean, profile, apiKey);
				//bean.setProfile(profile);
				beans.add((T) view);
			// in case profile = 'minimal'
			} else if (b instanceof BriefBean) {
				BriefBean bean = (BriefBean)b;
				BriefView view = new BriefView(bean, profile, apiKey);
				beans.add((T) view);
			}
		}

		log.info("beans: " + beans.size());
		response.items = beans;
		if (StringUtils.containsIgnoreCase(profile, "facets") || StringUtils.containsIgnoreCase(profile, "portal")) {
			response.facets = ModelUtils.conventFacetList(resultSet.getFacetFields());
		}
		if (StringUtils.containsIgnoreCase(profile, "breadcrumb") || StringUtils.containsIgnoreCase(profile, "portal")) {
			response.breadCrumbs = NavigationUtils.createBreadCrumbList(query);
		}
		if (StringUtils.containsIgnoreCase(profile, "spelling") || StringUtils.containsIgnoreCase(profile, "portal")) {
			response.spellcheck = ModelUtils.convertSpellCheck(resultSet.getSpellcheck());
		}
//		if (StringUtils.containsIgnoreCase(profile, "suggestions") || StringUtils.containsIgnoreCase(profile, "portal")) {
//		}
		return response;
	}

	@RequestMapping(value = "/v2/search.kml", produces= MediaType.APPLICATION_XML_VALUE)
	// @RequestMapping(value = "/v2/search.kml", produces = "application/vnd.google-earth.kml+xml")
	public @ResponseBody KmlResponse searchKml(
		Principal principal,
		@RequestParam(value = "query", required = true) String queryString,
		@RequestParam(value = "qf", required = false) String[] refinements,
		@RequestParam(value = "start", required = false, defaultValue="1") int start,
		@RequestParam(value = "rows", required = false, defaultValue="12") int rows,
		@RequestParam(value = "sort", required = false) String sort,
		@RequestParam(value = "wskey", required = true) String wskey,
		HttpServletRequest request, HttpServletResponse response
			) throws Exception {

		// workaround of a Spring issue (https://jira.springsource.org/browse/SPR-7963)
		String[] _qf = (String[]) request.getParameterMap().get("qf");
		if (_qf != null && _qf.length != refinements.length) {
			refinements = _qf;
		}

		long usageLimit = 0;
		try{
			usageLimit = apiService.findByID(wskey).getUsageLimit();
			if(apiLogger.getRequestNumber(wskey) > usageLimit){
				response.setStatus(429);
				throw new LimitReachedException();
			}
		} catch (DatabaseException e){
			throw new Exception(e);
		} catch (LimitReachedException e){
			throw new Exception(e);
		}
		KmlResponse kmlResponse = new KmlResponse();
		Query query = new Query(SolrUtils.translateQuery(queryString))
					.setApiQuery(true)
					.setAllowSpellcheck(false)
					.setAllowFacets(false);
		query.setRefinements("pl_wgs84_pos_lat_long:[* TO *]");
		try {
			ResultSet<BriefBean> resultSet = searchService.search(BriefBean.class, query);
			kmlResponse.document.extendedData.totalResults.value = Long.toString(resultSet.getResultSize());
			kmlResponse.document.extendedData.startIndex.value = Integer.toString(start);
			kmlResponse.setItems(resultSet.getResults());
			apiLogger.saveApiRequest(wskey, query.getQuery(), RecordType.SEARCH, "kml");
		} catch (SolrTypeException e) {
			response.setStatus(429);
			throw new Exception(e);
		}
		return kmlResponse;
	}

	@RequestMapping(value = "/v2/opensearch.rss", produces= MediaType.APPLICATION_XML_VALUE) //, produces = "?rss?")
	public @ResponseBody RssResponse openSearchRss(
		@RequestParam(value = "searchTerms", required = true) String queryString,
		@RequestParam(value = "startIndex", required = false, defaultValue="1") int start,
		@RequestParam(value = "count", required = false, defaultValue="12") int count,
		@RequestParam(value = "sort", required = false) String sort
	) {
		RssResponse rss = new RssResponse();
		Channel channel = rss.channel;
		channel.startIndex.value = start;
		channel.itemsPerPage.value = count;
		channel.query.searchTerms = queryString;
		channel.query.startPage = start;

		try {
			Query query = new Query(SolrUtils.translateQuery(queryString)).setApiQuery(true).setPageSize(count).setStart(start).setAllowFacets(false).setAllowSpellcheck(false);
			ResultSet<BriefBean> resultSet = searchService.search(BriefBean.class, query);
			channel.totalResults.value = resultSet.getResultSize();
			for (BriefBean bean : resultSet.getResults()) {
				Item item = new Item();
				item.guid = getPortalUrl() + "/record" + bean.getId() + ".html";
				item.title = bean.getTitle()[0];
				item.link = item.guid;
				log.info("item: " + item);
				channel.items.add(item);
			}
		} catch (SolrTypeException e) {
			channel.totalResults.value = 0;
			Item item = new Item();
			item.title = "Error";
			item.description = e.getMessage();
			channel.items.add(item);
		}
		return rss;
	}

	@RequestMapping(value = "/v2/suggestions.json")//, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ApiResponse suggestionsJson(
		@RequestParam(value = "query", required = true) String query,
		@RequestParam(value = "rows", required = false, defaultValue="10") int count,
		@RequestParam(value = "phrases", required = false, defaultValue="false") boolean phrases // 0, no, false, 1 yes, true
	) {
		log.info("phrases: " + phrases);
		Suggestions response = new Suggestions(null, "suggestions.json");
		try {
			response.items = searchService.suggestions(query, count);
			response.itemsCount = response.items.size();
		} catch (SolrTypeException e) {
			return new ApiError(null, "suggestions.json", e.getMessage());
		}
		return response;
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
