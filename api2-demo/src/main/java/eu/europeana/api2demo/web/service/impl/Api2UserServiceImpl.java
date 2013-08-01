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
import eu.europeana.api2demo.web.model.UserProfile;
import eu.europeana.api2demo.web.model.UserSavedItems;
import eu.europeana.api2demo.web.model.UserSearches;
import eu.europeana.api2demo.web.model.UserTags;
import eu.europeana.api2demo.web.service.Api2UserService;

public class Api2UserServiceImpl implements Api2UserService {

	private RestOperations restTemplate;
	
	@Resource
	private Config config;
	
	private <T> T readJson(String uri, Class<T> clazz) {
		InputStream is = new ByteArrayInputStream(restTemplate.getForObject(
				URI.create(uri), byte[].class));
		try {
			return new ObjectMapper().readValue(is, clazz);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public UserProfile getProfile() {
		return readJson(config.getUriProfile(), UserProfile.class);
	}
	
	@Override
	public UserSavedItems getSavedItems() {
		return readJson(config.getUriSavedItemGet(), UserSavedItems.class);
	}
	
	@Override
	public boolean createSavedItem(String id) {
		UserModification result = readJson(config.getUriSavedItemCreate() + id, UserModification.class);
		return result != null ? result.success : false;
	}
	
	@Override
	public boolean deleteSavedItem(Long id) {
		UserModification result = readJson(config.getUriSavedItemDelete() + id.toString(), UserModification.class);
		return result != null ? result.success : false;
	}
	
	@Override
	public UserTags getTags(String filter) {
		StringBuilder url = new StringBuilder(config.getUriTagsGet());
		if (StringUtils.isNotBlank(filter)) {
			url.append("?filter=").append(filter);
		}
		return readJson(url.toString(), UserTags.class);
	}
	
	@Override
	public TagCloud createTagCloud() {
		return readJson(config.getUriTagsTagcloud(), TagCloud.class);
	}
	
	@Override
	public boolean createTag(String id, String tag) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean deleteTag(Long id) {
		UserModification result = readJson(config.getUriTagsDelete() + id.toString(), UserModification.class);
		return result != null ? result.success : false;
	}
	
	@Override
	public UserSearches getSavedSearches() {
		return readJson(config.getUriSearchesGet(), UserSearches.class);
	}
	
	@Override
	public boolean deleteSavedSearche(Long id) {
		UserModification result = readJson(config.getUriSearchesDelete() + id.toString(), UserModification.class); 
		return result != null ? result.success : false;
	}
	
	public void setRestTemplate(OAuth2RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}
}
