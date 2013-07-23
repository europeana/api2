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
import java.util.List;

import javax.annotation.Resource;

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
import eu.europeana.api2.v2.model.json.user.Tag;
import eu.europeana.api2.v2.web.controller.abstracts.AbstractUserController;
import eu.europeana.corelib.db.entity.relational.custom.TagCloudItem;
import eu.europeana.corelib.db.exception.DatabaseException;
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

	@RequestMapping(value = "/v2/user/tag.json", params = "!action", produces = MediaType.APPLICATION_JSON_VALUE)
	public ModelAndView defaultAction(
			@RequestParam(value = "objectid", required = false) String objectId,
			@RequestParam(value = "filter", required = false) String filter,
			@RequestParam(value = "callback", required = false) String callback, 
			Principal principal) {
		return list(objectId, filter, callback, principal);
	}

	@RequestMapping(value = "/v2/user/tag.json", params = "action=LIST", produces = MediaType.APPLICATION_JSON_VALUE)
	public ModelAndView list(
			@RequestParam(value = "objectid", required = false) String objectId,
			@RequestParam(value = "filter", required = false) String filter,
			@RequestParam(value = "callback", required = false) String callback, 
			Principal principal) {
		User user = userService.findByEmail(principal.getName());
		if (user != null) {
			UserResults<Tag> response = new UserResults<Tag>(getApiId(principal), "/user/tag.json");
			response.items = new ArrayList<Tag>();
			response.username = user.getUserName();
			List<SocialTag> tags;
			try {
				if (StringUtils.isBlank(filter)) {
					tags = new ArrayList<SocialTag>(user.getSocialTags());
				} else {
					tags = userService.findSocialTagsByTag(user.getId(), filter);
				}
				for (SocialTag item : tags) {
					Tag tag = new Tag();
					copyUserObjectData(tag, item);
					tag.tag = item.getTag();
					response.items.add(tag);
				}
			} catch (DatabaseException e) {
				response.success = false;
				response.error = e.getMessage();
			}
			return JsonUtils.toJson(response, callback);
		}
		return null;
	}

	@RequestMapping(value = "/v2/user/tag.json", params = "action=TAGCLOUD", produces = MediaType.APPLICATION_JSON_VALUE)
	public ModelAndView listDistinct(@RequestParam(value = "callback", required = false) String callback, Principal principal) {
		User user = userService.findByEmail(principal.getName());
		if (user != null) {
			UserResults<TagCloudItem> response = new UserResults<TagCloudItem>(getApiId(principal), "/user/tag.json");
			try {
				response.items = userService.createSocialTagCloud(user.getId());
				response.success = true;
			} catch (DatabaseException e) {
				response.success = false;
				response.error = e.getMessage();
			}
			response.username = user.getUserName();
			return JsonUtils.toJson(response, callback);
		}
		return null;
	}
	
	@RequestMapping(value = "/v2/user/tag.json", params = "!action", produces = MediaType.APPLICATION_JSON_VALUE, method = {
			RequestMethod.POST, RequestMethod.PUT })
	public ModelAndView createRest(@RequestParam(value = "objectid", required = true) String objectId,
			@RequestParam(value = "tag", required = true) String tag,
			@RequestParam(value = "callback", required = false) String callback, Principal principal) {
		return create(objectId, tag, callback, principal);
	}

	@RequestMapping(value = "/v2/user/tag.json", params = "action=CREATE", produces = MediaType.APPLICATION_JSON_VALUE)
	public ModelAndView create(@RequestParam(value = "objectid", required = true) String objectId,
			@RequestParam(value = "tag", required = true) String tag,
			@RequestParam(value = "callback", required = false) String callback, Principal principal) {
		User user = userService.findByEmail(principal.getName());
		UserModification response = new UserModification(getApiId(principal), "/user/tag.json?action=CREATE");
		if (user != null) {
			try {
				userService.createSocialTag(user.getId(), objectId, tag);
				response.success = true;
			} catch (DatabaseException e) {
				response.success = false;
				response.error = e.getMessage();
			}
		}
		return JsonUtils.toJson(response, callback);
	}

	@RequestMapping(value = "/v2/user/tag.json", params = "!action", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.DELETE)
	public ModelAndView deleteRest(@RequestParam(value = "tagid", required = false) Long tagId,
			@RequestParam(value = "callback", required = false) String callback, Principal principal) {
		return delete(tagId, callback, principal);
	}

	@RequestMapping(value = "/v2/user/tag.json", params = "action=DELETE", produces = MediaType.APPLICATION_JSON_VALUE)
	public ModelAndView delete(@RequestParam(value = "tagid", required = true) Long tagId,
			@RequestParam(value = "callback", required = false) String callback, Principal principal) {
		User user = userService.findByEmail(principal.getName());
		UserModification response = new UserModification(getApiId(principal), "/user/tag.json?action=DELETE");
		if (user != null) {
			try {
				userService.removeSocialTag(user.getId(), tagId);
				response.success = true;
			} catch (DatabaseException e) {
				response.success = false;
				response.error = e.getMessage();
			}
		}
		return JsonUtils.toJson(response, callback);
	}

}
