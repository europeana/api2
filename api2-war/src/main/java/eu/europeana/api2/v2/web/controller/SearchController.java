package eu.europeana.api2.v2.web.controller;

import eu.europeana.api2.ApiLimitException;
import eu.europeana.api2.model.json.ApiError;
import eu.europeana.api2.model.utils.Api2UrlService;
import eu.europeana.api2.utils.FieldTripUtils;
import eu.europeana.api2.utils.JsonUtils;
import eu.europeana.api2.utils.XmlUtils;
import eu.europeana.api2.v2.exceptions.DateMathParseException;
import eu.europeana.api2.v2.exceptions.InvalidGapException;
import eu.europeana.api2.v2.model.LimitResponse;
import eu.europeana.api2.v2.model.json.SearchResults;
import eu.europeana.api2.v2.model.json.view.ApiView;
import eu.europeana.api2.v2.model.json.view.BriefView;
import eu.europeana.api2.v2.model.json.view.RichView;
import eu.europeana.api2.v2.model.xml.kml.KmlResponse;
import eu.europeana.api2.v2.model.xml.rss.Channel;
import eu.europeana.api2.v2.model.xml.rss.Item;
import eu.europeana.api2.v2.model.xml.rss.RssResponse;
import eu.europeana.api2.v2.model.xml.rss.fieldtrip.FieldTripChannel;
import eu.europeana.api2.v2.model.xml.rss.fieldtrip.FieldTripImage;
import eu.europeana.api2.v2.model.xml.rss.fieldtrip.FieldTripItem;
import eu.europeana.api2.v2.model.xml.rss.fieldtrip.FieldTripResponse;
import eu.europeana.api2.v2.service.FacetWrangler;
import eu.europeana.api2.v2.service.HitMaker;
import eu.europeana.api2.v2.utils.ApiKeyUtils;
import eu.europeana.api2.v2.utils.ControllerUtils;
import eu.europeana.api2.v2.utils.FacetParameterUtils;
import eu.europeana.api2.v2.utils.ModelUtils;
import eu.europeana.api2.v2.web.swagger.SwaggerIgnore;
import eu.europeana.api2.v2.web.swagger.SwaggerSelect;
import eu.europeana.corelib.db.entity.enums.RecordType;
import eu.europeana.corelib.db.exception.DatabaseException;
import eu.europeana.corelib.db.service.ApiKeyService;
import eu.europeana.corelib.definitions.db.entity.relational.ApiKey;
import eu.europeana.corelib.definitions.edm.beans.ApiBean;
import eu.europeana.corelib.definitions.edm.beans.BriefBean;
import eu.europeana.corelib.definitions.edm.beans.IdBean;
import eu.europeana.corelib.definitions.edm.beans.RichBean;
import eu.europeana.corelib.definitions.solr.model.Query;
import eu.europeana.corelib.edm.exceptions.SolrTypeException;
import eu.europeana.corelib.edm.utils.CountryUtils;
import eu.europeana.corelib.search.SearchService;
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
import eu.europeana.corelib.web.support.Configuration;
import eu.europeana.corelib.web.utils.NavigationUtils;
import eu.europeana.corelib.web.utils.RequestUtils;
import eu.europeana.api2.v2.utils.technicalfacets.CommonTagExtractor;
import eu.europeana.api2.v2.utils.TagUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.response.FacetField;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static eu.europeana.api2.v2.utils.ModelUtils.decodeFacetTag;

/**
 * Controller that handles all search requests (search.json, opensearch.rss, search.rss, and search.kml)
 * @author Willem-Jan Boogerd
 * @author Luthien
 * @author Patrick Ehlert
 */
@Controller
@SwaggerSelect
@Api(tags = {"Search"})
public class SearchController {

    private static final Logger LOG         = LogManager.getLogger(SearchController.class);
    private static final String SEARCHJSON  = " [search.json] ";
    private static final String PORTAL      = "portal";
    private static final String FACETS      = "facets";
    private static final String DEBUG       = "debug";
    private static final String SPELLING    = "spelling";
    private static final String BREADCRUMB  = "breadcrumb";
    private static final String FACET_RANGE = "facet.range";
    private static final String HITS        = "hits";

    // First pattern is country with value between quotes, second pattern is with value without quotes (ending with &,
    // space or end of string)
    private static final Pattern COUNTRY_PATTERN = Pattern.compile("COUNTRY:\"(.*?)\"|COUNTRY:(.*?)(&|\\s|$)");

    @Resource
    private SearchService searchService;

    @Resource
    private ApiKeyService apiService;

    @Resource
    private Configuration configuration;

    @Resource
    private Api2UrlService urlService;

    @Resource
    private ApiKeyUtils apiKeyUtils;

    @Resource(name = "api2_mvc_xmlUtils")
    private XmlUtils xmlUtils;

    @Value("${api.search.hl.MaxAnalyzedChars}")
    private String hlMaxAnalyzedChars;

