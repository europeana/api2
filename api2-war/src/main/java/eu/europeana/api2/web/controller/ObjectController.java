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
import java.util.logging.Logger;

import javax.annotation.Resource;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.europeana.api2.web.model.json.ApiError;
import eu.europeana.api2.web.model.json.ApiNotImplementedYet;
import eu.europeana.api2.web.model.json.ObjectResult;
import eu.europeana.api2.web.model.json.abstracts.ApiResponse;
import eu.europeana.corelib.solr.exceptions.SolrTypeException;
import eu.europeana.corelib.solr.service.SearchService;

/**
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 */
@Controller
@RequestMapping(value = "/record")
public class ObjectController {
	
	private final Logger log = Logger.getLogger(getClass().getName());
	@Resource
	private SearchService searchService;

	@Transactional
	@RequestMapping(value = "/{collectionId}/{recordId}.json", method=RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ApiResponse record(
		@PathVariable String collectionId,
		@PathVariable String recordId,
		Principal principal,
		@RequestParam(value = "profile", required = false, defaultValue="full") String profile
	) {
		log.info("record");
		ObjectResult response = new ObjectResult(principal.getName(), "record.json");
		try {
			response.object = searchService.findById(collectionId, recordId);
		} catch (SolrTypeException e) {
			return new ApiError(principal.getName(), "record.json", e.getMessage());
		}
		return response;
	}
	
	
	@RequestMapping(value = "/{collectionId}/{recordId}.kml", produces = "application/vnd.google-earth.kml+xml")
	public @ResponseBody ApiResponse searchKml(
	        @PathVariable String collectionId,
	        @PathVariable String recordId,
			@RequestParam(value = "apikey", required = true) String apiKey,
			@RequestParam(value = "sessionhash", required = true) String sessionHash
	) {
		return new ApiNotImplementedYet(apiKey, "record.kml");
	}
	
}