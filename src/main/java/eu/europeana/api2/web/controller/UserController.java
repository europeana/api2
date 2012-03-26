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
import eu.europeana.corelib.db.service.UserService;

/**
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 */
@Controller("/user")
public class UserController {
	
	@Resource
	private UserService userService;
	
	@RequestMapping(value = "/favorite.json", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ApiResponse favorite(
		@RequestParam(value = "apikey", required = true) String apiKey,
		@RequestParam(value = "sessionhash", required = true) String sessionHash,
		@RequestParam(value = "action", required = true) String action,
		@RequestParam(value = "objectid", required = false) String objectId
	) {
		return null;
	}
	
	@RequestMapping(value = "/tag.json", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ApiResponse tag(
		@RequestParam(value = "apikey", required = true) String apiKey,
		@RequestParam(value = "sessionhash", required = true) String sessionHash,
		@RequestParam(value = "action", required = true) String action,
		@RequestParam(value = "tagid", required = false) String tagId,
		@RequestParam(value = "objectid", required = false) String objectId,
		@RequestParam(value = "tag", required = false) String tag
	) {
		return null;
	}
	
	
	@RequestMapping(value = "/search.json", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ApiResponse search(
		@RequestParam(value = "apikey", required = true) String apiKey,
		@RequestParam(value = "sessionhash", required = true) String sessionHash,
		@RequestParam(value = "action", required = true) String action,
		@RequestParam(value = "searchid", required = false) String searchId,
		@RequestParam(value = "query", required = false) String query,
		@RequestParam(value = "qf", required = false) String[] refinements,
		@RequestParam(value = "start", required = false, defaultValue="1") int start
	) {
		return null;
	}

}