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

package eu.europeana.api2.web.controller.mydata;

import eu.europeana.api2.utils.JsonUtils;
import eu.europeana.api2.v2.model.json.ModificationConfirmation;
import eu.europeana.api2.v2.model.json.UserResults;
import eu.europeana.api2.v2.model.json.user.SavedItem;
import eu.europeana.api2.v2.web.swagger.SwaggerSelect;
import eu.europeana.api2.web.controller.abstracts.AbstractUserController;
import eu.europeana.corelib.db.exception.DatabaseException;
import eu.europeana.corelib.definitions.db.entity.relational.User;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 */
@Controller
@SwaggerSelect
@Api(value = "my_data", description = " ")
public class MyDataItemController extends AbstractUserController {

    @ApiOperation(value = "lets the user list their data items", nickname = "listMyDataItems")
    @RequestMapping(
            value = "/mydata/saveditem",
            produces = MediaType.APPLICATION_JSON_VALUE,
            method = RequestMethod.GET
    )
    public ModelAndView list(
            @RequestParam(value = "europeanaid", required = false) String europeanaId,
            @RequestParam(value = "callback", required = false) String callback,
            Principal principal) {
        UserResults<SavedItem> response = new UserResults<>(principal.getName());
        try {
            User user = getUserByApiId(principal.getName());
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
                response.error = "Invalid credentials";
            }
        } catch (DatabaseException e) {
            response.success = false;
            response.error = e.getMessage();
        }
        return JsonUtils.toJson(response, callback);
    }


    @ApiOperation(value = "lets the user create a new data item", nickname = "createMyDataItem")
    @RequestMapping(
            value = "/mydata/saveditem",
            produces = MediaType.APPLICATION_JSON_VALUE,
            method = RequestMethod.POST
    )
    public ModelAndView create(
            @RequestParam(value = "europeanaid", required = false) String europeanaId,
            @RequestParam(value = "callback", required = false) String callback,
            Principal principal) {
        ModificationConfirmation response = new ModificationConfirmation(principal.getName());
        try {
            User user = getUserByApiId(principal.getName());
            if (user != null) {
                userService.createSavedItem(user.getId(), europeanaId);
                response.success = true;
            } else {
                response.success = false;
                response.error = "Invalid credentials";
            }
        } catch (DatabaseException e) {
            response.success = false;
            response.error = e.getMessage();
        }
        return JsonUtils.toJson(response, callback);
    }

    @ApiOperation(value = "lets the user delete a data item", nickname = "deleteMyDataItem")
    @RequestMapping(
            value = "/mydata/saveditem",
            produces = MediaType.APPLICATION_JSON_VALUE,
            method = RequestMethod.DELETE
    )
    public ModelAndView delete(
            @RequestParam(value = "itemid", required = false) Long itemId,
            @RequestParam(value = "europeanaid", required = false) String europeanaId,
            @RequestParam(value = "callback", required = false) String callback,
            Principal principal) {
        ModificationConfirmation response = new ModificationConfirmation(principal.getName());
        try {
            User user = getUserByApiId(principal.getName());
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
