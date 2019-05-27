package eu.europeana.api2.web.controller.user;

import eu.europeana.api2.utils.JsonUtils;
import eu.europeana.api2.v2.model.json.ModificationConfirmation;
import eu.europeana.api2.v2.model.json.UserResults;
import eu.europeana.api2.v2.model.json.user.Search;
import eu.europeana.api2.web.controller.abstracts.AbstractUserController;
import eu.europeana.corelib.db.exception.DatabaseException;
import eu.europeana.corelib.definitions.db.entity.relational.ApiKey;
import eu.europeana.corelib.definitions.db.entity.relational.SavedSearch;
import eu.europeana.corelib.definitions.db.entity.relational.User;
import eu.europeana.corelib.web.service.EuropeanaUrlService;
import eu.europeana.corelib.web.utils.UrlBuilder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 * @deprecated 2018-01-09 old MyEuropeana functionality*
 */
@Controller
@RequestMapping(value = "/user/savedsearch")
@Api(value = "my_europeana")
@Deprecated
public class UserSearchController extends AbstractUserController {

    private static final Logger LOG = Logger.getLogger(UserSearchController.class);

    @Resource(name = "corelib_web_europeanaUrlService")
    private EuropeanaUrlService europeanaUrlService;

    @ApiOperation(value = "list a user's saved searches", nickname = "listUserSearches")
    @RequestMapping(
            produces = MediaType.APPLICATION_JSON_VALUE,
            method = RequestMethod.GET
    )
    public ModelAndView list(
            @RequestParam(value = "callback", required = false) String callback,
            Principal principal) {
        UserResults<Search> response = new UserResults<>(getApiId(principal));
        User user = getUserByPrincipal(principal);
        if (user != null) {
            response.items = new ArrayList<>();
            response.username = user.getUserName();
            response.itemsCount = (long) user.getSavedSearches().size();
            for (SavedSearch item : user.getSavedSearches()) {
                Search search = new Search();
                search.id = item.getId();
                search.query = item.getQuery();
                search.queryString = item.getQueryString();
                search.dateSaved = item.getDateSaved();
                response.items.add(search);
            }
        } else {
            response.success = false;
            response.error = "User profile not retrievable...";
        }
        return JsonUtils.toJson(response, callback);
    }

    @ApiOperation(value = "create a new saved search for a user", nickname = "createUserSearch")
    @RequestMapping(
            produces = MediaType.APPLICATION_JSON_VALUE,
            method = RequestMethod.POST
    )
    public ModelAndView create(
            @RequestParam(value = "query", required = true) String query,
            @RequestParam(value = "qf", required = false) String[] refinements,
            @RequestParam(value = "start", required = false, defaultValue = "1") String start,
            @RequestParam(value = "callback", required = false) String callback,
            Principal principal) {
        ModificationConfirmation response = new ModificationConfirmation(getApiId(principal));
        try {
            User user = getUserByPrincipal(principal);
            if (user != null) {
                String queryString = getQueryString(query, refinements, start, user);
                userService.createSavedSearch(user.getId(), query, queryString);
                response.success = true;
            } else {
                response.success = false;
                response.error = "User Profile not retrievable...";
            }
        } catch (DatabaseException e) {
            LOG.error("Error creating saved search", e);
            response.success = false;
            response.error = e.getMessage();
        }
        return JsonUtils.toJson(response, callback);
    }

    @ApiOperation(value = "update a saved search for a user", nickname = "updateUserSearch")
    @RequestMapping(
            value = "/{searchId}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            method = RequestMethod.PUT
    )
    public ModelAndView update(
            @PathVariable(value = "searchId") Long searchId,
            @RequestParam(value = "query", required = true) String query,
            @RequestParam(value = "qf", required = false) String[] refinements,
            @RequestParam(value = "start", required = false, defaultValue = "1") String start,
            @RequestParam(value = "callback", required = false) String callback,
            Principal principal) {
        ModificationConfirmation response = new ModificationConfirmation(getApiId(principal));
        try {
            User user = getUserByPrincipal(principal);
            if (user != null) {
                String queryString = getQueryString(query, refinements, start, user);
                userService.updateSavedSearch(user.getId(), searchId, query, queryString);
                response.success = true;
            } else {
                response.success = false;
                response.error = "User Profile not retrievable...";
            }
        } catch (DatabaseException e) {
            LOG.error("Error updating saved search", e);
            response.success = false;
            response.error = e.getMessage();
        }
        return JsonUtils.toJson(response, callback);
    }

    @ApiOperation(value = "delete a user's saved search", nickname = "deleteUserSearch")
    @RequestMapping(
            value = "/{searchId}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            method = RequestMethod.DELETE
    )
    public ModelAndView delete(
            @PathVariable Long searchId,
            @RequestParam(value = "callback", required = false) String callback,
            Principal principal) {
        ModificationConfirmation response = new ModificationConfirmation(getApiId(principal));
        try {
            User user = getUserByPrincipal(principal);
            if (user != null) {
                userService.removeSavedSearch(user.getId(), searchId);
                response.success = true;
            } else {
                response.success = false;
                response.error = "User Profile not retrievable...";
            }
        } catch (DatabaseException e) {
            LOG.error("Error deleting saved search", e);
            response.success = false;
            response.error = e.getMessage();
        }
        return JsonUtils.toJson(response, callback);
    }

    private String getQueryString(String query, String[] refinements, String start, User user) {
        List<ApiKey> apiKeys = apiKeyService.findByEmail(user.getEmail());
        String firstKey = "";
        if (!apiKeys.isEmpty()) {
            firstKey = apiKeys.get(0).getId();
        }
        UrlBuilder ub = null;
        try {
            String resultRowCount = "50";
            ub = europeanaUrlService.getApi2SearchJson(firstKey, query, resultRowCount);
            ub.addParam("qf", refinements, true);
            ub.addParam("start", start, true);
            return StringUtils.replace(ub.toString(), "?", "&", 0);
        } catch (UnsupportedEncodingException e) {
            LOG.error("Error retrieving saved search", e);
        }
        return null;
    }
}
