package eu.europeana.api2.v2.web.controller;

import eu.europeana.api2.model.json.ApiError;
import eu.europeana.api2.utils.JsonUtils;
import eu.europeana.api2.v2.model.json.QueryTranslationResult;
import eu.europeana.api2.v2.utils.ApiKeyUtils;
import eu.europeana.api2.v2.utils.ControllerUtils;
import eu.europeana.api2.v2.web.swagger.SwaggerSelect;
import eu.europeana.corelib.definitions.solr.model.QueryTranslation;
import eu.europeana.corelib.search.utils.SearchUtils;
import eu.europeana.corelib.utils.StringArrayUtils;
import eu.europeana.corelib.web.exception.EuropeanaException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

@Controller
@Api(tags = {"Search"})
public class QueryTranslationController {

    @Resource
    private ApiKeyUtils apiKeyUtils;

    private static final String ERROR_TERM = "Invalid parameter: term can not be empty";
    private static final String ERROR_LANGUAGE = "Invalid parameter: languageCodes can not be empty";

    @ApiOperation(value = "translate a term to different languages")
    @GetMapping(value = "/api/v2/translateQuery.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public ModelAndView translateQuery(
            @RequestParam(value = "term") String term,
            @RequestParam(value = "languageCodes") String[] languageCodes,
            @RequestParam(value = "wskey") String apikey,
            @RequestParam(value = "profile", required = false) String profile,
            @RequestParam(value = "callback", required = false) String callback,
            HttpServletRequest request,
            HttpServletResponse response) throws EuropeanaException {
        ControllerUtils.addResponseHeaders(response);

        languageCodes = StringArrayUtils.splitWebParameter(languageCodes);

        apiKeyUtils.validateApiKey(apikey);

        if (StringUtils.isBlank(term)) {
            response.setStatus(HttpStatus.SC_BAD_REQUEST);
            return JsonUtils.toJson(new ApiError(apikey, ERROR_TERM), callback);
        } else if (StringArrayUtils.isBlank(languageCodes)) {
            response.setStatus(HttpStatus.SC_BAD_REQUEST);
            return JsonUtils.toJson(new ApiError(apikey, ERROR_LANGUAGE), callback);
        }

        QueryTranslationResult queryTranslationResult =
                new QueryTranslationResult(apikey);

        QueryTranslation queryTranslation = SearchUtils.translateQuery(term, Arrays.asList(languageCodes));
        queryTranslationResult.translations = queryTranslation.getLanguageVersions();
        queryTranslationResult.translatedQuery = queryTranslation.getModifiedQuery();

        if (StringUtils.isNotBlank(profile) && StringUtils.containsIgnoreCase(profile, "param")) {
            queryTranslationResult.addParam("wskey", apikey);
            queryTranslationResult.addParam("term", term);
            queryTranslationResult.addParam("languageCodes", languageCodes);
            queryTranslationResult.addParam("profile", profile);
            queryTranslationResult.addParam("callback", callback);
        }

        return JsonUtils.toJson(queryTranslationResult, callback);
    }
}
