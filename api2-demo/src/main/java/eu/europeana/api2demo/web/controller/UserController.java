package eu.europeana.api2demo.web.controller;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import eu.europeana.api2demo.web.model.UserFavorites;
import eu.europeana.api2demo.web.model.UserTags;
import eu.europeana.api2demo.web.service.Api2UserService;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Resource
	private Api2UserService api2UserService;
	
	@RequestMapping(value = "/favorites", params="!action")
	public ModelAndView favorites() {
		UserFavorites userFavs = api2UserService.getFavorites();
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("favs", userFavs.items);
		model.put("username", userFavs.username);
		return new ModelAndView("user/favorites", model);
	}
	
	@RequestMapping(value = "/favorites", params="action=DELETE", method=RequestMethod.GET)
	public ModelAndView favoritesDelete(
		@RequestParam(value = "id", required = true) Long objectId
	) {
		api2UserService.deleteFavorite(objectId);
		return favorites();
	}
	
	@RequestMapping(value = "/tags", params="!action")
	public ModelAndView tags() {
		UserTags userTags = api2UserService.getTags();
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("tags", userTags.items);
		model.put("username", userTags.username);
		return new ModelAndView("user/tags", model);
	}
	
	@RequestMapping(value = "/tags", params="action=DELETE", method=RequestMethod.GET)
	public ModelAndView tagsDelete(
		@RequestParam(value = "id", required = true) Long objectId
	) {
		api2UserService.deleteTag(objectId);
		return favorites();
	}


}
