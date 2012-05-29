package eu.europeana.api2demo.web.service.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.web.client.RestOperations;

import eu.europeana.api2.web.model.json.UserModification;
import eu.europeana.api2demo.Config;
import eu.europeana.api2demo.web.model.UserFavorites;
import eu.europeana.api2demo.web.model.UserTags;
import eu.europeana.api2demo.web.service.Api2UserService;

public class Api2UserServiceImpl implements Api2UserService {

	private RestOperations restTemplate;
	
	public UserFavorites getFavorites() {
		InputStream is = new ByteArrayInputStream(restTemplate.getForObject(
				URI.create(Config.URI_FAVORITES_GET), byte[].class));
		try {
			return new ObjectMapper().readValue(is, UserFavorites.class);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public boolean createFavorite(String id) {
		InputStream is = new ByteArrayInputStream(restTemplate.getForObject(
				URI.create(Config.URI_FAVORITES_CREATE + id), byte[].class));
		try {
			UserModification response = new ObjectMapper().readValue(is, UserModification.class);
			return response.success;
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	@Override
	public boolean deleteFavorite(Long id) {
		InputStream is = new ByteArrayInputStream(restTemplate.getForObject(
				URI.create(Config.URI_FAVORITES_DELETE + id.toString()), byte[].class));
		try {
			UserModification response = new ObjectMapper().readValue(is, UserModification.class);
			return response.success;
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	@Override
	public UserTags getTags() {
		InputStream is = new ByteArrayInputStream(restTemplate.getForObject(
				URI.create(Config.URI_TAGS_GET), byte[].class));
		try {
			return new ObjectMapper().readValue(is, UserTags.class);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public boolean createTag(String id, String tag) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean deleteTag(Long id) {
		InputStream is = new ByteArrayInputStream(restTemplate.getForObject(
				URI.create(Config.URI_TAGS_DELETE + id.toString()), byte[].class));
		try {
			UserModification response = new ObjectMapper().readValue(is, UserModification.class);
			return response.success;
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public void setRestTemplate(OAuth2RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}
}
