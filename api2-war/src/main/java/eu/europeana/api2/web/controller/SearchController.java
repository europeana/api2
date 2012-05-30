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

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.europeana.api2.web.model.ModelUtils;
import eu.europeana.api2.web.model.json.ApiError;
import eu.europeana.api2.web.model.json.SearchResults;
import eu.europeana.api2.web.model.json.Suggestions;
import eu.europeana.api2.web.model.json.abstracts.ApiResponse;
import eu.europeana.api2.web.model.xml.kml.KmlResponse;
import eu.europeana.api2.web.model.xml.rss.Channel;
import eu.europeana.api2.web.model.xml.rss.Item;
import eu.europeana.api2.web.model.xml.rss.RssResponse;
import eu.europeana.corelib.definitions.solr.beans.ApiBean;
import eu.europeana.corelib.definitions.solr.beans.BriefBean;
import eu.europeana.corelib.definitions.solr.beans.IdBean;
import eu.europeana.corelib.definitions.solr.model.Query;
import eu.europeana.corelib.solr.exceptions.SolrTypeException;
import eu.europeana.corelib.solr.model.ResultSet;
import eu.europeana.corelib.solr.service.SearchService;
import eu.europeana.corelib.web.utils.NavigationUtils;

/**
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 */
@Controller
public class SearchController {
	
	@Resource
	private SearchService searchService;
	
	@RequestMapping(value = "/search.json", method=RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ApiResponse searchJson(
		Principal principal,
		@RequestParam(value = "query", required = true) String q,
		@RequestParam(value = "qf", required = false) String[] refinements,
		@RequestParam(value = "profile", required = false, defaultValue="standard") String profile,
		@RequestParam(value = "start", required = false, defaultValue="1") int start,
		@RequestParam(value = "rows", required = false, defaultValue="12") int rows,
		@RequestParam(value = "sort", required = false) String sort
	) {
		Query query = new Query(q).setRefinements(refinements).setPageSize(rows).setStart(start);
		Class<? extends IdBean> clazz = ApiBean.class;
		if (StringUtils.containsIgnoreCase(profile, "minimal")) {
			clazz = BriefBean.class;
		}
		try {
			SearchResults<? extends IdBean> response = createResults(principal.getName(), profile, query, clazz);
			return response;
		} catch (SolrTypeException e) {
			return new ApiError(principal.getName(), "search.json", e.getMessage());
		}
	}
	
	private <T extends IdBean> SearchResults<T> createResults(String apiKey, String profile, Query q, Class<T> clazz) throws SolrTypeException {
		SearchResults<T> response = new SearchResults<T>(apiKey, "search.json");
		ResultSet<T> resultSet = searchService.search(clazz, q);
		response.totalResults = resultSet.getResultSize();
		response.itemsCount = resultSet.getResults().size();
		response.items = resultSet.getResults();
		if (StringUtils.containsIgnoreCase(profile, "facets") || StringUtils.containsIgnoreCase(profile, "portal")) {
			response.facets = ModelUtils.conventFacetList(resultSet.getFacetFields());
		}
		if (StringUtils.containsIgnoreCase(profile, "breadcrumb") || StringUtils.containsIgnoreCase(profile, "portal")) {
			response.breadCrumbs = NavigationUtils.createBreadCrumbList(q);
		}
		if (StringUtils.containsIgnoreCase(profile, "spelling") || StringUtils.containsIgnoreCase(profile, "portal")) {
			response.spellcheck = ModelUtils.convertSpellCheck(resultSet.getSpellcheck());
		}
//		if (StringUtils.containsIgnoreCase(profile, "suggestions") || StringUtils.containsIgnoreCase(profile, "portal")) {
//		}
		return response;
	}
	
	@RequestMapping(value = "/search.kml", produces= MediaType.APPLICATION_XML_VALUE)//, produces = "application/vnd.google-earth.kml+xml")
	public @ResponseBody KmlResponse searchKml(
		Principal principal,
		@RequestParam(value = "query", required = true) String q,
		@RequestParam(value = "qf", required = false) String[] refinements,
		@RequestParam(value = "start", required = false, defaultValue="1") int start,
		@RequestParam(value = "rows", required = false, defaultValue="12") int rows,
		@RequestParam(value = "sort", required = false) String sort
	) {
		KmlResponse response = new KmlResponse();
		Query query = new Query(q);
		query.setRefinements("edm_place_latLon:[* TO *]");
		try {
			ResultSet<BriefBean> resultSet = searchService.search(BriefBean.class, query);
			response.document.extendedData.totalResults.value = Long.toString(resultSet.getResultSize());
			response.document.extendedData.startIndex.value = Integer.toString(start);
			response.setItems(resultSet.getResults());
		} catch (SolrTypeException e) {
//			ApiError error = new ApiError();
//			error.error = e.getMessage();
//			return error;
		}
		return response;
	}
	
	@RequestMapping(value = "/opensearch.rss", produces= MediaType.APPLICATION_XML_VALUE) //, produces = "?rss?")
	public @ResponseBody RssResponse openSearchRss(
		@RequestParam(value = "searchTerms", required = true) String q,
		@RequestParam(value = "startIndex", required = false, defaultValue="1") int start,
		@RequestParam(value = "count", required = false, defaultValue="12") int count,
		@RequestParam(value = "sort", required = false) String sort
	) {
		try {
			Query query = new Query(q).setPageSize(count).setStart(start);
			ResultSet<BriefBean> resultSet = searchService.search(BriefBean.class, query);
			RssResponse rss = new RssResponse();
			Channel channel = rss.channel;
			channel.totalResults.value = resultSet.getResultSize();
			channel.startIndex.value = start;
			channel.itemsPerPage.value = count;
			channel.query.searchTerms = q;
			channel.query.startPage = start;
			for (BriefBean bean : resultSet.getResults()) {
				Item item = new Item();
				item.title = bean.getTitle()[0];
				channel.items.add(item);
			}
			return rss;
		} catch (SolrTypeException e) {
			return null;
		}

	}
	
	@RequestMapping(value = "/suggestions.json")//, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ApiResponse suggestionsJson(
		@RequestParam(value = "query", required = true) String query,
		@RequestParam(value = "rows", required = false, defaultValue="10") int count,
		@RequestParam(value = "phrases", required = false, defaultValue="false") boolean phrases
	) {
		Suggestions response = new Suggestions(null, "suggestions.json");
		try {
			response.items = searchService.suggestions(query, count);
			response.itemsCount = response.items.size();
		} catch (SolrTypeException e) {
			return new ApiError(null, "suggestions.json", e.getMessage());
		}
		return response;
	}

}