    /**
     * Returns a list of Europeana datasets based on the search terms.
     * The response is an Array of JSON objects, each one containing the identifier and the name of a dataset.
     *
     * @return the JSON response
     */
    @ApiOperation(value = "search for records", nickname = "searchRecords", response = Void.class)
//	@ApiResponses(value = {@ApiResponse(code = 200, message = "OK") })
    @RequestMapping(value = "/v2/search.json",
                    method = {RequestMethod.GET, RequestMethod.POST},
                    produces = MediaType.APPLICATION_JSON_VALUE)
    public ModelAndView searchJson(
            @RequestParam(value = "wskey") String wskey,
            @RequestParam(value = "query") String queryString,
            @RequestParam(value = "qf", required = false) String[] refinementArray,
            @RequestParam(value = "reusability", required = false) String[] reusabilityArray,
            @RequestParam(value = "profile", required = false, defaultValue = "standard") String profile,
            @RequestParam(value = "start", required = false, defaultValue = "1") int start,
            @RequestParam(value = "rows", required = false, defaultValue = "12") int rows,
            @RequestParam(value = "facet", required = false) String[] mixedFacetArray,
            @RequestParam(value = "theme", required = false) String theme,
            @RequestParam(value = "sort", required = false) String sort,
            @RequestParam(value = "colourpalette", required = false) String[] colourPaletteArray,
            @RequestParam(value = "thumbnail", required = false) Boolean thumbnail,
            @RequestParam(value = "media", required = false) Boolean media,
            @RequestParam(value = "text_fulltext", required = false) Boolean fullText,
            @RequestParam(value = "landingpage", required = false) Boolean landingPage,
            @RequestParam(value = "cursor", required = false) String cursorMark,
            @RequestParam(value = "callback", required = false) String callback,
            @RequestParam(value = "hit.fl", required = false) String hlFl,
            @RequestParam(value = "hit.selectors", required = false) String hlSelectors,
            HttpServletRequest request,
            HttpServletResponse response) throws ApiLimitException {

        LimitResponse limitResponse = apiKeyUtils.checkLimit(wskey, request.getRequestURL().toString(), RecordType.SEARCH, profile);

        // check query parameter
        if (StringUtils.isBlank(queryString)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return JsonUtils.toJson(new ApiError("", "Empty query parameter"), callback);
        }

        queryString = queryString.trim();
        queryString = fixCountryCapitalization(queryString);

        // #579 rights URL's don't match well to queries containing ":https*"
        queryString = queryString.replace(":https://", ":http://");
        if (LOG.isDebugEnabled()) {
            LOG.debug("QUERY: |" + queryString + "|");
        }

        if (cursorMark != null) {
            if (start > 1) {
                response.setStatus(400);
                return JsonUtils.toJson(new ApiError("", "Parameters 'start' and 'cursorMark' cannot be used together"), callback);
            }
        }

        // TODO check whether this is still necessary?
        // workaround of a Spring issue
        // (https://jira.springsource.org/browse/SPR-7963)
        String[] _qf = request.getParameterMap().get("qf");
        if (_qf != null && _qf.length != refinementArray.length) {
            refinementArray = _qf;
        }

        if (StringUtils.isNotBlank(theme)){
            if (StringUtils.containsAny(theme, "+ #%^&*-='\"<>`!@[]{}\\/|")){
                return JsonUtils.toJson(new ApiError("", "Parameter 'theme' accepts one value only"), callback);
            } else {
                refinementArray = (String[]) ArrayUtils.add(refinementArray, "collection:" + theme);
            }
        }

        List<String> colourPalette = new ArrayList();
        if (ArrayUtils.isNotEmpty(colourPaletteArray)) {
            StringArrayUtils.addToList(colourPalette, colourPaletteArray);
        }
        colourPalette.replaceAll(String::toUpperCase);

        // Note that this is about the parameter 'colourpalette', not the refinement: they are processed below
        // [existing-query] AND [filter_tags-1 AND filter_tags-2 AND filter_tags-3 ... ]
        if (!colourPalette.isEmpty()) {
            queryString = filterQueryBuilder(TagUtils.colourPaletteFilterTags(colourPalette).iterator(),
                                             queryString,
                                             " AND ",
                                             false);
        }

        final List<Integer> filterTags = new ArrayList<>();
        refinementArray = processQfParameters(refinementArray, media, thumbnail, fullText, landingPage, filterTags);

        // add the CF filter facets to the query string like this:
        // [existing-query] AND ([filter_tags-1 OR filter_tags-2 OR filter_tags-3 ... ])
        if (!filterTags.isEmpty()) {
            queryString = filterQueryBuilder(filterTags.iterator(),
                                             queryString,
                                             " OR ",
                                             true);
        }

        String[] reusabilities = StringArrayUtils.splitWebParameter(reusabilityArray);
        String[] mixedFacets = StringArrayUtils.splitWebParameter(mixedFacetArray);

        boolean rangeFacetsSpecified = request.getParameterMap().containsKey(FACET_RANGE);
        boolean noFacetsSpecified = ArrayUtils.isEmpty(mixedFacets);
        boolean facetsRequested = StringUtils.containsIgnoreCase(profile, PORTAL) ||
                                  StringUtils.containsIgnoreCase(profile, FACETS);
        boolean defaultFacetsRequested = facetsRequested && !rangeFacetsSpecified &&
                                         (noFacetsSpecified ||  ArrayUtils.contains(mixedFacets, "DEFAULT"));
        boolean defaultOrReusabilityFacetsRequested = defaultFacetsRequested ||
                                                      (facetsRequested &&  ArrayUtils.contains(mixedFacets, "REUSABILITY"));

        // 1) replaces DEFAULT (or empty list of) facet with those defined in the enum types (removes explicit DEFAULT facet)
        // 2) separates the requested facets in Solr facets and technical (fake-) facets
        // 3) when many custom SOLR facets are supplied: caps the number of total facets to FACET_LIMIT
        Map<String, String[]> separatedFacets = ModelUtils.separateAndLimitFacets(mixedFacets, defaultFacetsRequested);
        String[] solrFacets = (String[]) ArrayUtils.addAll(separatedFacets.get("solrfacets"), separatedFacets.get("customfacets"));
        String[] technicalFacets = separatedFacets.get("technicalfacets");

        ControllerUtils.addResponseHeaders(response);
        rows = Math.min(rows, configuration.getApiRowLimit());

        Map<String, String> valueReplacements = null;
        if (ArrayUtils.isNotEmpty(reusabilities)) {
            valueReplacements = RightReusabilityCategorizer.mapValueReplacements(reusabilities, true);
            if (null != valueReplacements && !valueReplacements.isEmpty()){
                refinementArray = (String[]) ArrayUtils.addAll(refinementArray, new String[]{"REUSABILITY:list"});
            }
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
                .setParameter("facet.mincount", "1")
                .setParameter("fl", IdBeanImpl.getFields(getBeanImpl(clazz)))
                .setSpellcheckAllowed(false);

        if (facetsRequested) {
            Map<String, String[]> parameterMap = request.getParameterMap();
            try {
                query.setSolrFacets(solrFacets)
                        .setDefaultFacetsRequested(defaultFacetsRequested)
                        .convertAndSetSolrParameters(FacetParameterUtils.getSolrFacetParams(
                                "limit", solrFacets, parameterMap, defaultFacetsRequested))
                        .convertAndSetSolrParameters(FacetParameterUtils.getSolrFacetParams(
                                "offset", solrFacets, parameterMap, defaultFacetsRequested))
                        .setFacetDateRangeParameters(FacetParameterUtils.getDateRangeParams(parameterMap))
                        .setTechnicalFacets(technicalFacets)
                        .setTechnicalFacetLimits(FacetParameterUtils.getTechnicalFacetParams(
                                "limit", technicalFacets, parameterMap, defaultFacetsRequested))
                        .setTechnicalFacetOffsets(FacetParameterUtils.getTechnicalFacetParams(
                                "offset", technicalFacets, parameterMap, defaultFacetsRequested))
                        .setFacetsAllowed(true);
            } catch (DateMathParseException e) {
                response.setStatus(400);
                return JsonUtils.toJson(new ApiError("", "Error parsing value '" + e.getParsing() +
                                                         "' supplied for " + FACET_RANGE + " " +
                                                         e.getWhatsParsed()), callback);
            } catch (InvalidGapException e) {
                response.setStatus(400);
                return JsonUtils.toJson(new ApiError("", e.getMessage()), callback);
            }
        } else {
            query.setFacetsAllowed(false);
        }

        if (StringUtils.containsIgnoreCase(profile, HITS)){
            int nrSelectors;
            if (StringUtils.isBlank(hlSelectors)){
                nrSelectors = 1;
            } else {
                try{
                    nrSelectors = Integer.parseInt(hlSelectors);
                    if (nrSelectors < 1) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        return JsonUtils.toJson(new ApiError(""
                                , "Query parameter hit.selectors must be greater than 0"), callback);
                    }
                } catch (NumberFormatException nfe) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    return JsonUtils.toJson(new ApiError(""
                            , "Query parameter hit.selectors must be an integer"), callback);
                }
            }
            query.setParameter("hl", "on");
            query.setParameter("hl.fl", StringUtils.isBlank(hlFl) ? "*" : hlFl);
            // this sets both the Solr parameter and a separate nrSelectors variable used to limit the result set with
            query.setNrSelectors("hl.snippets", nrSelectors);
            // see EA-1570 (workaround to increase the number of characters that are being considered for highlighting)
            query.setParameter("hl.maxAnalyzedChars", hlMaxAnalyzedChars);
        }

