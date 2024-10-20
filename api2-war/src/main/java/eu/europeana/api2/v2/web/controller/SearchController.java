package eu.europeana.api2.v2.web.controller;

import static eu.europeana.api2.v2.utils.ModelUtils.findAllFacetsInTag;

import eu.europeana.api.commons.web.exception.HttpException;
import eu.europeana.api.search.syntax.field.FieldDeclaration;
import eu.europeana.api.search.syntax.field.FieldMode;
import eu.europeana.api.search.syntax.field.FieldRegistry;
import eu.europeana.api.search.syntax.utils.Constants;
import eu.europeana.api.search.syntax.utils.ParserUtils;
import eu.europeana.api.translation.definitions.exceptions.InvalidLanguageException;
import eu.europeana.api.translation.definitions.language.Language;
import eu.europeana.api2.model.utils.Api2UrlService;
import eu.europeana.api2.utils.JsonUtils;
import eu.europeana.api2.utils.SolrEscape;
import eu.europeana.api2.utils.XmlUtils;
import eu.europeana.api2.v2.exceptions.DateMathParseException;
import eu.europeana.api2.v2.exceptions.InvalidAuthorizationException;
import eu.europeana.api2.v2.exceptions.InvalidParamValueException;
import eu.europeana.api2.v2.exceptions.InvalidRangeOrGapException;
import eu.europeana.api2.v2.exceptions.MissingParamException;
import eu.europeana.api2.v2.exceptions.TranslationServiceDisabledException;
import eu.europeana.api2.v2.exceptions.TranslationServiceNotAvailableException;
import eu.europeana.api2.v2.model.GeoDistance;
import eu.europeana.api2.v2.model.SearchRequest;
import eu.europeana.api2.v2.model.enums.Profile;
import eu.europeana.api2.v2.model.json.SearchResults;
import eu.europeana.api2.v2.model.json.view.ApiView;
import eu.europeana.api2.v2.model.json.view.BriefView;
import eu.europeana.api2.v2.model.json.view.RichView;
import eu.europeana.api2.v2.model.json.view.submodel.Facet;
import eu.europeana.api2.v2.model.translate.MultilingualQueryGenerator;
import eu.europeana.api2.v2.model.xml.kml.KmlResponse;
import eu.europeana.api2.v2.model.xml.rss.Channel;
import eu.europeana.api2.v2.model.xml.rss.Item;
import eu.europeana.api2.v2.model.xml.rss.RssResponse;
import eu.europeana.api2.v2.service.FacetWrangler;
import eu.europeana.api2.v2.service.HitMaker;
import eu.europeana.api2.v2.service.RouteDataService;
import eu.europeana.api2.v2.service.translate.TranslationService;
import eu.europeana.api2.v2.utils.ApiKeyUtils;
import eu.europeana.api2.v2.utils.BoostParamUtils;
import eu.europeana.api2.v2.utils.ControllerUtils;
import eu.europeana.api2.v2.utils.FacetParameterUtils;
import eu.europeana.api2.v2.utils.LanguageFilter;
import eu.europeana.api2.v2.utils.ModelUtils;
import eu.europeana.api2.v2.utils.ProfileUtils;
import eu.europeana.api2.v2.utils.TagUtils;
import eu.europeana.api2.v2.web.swagger.SwaggerIgnore;
import eu.europeana.api2.v2.web.swagger.SwaggerSelect;
import eu.europeana.corelib.definitions.edm.beans.ApiBean;
import eu.europeana.corelib.definitions.edm.beans.BriefBean;
import eu.europeana.corelib.definitions.edm.beans.IdBean;
import eu.europeana.corelib.definitions.edm.beans.RichBean;
import eu.europeana.corelib.definitions.solr.model.Query;
import eu.europeana.corelib.edm.exceptions.SolrQueryException;
import eu.europeana.corelib.edm.utils.CountryUtils;
import eu.europeana.corelib.search.model.ResultSet;
import eu.europeana.corelib.search.utils.SearchUtils;
import eu.europeana.corelib.solr.bean.impl.ApiBeanImpl;
import eu.europeana.corelib.solr.bean.impl.BriefBeanImpl;
import eu.europeana.corelib.solr.bean.impl.IdBeanImpl;
import eu.europeana.corelib.solr.bean.impl.RichBeanImpl;
import eu.europeana.corelib.utils.StringArrayUtils;
import eu.europeana.corelib.web.exception.EuropeanaException;
import eu.europeana.corelib.web.exception.ProblemType;
import eu.europeana.corelib.web.model.rights.RightReusabilityCategorizer;
import eu.europeana.corelib.web.utils.RequestUtils;
import eu.europeana.indexing.solr.facet.FacetEncoder;
import eu.europeana.indexing.solr.facet.value.AudioDuration;
import eu.europeana.indexing.solr.facet.value.AudioQuality;
import eu.europeana.indexing.solr.facet.value.ImageAspectRatio;
import eu.europeana.indexing.solr.facet.value.ImageColorEncoding;
import eu.europeana.indexing.solr.facet.value.ImageColorSpace;
import eu.europeana.indexing.solr.facet.value.ImageSize;
import eu.europeana.indexing.solr.facet.value.MimeTypeEncoding;
import eu.europeana.indexing.solr.facet.value.VideoDuration;
import eu.europeana.indexing.solr.facet.value.VideoQuality;
import eu.europeana.metis.schema.model.MediaType;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.DataFormatException;
import javax.annotation.Nullable;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.response.FacetField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;



/**
 * Controller that handles all search requests (search.json, opensearch.rss, search.rss, and search.kml)
 *
 * @author Willem-Jan Boogerd
 * @author Luthien
 * @author Patrick Ehlert
 */
@Controller
@SwaggerSelect
@Api(tags = {"Search"})
public class SearchController extends BaseController {

    private static final Logger LOG                       = LogManager.getLogger(SearchController.class);
    private static final String FACET_RANGE               = "facet.range";
    private static final String UTF8                      = "UTF-8";

    // First pattern is country with value between quotes, second pattern is with value without quotes (ending with &,
    // space or end of string)
    private static final Pattern COUNTRY_PATTERN = Pattern.compile("COUNTRY:\"(.*?)\"|COUNTRY:(.*?)(&|\\s|$)");
    public static final String ASTERISK = "*";

    @Resource
    private Api2UrlService urlService;

    @Resource(name = "api2_mvc_xmlUtils")
    private XmlUtils xmlUtils;

    @Value("${api.search.rowLimit}")
    private Integer apiRowLimit;

    @Value("${api.search.hl.MaxAnalyzedChars}")
    private String hlMaxAnalyzedChars;

    @Value("#{europeanaProperties['translation.search.query']}")
    private Boolean queryTranslationEnabled;

    @Value("#{europeanaProperties['translation.search.results']}")
    private Boolean resultsTranslationEnabled;

    private MultilingualQueryGenerator queryGenerator;
    private TranslationService searchResultTranslator;

    @Autowired
    public SearchController(RouteDataService routeService, MultilingualQueryGenerator queryGenerator,
                            @Nullable TranslationService searchResultTranslator) {
        super(routeService);
        this.queryGenerator = queryGenerator;
        this.searchResultTranslator = searchResultTranslator;
        if (queryTranslationEnabled == null) {
            queryTranslationEnabled = false;
        }
        if (resultsTranslationEnabled == null) {
            resultsTranslationEnabled = false;
        }
    }

