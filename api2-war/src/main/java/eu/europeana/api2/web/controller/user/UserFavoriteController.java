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
import eu.europeana.api2.web.model.json.user.Favorite;
import eu.europeana.corelib.db.exception.DatabaseException;
import eu.europeana.corelib.definitions.db.entity.relational.SavedItem;
import eu.europeana.corelib.definitions.db.entity.relational.User;

/**
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 */
@Controller
public class UserFavoriteController extends AbstractUserController {
	
	@RequestMapping(value = "/user/favorite.json", params="!action",  produces = MediaType.APPLICATION_JSON_VALUE, method=RequestMethod.GET)
	public @ResponseBody ApiResponse defaultAction(
		Principal principal
	) {
		return list(principal);
	}
	
	@RequestMapping(value = "/user/favorite.json", params="action=LIST",  produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ApiResponse list(
		Principal principal
	) {
		User user = userService.findByEmail(principal.getName());
		if (user != null) {
			UserResults<Favorite> result = new UserResults<Favorite>(getApiId(principal), "/user/favorite.json");
			result.items = new ArrayList<Favorite>();
			result.username = user.getUserName();
			for (SavedItem item : user.getSavedItems()) {
				Favorite fav = new Favorite();
				copyUserObjectData(fav, item);
				fav.title = item.getTitle();
				result.items.add(fav);
			}
			return result;
		}
		return null;
	}
	
	@RequestMapping(value = "/user/favorite.json", params="!action",  produces = MediaType.APPLICATION_JSON_VALUE, method={RequestMethod.POST,RequestMethod.PUT})
	public @ResponseBody ApiResponse createRest(
		@RequestParam(value = "objectid", required = false) String objectId,
		Principal principal
	) {
		return create(objectId, principal);
	}
	
	@RequestMapping(value = "/user/favorite.json", params="action=CREATE",  produces = MediaType.APPLICATION_JSON_VALUE, method=RequestMethod.GET)
	public @ResponseBody ApiResponse create(
		@RequestParam(value = "objectid", required = false) String objectId,
		Principal principal
	) {
		User user = userService.findByEmail(principal.getName());
		UserModification response = new UserModification(getApiId(principal), "/user/favorite.json?action=CREATE");
		try {
			userService.createSavedItem(user.getId(), objectId);
			response.success = true;
		} catch (DatabaseException e) {
			response.success = false;
			response.error = e.getMessage();
		}
		return response;
	}
	
	@RequestMapping(value = "/user/favorite.json", params="!action",  produces = MediaType.APPLICATION_JSON_VALUE, method=RequestMethod.DELETE)
	public @ResponseBody ApiResponse deleteRest(
		@RequestParam(value = "objectid", required = false) Long objectId,
		Principal principal
	) {
		return delete(objectId, principal);
	}
	
	@RequestMapping(value = "/user/favorite.json", params="action=DELETE",  produces = MediaType.APPLICATION_JSON_VALUE, method=RequestMethod.GET)
	public @ResponseBody ApiResponse delete(
		@RequestParam(value = "objectid", required = false) Long objectId,
		Principal principal
	) {
		User user = userService.findByEmail(principal.getName());
		UserModification response = new UserModification(getApiId(principal), "/user/favorite.json?action=DELETE");
		if (user != null) {
			try {
				userService.removeSavedItem(user.getId(), objectId);
				response.success = true;
			} catch (DatabaseException e) {
				response.success = false;
				response.error = e.getMessage();
			}
		}
		return response;
	}
	
}
