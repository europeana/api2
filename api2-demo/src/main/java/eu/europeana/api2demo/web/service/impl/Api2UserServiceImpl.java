package eu.europeana.api2demo.web.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.api2.v2.model.json.ModificationConfirmation;
import eu.europeana.api2demo.Config;
import eu.europeana.api2demo.web.model.*;
import eu.europeana.api2demo.web.service.Api2UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestOperations;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

@Service
public class Api2UserServiceImpl implements Api2UserService {

    @Resource(name = "myEuropeanaRestTemplate")
    private RestOperations restTemplate;

    @Resource
    private Config config;

    private <T> T readJson(String uri, Class<T> clazz) {

        try (InputStream is = new ByteArrayInputStream(restTemplate.getForObject(
                URI.create(uri), byte[].class))) {
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
        ModificationConfirmation result = readJson(config.getUriSavedItemCreate() + id, ModificationConfirmation.class);
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
        ModificationConfirmation result = readJson(config.getUriTagsDelete() + id.toString(), ModificationConfirmation.class);
        return result != null && result.success;
    }

    @Override
    public UserSearches getSavedSearches() {
        return readJson(config.getUriSearchesGet(), UserSearches.class);
    }

    @Override
    public boolean deleteSavedSearch(Long id) {
        ModificationConfirmation result = readJson(config.getUriSearchesDelete() + id.toString(), ModificationConfirmation.class);
        return result != null && result.success;
    }
}
