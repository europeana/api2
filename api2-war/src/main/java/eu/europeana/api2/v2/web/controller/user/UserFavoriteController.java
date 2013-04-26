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

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import eu.europeana.api2.utils.JsonUtils;
import eu.europeana.api2.v2.model.json.UserModification;
import eu.europeana.api2.v2.model.json.UserResults;
import eu.europeana.api2.v2.model.json.user.Favorite;
import eu.europeana.api2.v2.web.controller.abstracts.AbstractUserController;
import eu.europeana.corelib.db.exception.DatabaseException;
import eu.europeana.corelib.definitions.db.entity.relational.SavedItem;
import eu.europeana.corelib.definitions.db.entity.relational.User;

/**
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 */
@Controller
public class UserFavoriteController extends AbstractUserController {

	@RequestMapping(value = "/user/favorite.json", params = "!action", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
	public ModelAndView defaultAction(
			@RequestParam(value = "callback", required = false) String callback,
			Principal principal) {
		return list(callback, principal);
	}

	@RequestMapping(value = "/user/favorite.json", params = "action=LIST", produces = MediaType.APPLICATION_JSON_VALUE)
	public ModelAndView list(
			@RequestParam(value = "callback", required = false) String callback,
			Principal principal) {
		User user = userService.findByEmail(principal.getName());
		if (user != null) {
			UserResults<Favorite> response = new UserResults<Favorite>(
					getApiId(principal), "/user/favorite.json");
			response.items = new ArrayList<Favorite>();
			response.username = user.getUserName();
			for (SavedItem item : user.getSavedItems()) {
				Favorite fav = new Favorite();
				copyUserObjectData(fav, item);
				fav.author = item.getAuthor();
				response.items.add(fav);
			}
			return JsonUtils.toJson(response, callback);
		}
		return null;
	}

	@RequestMapping(value = "/user/favorite.json", params = "!action", produces = MediaType.APPLICATION_JSON_VALUE, method = {
			RequestMethod.POST, RequestMethod.PUT })
	public ModelAndView createRest(
			@RequestParam(value = "objectid", required = false) String objectId,
			@RequestParam(value = "callback", required = false) String callback,
			Principal principal) {
		return create(objectId, callback, principal);
	}

	@RequestMapping(value = "/user/favorite.json", params = "action=CREATE", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
	public ModelAndView create(
			@RequestParam(value = "objectid", required = false) String objectId,
			@RequestParam(value = "callback", required = false) String callback,
			Principal principal) {
		User user = userService.findByEmail(principal.getName());
		UserModification response = new UserModification(getApiId(principal),
				"/user/favorite.json?action=CREATE");
		try {
			userService.createSavedItem(user.getId(), objectId);
			response.success = true;
		} catch (DatabaseException e) {
			response.success = false;
			response.error = e.getMessage();
		}
		return JsonUtils.toJson(response, callback);
	}

	@RequestMapping(value = "/user/favorite.json", params = "!action", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.DELETE)
	public ModelAndView deleteRest(
			@RequestParam(value = "objectid", required = false) Long objectId,
			@RequestParam(value = "callback", required = false) String callback,
			Principal principal) {
		return delete(objectId, callback, principal);
	}

	@RequestMapping(value = "/user/favorite.json", params = "action=DELETE", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
	public ModelAndView delete(
			@RequestParam(value = "objectid", required = false) Long favId,
			@RequestParam(value = "callback", required = false) String callback,
			Principal principal) {
		User user = userService.findByEmail(principal.getName());
		UserModification response = new UserModification(getApiId(principal),
				"/user/favorite.json?action=DELETE");
		if (user != null) {
			try {
				userService.removeSavedItem(user.getId(), favId);
				response.success = true;
			} catch (DatabaseException e) {
				response.success = false;
				response.error = e.getMessage();
			}
		}
		return JsonUtils.toJson(response, callback);
	}

}
