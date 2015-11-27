/*
 * Copyright 2007-2015 The Europeana Foundation
 *
 * Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 * by the European Commission;
 * You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 * http://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 * any kind, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under
 * the Licence.
 */

package eu.europeana.api2.web.controller.user;

import eu.europeana.api2.utils.JsonUtils;
import eu.europeana.api2.v2.model.json.ModificationConfirmation;
import eu.europeana.api2.v2.model.json.UserResults;
import eu.europeana.api2.v2.model.json.user.Tag;
import eu.europeana.api2.v2.web.swagger.SwaggerSelect;
import eu.europeana.api2.web.controller.abstracts.AbstractUserController;
import eu.europeana.corelib.db.entity.relational.custom.TagCloudItem;
import eu.europeana.corelib.db.exception.DatabaseException;
import eu.europeana.corelib.definitions.db.entity.relational.SocialTag;
import eu.europeana.corelib.definitions.db.entity.relational.User;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import static eu.europeana.corelib.utils.EuropeanaUriUtils.createEuropeanaId;

/**
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 */
@Controller
@RequestMapping(value = "/user/tag")
@Api(value = "my_europeana", description = " ")
@SwaggerSelect
public class UserTagController extends AbstractUserController {

    @ApiOperation(value = "lists a user's data tags", nickname = "listUserTags")
    @RequestMapping(
            produces = MediaType.APPLICATION_JSON_VALUE,
            method = RequestMethod.GET
    )
    public ModelAndView list(
            @RequestParam(value = "tag", required = false) String tagFilter,
            @RequestParam(value = "callback", required = false) String callback,
            Principal principal) {
        return list(null, tagFilter, callback, principal);
    }

    @ApiOperation(value = "lists a user's data tags", nickname = "listUserTags")
    @RequestMapping(
            value = "/{collectionId}/{recordId}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            method = RequestMethod.GET
    )
    public ModelAndView list(
            @PathVariable String collectionId,
            @PathVariable String recordId,
            @RequestParam(value = "tag", required = false) String tagFilter,
            @RequestParam(value = "callback", required = false) String callback,
            Principal principal) {
        return list(createEuropeanaId(collectionId, recordId), tagFilter, callback, principal);
    }

    private ModelAndView list(
            String europeanaId,
            String tagFilter,
            String callback,
            Principal principal) {
        UserResults<Tag> response = new UserResults<>(getApiId(principal));
        try {
            User user = userService.findByEmail(principal.getName());
            if (user != null) {
                response.items = new ArrayList<>();
                response.username = user.getUserName();
                List<SocialTag> tags;
                if (StringUtils.isNotBlank(tagFilter)) {
                    tags = userService.findSocialTagsByTag(user.getId(), tagFilter);
                } else if (StringUtils.isNotBlank(europeanaId)) {
                    tags = userService.findSocialTagsByEuropeanaId(user.getId(), europeanaId);
                } else {
                    tags = new ArrayList<>(user.getSocialTags());
                }
                response.itemsCount = (long) tags.size();
                for (SocialTag item : tags) {
                    Tag tag = new Tag();
                    copyUserObjectData(response.apikey, tag, item);
                    tag.tag = item.getTag();
                    response.items.add(tag);
                }
            } else {
                response.success = false;
                response.error = "User Profile not retrievable...";
            }
        } catch (DatabaseException e) {
            response.success = false;
            response.error = e.getMessage();
        }
        return JsonUtils.toJson(response, callback);
    }

    @ApiOperation(value = "shows the user's tag cloud", nickname = "showUsersTagcloud")
    @RequestMapping(
            value = "/cloud",
            produces = MediaType.APPLICATION_JSON_VALUE,
            method = RequestMethod.GET
    )
    public ModelAndView listDistinct(
            @RequestParam(value = "callback", required = false) String callback,
            Principal principal) {
        UserResults<TagCloudItem> response = new UserResults<>(getApiId(principal));
        try {
            User user = userService.findByEmail(principal.getName());
            if (user != null) {
                response.items = userService.createSocialTagCloud(user.getId());
                response.itemsCount = (long) response.items.size();
                response.success = true;
                response.username = user.getUserName();
            } else {
                response.success = false;
                response.error = "User Profile not retrievable...";
            }
        } catch (DatabaseException e) {
            response.success = false;
            response.error = e.getMessage();
        }
        return JsonUtils.toJson(response, callback);
    }

    @ApiOperation(value = "creates a new data tag for a user", nickname = "createUserTag")
    @RequestMapping(
            value = "/{collectionId}/{recordId}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            method = RequestMethod.POST
    )
    public ModelAndView create(
            @PathVariable String collectionId,
            @PathVariable String recordId,
            @RequestParam(value = "tag", required = true) String tag,
            @RequestParam(value = "callback", required = false) String callback,
            Principal principal) {
        User user = userService.findByEmail(principal.getName());
        ModificationConfirmation response = new ModificationConfirmation(getApiId(principal));
        if (user != null) {
            try {
                userService.createSocialTag(user.getId(), createEuropeanaId(collectionId, recordId), tag);
                response.success = true;
            } catch (DatabaseException e) {
                response.success = false;
                response.error = e.getMessage();
            }
        } else {
            response.success = false;
            response.error = "User Profile not retrievable...";
        }
        return JsonUtils.toJson(response, callback);
    }

    @ApiOperation(value = "deletes a data tag for a user", nickname = "deleteUserTag")
    @RequestMapping(
            produces = MediaType.APPLICATION_JSON_VALUE,
            method = RequestMethod.DELETE
    )
    public ModelAndView delete(
            @RequestParam(value = "tag", required = true) String tag,
            @RequestParam(value = "callback", required = false) String callback,
            Principal principal) {
        return delete((Long) null, tag, null, callback, principal);
    }

    @ApiOperation(value = "deletes a data tag for a user", nickname = "deleteUserTag")
    @RequestMapping(
            value = "/{tagId}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            method = RequestMethod.DELETE
    )
    public ModelAndView delete(
            @PathVariable Long tagId,
            @RequestParam(value = "callback", required = false) String callback,
            Principal principal) {
        return delete(tagId, null, null, callback, principal);
    }

    @ApiOperation(value = "deletes a data tag for a user", nickname = "deleteUserTag")
    @RequestMapping(
            value = "/{collectionId}/{recordId}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            method = RequestMethod.DELETE
    )
    public ModelAndView delete(
            @PathVariable String collectionId,
            @PathVariable String recordId,
            @RequestParam(value = "tag", required = false) String tag,
            @RequestParam(value = "callback", required = false) String callback,
            Principal principal) {
        return delete((Long) null, tag, createEuropeanaId(collectionId, recordId), callback, principal);
    }

    private ModelAndView delete(Long tagId,
                                String tag,
                                String europeanaId,
                                String callback,
                                Principal principal) {
        User user = userService.findByEmail(principal.getName());
        ModificationConfirmation response = new ModificationConfirmation(getApiId(principal));
        if (user != null) {
            try {
                if (tagId != null) {
                    userService.removeSocialTag(user.getId(), tagId);
                } else {
                    if (StringUtils.isNotBlank(europeanaId) || StringUtils.isNotBlank(tag)) {
                        userService.removeSocialTag(user.getId(), europeanaId, tag);
                    }
                }
                response.success = true;
            } catch (DatabaseException e) {
                response.success = false;
                response.error = e.getMessage();
            }
        } else {
            response.success = false;
            response.error = "User Profile not retrievable...";
        }
        return JsonUtils.toJson(response, callback);
    }

}
