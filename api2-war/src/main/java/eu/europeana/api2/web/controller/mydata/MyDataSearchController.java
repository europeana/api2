package eu.europeana.api2.web.controller.mydata;

import eu.europeana.api2.utils.JsonUtils;
import eu.europeana.api2.v2.model.json.ModificationConfirmation;
import eu.europeana.api2.v2.model.json.UserResults;
import eu.europeana.api2.v2.model.json.user.Search;
import eu.europeana.api2.web.controller.abstracts.AbstractUserController;
import eu.europeana.corelib.db.exception.DatabaseException;
import eu.europeana.corelib.definitions.db.entity.relational.SavedSearch;
import eu.europeana.corelib.definitions.db.entity.relational.User;
import eu.europeana.corelib.web.utils.UrlBuilder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.security.Principal;
import java.util.ArrayList;

/**
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 * @deprecated 2018-01-09 old MyEuropeana functionality
 */
@Controller
@Api(value = "my_data", description = " ")
@Deprecated
public class MyDataSearchController extends AbstractUserController {

    private static final Logger LOG = LogManager.getLogger(MyDataSearchController.class);

    @ApiOperation(value = "lets the user list their saved searches", nickname = "listMySavedSearches")
    @RequestMapping(
            value = "/mydata/savedsearch",
            produces = MediaType.APPLICATION_JSON_VALUE,
            method = RequestMethod.GET
    )
    public ModelAndView list(
            @RequestParam(value = "callback", required = false) String callback,
            Principal principal) {
        UserResults<Search> response = new UserResults<>(principal.getName());
        LOG.info("Principal: {}", principal.toString());
        User user = getUserByApiId(principal.getName());
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
            response.error = "Invalid credentials";
        }
        return JsonUtils.toJson(response, callback);
    }

    @ApiOperation(value = "lets the user create a new saved search", nickname = "createMySavedSearch")
    @RequestMapping(
            value = "/mydata/savedsearch",
            produces = MediaType.APPLICATION_JSON_VALUE,
            method = RequestMethod.POST
    )
    public ModelAndView create(
            @RequestParam(value = "query", required = true) String query,
            @RequestParam(value = "qf", required = false) String[] refinements,
            @RequestParam(value = "start", required = false, defaultValue = "1") String start,
            @RequestParam(value = "callback", required = false) String callback, Principal principal) {
        ModificationConfirmation response = new ModificationConfirmation(principal.getName());
        try {
            User user = getUserByApiId(principal.getName());
            if (user != null) {
                UrlBuilder ub = new UrlBuilder(query);
                ub.addParam("qf", refinements, true);
                ub.addParam("start", start, true);
                String queryString = StringUtils.replace(ub.toString(), "?", "&");
                userService.createSavedSearch(user.getId(), query, queryString);
                response.success = true;
            }
        } catch (DatabaseException e) {
            response.success = false;
            response.error = e.getMessage();
        }
        return JsonUtils.toJson(response, callback);
    }

    @ApiOperation(value = "lets the user delete a saved search", nickname = "deleteMySavedSearch")
    @RequestMapping(
            value = "/mydata/savedsearch",
            produces = MediaType.APPLICATION_JSON_VALUE,
            method = RequestMethod.DELETE)
    public ModelAndView delete(
            @RequestParam(value = "searchid", required = true) Long searchId,
            @RequestParam(value = "callback", required = false) String callback, Principal principal) {
        ModificationConfirmation response = new ModificationConfirmation(principal.getName());
        try {
            User user = getUserByApiId(principal.getName());
            if (user != null) {
                userService.removeSavedSearch(user.getId(), searchId);
                response.success = true;
            }
        } catch (DatabaseException e) {
            response.success = false;
            response.error = e.getMessage();
        }
        return JsonUtils.toJson(response, callback);
    }

}
