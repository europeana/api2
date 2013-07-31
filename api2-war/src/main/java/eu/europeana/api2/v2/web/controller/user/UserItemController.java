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

package eu.europeana.api2.v2.web.controller.user;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

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
import eu.europeana.api2.v2.model.json.user.SavedItem;
import eu.europeana.api2.v2.web.controller.abstracts.AbstractUserController;
import eu.europeana.corelib.db.exception.DatabaseException;
import eu.europeana.corelib.definitions.db.entity.relational.User;

/**
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 */
@Controller
public class UserItemController extends AbstractUserController {

	@RequestMapping(value = "/v2/user/saveditem.json", params = "!action", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
	public ModelAndView defaultAction(
			@RequestParam(value = "europeanaid", required = false) String europeanaId,
			@RequestParam(value = "callback", required = false) String callback, 
			Principal principal) {
		return list(europeanaId, callback, principal);
	}

	@RequestMapping(value = "/v2/user/saveditem.json", params = "action=LIST", produces = MediaType.APPLICATION_JSON_VALUE)
	public ModelAndView list(
			@RequestParam(value = "europeanaid", required = false) String europeanaId,
			@RequestParam(value = "callback", required = false) String callback, 
			Principal principal) {
		User user = userService.findByEmail(principal.getName());
		if (user != null) {
			UserResults<SavedItem> response = new UserResults<SavedItem>(getApiId(principal), "/v2/user/saveditem.json");
			try {
				response.items = new ArrayList<SavedItem>();
				response.username = user.getUserName();
				Set<eu.europeana.corelib.definitions.db.entity.relational.SavedItem> results;
				if (StringUtils.isBlank(europeanaId)) {
					results = user.getSavedItems();
				} else {
					results = new HashSet<eu.europeana.corelib.definitions.db.entity.relational.SavedItem>();
					results.add(userService.findSavedItemByEuropeanaId(user.getId(), europeanaId));
				}
				response.itemsCount = Long.valueOf(results.size());
				for (eu.europeana.corelib.definitions.db.entity.relational.SavedItem item : results) {
					SavedItem fav = new SavedItem();
					copyUserObjectData(fav, item);
					fav.author = item.getAuthor();
					response.items.add(fav);
				}
			} catch (DatabaseException e) {
				response.success = false;
				response.error = e.getMessage();
			}
			return JsonUtils.toJson(response, callback);
		}
		return null;
	}

	@RequestMapping(value = "/v2/user/saveditem.json", params = "!action", produces = MediaType.APPLICATION_JSON_VALUE, method = {
			RequestMethod.POST, RequestMethod.PUT })
	public ModelAndView createRest(
			@RequestParam(value = "europeanaid", required = false) String europeanaId,
			@RequestParam(value = "callback", required = false) String callback, 
			Principal principal) {
		return create(europeanaId, callback, principal);
	}

	@RequestMapping(value = "/v2/user/saveditem.json", params = "action=CREATE", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
	public ModelAndView create(
			@RequestParam(value = "europeanaid", required = false) String europeanaId,
			@RequestParam(value = "callback", required = false) String callback, 
			Principal principal) {
		User user = userService.findByEmail(principal.getName());
		UserModification response = new UserModification(getApiId(principal), "/v2/user/saveditem.json?action=CREATE");
		try {
			userService.createSavedItem(user.getId(), europeanaId);
			response.success = true;
		} catch (DatabaseException e) {
			response.success = false;
			response.error = e.getMessage();
		}
		return JsonUtils.toJson(response, callback);
	}

	@RequestMapping(value = "/v2/user/saveditem.json", params = "!action", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.DELETE)
	public ModelAndView deleteRest(
			@RequestParam(value = "itemid", required = false) Long itemId,
			@RequestParam(value = "europeanaid", required = false) String europeanaId,
			@RequestParam(value = "callback", required = false) String callback, 
			Principal principal) {
		return delete(itemId, europeanaId, callback, principal);
	}

	@RequestMapping(value = "/v2/user/saveditem.json", params = "action=DELETE", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
	public ModelAndView delete(
			@RequestParam(value = "itemid", required = false) Long itemId,
			@RequestParam(value = "europeanaid", required = false) String europeanaId,
			@RequestParam(value = "callback", required = false) String callback, 
			Principal principal) {
		User user = userService.findByEmail(principal.getName());
		UserModification response = new UserModification(getApiId(principal), "/v2/user/saveditem.json?action=DELETE");
		if (user != null) {
			try {
				response.success = true;
				if (itemId != null) {
					userService.removeSavedItem(user.getId(), itemId);
				} else {
					if (StringUtils.isNotBlank(europeanaId)) {
						userService.removeSavedItem(user.getId(), europeanaId);
					} else {
						response.success = false;
						response.error = "Invalid arguments";
					}
				}
			} catch (DatabaseException e) {
				response.success = false;
				response.error = e.getMessage();
			}
		}
		return JsonUtils.toJson(response, callback);
	}

}