    /**
     * Returns a list of Europeana datasets based on the search terms.
     * The response is an Array of JSON objects, each one containing the identifier and the name of a dataset.
     *
     * @return the JSON response
     */
    @ApiOperation(value = "search for records post", nickname = "searchRecordsPost", response = Void.class)
    @PostMapping(value = {"/api/v2/search.json", "/record/v2/search.json", "/record/search.json"},
                 produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                 consumes = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    public ModelAndView searchJsonPost(
                                       @RequestBody SearchRequest searchRequest,
                                       HttpServletRequest request,
                                       HttpServletResponse response)
            throws EuropeanaException, HttpException, InvalidLanguageException {
        return searchJsonGet(
                             searchRequest.getQuery(),
                             searchRequest.getQf(),
                             searchRequest.getReusability(),
                             StringUtils.join(searchRequest.getProfile(), ","),
                             searchRequest.getStart(),
                             searchRequest.getRows(),
                             searchRequest.getFacet(),
                             searchRequest.getTheme(),
                             StringUtils.join(searchRequest.getSort(), ","),
                             searchRequest.getColourPalette(),
                             searchRequest.isThumbnail(),
                             searchRequest.isMedia(),
                             searchRequest.isTextFulltext(),
                             searchRequest.isLandingPage(),
                             searchRequest.getCursor(),
                             searchRequest.getCallback(),
                             searchRequest.getHit().getFl(),
                             searchRequest.getHit().getSelectors(),
                             null, // TODO for now we set sourceLang and targetLang to null for POSTS until we decide how this will work officially
                             null,
                             null,
                             searchRequest.getBoost(),
                             request,
                             response);
    }


    /**
     * Returns a list of Europeana datasets based on the search terms.
     * The response is an Array of JSON objects, each one containing the identifier and the name of a dataset.
     *
     * @return the JSON response
     */
    @ApiOperation(value = "search for records", nickname = "searchRecords", response = Void.class)
    @GetMapping(value = {"/api/v2/search.json", "/record/v2/search.json", "/record/search.json"},
        produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    public ModelAndView searchJsonGet(
        @SolrEscape @RequestParam(value = "query") String queryString,
        @RequestParam(value = "qf", required = false) String[] refinementArray,
        @RequestParam(value = "reusability", required = false) String[] reusabilityArray,
        @RequestParam(value = "profile", required = false, defaultValue = "standard")
        String profile,
        @RequestParam(value = "start", required = false, defaultValue = "1") int start,
        @RequestParam(value = "rows", required = false, defaultValue = "12") int rows,
        @RequestParam(value = "facet", required = false) String[] mixedFacetArray,
        @RequestParam(value = "theme", required = false) String theme,
        @RequestParam(value = "sort", required = false) String sort,
        @RequestParam(value = "colourpalette", required = false)
        String[] colourPaletteArray,
        @RequestParam(value = "thumbnail", required = false) Boolean thumbnail,
        @RequestParam(value = "media", required = false) Boolean media,
        @RequestParam(value = "text_fulltext", required = false) Boolean fullText,
        @RequestParam(value = "landingpage", required = false) Boolean landingPage,
        @RequestParam(value = "cursor", required = false) String cursorMark,
        @RequestParam(value = "callback", required = false) String callback,
        @SolrEscape @RequestParam(value = "hit.fl", required = false) String hlFl,
        @RequestParam(value = "hit.selectors", required = false) String hlSelectors,
        @RequestParam(value = "q.source", required = false) String querySourceLang,
        @RequestParam(value = "q.target", required = false) String queryTargetLang,
        @RequestParam(value = "lang", required = false) String lang,
        @RequestParam(value = "boost", required = false) String boostParam,
        HttpServletRequest request,
        HttpServletResponse response)
        throws EuropeanaException, HttpException {
        
        // debugging internal routes for API-gateway-routed requests
        LOG.trace("Incoming request comes from server: |{}|", request.getServerName());
        
        // get the profiles
        Set<Profile> profiles = ProfileUtils.getProfiles(profile);

        if (profiles.contains(Profile.TRANSLATE) && getAuthorizationHeader(request) == null) {
            throw new InvalidAuthorizationException();
        }

        String apiKey = ApiKeyUtils.extractApiKeyFromAuthorization(verifyReadAccess(request));

        // check query parameter
        if (StringUtils.isBlank(queryString)) {
            throw new SolrQueryException(ProblemType.SEARCH_QUERY_EMPTY);
        }

        // validate boost Param
        BoostParamUtils.validateBoostParam(boostParam);

        // validate provided languages
        List<Language> filterLanguages = null;
        if (lang != null) {
            try {
                filterLanguages = Language.validateMultiple(lang);
            } catch (InvalidLanguageException e) {
                throw new InvalidParamValueException(e.getMessage());

            }
        }

        boolean isTranslateProfileActive = profiles.contains(Profile.TRANSLATE);
        // fail fast if user is requesting translations when translation service is not enabled
        // note that we'll ignore when query translations or results translations is disabled
        if (isTranslateProfileActive && (
            (queryTranslationEnabled && queryGenerator == null) ||
                (resultsTranslationEnabled && !searchResultTranslator.isEnabled()))) {
            throw new TranslationServiceDisabledException();
        }
        queryString = queryString.trim();

        // append the boost value in the query
        if (StringUtils.isNotEmpty(boostParam)) {
            queryString = boostParam + queryString;
        }

        queryString = fixCountryCapitalization(queryString);

        // #579 rights URL's don't match well to queries containing ":https*"
        queryString = queryString.replace(":https://", ":http://");
        LOG.debug("ORIGINAL QUERY: |{}|", queryString);

        if (queryTranslationEnabled && isTranslateProfileActive && StringUtils.isNotBlank(
            queryTargetLang)) {
            validateQueryTranslateParams(querySourceLang, queryTargetLang);
            // generate multi-lingual search query
            try {
                queryString = queryGenerator.getMultilingualQuery(queryString, queryTargetLang,
                    querySourceLang, getAuthorizationHeader(request));
                LOG.debug("TRANSLATED QUERY: |{}|", queryString);
            } catch (TranslationServiceNotAvailableException e) {
                // EA-3463 - return 307 redirect without profile param and Keep the Error Response
                // Body indicating the reason for troubleshooting
                ControllerUtils.redirectForTranslationsLimitException(request, response, profiles);
                // throwing exception again overwrites the exception message with problem type message. Hence, fetch the original message from cause
                throw new TranslationServiceNotAvailableException(e.getCause().getMessage(), e);
            }

        }
        boolean isMinimalProfileActive = profiles.contains(Profile.MINIMAL);
        //StringUtils.containsIgnoreCase(profile, Profile.MINIMAL.getName());

        String translateTargetLang = null;
        if (resultsTranslationEnabled && isTranslateProfileActive && isMinimalProfileActive) {
            if (filterLanguages == null || filterLanguages.isEmpty()) {
                try {
                    Language.validateSingle(null); // let that method throw appropriate error
                } catch (InvalidLanguageException e) {
                    throw new InvalidParamValueException(e.getMessage());
                }
            }
            translateTargetLang = filterLanguages.get(0).name()
                .toLowerCase(Locale.ROOT); // only use first provided language for translations
        }

        //Add Validation For Cursormark
        if (cursorMark != null) {
            if( (start > 1)) {
                throw new SolrQueryException(ProblemType.SEARCH_START_AND_CURSOR,
                    "Parameters 'start' and 'cursorMark' cannot be used together");
            }
            //If the cursor value other than * is provided then it needs to be Base64 Encoded
            if (!ASTERISK.equals(cursorMark) && !ControllerUtils.isBase64Encoded(cursorMark)) {
                  new SolrQueryException(ProblemType.SEARCH_CURSORMARK_INVALID,
                    "Please make sure you encode the cursor value before sending it to the API.");
            }
        }


        // TODO April '22 - this issue is now over 11 years old and I'm quite certain that we can stop checking this
        // TODO check whether this is still necessary? <= about time we did that!
        // workaround of a Spring issue
        // (https://jira.springsource.org/browse/SPR-7963)
        String[] qfArray = request.getParameterMap().get("qf");
        if (qfArray != null && qfArray.length != refinementArray.length) {
            refinementArray = qfArray;
        }

        if (StringUtils.isNotBlank(theme)) {
            if (StringUtils.containsAny(theme, "+ #%^&*-='\"<>`!@[]{}\\/|")) {
                throw new SolrQueryException(ProblemType.SEARCH_THEME_MULTIPLE);
            } else {
                refinementArray = ArrayUtils.add(refinementArray, "collection:" + theme);
            }
        }

        List<String> colourPalette = new ArrayList<>();
        if (ArrayUtils.isNotEmpty(colourPaletteArray)) {
            StringArrayUtils.addToList(colourPalette, colourPaletteArray);
        }
        colourPalette.replaceAll(String::toUpperCase);

        // Note that this is about the parameter 'colourpalette', not the refinement: they are processed below
        // [existing-query] AND [filter_tags-1 AND filter_tags-2 AND filter_tags-3 ... ]
        if (!colourPalette.isEmpty()) {
            Set<Integer> colourPaletteTags = TagUtils.encodeColourPalette(colourPalette);
            if (!colourPaletteTags.isEmpty()) {
                queryString = filterQueryBuilder(colourPaletteTags.iterator(), queryString, " AND ", false);
            }
        }

        final List<Integer> filterTags = new ArrayList<>();

        // EA-2996 this is to hold the sfield, pt and d geospatial parameters
        // Created here, passed to processQfParameters() & initialised there
        GeoDistance geoDistance = new GeoDistance();

        // NOTE the zero tag is now added in processQfParameters
        try {
            refinementArray = processQfParameters(refinementArray, media, thumbnail, fullText, landingPage, filterTags, geoDistance);
        } catch (InvalidParamValueException e) {
            throw new SolrQueryException(ProblemType.INVALID_PARAMETER_VALUE, e.getErrorDetails());
        }

        // add the CF filter facets to the query string like this:
        // [existing-query] AND ([filter_tags-1 OR filter_tags-2 OR filter_tags-3 ... ])
        if (!filterTags.isEmpty()) {
            queryString = filterQueryBuilder(filterTags.iterator(),
                queryString,
                " OR ",
                true);
        }

        String[] reusabilities = StringArrayUtils.splitWebParameter(reusabilityArray);
        String[] mixedFacets   = StringArrayUtils.splitWebParameter(mixedFacetArray);

        boolean rangeFacetsSpecified = request.getParameterMap().containsKey(FACET_RANGE);
        boolean noFacetsSpecified    = ArrayUtils.isEmpty(mixedFacets);
        boolean facetsRequested = profiles.contains(Profile.PORTAL) || profiles.contains(Profile.FACETS);

        boolean defaultFacetsRequested = facetsRequested && !rangeFacetsSpecified &&
            (noFacetsSpecified || ArrayUtils.contains(mixedFacets, "DEFAULT"));
        boolean defaultOrReusabilityFacetsRequested =
            defaultFacetsRequested || (facetsRequested && ArrayUtils.contains(mixedFacets, "REUSABILITY"));

        // 1) replaces DEFAULT (or empty list of) facet with those defined in the enum types (removes explicit DEFAULT facet)
        // 2) separates the requested facets in Solr facets and technical (fake-) facets
        // 3) when many custom SOLR facets are supplied: caps the number of total facets to FACET_LIMIT
        Map<String, String[]> separatedFacets = ModelUtils.separateAndLimitFacets(mixedFacets, defaultFacetsRequested);
        String[] solrFacets = ArrayUtils.addAll(separatedFacets.get("solrfacets"), separatedFacets.get("customfacets"));
        String[] technicalFacets = separatedFacets.get("technicalfacets");

        rows = Math.min(rows, apiRowLimit);

        Map<String, String> valueReplacements = null;
        if (ArrayUtils.isNotEmpty(reusabilities)) {
            valueReplacements = RightReusabilityCategorizer.mapValueReplacements(reusabilities, true);
            if (null != valueReplacements && !valueReplacements.isEmpty()) {
                refinementArray = ArrayUtils.addAll(refinementArray, "REUSABILITY:list");
            }
        }

        // EA-2996 only allow sorting on distance if a qf distance function is requested => if geoDistance is initialised
        if (geoDistance.isInitialised()){
            if (StringUtils.containsIgnoreCase(sort, "distance")) {
                sort = StringUtils.replaceIgnoreCase(sort, "distance", "geodist()");
            }
        } else if (StringUtils.containsIgnoreCase(sort, "distance")){
            // removes "distance", "distance asc", "distance desc" also when followed by other sort parameters,
            // including possible spaces and the trailing comma in those cases
            sort = org.apache.commons.lang3.RegExUtils.removePattern(sort, "distance\\s?(asc|desc)?(\\s|,)*");
        }

        Class<? extends IdBean> clazz = selectBean(profile);
        Query query = new Query(SearchUtils.rewriteQueryFields(
            SearchUtils.fixBuggySolrIndex(queryString)))
            .setApiQuery(true)
            .setRefinements(refinementArray)
            .setPageSize(rows)
            .setStart(start - 1)
            .setSort(sort)
            .setCurrentCursorMark(cursorMark)
            .setParameter("facet.mincount","1")
            .setParameter("fl", IdBeanImpl.getFields(getBeanImpl(clazz)))
            .setSpellcheckAllowed(false);

        // EA-2996
        if (geoDistance.isInitialised()){
            query.addGeoParamsToQuery(geoDistance.getSField(), geoDistance.getPoint(), geoDistance.getDistance());
        }

        if (facetsRequested) {
            Map<String, String[]> parameterMap = request.getParameterMap();
            try {
                query.setSolrFacets(solrFacets)
                    .setDefaultFacetsRequested(defaultFacetsRequested)
                    .convertAndSetSolrParameters(FacetParameterUtils.getSolrFacetParams("limit",
                        solrFacets,
                        parameterMap,
                        defaultFacetsRequested))
                    .convertAndSetSolrParameters(FacetParameterUtils.getSolrFacetParams("offset",
                        solrFacets,
                        parameterMap,
                        defaultFacetsRequested))
                    .setFacetDateRangeParameters(FacetParameterUtils.getDateRangeParams(parameterMap))
                    .setTechnicalFacets(technicalFacets)
                    .setTechnicalFacetLimits(FacetParameterUtils.getTechnicalFacetParams("limit",
                        technicalFacets,
                        parameterMap,
                        defaultFacetsRequested))
                    .setTechnicalFacetOffsets(FacetParameterUtils.getTechnicalFacetParams("offset",
                        technicalFacets,
                        parameterMap,
                        defaultFacetsRequested))
                    .setFacetsAllowed(true);
            } catch (DateMathParseException e) {
                String errorDetails = "Error parsing value '" + e.getParsing() + "' supplied for " + FACET_RANGE + " " +
                    e.getWhatsParsed();
                throw new SolrQueryException(ProblemType.SEARCH_FACET_RANGE_INVALID, errorDetails);
            } catch (InvalidRangeOrGapException e) {
                throw new SolrQueryException(ProblemType.SEARCH_FACET_RANGE_INVALID, e.getMessage());
            } catch (DataFormatException e) {
                throw new InvalidParamValueException(e.getMessage());
            }
        } else {
            query.setFacetsAllowed(false);
        }

        if (profiles.contains(Profile.HITS)) {
            int nrSelectors;
            if (StringUtils.isBlank(hlSelectors)) {
                nrSelectors = 1;
            } else {
                try {
                    nrSelectors = Integer.parseInt(hlSelectors);
                    if (nrSelectors < 1) {
                        throw new SolrQueryException(ProblemType.SEARCH_HITSELECTOR_INVALID,
                            "Parameter hit.selectors must be greater than 0");
                    }
                } catch (NumberFormatException nfe) {
                    throw new SolrQueryException(ProblemType.SEARCH_HITSELECTOR_INVALID,
                        "Parameter hit.selectors must be an integer");
                }
            }
            query.setParameter("hl", "on");
            query.setParameter("hl.fl", StringUtils.isBlank(hlFl) ? "fulltext.*" : hlFl);
            // this sets both the Solr parameter and a separate nrSelectors variable used to limit the result set with
            query.setNrSelectors("hl.snippets", nrSelectors);
            // see EA-1570 (workaround to increase the number of characters that are being considered for highlighting)
            query.setParameter("hl.maxAnalyzedChars", hlMaxAnalyzedChars);
        }

        if (null != valueReplacements && !valueReplacements.isEmpty()) {
            query.setValueReplacements(valueReplacements);
        }

        // reusability facet settings; spell check allowed, etcetera
        if (defaultOrReusabilityFacetsRequested) {
            query.setQueryFacets(RightReusabilityCategorizer.getQueryFacets());
        }
        if (profiles.contains(Profile.PORTAL) || profiles.contains(Profile.SPELLING)) {
            query.setSpellcheckAllowed(true);
        }
        if (facetsRequested && !query.hasParameter("f.DATA_PROVIDER.facet.limit") &&
            (ArrayUtils.contains(solrFacets, "DATA_PROVIDER") || ArrayUtils.contains(solrFacets, "DEFAULT"))) {
            query.setParameter("f.DATA_PROVIDER.facet.limit", FacetParameterUtils.getLimitForDataProvider());
        }

        SearchResults<? extends IdBean> result = createResults(apiKey, profiles, query, clazz, request.getServerName(),
            translateTargetLang, filterLanguages, request, response,true);

        if (profiles.contains(Profile.PARAMS)) {
            result.addParams(RequestUtils.getParameterMap(request), "apikey");
            result.addParam("profile", profile);
            result.addParam("start", start);
            result.addParam("rows", rows);
            result.addParam("sort", sort);
        }
        response.setCharacterEncoding(UTF8);
        response.addHeader("Allow", ControllerUtils.ALLOWED_GET_HEAD_POST);
        return JsonUtils.toJson(result, callback);
    }



    /** Search API V3
     * Returns a list of Europeana datasets based on the search terms.
     * The response is an Array of JSON objects, each one containing the identifier and the name of a dataset.
     *
     * @return the JSON response
     */
    @ApiOperation(value = "search for records V3", nickname = "searchRecordsV3", response = Void.class)
    @GetMapping(value = {"/record/v3/search.json"},
                produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    public ModelAndView searchJsonGetV3(
                                      @SolrEscape @RequestParam(value = "query") String queryString,
                                      @RequestParam(value = "qf", required = false) String[] refinementArray,
                                      @RequestParam(value = "reusability", required = false) String[] reusabilityArray,
                                      @RequestParam(value = "profile", required = false, defaultValue = "standard")
                                              String profile,
                                      @RequestParam(value = "start", required = false, defaultValue = "1") int start,
                                      @RequestParam(value = "rows", required = false, defaultValue = "12") int rows,
                                      @RequestParam(value = "facet", required = false) String[] mixedFacetArray,
                                      @RequestParam(value = "theme", required = false) String theme,
                                      @RequestParam(value = "sort", required = false) String sort,
                                      @RequestParam(value = "colourpalette", required = false)
                                              String[] colourPaletteArray,
                                      @RequestParam(value = "thumbnail", required = false) Boolean thumbnail,
                                      @RequestParam(value = "media", required = false) Boolean media,
                                      @RequestParam(value = "text_fulltext", required = false) Boolean fullText,
                                      @RequestParam(value = "landingpage", required = false) Boolean landingPage,
                                      @RequestParam(value = "cursor", required = false) String cursorMark,
                                      @RequestParam(value = "callback", required = false) String callback,
                                      @SolrEscape @RequestParam(value = "hit.fl", required = false) String hlFl,
                                      @RequestParam(value = "hit.selectors", required = false) String hlSelectors,
                                      @RequestParam(value = "q.source", required = false) String querySourceLang,
                                      @RequestParam(value = "q.target", required = false) String queryTargetLang,
                                      @RequestParam(value = "lang", required = false) String lang,
                                      @RequestParam(value = "boost", required = false) String boostParam,
                                      HttpServletRequest request,
                                      HttpServletResponse response)
            throws EuropeanaException, HttpException {

        // get the profiles
        Set<Profile> profiles = ProfileUtils.getProfiles(profile);
        if (profiles.contains(Profile.TRANSLATE) && getAuthorizationHeader(request) == null) {
            throw new InvalidAuthorizationException();
        }
        // check query parameter
        if (StringUtils.isBlank(queryString)) {
            throw new SolrQueryException(ProblemType.SEARCH_QUERY_EMPTY);
        }



        // validate boost Param
        BoostParamUtils.validateBoostParam(boostParam);
 
        String apiKey = ApiKeyUtils.extractApiKeyFromAuthorization(verifyReadAccess(request));
 
        // validate provided languages
        List<Language> filterLanguages = null;
        if (lang != null) {
            try {
                filterLanguages = Language.validateMultiple(lang);
            } catch (InvalidLanguageException e) {
                throw new InvalidParamValueException(e.getMessage());
            }
        }

        // EA 3657 - Start - New Parser Logic -load the registry before parsing
        ParserUtils.loadFieldRegistryFromResource(Constants.FIELD_REGISTRY_XML);
        ParserUtils.loadFunctionRegistry(Constants.FUNCTION_REGISTRY_XML);

        Map<String, List<String>> parsedQueryFilterParams = ParserUtils.getParsedParametersMap(request.getParameterMap().get("qf"));
        //EA 3657 -If qf parameter not populated and new qf parameter parsing is used geodistance parameters calculation is handled with parser.
        String sField = CollectionUtils.isNotEmpty(parsedQueryFilterParams.get("sfield")) ?parsedQueryFilterParams.get("sfield").get(0):null;
        String pt = CollectionUtils.isNotEmpty(parsedQueryFilterParams.get("pt"))?parsedQueryFilterParams.get("pt").get(0):null;
        String d = CollectionUtils.isNotEmpty(parsedQueryFilterParams.get("d"))?parsedQueryFilterParams.get("d").get(0):null;

        boolean isGeoSearchRequested =  (StringUtils.isNotBlank(sField) && StringUtils.isNotBlank(pt) && StringUtils.isNotBlank(d) );

        //EA 3657 - handle old conditions from EA-2996  ,sort parameter related to distance are removed if geodistance qf param not provided.
        if(!isGeoSearchRequested){
            RegExUtils.removePattern(sort, "distance\\s?(asc|desc)?(\\s|,)*");
        }

        //Validate sort param
        sort= validateAndUpdateSortParameters(sort);

        // If  Query filters are successfully parsed  , refinements division not required while calling SOLR
        List<String> fq_param =  parsedQueryFilterParams.get("parsed_param");
        boolean isRefinementDivisionRequired = CollectionUtils.isEmpty(fq_param);
        if (!isRefinementDivisionRequired) {
            refinementArray =  fq_param.toArray(new String[0]);
        }

        boolean isTranslateProfileActive = profiles.contains(Profile.TRANSLATE);
        // fail fast if user is requesting translations when translation service is not enabled
        // note that we'll ignore when query translations or results translations is disabled
        if (isTranslateProfileActive && (
            (queryTranslationEnabled && queryGenerator == null) ||
                (resultsTranslationEnabled && !searchResultTranslator.isEnabled()))) {
            throw new TranslationServiceDisabledException();
        }


        queryString = ParserUtils.getParsedParametersMap(queryString.trim()).getOrDefault("parsed_param",Collections.emptyList()).get(0);
        // append the boost value in the query
        if (StringUtils.isNotEmpty(boostParam)) {
            queryString = boostParam + queryString;
        }
        queryString = fixCountryCapitalization(queryString); 

        // #579 rights URL's don't match well to queries containing ":https*"
        queryString = queryString.replace(":https://", ":http://");
        LOG.debug("ORIGINAL QUERY: |{}|", queryString);
 
        if (queryTranslationEnabled && isTranslateProfileActive && StringUtils.isNotBlank(
            queryTargetLang)) {
            validateQueryTranslateParams(querySourceLang, queryTargetLang);
            // generate multi-lingual search query
            try {
                queryString = queryGenerator.getMultilingualQuery(queryString, queryTargetLang,
                    querySourceLang, getAuthorizationHeader(request));
                LOG.debug("TRANSLATED QUERY: |{}|", queryString);
            } catch (TranslationServiceNotAvailableException e) {
                // EA-3463 - return 307 redirect without profile param and Keep the Error Response
                // Body indicating the reason for troubleshooting
                ControllerUtils.redirectForTranslationsLimitException(request, response, profiles);
                // throwing exception again overwrites the exception message with problem type message. Hence, fetch the original message from cause
                throw new TranslationServiceNotAvailableException(e.getCause().getMessage(), e);
            }
        }
        boolean isMinimalProfileActive = profiles.contains(Profile.MINIMAL);
        //StringUtils.containsIgnoreCase(profile, Profile.MINIMAL.getName());
        String translateTargetLang = null;
        if (resultsTranslationEnabled && isTranslateProfileActive && isMinimalProfileActive) {
            if (filterLanguages == null || filterLanguages.isEmpty()) {
                try {
                    Language.validateSingle(null); // let that method throw appropriate error
                } catch (InvalidLanguageException e) {
                    throw new InvalidParamValueException(e.getMessage());
                }
            }
            translateTargetLang = filterLanguages.get(0).name()
                .toLowerCase(Locale.ROOT); // only use first provided language for translations
        }
       //Add Validation For Cursormark
        if (cursorMark != null) {
           if( (start > 1)) {
               throw new SolrQueryException(ProblemType.SEARCH_START_AND_CURSOR,
                   "Parameters 'start' and 'cursorMark' cannot be used together");
           }
           //If the cursor value other than * is provided then it needs to be Base64 Encoded
           if (!ASTERISK.equals(cursorMark) && !ControllerUtils.isBase64Encoded(cursorMark)) {
               throw new SolrQueryException(ProblemType.SEARCH_CURSORMARK_INVALID,
                   "Please make sure you encode the cursor value before sending it to the API.");
           }
         }


 
        // TODO April '22 - this issue is now over 11 years old and I'm quite certain that we can stop checking this
        // TODO check whether this is still necessary? <= about time we did that!
        // workaround of a Spring issue
        // (https://jira.springsource.org/browse/SPR-7963)
        String[] qfArray = request.getParameterMap().get("qf");
        if (qfArray != null && qfArray.length != refinementArray.length) {
            refinementArray = qfArray;
        }
        if (StringUtils.isNotBlank(theme)) {
            if (StringUtils.containsAny(theme, "+ #%^&*-='\"<>`!@[]{}\\/|")) {
                throw new SolrQueryException(ProblemType.SEARCH_THEME_MULTIPLE);
            } else {
                refinementArray = ArrayUtils.add(refinementArray, "collection:" + theme);
            }
        }
        List<String> colourPalette = new ArrayList<>();
        if (ArrayUtils.isNotEmpty(colourPaletteArray)) {
            StringArrayUtils.addToList(colourPalette, colourPaletteArray);
        }
        colourPalette.replaceAll(String::toUpperCase);
        // Note that this is about the parameter 'colourpalette', not the refinement: they are processed below
        // [existing-query] AND [filter_tags-1 AND filter_tags-2 AND filter_tags-3 ... ]
        if (!colourPalette.isEmpty()) {
            Set<Integer> colourPaletteTags = TagUtils.encodeColourPalette(colourPalette);
            if (!colourPaletteTags.isEmpty()) {
                queryString = filterQueryBuilder(colourPaletteTags.iterator(), queryString, " AND ", false);
            }
        }
        final List<Integer> filterTags = new ArrayList<>();
        // EA-2996 this is to hold the sfield, pt and d geospatial parameters
        // Created here, passed to processQfParameters() & initialised there
        GeoDistance geoDistance = new GeoDistance();
        // NOTE the zero tag is now added in processQfParameters
        try {
            refinementArray = processQfParameters(refinementArray, media, thumbnail, fullText, landingPage, filterTags, geoDistance);
        } catch (InvalidParamValueException e) {
            throw new SolrQueryException(ProblemType.INVALID_PARAMETER_VALUE, e.getErrorDetails());
        }
        // add the CF filter facets to the query string like this:
        // [existing-query] AND ([filter_tags-1 OR filter_tags-2 OR filter_tags-3 ... ])
        if (!filterTags.isEmpty()) {
            queryString = filterQueryBuilder(filterTags.iterator(),
                    queryString,
                    " OR ",
                    true);
        }
        String[] reusabilities = StringArrayUtils.splitWebParameter(reusabilityArray);
        String[] mixedFacets   = StringArrayUtils.splitWebParameter(mixedFacetArray);

        boolean rangeFacetsSpecified = request.getParameterMap().containsKey(FACET_RANGE);
        boolean noFacetsSpecified    = ArrayUtils.isEmpty(mixedFacets);
        boolean facetsRequested = profiles.contains(Profile.PORTAL) || profiles.contains(Profile.FACETS);

        boolean defaultFacetsRequested = facetsRequested && !rangeFacetsSpecified &&
                                         (noFacetsSpecified || ArrayUtils.contains(mixedFacets, "DEFAULT"));
        boolean defaultOrReusabilityFacetsRequested =
                defaultFacetsRequested || (facetsRequested && ArrayUtils.contains(mixedFacets, "REUSABILITY"));

        // 1) replaces DEFAULT (or empty list of) facet with those defined in the enum types (removes explicit DEFAULT facet)
        // 2) separates the requested facets in Solr facets and technical (fake-) facets
        // 3) when many custom SOLR facets are supplied: caps the number of total facets to FACET_LIMIT
        Map<String, String[]> separatedFacets = ModelUtils.separateAndLimitFacets(mixedFacets, defaultFacetsRequested);
        String[] solrFacets = ArrayUtils.addAll(separatedFacets.get("solrfacets"), separatedFacets.get("customfacets"));
        String[] technicalFacets = separatedFacets.get("technicalfacets");

        rows = Math.min(rows, apiRowLimit);

        Map<String, String> valueReplacements = null;
        if (ArrayUtils.isNotEmpty(reusabilities)) {
            valueReplacements = RightReusabilityCategorizer.mapValueReplacements(reusabilities, true);
            if (null != valueReplacements && !valueReplacements.isEmpty()) {
                refinementArray = ArrayUtils.addAll(refinementArray, "REUSABILITY:list");
            }
        }

        // EA-2996 only allow sorting on distance if a qf distance function is requested => if geoDistance is initialised
        if (geoDistance.isInitialised()){
            if (StringUtils.containsIgnoreCase(sort, "distance")) {
                sort = StringUtils.replaceIgnoreCase(sort, "distance", "geodist()");
            }
        } else if (StringUtils.containsIgnoreCase(sort, "distance")){
            // removes "distance", "distance asc", "distance desc" also when followed by other sort parameters,
            // including possible spaces and the trailing comma in those cases
            sort = RegExUtils.removePattern(sort, "distance\\s?(asc|desc)?(\\s|,)*");
        }


        Class<? extends IdBean> clazz = selectBean(profile);
        Query query = new Query(SearchUtils.rewriteQueryFields(
                SearchUtils.fixBuggySolrIndex(queryString)))
                                .setApiQuery(true)
                                .setRefinements(refinementArray)
                                .setPageSize(rows)
                                .setStart(start - 1)
                                .setSort(sort)
                                .setCurrentCursorMark(cursorMark)
                                .setParameter("facet.mincount","1")
                                .setParameter("fl", IdBeanImpl.getFields(getBeanImpl(clazz)))
                                .setSpellcheckAllowed(false);

        if(isGeoSearchRequested) {
            query.addGeoParamsToQuery(sField, pt, d);
        }
        // EA 3657 -End -New Parser Logic

        // EA-2996
        if (geoDistance.isInitialised()){
            query.addGeoParamsToQuery(geoDistance.getSField(), geoDistance.getPoint(), geoDistance.getDistance());
        }
        if (facetsRequested) {
            Map<String, String[]> parameterMap = request.getParameterMap();
            try {
                query.setSolrFacets(solrFacets)
                     .setDefaultFacetsRequested(defaultFacetsRequested)
                     .convertAndSetSolrParameters(FacetParameterUtils.getSolrFacetParams("limit",
                                                                                         solrFacets,
                                                                                         parameterMap,
                                                                                         defaultFacetsRequested))
                     .convertAndSetSolrParameters(FacetParameterUtils.getSolrFacetParams("offset",
                                                                                         solrFacets,
                                                                                         parameterMap,
                                                                                         defaultFacetsRequested))
                     .setFacetDateRangeParameters(FacetParameterUtils.getDateRangeParams(parameterMap))
                     .setTechnicalFacets(technicalFacets)
                     .setTechnicalFacetLimits(FacetParameterUtils.getTechnicalFacetParams("limit",
                                                                                          technicalFacets,
                                                                                          parameterMap,
                                                                                          defaultFacetsRequested))
                     .setTechnicalFacetOffsets(FacetParameterUtils.getTechnicalFacetParams("offset",
                                                                                           technicalFacets,
                                                                                           parameterMap,
                                                                                           defaultFacetsRequested))
                     .setFacetsAllowed(true);
            } catch (DateMathParseException e) {
                String errorDetails = "Error parsing value '" + e.getParsing() + "' supplied for " + FACET_RANGE + " " +
                                      e.getWhatsParsed();
                throw new SolrQueryException(ProblemType.SEARCH_FACET_RANGE_INVALID, errorDetails);
            } catch (InvalidRangeOrGapException e) {
                throw new SolrQueryException(ProblemType.SEARCH_FACET_RANGE_INVALID, e.getMessage());
            } catch (DataFormatException e) {
              throw new InvalidParamValueException(e.getMessage());
            }
        } else {
            query.setFacetsAllowed(false);
        }
        if (profiles.contains(Profile.HITS)) {
            int nrSelectors;
            if (StringUtils.isBlank(hlSelectors)) {
                nrSelectors = 1;
            } else {
                try {
                    nrSelectors = Integer.parseInt(hlSelectors);
                    if (nrSelectors < 1) {
                        throw new SolrQueryException(ProblemType.SEARCH_HITSELECTOR_INVALID,
                                                     "Parameter hit.selectors must be greater than 0");
                    }
                } catch (NumberFormatException nfe) {
                    throw new SolrQueryException(ProblemType.SEARCH_HITSELECTOR_INVALID,
                                                 "Parameter hit.selectors must be an integer");
                }
            }
            query.setParameter("hl", "on");
            query.setParameter("hl.fl", StringUtils.isBlank(hlFl) ? "fulltext.*" : hlFl);
            // this sets both the Solr parameter and a separate nrSelectors variable used to limit the result set with
            query.setNrSelectors("hl.snippets", nrSelectors);
            // see EA-1570 (workaround to increase the number of characters that are being considered for highlighting)
            query.setParameter("hl.maxAnalyzedChars", hlMaxAnalyzedChars);
        }
        if (null != valueReplacements && !valueReplacements.isEmpty()) {
            query.setValueReplacements(valueReplacements);
        }
        // reusability facet settings; spell check allowed, etcetera
        if (defaultOrReusabilityFacetsRequested) {
            query.setQueryFacets(RightReusabilityCategorizer.getQueryFacets());
        }
        if (profiles.contains(Profile.PORTAL) || profiles.contains(Profile.SPELLING)) {
            query.setSpellcheckAllowed(true);
        }
        if (facetsRequested && !query.hasParameter("f.DATA_PROVIDER.facet.limit") &&
            (ArrayUtils.contains(solrFacets, "DATA_PROVIDER") || ArrayUtils.contains(solrFacets, "DEFAULT"))) {
            query.setParameter("f.DATA_PROVIDER.facet.limit", FacetParameterUtils.getLimitForDataProvider());
        }

        SearchResults<? extends IdBean> result = createResults(apiKey, profiles, query, clazz, request.getServerName(),
                translateTargetLang, filterLanguages, request, response,isRefinementDivisionRequired );
        
        if (profiles.contains(Profile.PARAMS)) {
            result.addParams(RequestUtils.getParameterMap(request), "apikey");
            result.addParam("profile", profile);
            result.addParam("start", start);
            result.addParam("rows", rows);
            result.addParam("sort", sort);
        }
        response.setCharacterEncoding(UTF8);
        response.addHeader("Allow", ControllerUtils.ALLOWED_GET_HEAD_POST);
        return JsonUtils.toJson(result, callback);
    }

    public  String validateAndUpdateSortParameters(String sort) throws InvalidParamValueException {
        if(StringUtils.isNotBlank(sort)) {
            StringBuilder newSortString = new StringBuilder();
            String[] sortInfo =  sort.split("\\s*,\\s*");
                for (String info:sortInfo) {
                    String[] sortingInfo = info.split("\\s+");
                    if (sortingInfo.length >0) {
                        FieldDeclaration field = FieldRegistry.INSTANCE.getField(sortingInfo[0]);
                        if (field == null) {
                            throw new InvalidParamValueException("Invalid Sort Parameter value : "+sortingInfo[0]);
                        }
                        //get equivalent sort param for solr
                        String mode= sortingInfo.length ==2 ? sortingInfo[1].trim() : "desc";
                         if(!newSortString.isEmpty())
                             newSortString.append(",");
                          newSortString.append(getSolrFieldForSorting(mode, field)+" "+mode);
                    }
                }
                return newSortString.toString();
        }
        return null;
    }

    private String getSolrFieldForSorting(String mode, FieldDeclaration field) throws InvalidParamValueException {
        String solrSortFieldName= "";
        if("asc".equals(mode))
                solrSortFieldName = field.getField(FieldMode.SORT_DESC);
        if("desc".equals(mode))
                solrSortFieldName =  field.getField(FieldMode.SORT_DESC);
        if(StringUtils.isEmpty(solrSortFieldName))
            throw new InvalidParamValueException("Equivalent solr sort parameter not found in Field registry!");
        return solrSortFieldName;
    }

    /**
     * @return targetLanguage
     */
    private Language validateQueryTranslateParams(String querySourceLang, String queryTargetLang) throws EuropeanaException {
        Language result = null;
        try {
            if (queryTargetLang != null) {
                result = Language.validateSingle(queryTargetLang);
            }
            if (querySourceLang != null) {
                Language.validateSingle(querySourceLang);
                // if a source language is provided, then we must also have a target language
                if (queryTargetLang == null) {
                    throw new MissingParamException(
                            "Parameter q.target is required when q.source is specified");
                }
            }
        } catch (InvalidLanguageException e) {
            throw new InvalidParamValueException(e.getMessage());
        }
        return result;
    }

    private String filterQueryBuilder(Iterator<Integer> it, String queryString, String andOrOr, boolean addBrackets) {
        StringBuilder filterQuery = new StringBuilder();
        if (StringUtils.isNotBlank(queryString)) {
            filterQuery.append(queryString);
            filterQuery.append(" AND ");
        }
        if (addBrackets) {
            filterQuery.append("(");
        }
        if (it.hasNext()) {
            filterQuery.append("filter_tags:");
            filterQuery.append(it.next().toString());
        }
        while (it.hasNext()) {
            filterQuery.append(andOrOr);
            filterQuery.append("filter_tags:");
            filterQuery.append(it.next().toString());
        }
        if (addBrackets) {
            filterQuery.append(")");
        }
        return filterQuery.toString();
    }

    /**
     * Processes all qf parameters. Note that besides returning a new array of refinements we may add new filterTags to
     * the provided filterTags list (if there are image, audio, video or mimetype refinements)
     */
    protected String[] processQfParameters( String[] refinementArray,
                                            Boolean media,
                                            Boolean thumbnail,
                                            Boolean fullText,
                                            Boolean landingPage,
                                            List<Integer> filterTags,
                                            GeoDistance geoDistance) throws InvalidParamValueException {
        boolean hasImageRefinements = false;
        boolean hasAudioRefinements = false;
        boolean hasVideoRefinements = false;
       // boolean hasTextRefinements  = false;
        boolean hasBrokenTechFacet  = false;

        boolean hasGeoDistanceSearch = false;

        Boolean whatYouWant;
        FacetEncoder facetEncoder        = new FacetEncoder();
        List<String> newRefinements      = new ArrayList<>();

        Set<MimeTypeEncoding>   imageMimeTypeRefinements      = new HashSet<>();
        Set<MimeTypeEncoding>   audioMimeTypeRefinements      = new HashSet<>();
        Set<MimeTypeEncoding>   videoMimeTypeRefinements      = new HashSet<>();
        Set<MimeTypeEncoding>   textMimeTypeRefinements       = new HashSet<>();
        Set<ImageSize>          imageSizeRefinements          = new HashSet<>();
        Set<ImageAspectRatio>   imageAspectRatioRefinements   = new HashSet<>();
        Set<AudioDuration>      audioDurationRefinements      = new HashSet<>();
        Set<VideoDuration>      videoDurationRefinements      = new HashSet<>();
        Set<ImageColorSpace>    imageColourSpaceRefinements   = new HashSet<>();
        Set<VideoQuality>       videoHDRefinements            = new HashSet<>();
        Set<AudioQuality>       audioHQRefinements            = new HashSet<>();
        Set<ImageColorEncoding> imageColourPaletteRefinements = new HashSet<>();

        MimeTypeEncoding mte;

        // retrieves the faceted refinements from the QF part of the request and stores them separately
        // the rest of the refinements is kept in the refinementArray
        // NOTE prefixes are case sensitive: tech facets processed here are uppercase, but (geo) distance IS NOT!
        // ALSO NOTE that the suffixes are NOT case sensitive. They are all made lowercase, except 'colourpalette'
        if (refinementArray != null) {
            for (String qf : refinementArray) {
                if (StringUtils.contains(qf, ":")) {
                    String refinementValue = StringUtils.substringAfter(qf, ":")
                                                        .toLowerCase(Locale.GERMAN)
                                                        .replaceAll("^\"|\"$", "");
                    switch (StringUtils.substringBefore(qf, ":")) {
                        case "MIME_TYPE":
                            mte =  MimeTypeEncoding.categorizeMimeType(refinementValue);
                            if (null == mte){
                                hasBrokenTechFacet = true;
                            } else {
                                switch (MediaType.getMediaType(refinementValue)) {
                                    case IMAGE:
                                        CollectionUtils.addIgnoreNull(imageMimeTypeRefinements, mte);
                                        break;
                                    case AUDIO:
                                        CollectionUtils.addIgnoreNull(audioMimeTypeRefinements, mte);
                                        break;
                                    case VIDEO:
                                        CollectionUtils.addIgnoreNull(videoMimeTypeRefinements, mte);
                                        break;
                                    case TEXT:
                                        CollectionUtils.addIgnoreNull(textMimeTypeRefinements, mte);
                                        break;
                                    case OTHER: // <-- note that this is a valid Mediatype, but mimetypes of this type are not stored in Solr by Metis
                                        break;
                                    default:
                                        break;
                                }
                            }
                            break;
                        case "IMAGE_SIZE":
                            ImageSize imageSize = TagUtils.getImageSize(refinementValue);
                            if (Objects.nonNull(imageSize)) {
                                imageSizeRefinements.add(imageSize);
                                hasImageRefinements = true;
                            } else {
                                hasBrokenTechFacet = true;
                            }
                            break;
                        case "IMAGE_COLOUR":
                        case "IMAGE_COLOR":
                            whatYouWant = parseValidBoolean(refinementValue);
                            if (null == whatYouWant){
                                hasBrokenTechFacet = true;
                            } else if (whatYouWant) {
                                imageColourSpaceRefinements.add(ImageColorSpace.COLOR);
                                hasImageRefinements = true;
                            } else {
                                imageColourSpaceRefinements.add(ImageColorSpace.GRAYSCALE);
                                hasImageRefinements = true;
                            }
                            break;
                        case "IMAGE_GREYSCALE":
                        case "IMAGE_GRAYSCALE":
                            whatYouWant = parseValidBoolean(refinementValue);
                            if (null == whatYouWant){
                                hasBrokenTechFacet = true;
                            } else if (whatYouWant) {
                                imageColourSpaceRefinements.add(ImageColorSpace.GRAYSCALE);
                                hasImageRefinements = true;
                            } else {
                                imageColourSpaceRefinements.add(ImageColorSpace.COLOR);
                                hasImageRefinements = true;
                            }
                            break;
                        case "COLOURPALETTE":
                        case "COLORPALETTE":
                            ImageColorEncoding imageColorEncoding = ImageColorEncoding.categorizeImageColor(
                                    refinementValue);
                            if (Objects.nonNull(imageColorEncoding)) {
                                imageColourPaletteRefinements.add(imageColorEncoding);
                                hasImageRefinements = true;
                            } else {
                                hasBrokenTechFacet = true;
                            }
                            break;
                        case "IMAGE_ASPECTRATIO":
                            if (StringUtils.containsIgnoreCase(refinementValue, "portrait")) {
                                imageAspectRatioRefinements.add(ImageAspectRatio.PORTRAIT);
                                hasImageRefinements = true;
                            } else if (StringUtils.containsIgnoreCase(refinementValue, "landscape")) {
                                imageAspectRatioRefinements.add(ImageAspectRatio.LANDSCAPE);
                                hasImageRefinements = true;
                            } else {
                                hasBrokenTechFacet = true;
                            }
                            break;
                        case "SOUND_HQ":
                            whatYouWant = parseValidBoolean(refinementValue);
                            if (null == whatYouWant){
                                hasBrokenTechFacet = true;
                            } else if (whatYouWant) {
                                audioHQRefinements.add(AudioQuality.HIGH);
                                hasAudioRefinements = true;
                            }
                            // uncomment this to treat FALSE as invalid
//                            else {
//                                hasBrokenTechFacet = true;
//                            }
                            break;
                        case "SOUND_DURATION":
                            AudioDuration audioDuration = TagUtils.getAudioDurationCode(refinementValue);
                            if (Objects.nonNull(audioDuration)) {
                                audioDurationRefinements.add(audioDuration);
                                hasAudioRefinements = true;
                            } else {
                                hasBrokenTechFacet = true;
                            }
                            break;
                        case "VIDEO_HD":
                            whatYouWant = parseValidBoolean(refinementValue);
                            if (null == whatYouWant){
                                hasBrokenTechFacet = true;
                            } else if (whatYouWant) {
                                videoHDRefinements.add(VideoQuality.HIGH);
                                hasVideoRefinements = true;
                            }
                            // uncomment this to treat FALSE as invalid
//                            else {
//                                hasBrokenTechFacet = true;
//                            }
                            break;
                        case "VIDEO_DURATION":
                            VideoDuration videoDuration = TagUtils.getVideoDurationCode(refinementValue);
                            if (Objects.nonNull(videoDuration)) {
                                videoDurationRefinements.add(videoDuration);
                                hasVideoRefinements = true;
                            } else {
                                hasBrokenTechFacet = true;
                            }
                            break;
                        case "MEDIA":
                            if (null == media) {
                                media = Boolean.valueOf(refinementValue);
                            }
                            break;
                        case "THUMBNAIL":
                            if (null == thumbnail) {
                                thumbnail = Boolean.valueOf(refinementValue);
                            }
                            break;
                        case "TEXT_FULLTEXT":
                            if (null == fullText) {
                                fullText = Boolean.valueOf(refinementValue);
                            }
                            break;
                        case "LANDINGPAGE":
                            if (null == landingPage) {
                                landingPage = Boolean.valueOf(refinementValue);
                            }
                            break;
                        case "COUNTRY":
                            // Temporary fix for country capitalization for Metis (EA-1350)
                            newRefinements.add(fixCountryCapitalization(qf));
                            break;
                        default:
                            newRefinements.add(qf);
                    }
                    // EA-2996 geo distance search
                } else if (StringUtils.contains(qf, "distance")) {
                    if (hasGeoDistanceSearch){
                        throw new InvalidParamValueException("Only one distance query can be supplied");
                    } else {
                        String refinementValue = StringUtils.substringAfter(qf, "distance(")
                                                            .replaceAll("^\"|\"$", "")
                                                            .replaceAll("[\\(\\)]", "");
                        geoDistance.initialise(refinementValue);
                        if (geoDistance.isInitialised()) {
                            newRefinements.add(geoDistance.getFQGeo());
                            hasGeoDistanceSearch = true;
                        }
                    }
                } else {
                    newRefinements.add(qf);
                }
            }
        }
        // add boolean properties as refinements
        if (null != media) {
            newRefinements.add("has_media:" + media.toString());
        }
        if (null != thumbnail) {
            newRefinements.add("has_thumbnails:" + thumbnail.toString());
        }
        if (null != fullText) {
            newRefinements.add("is_fulltext:" + fullText.toString());
        }
        if (null != landingPage) {
            newRefinements.add("has_landingpage:" + landingPage.toString());
        }

        // Encode the faceted refinements ...
        if (hasImageRefinements || CollectionUtils.isNotEmpty(imageMimeTypeRefinements)) {
            filterTags.addAll(facetEncoder.getImageFacetSearchCodes(imageMimeTypeRefinements,
                                                                    imageSizeRefinements,
                                                                    imageColourSpaceRefinements,
                                                                    imageAspectRatioRefinements,
                                                                    imageColourPaletteRefinements));
        }
        if (hasAudioRefinements || CollectionUtils.isNotEmpty(audioMimeTypeRefinements)) {
            filterTags.addAll(facetEncoder.getAudioFacetSearchCodes(audioMimeTypeRefinements,
                                                                    audioHQRefinements,
                                                                    audioDurationRefinements));
        }
        if (hasVideoRefinements || CollectionUtils.isNotEmpty(videoMimeTypeRefinements)) {
            filterTags.addAll(facetEncoder.getVideoFacetSearchCodes(videoMimeTypeRefinements,
                                                                    videoHDRefinements,
                                                                    videoDurationRefinements));
        }
        if (CollectionUtils.isNotEmpty(textMimeTypeRefinements)) {
            filterTags.addAll(facetEncoder.getTextFacetSearchCodes(textMimeTypeRefinements));
        }

        if (hasBrokenTechFacet && filterTags.isEmpty()){
            filterTags.add(0);
        }

        if (LOG.isDebugEnabled()) {
            for (Integer filterTag : filterTags) {
                if (filterTag != null) {
                    LOG.debug("filterTag = {}", Integer.toBinaryString(filterTag));
                }
            }
        }
        return newRefinements.toArray(new String[0]);
    }

    /**
     * Due to changed capitalization of country names between UIM and Metis data we need to convert lowercase country
     * names (UIM) to camel-case names (Metis). This fix is to prevent errors during the UIM-to-Metis switch, but also
     * to support saved queries for the time being. See also ticket EA-1350
     *
     * @param queryPart either query or qf part that is to be converted
     * @return converted query or qf part
     */
    private String fixCountryCapitalization(String queryPart) {
        String  result  = queryPart;
        Matcher matcher = COUNTRY_PATTERN.matcher(result);
        while (matcher.find()) {
            // if group 1 has content then we found country with quotes, if group 2 has content then it's a country without quotes
            String  countryName = matcher.group(1);
            boolean withQuotes  = StringUtils.isNotEmpty(countryName);
            if (!withQuotes) {
                countryName = matcher.group(2);
            }
            StringBuilder s = new StringBuilder(result.substring(0, matcher.start()));
            s.append("COUNTRY:");
            if (withQuotes) {
                s.append("\"");
            }
            s.append(CountryUtils.capitalizeCountry(countryName));
            if (withQuotes) {
                s.append("\"");
            } else {
                s.append(matcher.group(3)); // put back the &, space or + character
            }
            s.append(result.substring(matcher.end(), result.length()));
            result = s.toString();
            if (LOG.isDebugEnabled()) {
                LOG.debug("Found {} at {},{}, countryName is {}",
                          matcher.group(),
                          matcher.start(),
                          matcher.end(),
                          countryName);
                LOG.debug("Converted to {}", result);
            }
        }
        return result;
    }

    private Boolean parseValidBoolean(String value){
        if (StringUtils.equalsAnyIgnoreCase(value, "true", "false")){
            return Boolean.parseBoolean(value);
        } else {
            return null;
        }
    }

    private Class<? extends IdBean> selectBean(String profile) {
        Class<? extends IdBean> clazz;
        if (StringUtils.containsIgnoreCase(profile, "minimal")) {
            clazz = BriefBean.class;
        } else if (StringUtils.containsIgnoreCase(profile, "rich")) {
            clazz = RichBean.class;
        } else {
            clazz = ApiBean.class;
        }
        return clazz;
    }

    private Class<? extends IdBeanImpl> getBeanImpl(Class clazz) {
        if (BriefBean.class.equals(clazz)) {
            return BriefBeanImpl.class;
        }
        if (RichBean.class.equals(clazz)) {
            return RichBeanImpl.class;
        }
        return ApiBeanImpl.class;
    }

    @SuppressWarnings("unchecked")
    private <T extends IdBean> SearchResults<T> createResults(
                                                              String apiKey,
                                                              Set<Profile> profiles,
                                                              Query query,
                                                              Class<T> clazz,
                                                              String requestRoute,
                                                              String translateTargetLang,
                                                              List<Language> filterLanguages,
                                                              HttpServletRequest servletRequest,
                                                              HttpServletResponse servletResponse,
        boolean isToDivideQueryRefinements) throws EuropeanaException {

        SearchResults<T> response = new SearchResults<>(apiKey);
        ResultSet<T>     resultSet;

        SolrClient solrClient = getSolrClient(requestRoute);

        if (profiles.contains(Profile.DEBUG)) {
            resultSet = searchService.search(solrClient, clazz, query, true,isToDivideQueryRefinements);
        } else {
            resultSet = searchService.search(solrClient, clazz, query,isToDivideQueryRefinements);
        }
        response.totalResults = resultSet.getResultSize();
        if (StringUtils.isNotBlank(resultSet.getCurrentCursorMark()) &&
            StringUtils.isNotBlank(resultSet.getNextCursorMark()) &&
            !resultSet.getNextCursorMark().equalsIgnoreCase(resultSet.getCurrentCursorMark())) {
            response.nextCursor = resultSet.getNextCursorMark();
        }

        response.itemsCount = resultSet.getResults().size();
        response.items = resultSet.getResults();

        // We need to modify BriefBeans with translations before creating views otherwise access becomes harder
        // Note that translateTargetLang is only set when minimal profile is enabled (so we are sure we get BriefBeans)
        if (translateTargetLang != null) {
            try {
                searchResultTranslator.translate((List<BriefBean>) resultSet.getResults(), translateTargetLang, getAuthorizationHeader(servletRequest));
            } catch (TranslationServiceNotAvailableException e) {
                // EA-3463 - return 307 redirect without profile param and Keep the Error Response
                // Body indicating the reason for troubleshooting
                ControllerUtils.redirectForTranslationsLimitException(servletRequest, servletResponse, profiles);
                // throwing exception again overwrites the exception message with problem type message. Hence, fetch the original message from cause
                throw new TranslationServiceNotAvailableException(e.getCause().getMessage(), e);
            }

        }
        // Filtering of results
        if (filterLanguages != null) {
            for (IdBean result : resultSet.getResults()) {
                LanguageFilter.filter(result, filterLanguages);
                // The non-language aware fields should disappear (Title, dcCreator, dcDescription).
                LanguageFilter.removeNonLanguageAwareFields(result);
            }
        }

        // Generate views
        List<T> beans = new ArrayList<>();
        for (T b : resultSet.getResults()) {
            if (b instanceof RichBean) {
                beans.add((T) new RichView((RichBean) b, profiles, apiKey, requestRoute));
            } else if (b instanceof ApiBean) {
                beans.add((T) new ApiView((ApiBean) b, profiles, apiKey, requestRoute));
            } else if (b instanceof BriefBean) {
                beans.add((T) new BriefView((BriefBean) b, profiles, apiKey, requestRoute));
            }
        }

        List<FacetField> facetFields = resultSet.getFacetFields();
        if (MapUtils.isNotEmpty(resultSet.getQueryFacets())) {
            List<FacetField> allQueryFacetsMap = SearchUtils.extractQueryFacets(resultSet.getQueryFacets());
            if (!allQueryFacetsMap.isEmpty()) {
                facetFields.addAll(allQueryFacetsMap);
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("results: {}", beans.size());
        }

        response.items = beans;
        if (profiles.contains(Profile.FACETS) || profiles.contains(Profile.PORTAL)) {
            response.facets = new FacetWrangler().consolidateFacetList(resultSet.getFacetFields(),
                                                                       resultSet.getRangeFacets(),
                                                                       query.getTechnicalFacets(),
                                                                       query.isDefaultFacetsRequested(),
                                                                       query.getTechnicalFacetLimits(),
                                                                       query.getTechnicalFacetOffsets());
            //map facet name returned by solr to the facet term requested by user
            if (CollectionUtils.isNotEmpty(query.getSolrFacets())) {
                for (String facetTerm : query.getSolrFacets()) {
                    String filedNameForSpecificMode = ParserUtils.getFiledNameForSpecificMode(
                        FieldMode.FACET, FieldRegistry.INSTANCE.getField(facetTerm));
                    for (Facet facet : response.facets) {
                        if (filedNameForSpecificMode!= null && filedNameForSpecificMode.equals(facet.name)) {
                            facet.name = facetTerm;
                        }
                    }
                }
            }
        }

        if (profiles.contains(Profile.HITS) && MapUtils.isNotEmpty(resultSet.getHighlighting())) {
            response.hits = new HitMaker().createHitList(resultSet.getHighlighting(), query.getNrSelectors());
        }
        if (profiles.contains(Profile.SPELLING) || profiles.contains(Profile.PORTAL)) {
            response.spellcheck = ModelUtils.convertSpellCheck(resultSet.getSpellcheck());
        }
        if (profiles.contains(Profile.DEBUG)) {
            response.debug = resultSet.getSolrQueryString();
        }
        return response;
    }

    /**
     * @return the JSON response
     * @deprecated 2018-01-09 search with coordinates functionality
     */
    @SwaggerIgnore
    @GetMapping(value = "/api/v2/search.kml",
                produces = {"application/vnd.google-earth.kml+xml",
                        org.springframework.http.MediaType.APPLICATION_XML_VALUE,
                        org.springframework.http.MediaType.APPLICATION_XHTML_XML_VALUE})
    @ResponseBody
    @Deprecated(since = "jan 2018")
    public KmlResponse searchKml(@SolrEscape @RequestParam(value = "query") String queryString,
                                 @RequestParam(value = "qf", required = false) String[] refinementArray,
                                 @RequestParam(value = "start", required = false, defaultValue = "1") int start,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
        throws EuropeanaException, HttpException {

        verifyReadAccess(request);
        SolrClient solrClient = getSolrClient(request.getServerName());

        String[] qfArray = request.getParameterMap().get("qf");
        if (qfArray != null && qfArray.length != refinementArray.length) {
            refinementArray = qfArray;
        }
        KmlResponse kmlResponse = new KmlResponse();
        Query query = new Query(SearchUtils.rewriteQueryFields(queryString)).setRefinements(refinementArray)
                                                                            .setApiQuery(true)
                                                                            .setSpellcheckAllowed(false)
                                                                            .setFacetsAllowed(false);
        query.setRefinements("pl_wgs84_pos_lat_long:[* TO *]");
        ResultSet<BriefBean> resultSet = searchService.search(solrClient, BriefBean.class, query,true);
        kmlResponse.document.extendedData.totalResults.value = Long.toString(resultSet.getResultSize());
        kmlResponse.document.extendedData.startIndex.value = Integer.toString(start);
        kmlResponse.setItems(resultSet.getResults());
        return kmlResponse;
    }

    /**
     * Handles an opensearch query (see also https://en.wikipedia.org/wiki/OpenSearch)
     *
     * @param queryString the search terms used to query the Europeana repository; similar to the query parameter in the
     *                    search method.
     * @param start       the first object in the search result set to start with (first item = 1), e.g., if a result set is
     *                    made up of 100 objects, you can set the first returned object to the 22nd object in the set
     *                    [optional parameter, default = 1]
     * @param count       the number of search results to return; possible values can be any integer up to 100 [optional
     *                    parameter, default = 12]
     * @return rss response of the query
     */
    @ApiOperation(value = "basic search function following the OpenSearch specification", nickname = "openSearch")
    @GetMapping(value = {"/api/v2/opensearch.rss", "/record/v2/opensearch.rss", "/record/opensearch.rss"},
                produces = {"application/rss+xml",
                        org.springframework.http.MediaType.APPLICATION_XML_VALUE,
                        org.springframework.http.MediaType.APPLICATION_XHTML_XML_VALUE})
    @ResponseBody
    public ModelAndView openSearchRss(@SolrEscape @RequestParam(value = "searchTerms") String queryString,
                                      @RequestParam(value = "startIndex", required = false, defaultValue = "1")
                                              int start,
                                      @RequestParam(value = "count", required = false, defaultValue = "12") int count,
                                      HttpServletRequest request,
                                      HttpServletResponse response) throws EuropeanaException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("openSearch query with terms: {}", queryString);
        }
        ControllerUtils.addResponseHeaders(response);
        RssResponse rss = new RssResponse();

        Channel channel = rss.channel;
        channel.startIndex.value = start;
        channel.itemsPerPage.value = count;
        channel.query.searchTerms = queryString;
        channel.query.startPage = start;

        SolrClient solrClient = getSolrClient(request.getServerName());
        Query query = new Query(SearchUtils.rewriteQueryFields(queryString)).setApiQuery(true)
                                                                            .setPageSize(count)
                                                                            .setStart(start - 1)
                                                                            .setFacetsAllowed(false)
                                                                            .setSpellcheckAllowed(false);
        ResultSet<BriefBean> resultSet = searchService.search(solrClient, BriefBean.class, query,true);
        channel.totalResults.value = resultSet.getResultSize();
        for (BriefBean bean : resultSet.getResults()) {
            Item item = new Item();
            item.guid = urlService.getRecordPortalUrl(request.getServerName(), bean.getId());
            item.title = getTitle(bean);
            item.description = getDescription(bean);
            item.link = item.guid;
            channel.items.add(item);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Returning rss result: {}", rss);
        }

        String              xml   = xmlUtils.toString(rss);
        Map<String, Object> model = new HashMap<>();
        model.put("rss", xml);

        response.setCharacterEncoding(UTF8);
        response.setContentType("application/xml");

        return new ModelAndView("rss", model);
    }

    /**
     * Method to find all encoded facets in tags
     *
     * @return the JSON response
     */
    @SwaggerIgnore
    @GetMapping(value = {"/api/v2/decodetags.json", "/api/v2/tagdecoder.json"},
                produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    public ModelAndView decodeTags(@RequestParam(value = "tag") String tag) {
        if (tag.matches("[0-9]+") && tag.length() > 7) {
            return JsonUtils.toJson(findAllFacetsInTag(Integer.valueOf(tag)));
        } else {
            return JsonUtils.toJson("Cannot decode this tag: it must be numerical and 8 digits long");
        }
    }

    /**
     * Retrieves the title from the bean if not null; otherwise, returns a concatenation of the Data
     * Provier and ID fields.
     * TODO Note that this method will yield unwanted results when there is more than one Title
     * TODO field! (especially now we consider language aware titles)
     *
     * @param bean mapped pojo bean
     * @return String containing the concatenated fields
     */
    private String getTitle(BriefBean bean) {
        if (!ArrayUtils.isEmpty(bean.getTitle())) {
            for (String title : bean.getTitle()) {
                if (!StringUtils.isBlank(title)) {
                    return title;
                }
            }
        }
        return (bean.getDataProvider() == null ? "Unknown data provider " : bean.getDataProvider()[0] + " ") +
               bean.getId();
    }

    /**
     * retrieves a concatenation of the bean's DC Creator, Year and Provider
     * fields (if available)
     *
     * @param bean mapped pojo bean
     * @return String containing the fields, separated by semicolons
     */
    private String getDescription(BriefBean bean) {
        StringBuilder sb = new StringBuilder();
        if (bean.getDcCreator() != null && bean.getDcCreator().length > 0 &&
            StringUtils.isNotBlank(bean.getDcCreator()[0])) {
            sb.append(bean.getDcCreator()[0]);
        }
        if (bean.getYear() != null && bean.getYear().length > 0) {
            if (sb.length() > 0) {
                sb.append("; ");
            }
            sb.append(StringUtils.join(bean.getYear(), ", "));
        }
        if (!ArrayUtils.isEmpty(bean.getProvider())) {
            if (sb.length() > 0) {
                sb.append("; ");
            }
            sb.append(StringUtils.join(bean.getProvider(), ", "));
        }
        return sb.toString();
    }



}
