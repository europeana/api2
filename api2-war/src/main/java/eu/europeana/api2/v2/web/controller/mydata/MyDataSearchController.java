/*
 * Copyright 2007-2013 The Europeana Foundation
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

package eu.europeana.api2.v2.web.controller.mydata;

import java.security.Principal;
import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import eu.europeana.api2.utils.JsonUtils;
import eu.europeana.api2.v2.model.json.UserModification;
import eu.europeana.api2.v2.model.json.UserResults;
import eu.europeana.api2.v2.model.json.user.Search;
import eu.europeana.api2.v2.web.controller.abstracts.AbstractUserController;
import eu.europeana.corelib.db.exception.DatabaseException;
import eu.europeana.corelib.definitions.db.entity.relational.ApiKey;
import eu.europeana.corelib.definitions.db.entity.relational.SavedSearch;
import eu.europeana.corelib.definitions.db.entity.relational.User;
import eu.europeana.corelib.web.utils.UrlBuilder;

/**
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 */
@Controller
public class MyDataSearchController extends AbstractUserController {

	@RequestMapping(value = "/v2/mydata/savedsearch.json", params = "!action", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
	public ModelAndView defaultAction(
			@RequestParam(value = "callback", required = false) String callback,
			Principal principal) {
		return list(callback, principal);
	}

	@RequestMapping(value = "/v2/mydata/savedsearch.json", params = "action=LIST", produces = MediaType.APPLICATION_JSON_VALUE)
	public ModelAndView list(
			@RequestParam(value = "callback", required = false) String callback,
			Principal principal) {
		UserResults<Search> response = new UserResults<Search>(principal.getName(), "/v2/mydata/search.json");
		try {
			ApiKey apiKey = apiKeyService.findByID(principal.getName());
			if (apiKey != null) {
				User user = apiKey.getUser();
				response.items = new ArrayList<Search>();
				response.username = user.getUserName();
				response.itemsCount = Long.valueOf(user.getSavedSearches().size());
				for (SavedSearch item : user.getSavedSearches()) {
					Search search = new Search();
					search.id = item.getId();
					search.query = item.getQuery();
					search.queryString = item.getQueryString();
					search.dateSaved = item.getDateSaved();
					response.items.add(search);
				}
			} else {
				response.success = false;
				response.error = "Invalid credentials";
			}
		} catch (DatabaseException e) {
			response.success = false;
			response.error = e.getMessage();
		}
		return JsonUtils.toJson(response, callback);
	}

	@RequestMapping(value = "/v2/mydata/savedsearch.json", produces = MediaType.APPLICATION_JSON_VALUE, method = {
			RequestMethod.POST, RequestMethod.PUT })
	public ModelAndView createRest(
			@RequestParam(value = "query", required = true) String query,
			@RequestParam(value = "qf", required = false) String[] refinements,
			@RequestParam(value = "start", required = false, defaultValue = "1") String start,
			@RequestParam(value = "callback", required = false) String callback, Principal principal) {
		return create(query, refinements, start, callback, principal);
	}

	@RequestMapping(value = "/v2/mydata/savedsearch.json", params = "action=CREATE", produces = MediaType.APPLICATION_JSON_VALUE)
	public ModelAndView create(
			@RequestParam(value = "query", required = true) String query,
			@RequestParam(value = "qf", required = false) String[] refinements,
			@RequestParam(value = "start", required = false, defaultValue = "1") String start,
			@RequestParam(value = "callback", required = false) String callback, Principal principal) {
		UserModification response = new UserModification(getApiId(principal), "/v2/mydata/tag.search?action=CREATE");
		try {
			ApiKey apiKey = apiKeyService.findByID(principal.getName());
			if (apiKey != null) {
				User user = apiKey.getUser();
				UrlBuilder ub = new UrlBuilder(query);
				ub.addParam("qf", refinements, true);
				ub.addParam("start", start, true);
				String queryString = StringUtils.replace(ub.toString(), "?", "&");
				userService.createSavedSearch(user.getId(), query, queryString);
				response.success = true;
			}
		} catch (DatabaseException e) {
			response.success = false;
			response.error = e.getMessage();
		}
		return JsonUtils.toJson(response, callback);
	}

	@RequestMapping(value = "/v2/mydata/savedsearch.json", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.DELETE)
	public ModelAndView deleteRest(
			@RequestParam(value = "searchid", required = false) Long objectId,
			@RequestParam(value = "callback", required = false) String callback, Principal principal) {
		return delete(objectId, callback, principal);
	}

	@RequestMapping(value = "/v2/mydata/savedsearch.json", params = "action=DELETE", produces = MediaType.APPLICATION_JSON_VALUE)
	public ModelAndView delete(
			@RequestParam(value = "searchid", required = true) Long searchId,
			@RequestParam(value = "callback", required = false) String callback, Principal principal) {
		UserModification response = new UserModification(getApiId(principal), "/v2/mydata/search.json?action=DELETE");
		try {
			ApiKey apiKey = apiKeyService.findByID(principal.getName());
			if (apiKey != null) {
				User user = apiKey.getUser();
				userService.removeSavedSearch(user.getId(), searchId);
				response.success = true;
			}
		} catch (DatabaseException e) {
			response.success = false;
			response.error = e.getMessage();
		}
		return JsonUtils.toJson(response, callback);
	}
	
}
