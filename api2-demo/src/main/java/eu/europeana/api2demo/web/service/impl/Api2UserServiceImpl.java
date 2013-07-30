package eu.europeana.api2demo.web.service.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.web.client.RestOperations;

import eu.europeana.api2.v2.model.json.UserModification;
import eu.europeana.api2demo.Config;
import eu.europeana.api2demo.web.model.TagCloud;
import eu.europeana.api2demo.web.model.UserSavedItems;
import eu.europeana.api2demo.web.model.UserSearches;
import eu.europeana.api2demo.web.model.UserTags;
import eu.europeana.api2demo.web.service.Api2UserService;

public class Api2UserServiceImpl implements Api2UserService {

	private RestOperations restTemplate;
	
	@Resource
	private Config config;
	
	@Override
	public UserSavedItems getSavedItems() {
		InputStream is = new ByteArrayInputStream(restTemplate.getForObject(
				URI.create(config.getUriSavedItemGet()), byte[].class));
		try {
			return new ObjectMapper().readValue(is, UserSavedItems.class);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public boolean createSavedItem(String id) {
		InputStream is = new ByteArrayInputStream(restTemplate.getForObject(
				URI.create(config.getUriSavedItemCreate() + id), byte[].class));
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
	public boolean deleteSavedItem(Long id) {
		InputStream is = new ByteArrayInputStream(restTemplate.getForObject(
				URI.create(config.getUriSavedItemDelete() + id.toString()), byte[].class));
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
	public UserTags getTags(String filter) {
		StringBuilder url = new StringBuilder(config.getUriTagsGet());
		if (StringUtils.isNotBlank(filter)) {
			url.append("?filter=").append(filter);
		}
		InputStream is = new ByteArrayInputStream(restTemplate.getForObject(
				URI.create(url.toString()), byte[].class));
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
	public TagCloud createTagCloud() {
		InputStream is = new ByteArrayInputStream(restTemplate.getForObject(
				URI.create(config.getUriTagsTagcloud()), byte[].class));
		try {
			return new ObjectMapper().readValue(is, TagCloud.class);
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
				URI.create(config.getUriTagsDelete() + id.toString()), byte[].class));
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
	public UserSearches getSavedSearches() {
		InputStream is = new ByteArrayInputStream(restTemplate.getForObject(
				URI.create(config.getUriSearchesGet()), byte[].class));
		try {
			return new ObjectMapper().readValue(is, UserSearches.class);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public boolean deleteSavedSearche(Long id) {
		InputStream is = new ByteArrayInputStream(restTemplate.getForObject(
				URI.create(config.getUriSearchesDelete() + id.toString()), byte[].class));
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
