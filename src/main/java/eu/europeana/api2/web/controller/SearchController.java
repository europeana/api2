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

import javax.annotation.Resource;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.europeana.api2.web.model.abstracts.ApiResponse;
import eu.europeana.corelib.solr.service.SearchService;

/**
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 */
@Controller
public class SearchController {
	
	@Resource
	private SearchService searchService;
	
	@RequestMapping(value = "/search.json", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ApiResponse searchJson(
		@RequestParam(value = "apikey", required = true) String apiKey,
		@RequestParam(value = "sessionhash", required = true) String sessionHash,
		@RequestParam(value = "query", required = true) String query,
		@RequestParam(value = "qf", required = false) String[] refinements,
		@RequestParam(value = "profile", required = false, defaultValue="standard") String profile,
		@RequestParam(value = "start", required = false, defaultValue="1") int start,
		@RequestParam(value = "rows", required = false, defaultValue="12") int rows,
		@RequestParam(value = "sort", required = false) String sort
	) {
		return null;
	}
	
	@RequestMapping(value = "/search.kml", produces = "?kml?")
	public @ResponseBody ApiResponse searchKml(
		@RequestParam(value = "apikey", required = true) String apiKey,
		@RequestParam(value = "sessionhash", required = true) String sessionHash,
		@RequestParam(value = "query", required = true) String query,
		@RequestParam(value = "qf", required = false) String[] refinements,
		@RequestParam(value = "start", required = false, defaultValue="1") int start,
		@RequestParam(value = "rows", required = false, defaultValue="12") int rows,
		@RequestParam(value = "sort", required = false) String sort
	) {
		return null;
	}
	
	@RequestMapping(value = "/opensearch.rss", produces = "?rss?")
	public @ResponseBody ApiResponse openSearchRss(
		@RequestParam(value = "searchTerms", required = true) String query,
		@RequestParam(value = "startIndex", required = false, defaultValue="1") int start,
		@RequestParam(value = "count", required = false, defaultValue="12") int count,
		@RequestParam(value = "sort", required = false) String sort
	) {
		return null;
	}
	
	@RequestMapping(value = "/suggestions.json", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ApiResponse suggestionsJson(
		@RequestParam(value = "query", required = true) String query,
		@RequestParam(value = "rows", required = false, defaultValue="10") int count
	) {
		return null;
	}

}
