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

package eu.europeana.api2.web.controller.user;

import javax.annotation.Resource;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.europeana.api2.web.model.json.abstracts.ApiResponse;
import eu.europeana.corelib.db.service.UserService;

/**
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 */
@Controller
public class UserTagController {
	
	@Resource(name="corelib_db_userService")
	private UserService userService;
	
	@RequestMapping(value = "/user/tag.json", params="!action",  produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ApiResponse defaultAction(
		@RequestParam(value = "apikey", required = true) String apiKey,
		@RequestParam(value = "sessionhash", required = true) String sessionHash,
		@RequestParam(value = "objectid", required = false) String objectId
	) {
		return list(apiKey, sessionHash, objectId);
	}
	
	@RequestMapping(value = "/user/tag.json", params="action=LIST",  produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ApiResponse list(
		@RequestParam(value = "apikey", required = true) String apiKey,
		@RequestParam(value = "sessionhash", required = true) String sessionHash,
		@RequestParam(value = "objectid", required = false) String objectId
	) {
		return null;
	}
	
	@RequestMapping(value = "/user/tag.json", params="action=CREATE",  produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ApiResponse create(
		@RequestParam(value = "apikey", required = true) String apiKey,
		@RequestParam(value = "sessionhash", required = true) String sessionHash,
		@RequestParam(value = "objectid", required = true) String objectId,
		@RequestParam(value = "tag", required = true) String tag
	) {
		return null;
	}
	
	@RequestMapping(value = "/user/tag.json", params="action=DELETE",  produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ApiResponse delete(
		@RequestParam(value = "apikey", required = true) String apiKey,
		@RequestParam(value = "sessionhash", required = true) String sessionHash,
		@RequestParam(value = "tagid", required = true) String tagId
	) {
		return null;
	}
	
}
