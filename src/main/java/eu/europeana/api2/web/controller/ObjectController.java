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
public class ObjectController {
	
	@Resource
	private SearchService searchService;

	@RequestMapping(value = "/record.json", method=RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ApiResponse record(
		@RequestParam(value = "apikey", required = true) String apiKey,
		@RequestParam(value = "sessionhash", required = true) String sessionHash,
		@RequestParam(value = "objectid", required = true) String objectId,
		@RequestParam(value = "profile", required = false, defaultValue="full") String profile
	) {
		ObjectResult response = new ObjectResult(apiKey, "record.json");
		try {
			response.object = searchService.findById(objectId);
		} catch (SolrTypeException e) {
			return new ApiError(apiKey, "record.json", e.getMessage());
		}
		return response;
	}
	
	
	@RequestMapping(value = "/record.kml", produces = "application/vnd.google-earth.kml+xml")
	public @ResponseBody ApiResponse searchKml(
			@RequestParam(value = "apikey", required = true) String apiKey,
			@RequestParam(value = "sessionhash", required = true) String sessionHash,
			@RequestParam(value = "objectid", required = true) String objectId
	) {
		return new ApiNotImplementedYet(apiKey, "record.kml");
	}
	
}