package eu.europeana.api2demo.web.service.impl;

import eu.europeana.api2.v2.model.json.UserModification;
import eu.europeana.api2demo.Config;
import eu.europeana.api2demo.web.model.*;
import eu.europeana.api2demo.web.service.Api2UserService;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.web.client.RestOperations;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

public class Api2UserServiceImpl implements Api2UserService {

    private RestOperations restTemplate;

    @Resource
    private Config config;

    private <T> T readJson(String uri, Class<T> clazz) {
        InputStream is = new ByteArrayInputStream(restTemplate.getForObject(
                URI.create(uri), byte[].class));
        try {
            return new ObjectMapper().readValue(is, clazz);
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
        return result != null && result.success;
    }

    @Override
    public void deleteSavedItem(Long id) {
        restTemplate.delete(URI.create(config.getUriSavedItemDelete() + id.toString()));
    }

    @Override
    public UserTags getTags(String tag) {
        StringBuilder url = new StringBuilder(config.getUriTagsGet());
        if (StringUtils.isNotBlank(tag)) {
            url.append("?tag=").append(tag);
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
        return result != null && result.success;
    }

    @Override
    public UserSearches getSavedSearches() {
        return readJson(config.getUriSearchesGet(), UserSearches.class);
    }

    @Override
    public boolean deleteSavedSearch(Long id) {
        UserModification result = readJson(config.getUriSearchesDelete() + id.toString(), UserModification.class);
        return result != null && result.success;
    }

    public void setRestTemplate(RestOperations restTemplate) {
        this.restTemplate = restTemplate;
    }
}
