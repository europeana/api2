package eu.europeana.api2demo.web.service.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;

import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.web.client.RestOperations;

import eu.europeana.api2demo.Config;
import eu.europeana.api2demo.web.model.UserFavorites;
import eu.europeana.api2demo.web.service.Api2UserService;
import eu.europeana.corelib.definitions.db.entity.relational.SavedItem;

public class Api2UserServiceImpl implements Api2UserService {

	private RestOperations restTemplate;
	
	public List<SavedItem> getFavorites() {
		InputStream photosJson = new ByteArrayInputStream(restTemplate.getForObject(
				URI.create(Config.URI_FAVORITES_GET), byte[].class));
		ObjectMapper mapper = new ObjectMapper();
		try {
			UserFavorites response = mapper.readValue(photosJson, UserFavorites.class);
			return response.items;
		} catch (JsonProcessingException e) {
		} catch (IOException e) {
		}
		return null;
	}
	
	public void setRestTemplate(OAuth2RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}
}
