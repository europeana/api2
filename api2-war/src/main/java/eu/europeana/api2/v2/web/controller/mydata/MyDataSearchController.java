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

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import eu.europeana.api2.utils.JsonUtils;
import eu.europeana.api2.v2.model.json.UserResults;
import eu.europeana.api2.v2.model.json.user.Search;
import eu.europeana.api2.v2.web.controller.abstracts.AbstractUserController;
import eu.europeana.corelib.db.exception.DatabaseException;
import eu.europeana.corelib.definitions.db.entity.relational.ApiKey;
import eu.europeana.corelib.definitions.db.entity.relational.SavedSearch;
import eu.europeana.corelib.definitions.db.entity.relational.User;

/**
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 */
@Controller
public class MyDataSearchController extends AbstractUserController {

	@RequestMapping(value = "/v2/mydata/savedsearch.json", params = "!action", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
	public ModelAndView defaultAction(
			@RequestParam(value = "wskey", required = true) String wskey,
			@RequestParam(value = "username", required = true) String username,
			@RequestParam(value = "callback", required = false) String callback) {
		return list(wskey, username, callback);
	}

	@RequestMapping(value = "/v2/mydata/savedsearch.json", params = "action=LIST", produces = MediaType.APPLICATION_JSON_VALUE)
	public ModelAndView list(
			@RequestParam(value = "wskey", required = true) String wskey,
			@RequestParam(value = "username", required = true) String username,
			@RequestParam(value = "callback", required = false) String callback) {
		UserResults<Search> response = new UserResults<Search>(wskey, "/v2/mydata/search.json");
		try {
			ApiKey apiKey = apiKeyService.findByID(wskey);
			if ((apiKey != null) && StringUtils.equalsIgnoreCase(username, apiKey.getUser().getUserName())) {
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

}
