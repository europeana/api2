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

import java.security.Principal;
import java.util.ArrayList;

import javax.annotation.Resource;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.europeana.api2.web.controller.abstracts.AbstractUserController;
import eu.europeana.api2.web.model.json.UserModification;
import eu.europeana.api2.web.model.json.UserResults;
import eu.europeana.api2.web.model.json.abstracts.ApiResponse;
import eu.europeana.api2.web.model.json.user.Search;
import eu.europeana.corelib.db.exception.DatabaseException;
import eu.europeana.corelib.db.service.UserService;
import eu.europeana.corelib.definitions.db.entity.relational.SavedSearch;
import eu.europeana.corelib.definitions.db.entity.relational.User;

/**
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 */
@Controller
public class UserSearchController extends AbstractUserController {
	
	@Resource(name="corelib_db_userService")
	private UserService userService;
	
	@RequestMapping(value = "/user/search.json", params="!action",  produces = MediaType.APPLICATION_JSON_VALUE, method=RequestMethod.GET)
	public @ResponseBody ApiResponse defaultAction(
		Principal principal
	) {
		return list(principal);
	}
	
	@RequestMapping(value = "/user/search.json", params="action=LIST",  produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ApiResponse list(
		Principal principal
	) {
		User user = userService.findByEmail(principal.getName());
		if (user != null) {
			UserResults<Search> result = new UserResults<Search>(getApiId(principal), "/user/search.json");
			result.items = new ArrayList<Search>();
			result.username = user.getUserName();
			for (SavedSearch item : user.getSavedSearches()) {
				Search search = new Search();
				search.id = item.getId();
				search.query = item.getQuery();
				search.queryString = item.getQueryString();
				search.dateSaved = item.getDateSaved();
				result.items.add(search);
			}
			return result;
		}
		return null;
	}
	
	@RequestMapping(value = "/user/search.json", params="!action",  produces = MediaType.APPLICATION_JSON_VALUE, method={RequestMethod.POST,RequestMethod.PUT})
	public @ResponseBody ApiResponse createRest(
			@RequestParam(value = "query", required = true) String query,
			@RequestParam(value = "qf", required = false) String[] refinements,
			@RequestParam(value = "start", required = false, defaultValue="1") int start,
		Principal principal
	) {
		return create(query, refinements, start, principal);
	}
	
	@RequestMapping(value = "/user/search.json", params="action=CREATE",  produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ApiResponse create(
		@RequestParam(value = "query", required = true) String query,
		@RequestParam(value = "qf", required = false) String[] refinements,
		@RequestParam(value = "start", required = false, defaultValue="1") int start,
		Principal principal
	) {
		return null;
	}
	
	@RequestMapping(value = "/user/search.json", params="!action",  produces = MediaType.APPLICATION_JSON_VALUE, method=RequestMethod.DELETE)
	public @ResponseBody ApiResponse deleteRest(
		@RequestParam(value = "objectid", required = false) Long objectId,
		Principal principal
	) {
		return delete(objectId, principal);
	}
	
	@RequestMapping(value = "/user/search.json", params="action=DELETE",  produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ApiResponse delete(
		@RequestParam(value = "searchid", required = true) Long searchId,
		Principal principal
	) {
		User user = userService.findByEmail(principal.getName());
		UserModification response = new UserModification(getApiId(principal), "/user/search.json?action=DELETE");
		if (user != null) {
			try {
				userService.removeSavedSearch(user.getId(), searchId);
				response.success = true;
			} catch (DatabaseException e) {
				response.success = false;
				response.error = e.getMessage();
			}
		}
		return response;
	}
	
}
