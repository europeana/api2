package eu.europeana.api2demo.web.controller;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import eu.europeana.api2demo.web.service.Api2UserService;
import eu.europeana.corelib.definitions.db.entity.relational.SavedItem;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Resource
	private Api2UserService api2UserService;
	
	@RequestMapping("/favorites")
	public ModelAndView favorites() {
		List<SavedItem> favs = api2UserService.getFavorites();
		return null;
	}

}
