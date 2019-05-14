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
import eu.europeana.api2.v2.model.json.user.SavedItem;
import eu.europeana.api2.web.controller.abstracts.AbstractUserController;
import eu.europeana.corelib.db.exception.DatabaseException;
import eu.europeana.corelib.definitions.db.entity.relational.User;
import eu.europeana.corelib.neo4j.exception.Neo4JException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static eu.europeana.corelib.utils.EuropeanaUriUtils.createEuropeanaId;


/**
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 * @deprecated 2018-01-09 old MyEuropeana functionality*
 */
@Controller
@RequestMapping(value = "/user/saveditem")
@Api(value = "my_europeana", description = " ")
@Deprecated
public class UserItemController extends AbstractUserController {

    private static final Logger LOG = LogManager.getLogger(UserItemController.class);

    @ApiOperation(value = "list a user's data items", nickname = "listUserItems")
    @RequestMapping(
            produces = MediaType.APPLICATION_JSON_VALUE,
            method = RequestMethod.GET
    )
    public ModelAndView list(
            @RequestParam(value = "callback", required = false) String callback,
            Principal principal) {
        return list(null, callback, principal);
    }

    @ApiOperation(value = "Check if a user has item saved", nickname = "listUserItems")
    @RequestMapping(
            value = "/{collectionId}/{recordId}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            method = RequestMethod.GET
    )
    public ModelAndView list(
            @PathVariable String collectionId,
            @PathVariable String recordId,
            @RequestParam(value = "callback", required = false) String callback,
            Principal principal) {
        return list(createEuropeanaId(collectionId, recordId), callback, principal);
    }

    private ModelAndView list(String europeanaId, String callback, Principal principal) {
        UserResults<SavedItem> response = new UserResults<>(getApiId(principal));
        try {
            User user = getUserByPrincipal(principal);
            if (user != null) {
                response.items = new ArrayList<>();
                response.username = user.getUserName();
                Set<eu.europeana.corelib.definitions.db.entity.relational.SavedItem> results;
                if (StringUtils.isBlank(europeanaId)) {
                    results = user.getSavedItems();
                } else {
                    results = new HashSet<>();
                    results.add(userService.findSavedItemByEuropeanaId(user.getId(), europeanaId));
                }
                response.itemsCount = (long) results.size();
                for (eu.europeana.corelib.definitions.db.entity.relational.SavedItem item : results) {
                    SavedItem fav = new SavedItem();
                    copyUserObjectData(response.apikey, fav, item);
                    fav.author = item.getAuthor();
                    response.items.add(fav);
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

    @ApiOperation(value = "create a new data item for a user", nickname = "createUserItem")
    @RequestMapping(
            value = "/{collectionId}/{recordId}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            method = RequestMethod.POST
    )
    public ModelAndView create(
            @PathVariable String collectionId,
            @PathVariable String recordId,
            @RequestParam(value = "callback", required = false) String callback,
            Principal principal) {
        ModificationConfirmation response = new ModificationConfirmation(getApiId(principal));
        try {
            User user = getUserByPrincipal(principal);
            userService.createSavedItem(user.getId(), createEuropeanaId(collectionId, recordId));
            response.success = true;
        } catch (DatabaseException e) {
            response.success = false;
            response.error = e.getMessage();
        } catch (Neo4JException e) {
            LOG.error("Neo4JException thrown: " + e.getMessage());
            LOG.error("Cause: " + e.getCause());
        }
        return JsonUtils.toJson(response, callback);
    }

    @ApiOperation(value = "deletes a user's data item", nickname = "deleteUserItem")
    @RequestMapping(
            value = "/{itemId}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            method = RequestMethod.DELETE
    )
    public ModelAndView delete(
            @PathVariable Long itemId,
            @RequestParam(value = "callback", required = false) String callback,
            Principal principal) {
        return delete(itemId, null, callback, principal);
    }

    @ApiOperation(value = "deletes a user's data item", nickname = "deleteUserItem")
    @RequestMapping(
            value = "/{collectionId}/{recordId}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            method = RequestMethod.DELETE
    )
    public ModelAndView delete(
            @PathVariable String collectionId,
            @PathVariable String recordId,
            @RequestParam(value = "callback", required = false) String callback,
            Principal principal) {
        return delete((Long) null, createEuropeanaId(collectionId, recordId), callback, principal);
    }

    private ModelAndView delete(Long itemId, String europeanaId, String callback, Principal principal) {
        ModificationConfirmation response = new ModificationConfirmation(getApiId(principal));
        try {
            User user = getUserByPrincipal(principal);
            if (user != null) {
                response.success = true;
                if (itemId != null) {
                    userService.removeSavedItem(user.getId(), itemId);
                } else {
                    if (StringUtils.isNotBlank(europeanaId)) {
                        userService.removeSavedItem(user.getId(), europeanaId);
                    } else {
                        response.success = false;
                        response.error = "Invalid arguments";
                    }
                }
            }
        } catch (DatabaseException e) {
            response.success = false;
            response.error = e.getMessage();
        }
        return JsonUtils.toJson(response, callback);
    }

}