        if (null != valueReplacements && !valueReplacements.isEmpty()){
            query.setValueReplacements(valueReplacements);
        }

        // reusability facet settings; spell check allowed, etcetera
        if (defaultOrReusabilityFacetsRequested) {
            query.setQueryFacets(RightReusabilityCategorizer.getQueryFacets());
        }
        if (StringUtils.containsIgnoreCase(profile, PORTAL) || StringUtils.containsIgnoreCase(profile, SPELLING)) {
            query.setSpellcheckAllowed(true);
        }
        if (facetsRequested && !query.hasParameter("f.DATA_PROVIDER.facet.limit") &&
            ( ArrayUtils.contains(solrFacets, "DATA_PROVIDER") ||
              ArrayUtils.contains(solrFacets, "DEFAULT")
            ) ) query.setParameter("f.DATA_PROVIDER.facet.limit", FacetParameterUtils.getLimitForDataProvider());

        try {
            SearchResults<? extends IdBean> result = createResults(wskey, profile, query, clazz);
            result.requestNumber = limitResponse.getRequestNumber();
            if (StringUtils.containsIgnoreCase(profile, "params")) {
                result.addParams(RequestUtils.getParameterMap(request), "wskey");
                result.addParam("profile", profile);
                result.addParam("start", start);
                result.addParam("rows", rows);
                result.addParam("sort", sort);
            }
            return JsonUtils.toJson(result, callback);

        } catch (SolrTypeException e) {
            if (ProblemType.PAGINATION_LIMIT_REACHED.equals(e.getProblem())){
                // not a real error so we log it as a warning instead
                LOG.warn(wskey + SEARCHJSON + e.getProblem().getMessage());
            } else if (ProblemType.INVALID_THEME.equals(e.getProblem())) {
                // not a real error so we log it as a warning instead
                LOG.warn(wskey + SEARCHJSON + e.getProblem().getMessage());
                response.setStatus(400);
                return JsonUtils.toJson(new ApiError(wskey, "Theme '" +
                                                            StringUtils.substringBetween(e.getCause().getCause().toString(), "Collection \"","\" not defined") +
                                                            "' is not defined"), callback);
            } else if (ProblemType.NO_LIVE_SOLR.equals(e.getProblem()) && StringUtils.contains(query.getParameterMap().get("hl.fl"), '*')) {
                LOG.error("This is the \"field 'what' was indexed without offsets\"-error, see EA-1441 when executing search: " + SEARCHJSON);
                response.setStatus(400);
                return JsonUtils.toJson(new ApiError(wskey, "Solr indexing error, occurs when hits.fl parameter contains '*'"));
            } else if (ProblemType.SOLR_ERROR.equals(e.getProblem())){
                response.setStatus(400); // e.g. SOLR cannot handle weird parameter value
                LOG.error(wskey + SEARCHJSON + e.getProblem().getMessage(), e);
                return JsonUtils.toJson(new ApiError(wskey, (StringUtils.removeEnd(e.getProblem().getMessage(), ".") + ": "
                                                             + RegExUtils.removeFirst(e.getCause().getMessage(), "^.+?https?:.+?\\s"))));
            } else if (ProblemType.CANT_CONNECT_ZOOKEEPER.equals(e.getProblem()) ||
                       ProblemType.CANT_CONNECT_SOLR.equals(e.getProblem())) {
                response.setStatus(503); // Service temporarily unavailable
                LOG.error(wskey + SEARCHJSON + e.getProblem().getMessage(), e);
                return JsonUtils.toJson(new ApiError(wskey, (e.getProblem().getMessage() + ": " + e.getMessage())));
            } else {
                LOG.error(wskey + SEARCHJSON, e);
            }
            response.setStatus(400);
            return JsonUtils.toJson(new ApiError(wskey, e.getMessage()), callback);

        } catch (Exception e) {
            LOG.error(wskey + SEARCHJSON, e);
            response.setStatus(400);
            return JsonUtils.toJson(new ApiError(wskey, e.getMessage()), callback);
        }
    }

    private String filterQueryBuilder(Iterator<Integer> it, String queryString, String andOrOr, boolean addBrackets){
        StringBuilder filterQuery = new StringBuilder();
        if (StringUtils.isNotBlank(queryString)){
            filterQuery.append(queryString);
            filterQuery.append(" AND ");
        }
        if (addBrackets){
            filterQuery.append("(");
        }
        if (it.hasNext()){
            filterQuery.append("filter_tags:");
            filterQuery.append(it.next().toString());
        }
        while (it.hasNext()){
            filterQuery.append(andOrOr);
            filterQuery.append("filter_tags:");
            filterQuery.append(it.next().toString());
        }
        if (addBrackets){
            filterQuery.append(")");
        }
        return filterQuery.toString();
    }

    /**
     * Processes all qf parameters. Note that besides returning a new array of refinements we may add new filterTags to
     * the provided filterTags list (if there are image, sound, video or mimetype refinements)
     */
    private String[] processQfParameters(String[] refinementArray, Boolean media, Boolean thumbnail, Boolean fullText,
                                         Boolean landingPage, List<Integer> filterTags) {
        boolean      hasImageRefinements            = false;
        boolean      hasSoundRefinements            = false;
        boolean      hasVideoRefinements            = false;
        List<String> newRefinements                 = new ArrayList<>();
        List<String> imageMimeTypeRefinements       = new ArrayList<>();
        List<String> soundMimeTypeRefinements       = new ArrayList<>();
        List<String> videoMimeTypeRefinements       = new ArrayList<>();
        List<String> otherMimeTypeRefinements       = new ArrayList<>();
        List<String> imageSizeRefinements           = new ArrayList<>();
        List<String> imageAspectRatioRefinements    = new ArrayList<>();
        List<String> soundDurationRefinements       = new ArrayList<>();
        List<String> videoDurationRefinements       = new ArrayList<>();
        List<String> imageColourSpaceRefinements    = new ArrayList<>();
        List<String> videoHDRefinements             = new ArrayList<>();
        List<String> soundHQRefinements             = new ArrayList<>();
        // NOTE: ColourPalette is a *parameter*; imageColourPaletteRefinements are *facets*
        List<String> imageColourPaletteRefinements  = new ArrayList<>();

        // retrieves the faceted refinements from the QF part of the request and stores them separately
        // the rest of the refinements is kept in the refinementArray
        // NOTE prefixes are case sensitive, only uppercase cf:params are recognised
        // ALSO NOTE that the suffixes are NOT case sensitive. They are all made lowercase, except 'colourpalette'
        if (refinementArray != null) {
            for (String qf : refinementArray) {
                if (StringUtils.contains(qf, ":")){
                    String refinementValue = StringUtils.substringAfter(qf, ":").toLowerCase(Locale.GERMAN);
                    switch (StringUtils.substringBefore(qf, ":")) {
                        case "MIME_TYPE":
                            if (CommonTagExtractor.isImageMimeType(refinementValue)) {
                                imageMimeTypeRefinements.add(refinementValue);
                                hasImageRefinements = true;
                            } else if (CommonTagExtractor.isSoundMimeType(refinementValue)) {
                                soundMimeTypeRefinements.add(refinementValue);
                                hasSoundRefinements = true;
                            } else if (CommonTagExtractor.isVideoMimeType(refinementValue)) {
                                videoMimeTypeRefinements.add(refinementValue);
                                hasVideoRefinements = true;
                            } else otherMimeTypeRefinements.add(refinementValue);
                            break;
                        case "IMAGE_SIZE":
                            imageSizeRefinements.add(refinementValue);
                            hasImageRefinements = true;
                            break;
                        case "IMAGE_COLOUR":
                        case "IMAGE_COLOR":
                            if (Boolean.valueOf(refinementValue)) {
                                imageColourSpaceRefinements.add("rgb");
                                hasImageRefinements = true;
                            } else if (StringUtils.equalsIgnoreCase(refinementValue, "false")){
                                imageColourSpaceRefinements.add("grayscale");
                                hasImageRefinements = true;
                            }
                            break;
                        case "COLOURPALETTE":
                        case "COLORPALETTE":
                            imageColourPaletteRefinements.add(refinementValue.toUpperCase(Locale.GERMAN));
                            hasImageRefinements = true;
                            break;
                        case "IMAGE_ASPECTRATIO":
                            imageAspectRatioRefinements.add(refinementValue);
                            hasImageRefinements = true;
                            break;
                        case "SOUND_HQ":
                            soundHQRefinements.add(Boolean.valueOf(refinementValue) ? "true" : "false");
                            hasSoundRefinements = true;
                            break;
                        case "SOUND_DURATION":
                            soundDurationRefinements.add(refinementValue);
                            hasSoundRefinements = true;
                            break;
                        case "VIDEO_HD":
                            videoHDRefinements.add(Boolean.valueOf(refinementValue) ? "true" : "false");
                            hasVideoRefinements = true;
                            break;
                        case "VIDEO_DURATION":
                            videoDurationRefinements.add(refinementValue);
                            hasVideoRefinements = true;
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
        if (hasImageRefinements) {
            filterTags.addAll(TagUtils.imageFilterTags(imageMimeTypeRefinements, imageSizeRefinements, imageColourSpaceRefinements,
                                                       imageAspectRatioRefinements, imageColourPaletteRefinements));
        }
        if (hasSoundRefinements) {
            filterTags.addAll(TagUtils.soundFilterTags(soundMimeTypeRefinements, soundHQRefinements, soundDurationRefinements));
        }
        if (hasVideoRefinements) {
            filterTags.addAll(TagUtils.videoFilterTags(videoMimeTypeRefinements, videoHDRefinements, videoDurationRefinements));
        }
        if (!otherMimeTypeRefinements.isEmpty()) {
            filterTags.addAll(TagUtils.otherFilterTags(otherMimeTypeRefinements));
        }
        if (LOG.isDebugEnabled()) {
            for (Integer filterTag : filterTags) {
                if (filterTag != null) {
                    LOG.debug("filterTag = " + Integer.toBinaryString(filterTag));
                }
            }
        }
        return newRefinements.toArray(new String[0]);
    }

    /**
     * Due to changed capitalization of country names between UIM and Metis data we need to convert lowercase country
     * names (UIM) to camel-case names (Metis). This fix is to prevent errors during the UIM-to-Metis switch, but also
     * to support saved queries for the time being. See also ticket EA-1350
     * @param queryPart either query or qf part that is to be converted
     * @return converted query or qf part
     */
    private String fixCountryCapitalization(String queryPart) {
        String result = queryPart;
        Matcher matcher = COUNTRY_PATTERN.matcher(result);
        while (matcher.find()) {
            // if group 1 has content then we found country with quotes, if group 2 has content then it's a country without quotes
            String countryName = matcher.group(1);
            boolean withQuotes = StringUtils.isNotEmpty(countryName);
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
                LOG.debug("Found {} at {},{}, countryName is {}", matcher.group(), matcher.start(), matcher.end(), countryName);
                LOG.debug("Converted to {}", result);
            }
        }
        return result;
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
        if (BriefBean.class.equals(clazz)) return BriefBeanImpl.class;
        if (RichBean.class.equals(clazz)) return RichBeanImpl.class;
        return ApiBeanImpl.class;
    }

    @SuppressWarnings("unchecked")
    private <T extends IdBean> SearchResults<T> createResults(
            String apiKey,
            String profile,
            Query query,
            Class<T> clazz)
            throws EuropeanaException {
        SearchResults<T> response = new SearchResults<>(apiKey);
        ResultSet<T>     resultSet;
        if (StringUtils.containsIgnoreCase(profile, DEBUG)) {
            resultSet = searchService.search(clazz, query, true);
        } else {
            resultSet = searchService.search(clazz, query);
        }
        response.totalResults = resultSet.getResultSize();
        if ( StringUtils.isNotBlank(resultSet.getCurrentCursorMark()) &&
             StringUtils.isNotBlank(resultSet.getNextCursorMark()) &&
             !resultSet.getNextCursorMark().equalsIgnoreCase(resultSet.getCurrentCursorMark())
        ) response.nextCursor = resultSet.getNextCursorMark();

        response.itemsCount = resultSet.getResults().size();
        response.items = resultSet.getResults();

        List<T> beans = new ArrayList<>();
        for (T b : resultSet.getResults()) {
            if (b instanceof RichBean) {
                beans.add((T) new RichView((RichBean) b, profile, apiKey));
            } else if (b instanceof ApiBean) {
                beans.add((T) new ApiView((ApiBean) b, profile, apiKey));
            } else if (b instanceof BriefBean) {
                beans.add((T) new BriefView((BriefBean) b, profile, apiKey));
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
            LOG.debug("results: " + beans.size());
        }

        response.items = beans;
        if (StringUtils.containsIgnoreCase(profile, FACETS) || StringUtils.containsIgnoreCase(profile, PORTAL)) {
            response.facets = new FacetWrangler().consolidateFacetList(resultSet.getFacetFields(),
                                                                       resultSet.getRangeFacets(),
                                                                       query.getTechnicalFacets(),
                                                                       query.isDefaultFacetsRequested(),
                                                                       query.getTechnicalFacetLimits(),
                                                                       query.getTechnicalFacetOffsets());
        }
        if (StringUtils.containsIgnoreCase(profile, BREADCRUMB) || StringUtils.containsIgnoreCase(profile, PORTAL)) {
            response.breadCrumbs = NavigationUtils.createBreadCrumbList(query);
        }
        if (StringUtils.containsIgnoreCase(profile, HITS) && MapUtils.isNotEmpty(resultSet.getHighlighting())) {
            response.hits = new HitMaker().createHitList(resultSet.getHighlighting(), query.getNrSelectors());
        }
        if (StringUtils.containsIgnoreCase(profile, SPELLING) || StringUtils.containsIgnoreCase(profile, PORTAL)) {
            response.spellcheck = ModelUtils.convertSpellCheck(resultSet.getSpellcheck());
        }
        if (StringUtils.containsIgnoreCase(profile, DEBUG)) {
            response.debug = resultSet.getSolrQueryString();
        }
//        if (StringUtils.containsIgnoreCase(profile, "params")) {
//            response.addParam("sort", resultSet.getSortField());
//        }
//        if (StringUtils.containsIgnoreCase(profile, "suggestions") ||
//            StringUtils.containsIgnoreCase(profile, PORTAL)) {
//        }
        return response;
    }

    /**
     *
     * @return the JSON response
     * @deprecated 2018-01-09 search with coordinates functionality
     */
    @SwaggerIgnore
    @RequestMapping(value = "/v2/search.kml", method = {RequestMethod.GET},
                    produces = {"application/vnd.google-earth.kml+xml", MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_XHTML_XML_VALUE})
    @ResponseBody
    @Deprecated
    public KmlResponse searchKml(
            @RequestParam(value = "query") String queryString,
            @RequestParam(value = "qf", required = false) String[] refinementArray,
            @RequestParam(value = "start", required = false, defaultValue = "1") int start,
            @RequestParam(value = "wskey") String wskey,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        // workaround of a Spring issue
        // (https://jira.springsource.org/browse/SPR-7963)
        String[] _qf = request.getParameterMap().get("qf");
        if (_qf != null && _qf.length != refinementArray.length) {
            refinementArray = _qf;
        }

        try {
            ApiKey apiKey = apiService.findByID(wskey);
            apiService.checkNotEmpty(apiKey);
        } catch (DatabaseException e) {
            response.setStatus(401);
            throw new Exception(e);
        }
        KmlResponse kmlResponse = new KmlResponse();
        Query query =
                new Query(SearchUtils.rewriteQueryFields(queryString)).setRefinements(refinementArray)
                        .setApiQuery(true).setSpellcheckAllowed(false).setFacetsAllowed(false);
        query.setRefinements("pl_wgs84_pos_lat_long:[* TO *]");
        try {
            ResultSet<BriefBean> resultSet = searchService.search(BriefBean.class, query);
            kmlResponse.document.extendedData.totalResults.value =
                    Long.toString(resultSet.getResultSize());
            kmlResponse.document.extendedData.startIndex.value = Integer.toString(start);
            kmlResponse.setItems(resultSet.getResults());
        } catch (SolrTypeException e) {
            response.setStatus(500);
            throw new Exception(e);
        }
        return kmlResponse;
    }

    /**
     * Handles an opensearch query (see also https://en.wikipedia.org/wiki/OpenSearch)
     *
     * @param queryString the search terms used to query the Europeana repository; similar to the query parameter in the
     *                   search method.
     * @param start the first object in the search result set to start with (first item = 1), e.g., if a result set is
     *              made up of 100 objects, you can set the first returned object to the 22nd object in the set
     *              [optional parameter, default = 1]
     * @param count the number of search results to return; possible values can be any integer up to 100 [optional
     *              parameter, default = 12]
     * @return rss response of the query
     */
    @ApiOperation(value = "basic search function following the OpenSearch specification", nickname = "openSearch")
    @RequestMapping(value = "/v2/opensearch.rss", method = {RequestMethod.GET, RequestMethod.POST}
            , produces = {"application/rss+xml",
            MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_XHTML_XML_VALUE})
    @ResponseBody
    public ModelAndView openSearchRss(
            @RequestParam(value = "searchTerms") String queryString,
            @RequestParam(value = "startIndex", required = false, defaultValue = "1") int start,
            @RequestParam(value = "count", required = false, defaultValue = "12") int count,
            HttpServletResponse response) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("openSearch query with terms: " + queryString);
        }
        ControllerUtils.addResponseHeaders(response);
        RssResponse rss = new RssResponse();

        Channel channel = rss.channel;
        channel.startIndex.value = start;
        channel.itemsPerPage.value = count;
        channel.query.searchTerms = queryString;
        channel.query.startPage = start;

        try {
            Query query = new Query(SearchUtils.rewriteQueryFields(queryString)).setApiQuery(true)
                    .setPageSize(count).setStart(start - 1).setFacetsAllowed(false)
                    .setSpellcheckAllowed(false);
            ResultSet<BriefBean> resultSet = searchService.search(BriefBean.class, query);
            channel.totalResults.value = resultSet.getResultSize();
            for (BriefBean bean : resultSet.getResults()) {
                Item item = new Item();
                item.guid = urlService.getRecordPortalUrl(bean.getId());
                item.title = getTitle(bean);
                item.description = getDescription(bean);
                item.link = item.guid;
                channel.items.add(item);
            }
        } catch (EuropeanaException e) {
            response.setStatus(400);
            String errorMsg = "Error executing openSearch query '" + queryString + "'";
            LOG.error(errorMsg, e);
            channel.totalResults.value = 0;
            Item item = new Item();
            item.title = "Error";
            item.description = e.getMessage();
            channel.items.add(item);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Returning rss result: " + rss);
        }

        String xml = xmlUtils.toString(rss);
        Map<String, Object> model = new HashMap<>();
        model.put("rss", xml);

        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/xml");

        return new ModelAndView("rss", model);
    }

    /**
     * returns ModelAndView containing RSS data to populate the Google Field
     * Trip app for some selected collections
     *
     * @param queryTerms  the collection ID, e.g. "europeana_collectionName:91697*"
     * @param offset      list items from this index on
     * @param limit       max number of items to list
     * @param profile     should be "FieldTrip"
     * @param reqLanguage if supplied, the API returns only those items having a dc:language that match this language
     * @param response    servlet response object
     * @return ModelAndView instance
     */
    @SwaggerIgnore
    @ApiOperation(value = "Google Fieldtrip formatted RSS of selected collections", nickname = "fieldTrip")
    @RequestMapping(value = "/v2/search.rss", method = {RequestMethod.GET, RequestMethod.POST},
                    produces = {MediaType.APPLICATION_XML_VALUE, MediaType.ALL_VALUE})
    public ModelAndView fieldTripRss(
            @RequestParam(value = "query") String queryTerms,
            @RequestParam(value = "offset", required = false, defaultValue = "1") int offset,
            @RequestParam(value = "limit", required = false, defaultValue = "12") int limit,
            @RequestParam(value = "profile", required = false, defaultValue = "FieldTrip") String profile,
            @RequestParam(value = "language", required = false) String reqLanguage,
            HttpServletResponse response) {
        ControllerUtils.addResponseHeaders(response);

        FieldTripResponse rss = new FieldTripResponse();
        FieldTripChannel channel = rss.channel;
        FieldTripUtils fieldTripUtils = new FieldTripUtils(urlService);

        if  (queryTerms == null || "".equalsIgnoreCase(queryTerms) || "".equals(getIdFromQueryTerms(queryTerms))){
            response.setStatus(400);
            String errorMsg = "error: Query ('" + queryTerms + "') is malformed, can't retrieve collection ID";
            LOG.error(errorMsg);
            FieldTripItem item = new FieldTripItem();
            item.title = "Error";
            item.description = errorMsg;
            channel.items.add(item);
        } else {

            String collectionID = getIdFromQueryTerms(queryTerms);
            Map<String, String> gftChannelAttributes = configuration.getGftChannelAttributes(collectionID);

            if (gftChannelAttributes.isEmpty() || gftChannelAttributes.size() < 5) {
                LOG.error("error: one or more attributes are not defined in europeana.properties for [INSERT COLLECTION ID HERE]");
                channel.title = "error retrieving attributes";
                channel.description = "error retrieving attributes";
                channel.language = "--";
                channel.link = "error retrieving attributes";
                channel.image = null;
            } else {
                channel.title = gftChannelAttributes.get(reqLanguage + "_title") == null || gftChannelAttributes.get(reqLanguage + "_title").equalsIgnoreCase("")
                                ? (gftChannelAttributes.get("title") == null
                                   || gftChannelAttributes.get("title").equalsIgnoreCase("") ? "no title defined" : gftChannelAttributes.get("title")) :
                                gftChannelAttributes.get(reqLanguage + "_title");
                channel.description = gftChannelAttributes.get(reqLanguage + "_description") == null
                                      || gftChannelAttributes.get(reqLanguage + "_description").equalsIgnoreCase("")
                                      ? (gftChannelAttributes.get("description") == null
                                         || gftChannelAttributes.get("description").equalsIgnoreCase("") ? "no description defined" : gftChannelAttributes.get("description")) :
                                      gftChannelAttributes.get(reqLanguage + "_description");
                channel.language = gftChannelAttributes.get("language") == null
                                   || gftChannelAttributes.get("language").equalsIgnoreCase("") ? "--" : gftChannelAttributes.get("language");
                channel.link = gftChannelAttributes.get("link") == null
                               || gftChannelAttributes.get("link").equalsIgnoreCase("") ? "no link defined" : gftChannelAttributes.get("link");
                channel.image = gftChannelAttributes.get("image") == null
                                || gftChannelAttributes.get("image").equalsIgnoreCase("") ? null : new FieldTripImage(gftChannelAttributes.get("image"));
            }

            if (StringUtils.equals(profile, "FieldTrip")) {
                offset++;
            }
            try {
                Query query = new Query(SearchUtils.rewriteQueryFields(queryTerms)).setApiQuery(true).setPageSize(limit)
                        .setStart(offset - 1).setFacetsAllowed(false).setSpellcheckAllowed(false);
                ResultSet<RichBean> resultSet = searchService.search(RichBean.class, query);
                for (RichBean bean : resultSet.getResults()) {
                    if (reqLanguage == null || getDcLanguage(bean).equalsIgnoreCase(reqLanguage)) {
                        channel.items.add(fieldTripUtils.createItem(bean));
                    }
                }
            } catch (EuropeanaException|MissingResourceException e) {
                LOG.error("error: " + e.getLocalizedMessage());
                FieldTripItem item = new FieldTripItem();
                item.title = "Error";
                item.description = e.getMessage();
                channel.items.add(item);
            }
        }
        String xml = fieldTripUtils.cleanRss(xmlUtils.toString(rss));

        Map<String, Object> model = new HashMap<>();
        model.put("rss", xml);

        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/xml");

        return new ModelAndView("rss", model);
    }

    /**
     * Temporary method to facilitate debugging the facet tags
     *
     * @return the JSON response
     */
    @SwaggerIgnore
    @GetMapping(value = "/v2/tagdecoder.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public ModelAndView searchJson(
            @RequestParam(value = "tag") String tag) {

        String decodedTagName;
        String decodedTagValue;
        if (tag.matches("[0-9]+") && tag.length() > 7) {
            decodedTagName = decodeFacetTag(Integer.valueOf(tag), true);
            decodedTagValue = decodeFacetTag(Integer.valueOf(tag), false);
        } else {
            decodedTagName = "You're not doing it right, dude / diderina";
            decodedTagValue = "a tag must be numerical and 8 digits long";
        }

        return JsonUtils.toJson(decodedTagName + ": " + decodedTagValue);
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
        return bean.getDataProvider()[0] + " " + bean.getId();
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
        if (bean.getDcCreator() != null && bean.getDcCreator().length > 0
            && StringUtils.isNotBlank(bean.getDcCreator()[0])) {
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

    private String getDcLanguage(BriefBean bean) {
        if (bean.getDcLanguage() != null && bean.getDcLanguage().length > 0
            && StringUtils.isNotBlank(bean.getDcLanguage()[0])) {
            return bean.getDcLanguage()[0];
        } else {
            return "";
        }
    }

    /**
     * retrieves the numerical part of the substring between the ':' and '*'
     * characters.
     * <p>e.g. "europeana_collectionName:91697*" will result in "91697"
     *
     * @param queryTerms provided String
     * @return String containing the Europeana collection ID only
     */
    private String getIdFromQueryTerms(String queryTerms) {
        return queryTerms.substring(queryTerms.indexOf(':'), queryTerms.indexOf('*')).replaceAll(
                "\\D+", "");
    }
}
