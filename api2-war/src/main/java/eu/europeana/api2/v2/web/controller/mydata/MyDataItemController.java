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
import java.util.HashSet;
import java.util.Set;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
import eu.europeana.api2.v2.web.swagger.SwaggerSelect;
import eu.europeana.api2.v2.web.swagger.SwaggerIgnore;
import eu.europeana.corelib.db.exception.DatabaseException;
import eu.europeana.corelib.definitions.db.entity.relational.ApiKey;
import eu.europeana.corelib.definitions.db.entity.relational.User;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 */
@Controller
//@SwaggerSelect
//@Api(value = "my_europeana", description = " ")
public class MyDataItemController extends AbstractUserController {

    
	/**
	 * @param europeanaId
	 * @param callback
     * @param principal
	 * @return the JSON response
	 */
    @SwaggerIgnore
	@RequestMapping(value = "/v2/mydata/saveditem.json", params = "!action", produces = MediaType.APPLICATION_JSON_VALUE
			, method = RequestMethod.GET)
	public ModelAndView defaultAction(
			@RequestParam(value = "europeanaid", required = false) String europeanaId,
			@RequestParam(value = "callback", required = false) String callback,
			Principal principal) {
		return list(europeanaId, callback, principal);
	}

	/**
	 * @param europeanaId
	 * @param callback
     * @param principal
	 * @return the JSON response
	 */
    @ApiOperation(value = "lets the user list their data items", nickname = "listMyDataItems")
	@RequestMapping(value = "/v2/mydata/saveditem.json", params = "action=LIST", produces = MediaType.APPLICATION_JSON_VALUE
			, method = RequestMethod.GET)
	public ModelAndView list(
			@RequestParam(value = "europeanaid", required = false) String europeanaId,
			@RequestParam(value = "callback", required = false) String callback,
			Principal principal) {
		UserResults<SavedItem> response = new UserResults<>(principal.getName(), "/v2/mydata/saveditem.json");
		try {
			ApiKey apiKey = apiKeyService.findByID(principal.getName());
			if (apiKey != null) {
				User user = apiKey.getUser();
				response.items = new ArrayList<>();
				response.username = user.getUserName();
				Set<eu.europeana.corelib.definitions.db.entity.relational.SavedItem> results;
				if (StringUtils.isBlank(europeanaId)) {
					results = user.getSavedItems();
				} else {
					results = new HashSet<>();
					results.add(userService.findSavedItemByEuropeanaId(user.getId(), europeanaId));
				}
				response.itemsCount = (long) results.size();
				for (eu.europeana.corelib.definitions.db.entity.relational.SavedItem item : results) {
					SavedItem fav = new SavedItem();
					copyUserObjectData(response.apikey, fav, item);
					fav.author = item.getAuthor();
					response.items.add(fav);
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


	/**
	 * @param europeanaId
	 * @param callback
     * @param principal
	 * @return the JSON response
	 */
    @ApiOperation(value = "lets the user create a new data item", nickname = "createMyDataItem")
	@RequestMapping(value = "/v2/mydata/saveditem.json", produces = MediaType.APPLICATION_JSON_VALUE
			, method = { RequestMethod.POST, RequestMethod.PUT })
	public ModelAndView createRest(
			@RequestParam(value = "europeanaid", required = false) String europeanaId,
			@RequestParam(value = "callback", required = false) String callback, 
			Principal principal) {
		return create(europeanaId, callback, principal);
	}

	/**
	 * @param europeanaId
	 * @param callback
     * @param principal
	 * @return the JSON response
	 */
	@SwaggerIgnore
	@RequestMapping(value = "/v2/mydata/saveditem.json", params = "action=CREATE", produces = MediaType.APPLICATION_JSON_VALUE)
	public ModelAndView create(
			@RequestParam(value = "europeanaid", required = false) String europeanaId,
			@RequestParam(value = "callback", required = false) String callback, 
			Principal principal) {
		UserModification response = new UserModification(principal.getName(), "/v2/mydata/saveditem.json?action=CREATE");
		try {
			ApiKey apiKey = apiKeyService.findByID(principal.getName());
			if (apiKey != null) {
				User user = apiKey.getUser();
				userService.createSavedItem(user.getId(), europeanaId);
				response.success = true;
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

	/**
	 * @param itemId
	 * @param europeanaId
	 * @param callback
     * @param principal
	 * @return the JSON response
	 */
    @ApiOperation(value = "lets the user delete a data item", nickname = "deleteMyDataItem")
	@RequestMapping(value = "/v2/mydata/saveditem.json", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.DELETE)
	public ModelAndView deleteRest(
			@RequestParam(value = "itemid", required = false) Long itemId,
			@RequestParam(value = "europeanaid", required = false) String europeanaId,
			@RequestParam(value = "callback", required = false) String callback, 
			Principal principal) {
		return delete(itemId, europeanaId, callback, principal);
	}

	/**
	 * @param itemId
	 * @param europeanaId
	 * @param callback
     * @param principal
	 * @return the JSON response
	 */
	@SwaggerIgnore
	@RequestMapping(value = "/v2/mydata/saveditem.json", params = "action=DELETE", produces = MediaType.APPLICATION_JSON_VALUE)
	public ModelAndView delete(
			@RequestParam(value = "itemid", required = false) Long itemId,
			@RequestParam(value = "europeanaid", required = false) String europeanaId,
			@RequestParam(value = "callback", required = false) String callback, 
			Principal principal) {
		UserModification response = new UserModification(principal.getName(), "/v2/mydata/saveditem.json?action=DELETE");
		try {
			ApiKey apiKey = apiKeyService.findByID(principal.getName());
			if (apiKey != null) {
				User user = apiKey.getUser();
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
			}
		} catch (DatabaseException e) {
			response.success = false;
			response.error = e.getMessage();
		}
		return JsonUtils.toJson(response, callback);
	}
	
}
