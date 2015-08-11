package eu.europeana.api2demo.web.controller;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import eu.europeana.api2demo.web.model.TagCloud;
import eu.europeana.api2demo.web.model.UserProfile;
import eu.europeana.api2demo.web.model.UserSavedItems;
import eu.europeana.api2demo.web.model.UserSearches;
import eu.europeana.api2demo.web.model.UserTags;
import eu.europeana.api2demo.web.service.Api2UserService;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Resource
	private Api2UserService api2UserService;
	
	@RequestMapping("/")
	public ModelAndView profile() {
		UserProfile profile = api2UserService.getProfile();
		Map<String, Object> model = new HashMap<>();
		model.put("profile", profile);
		return new ModelAndView("user/profile", model);
	}
	
	@RequestMapping(value = "/saveditems", params="!action")
	public ModelAndView items() {
		UserSavedItems userSavedItems = api2UserService.getSavedItems();
		Map<String, Object> model = new HashMap<>();
		model.put("items", userSavedItems.items);
		model.put("username", userSavedItems.username);
		return new ModelAndView("user/saveditems", model);
	}
	
	@RequestMapping(value = "/saveditems", params="action=DELETE", method=RequestMethod.GET)
	public ModelAndView itemsDelete(
		@RequestParam(value = "id", required = true) Long objectId
	) {
		api2UserService.deleteSavedItem(objectId);
		return items();
	}
	
	@RequestMapping(value = "/tags", params="!action")
	public ModelAndView tags(
			@RequestParam(value = "tag", required = false) String tag
			) {
		UserTags userTags = api2UserService.getTags(tag);
		Map<String, Object> model = new HashMap<>();
		model.put("items", userTags.items);
		model.put("username", userTags.username);
		return new ModelAndView("user/tags", model);
	}
	
	@RequestMapping(value = "/tagcloud", params="!action")
	public ModelAndView tagCloud() {
		TagCloud tagCloud = api2UserService.createTagCloud();
		Map<String, Object> model = new HashMap<>();
		model.put("items", tagCloud.items);
		model.put("username", tagCloud.username);
		return new ModelAndView("user/tagcloud", model);
	}
	
	
	@RequestMapping(value = "/tags", params="action=DELETE", method=RequestMethod.GET)
	public ModelAndView tagsDelete(
		@RequestParam(value = "id", required = true) Long objectId
	) {
		api2UserService.deleteTag(objectId);
		return tags(null);
	}

	@RequestMapping(value = "/searches", params="!action")
	public ModelAndView searches() {
		UserSearches userSearches = api2UserService.getSavedSearches();
		Map<String, Object> model = new HashMap<>();
		model.put("items", userSearches.items);
		model.put("username", userSearches.username);
		return new ModelAndView("user/searches", model);
	}
	
	@RequestMapping(value = "/searches", params="action=DELETE", method=RequestMethod.GET)
	public ModelAndView searchesDelete(
		@RequestParam(value = "id", required = true) Long objectId
	) {
		api2UserService.deleteSavedSearch(objectId);
		return searches();
	}

}
