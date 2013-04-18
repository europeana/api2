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

import javax.annotation.Resource;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.europeana.api2.model.json.abstracts.ApiResponse;
import eu.europeana.api2.v2.model.json.UserResults;
import eu.europeana.api2.v2.model.json.user.Tag;
import eu.europeana.api2.v2.web.controller.abstracts.AbstractUserController;
import eu.europeana.corelib.db.service.UserService;
import eu.europeana.corelib.definitions.db.entity.relational.SocialTag;
import eu.europeana.corelib.definitions.db.entity.relational.User;

/**
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 */
@Controller
public class UserTagController extends AbstractUserController {

	@Resource(name = "corelib_db_userService")
	private UserService userService;

	@RequestMapping(value = "/user/tag.json", params = "!action", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	ApiResponse defaultAction(
			@RequestParam(value = "objectid", required = false) String objectId,
			Principal principal) {
		return list(objectId, principal);
	}

	@RequestMapping(value = "/user/tag.json", params = "action=LIST", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	ApiResponse list(
			@RequestParam(value = "objectid", required = false) String objectId,
			Principal principal) {
		User user = userService.findByEmail(principal.getName());
		if (user != null) {
			UserResults<Tag> result = new UserResults<Tag>(getApiId(principal),
					"/user/tag.json");
			result.items = new ArrayList<Tag>();
			result.username = user.getUserName();
			for (SocialTag item : user.getSocialTags()) {
				Tag tag = new Tag();
				copyUserObjectData(tag, item);
				tag.tag = item.getTag();
				result.items.add(tag);
			}
			return result;
		}
		return null;
	}

	@RequestMapping(value = "/user/tag.json", params = "!action", produces = MediaType.APPLICATION_JSON_VALUE, method = {
			RequestMethod.POST, RequestMethod.PUT })
	public @ResponseBody
	ApiResponse createRest(
			@RequestParam(value = "objectid", required = true) String objectId,
			@RequestParam(value = "tag", required = true) String tag,
			Principal principal) {
		return create(objectId, tag, principal);
	}

	@RequestMapping(value = "/user/tag.json", params = "action=CREATE", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	ApiResponse create(
			@RequestParam(value = "objectid", required = true) String objectId,
			@RequestParam(value = "tag", required = true) String tag,
			Principal principal) {
		return null;
	}

	@RequestMapping(value = "/user/tag.json", params = "!action", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.DELETE)
	public @ResponseBody
	ApiResponse deleteRest(
			@RequestParam(value = "objectid", required = false) Long objectId,
			Principal principal) {
		return delete(objectId, principal);
	}

	@RequestMapping(value = "/user/tag.json", params = "action=DELETE", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	ApiResponse delete(
			@RequestParam(value = "tagid", required = true) Long tagId,
			Principal principal) {
		return null;
	}

}
