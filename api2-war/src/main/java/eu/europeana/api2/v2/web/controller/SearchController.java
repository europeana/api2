/*
 * Copyright 2007-2017 The Europeana Foundation
 *
 *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 *  by the European Commission;
 *  You may not use this work except in compliance with the Licence.
 *
 *  You may obtain a copy of the Licence at:
 *  http://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under
 *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 *  any kind, either express or implied.
 *  See the Licence for the specific language governing permissions and limitations under
 *  the Licence.
 */

package eu.europeana.api2.v2.web.controller;

import eu.europeana.api2.ApiLimitException;
import eu.europeana.api2.model.json.ApiError;
import eu.europeana.api2.utils.FieldTripUtils;
import eu.europeana.api2.utils.JsonUtils;
import eu.europeana.api2.utils.XmlUtils;
import eu.europeana.api2.v2.model.LimitResponse;
import eu.europeana.api2.v2.model.json.SearchResults;
import eu.europeana.api2.v2.model.json.Suggestions;
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
import eu.europeana.corelib.web.exception.ProblemType;
import eu.europeana.corelib.definitions.solr.model.Query;
import eu.europeana.corelib.edm.exceptions.SolrTypeException;
import eu.europeana.corelib.search.SearchService;
import eu.europeana.corelib.search.model.ResultSet;
import eu.europeana.corelib.search.utils.SearchUtils;
import eu.europeana.corelib.solr.bean.impl.ApiBeanImpl;
import eu.europeana.corelib.solr.bean.impl.BriefBeanImpl;
import eu.europeana.corelib.solr.bean.impl.IdBeanImpl;
import eu.europeana.corelib.solr.bean.impl.RichBeanImpl;
import eu.europeana.corelib.utils.StringArrayUtils;
import eu.europeana.corelib.web.model.rights.RightReusabilityCategorizer;
import eu.europeana.corelib.web.service.EuropeanaUrlService;
import eu.europeana.corelib.web.support.Configuration;
import eu.europeana.corelib.web.utils.NavigationUtils;
import eu.europeana.corelib.web.utils.RequestUtils;
import eu.europeana.crf_faketags.extractor.CommonTagExtractor;
import eu.europeana.crf_faketags.utils.FakeTagsUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.response.FacetField;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;


/**
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 * @author Maike (edits)
 */
@Controller
@SwaggerSelect
@Api(tags = {"Search"}, description = " ")
public class SearchController {

	private static final Logger LOG = LogManager.getLogger(SearchController.class);

    @Resource
    private SearchService searchService;

    @Resource
    private ApiKeyService apiService;

    @Resource
    private Configuration configuration;

    @Resource
    private EuropeanaUrlService urlService;

    @Resource
    private ApiKeyUtils apiKeyUtils;

    @Resource(name = "api2_mvc_xmlUtils")
    private XmlUtils xmlUtils;

    /**
     * Returns a list of Europeana datasets based on the search terms.
     * The response is an Array of JSON objects, each one containing the identifier and the name of a dataset.
     *
     * @return the JSON response
     */
    @ApiOperation(value = "search for records", nickname = "searchRecords", response = java.lang.Void.class)
//	@ApiResponses(value = {@ApiResponse(code = 200, message = "OK") })
    @RequestMapping(value = "/v2/search.json", method = {RequestMethod.GET}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ModelAndView searchJson(
			@RequestParam(value = "wskey", required = true) String wskey,
			@RequestParam(value = "query", required = true) String queryString,
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
            HttpServletRequest request,
            HttpServletResponse response) throws ApiLimitException {

        // do apikey check before anything else
        LimitResponse limitResponse = apiKeyUtils.checkLimit(wskey, request.getRequestURL().toString(), RecordType.SEARCH, profile);

//        String[] refinementAndThemeArray;

        // check query parameter
        if (StringUtils.isBlank(queryString)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return JsonUtils.toJson(new ApiError("", "Empty query parameter"), callback);
        }
        queryString = queryString.trim();
        // #579 rights URL's don't match well to queries containing ":https*"
        queryString = queryString.replace(":https://", ":http://");
        if (LOG.isInfoEnabled()) {
            LOG.info("QUERY: |" + queryString + "|");
        }

        // check other parameters
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
        if (_qf != null && _qf.length != refinementArray.length) refinementArray = _qf;

        if (StringUtils.isNotBlank(theme)){
            if (StringUtils.containsAny(theme, "+ #%^&*-='\"<>`!@[]{}\\/|")){
                return JsonUtils.toJson(new ApiError("", "Parameter 'theme' accepts one value only"), callback);
            } else {
                refinementArray = (String[]) ArrayUtils.add(refinementArray, "collection:" + theme);
            }
        }

        List<String> colourPalette = new ArrayList();
        if (ArrayUtils.isNotEmpty(colourPaletteArray)) StringArrayUtils.addToList(colourPalette, colourPaletteArray);
        colourPalette.replaceAll(String::toUpperCase);

        String colourPalettefilterQuery = "";
        Boolean hasImageRefinements = false;
        Boolean hasSoundRefinements = false;
        Boolean hasVideoRefinements = false;
        List<String> newRefinements = new ArrayList<>();
        List<String> imageMimeTypeRefinements = new ArrayList<>();
        List<String> soundMimeTypeRefinements = new ArrayList<>();
        List<String> videoMimeTypeRefinements = new ArrayList<>();
        List<String> otherMimeTypeRefinements = new ArrayList<>();
        List<String> imageSizeRefinements = new ArrayList<>();
        List<String> imageAspectRatioRefinements = new ArrayList<>();
        List<String> soundDurationRefinements = new ArrayList<>();
        List<String> videoDurationRefinements = new ArrayList<>();
        List<String> imageColourSpaceRefinements = new ArrayList<>();
        List<String> videoHDRefinements = new ArrayList<>();
        List<String> soundHQRefinements = new ArrayList<>();
        List<String> imageColourPaletteRefinements = new ArrayList<>(); //Note: ColourPalette is a parameter; imageColourPaletteRefinements are facets

        // retrieves the faceted refinements from the QF part of the request and stores them separately
        // the rest of the refinements is kept in the refinementArray
        // NOTE prefixes are case sensitive, only uppercase cf:params are recognised
        // ALSO NOTE that the suffixes are NOT case sensitive. They are all made lowercase, except 'colourpalette'
        if (refinementArray != null) {
            for (String qf : refinementArray) {
                if (StringUtils.contains(qf, ":")){
                    String refinementValue = StringUtils.substringAfter(qf, ":").toLowerCase();
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
                            imageColourPaletteRefinements.add(refinementValue.toUpperCase());
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
                            if (null == media) media = Boolean.valueOf(refinementValue);
                            break;
                        case "THUMBNAIL":
                            if (null == thumbnail) thumbnail = Boolean.valueOf(refinementValue);
                            break;
                        case "TEXT_FULLTEXT":
                            if (null == fullText) fullText = Boolean.valueOf(refinementValue);
                            break;
                        case "LANDINGPAGE":
                            if (null == landingPage) landingPage = Boolean.valueOf(refinementValue);
                            break;
                        default:
                            newRefinements.add(qf);
                    }
                } else {
                    newRefinements.add(qf);
                }
            }
        }
        // these
        if (null != media) newRefinements.add("has_media:" + media.toString());
        if (null != thumbnail) newRefinements.add("has_thumbnails:" + thumbnail.toString());
        if (null != fullText) newRefinements.add("is_fulltext:" + fullText.toString());
        if (null != landingPage) newRefinements.add("has_landingpage:" + landingPage.toString());

        refinementArray = newRefinements.toArray(new String[newRefinements.size()]);

        // Note that this is about the parameter 'colourpalette', not the refinement: they are processed below
        if (!colourPalette.isEmpty()) {
            Iterator<Integer> it = FakeTagsUtils.colourPaletteFilterTags(colourPalette).iterator();
            if (it.hasNext()) colourPalettefilterQuery = "filter_tags:" + it.next().toString();
            while (it.hasNext()) colourPalettefilterQuery += " AND filter_tags:" + it.next().toString();
            queryString += StringUtils.isNotBlank(queryString) ? " AND " + colourPalettefilterQuery: colourPalettefilterQuery ;
        }

        final List<Integer> filterTags = new ArrayList<>();

        // Encode the faceted refinements ...
        if (hasImageRefinements) filterTags.addAll(FakeTagsUtils.imageFilterTags(imageMimeTypeRefinements, imageSizeRefinements, imageColourSpaceRefinements,
                imageAspectRatioRefinements, imageColourPaletteRefinements));
        if (hasSoundRefinements) filterTags.addAll(FakeTagsUtils.soundFilterTags(soundMimeTypeRefinements, soundHQRefinements, soundDurationRefinements));
        if (hasVideoRefinements) filterTags.addAll(FakeTagsUtils.videoFilterTags(videoMimeTypeRefinements, videoHDRefinements, videoDurationRefinements));
        if (otherMimeTypeRefinements.size() > 0) filterTags.addAll(FakeTagsUtils.otherFilterTags(otherMimeTypeRefinements));

        // add the CF filter facets to the query string like this: [existing-query] AND [refinement-1 OR refinement-2 OR
        // refinement-3 ... ]
        String filterTagQuery = "";
        if (!filterTags.isEmpty()) {
            Iterator<Integer> it = filterTags.iterator();
            if (it.hasNext()) filterTagQuery = "filter_tags:" + it.next().toString();
            while (it.hasNext()) filterTagQuery += " OR filter_tags:" + it.next().toString();
            queryString += StringUtils.isNotBlank(queryString) ? " AND (" + filterTagQuery + ")" : filterTagQuery;
        }

        // TODO this isn't right -> retrieve facets from parameters; set some facet conditions
        String[] reusabilities = StringArrayUtils.splitWebParameter(reusabilityArray);
        String[] mixedFacets = StringArrayUtils.splitWebParameter(mixedFacetArray);


        boolean facetsRequested = StringUtils.containsIgnoreCase(profile, "portal") ||
                StringUtils.containsIgnoreCase(profile, "facets");
        boolean defaultFacetsRequested = facetsRequested &&
                (ArrayUtils.isEmpty(mixedFacets) ||  ArrayUtils.contains(mixedFacets, "DEFAULT"));
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

		Map<String, String> valueReplacements = new HashMap<>();
		if (ArrayUtils.isNotEmpty(reusabilities)) {
			valueReplacements = RightReusabilityCategorizer.mapValueReplacements(reusabilities, true);
            refinementArray = (String[]) ArrayUtils.addAll(
                    refinementArray,
                    valueReplacements.keySet().toArray(new String[valueReplacements.keySet().size()])
            );
        }

        // create Query object and set some params
        Class<? extends IdBean> clazz = selectBean(profile);
//        Query query = new Query(SearchUtils.rewriteQueryFields(queryString))
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

        // removed the spooky looping stuff from setting the Solr facets and their associated parameters by directly
        // passing the Maps to the Query object. Null checking happens there, too.
        if (facetsRequested) {
            Map<String, String[]> parameterMap = request.getParameterMap();
            query.setSolrFacets(solrFacets)
                    .setDefaultFacetsRequested(defaultFacetsRequested)
                    .convertAndSetSolrParameters(FacetParameterUtils.getSolrFacetParams("limit", solrFacets, parameterMap, defaultFacetsRequested))
                    .convertAndSetSolrParameters(FacetParameterUtils.getSolrFacetParams("offset", solrFacets, parameterMap, defaultFacetsRequested))
                    .setTechnicalFacets(technicalFacets)
                    .setTechnicalFacetLimits(FacetParameterUtils.getTechnicalFacetParams("limit", technicalFacets, parameterMap, defaultFacetsRequested))
                    .setTechnicalFacetOffsets(FacetParameterUtils.getTechnicalFacetParams("offset", technicalFacets, parameterMap, defaultFacetsRequested))
                    .setFacetsAllowed(true);
        } else {
            query.setFacetsAllowed(false);
        }

		query.setValueReplacements(valueReplacements);

		// reusability facet settings; spell check allowed, etcetera
        if (defaultOrReusabilityFacetsRequested) query.setQueryFacets(RightReusabilityCategorizer.getQueryFacets());
        if (StringUtils.containsIgnoreCase(profile, "portal") || StringUtils.containsIgnoreCase(profile, "spelling")) query.setSpellcheckAllowed(true);
        if (facetsRequested && !query.hasParameter("f.DATA_PROVIDER.facet.limit") &&
                    ( ArrayUtils.contains(solrFacets, "DATA_PROVIDER") ||
                      ArrayUtils.contains(solrFacets, "DEFAULT")
                    ) ) query.setParameter("f.DATA_PROVIDER.facet.limit", (String.valueOf(FacetParameterUtils.LIMIT_FOR_DATA_PROVIDER)));

        // do the search
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
            if (LOG.isDebugEnabled()) {
                LOG.debug("got response " + result.items.size());
            }
            return JsonUtils.toJson(result, callback);

        } catch (SolrTypeException e) {
            if(e.getProblem().equals(ProblemType.PAGINATION_LIMIT_REACHED)) {
                // not a real error so we log it as a warning instead
                LOG.warn(wskey + " [search.json] " + ProblemType.PAGINATION_LIMIT_REACHED.getMessage());
            } else if (e.getProblem().equals(ProblemType.INVALID_THEME)) {
                // not a real error so we log it as a warning instead
                LOG.warn(wskey + " [search.json] " + ProblemType.INVALID_THEME.getMessage());
                return JsonUtils.toJson(new ApiError(wskey, "Theme '" +
                      StringUtils.substringBetween(e.getCause().getCause().toString(), "Collection \"","\" not defined") +
                "' is not defined"), callback);
            } else {
                LOG.error(wskey + " [search.json] ", e);
            }
            response.setStatus(400);
            return JsonUtils.toJson(new ApiError(wskey, e.getMessage()), callback);

        } catch (Exception e) {
            LOG.error(wskey + " [search.json] " + e.getClass().getSimpleName(), e);
            response.setStatus(400);
            return JsonUtils.toJson(new ApiError(wskey, e.getMessage()), callback);
        }
    }

    private Class<? extends IdBean> selectBean(String profile) {
        Class<? extends IdBean> clazz;
        if (StringUtils.containsIgnoreCase(profile, "minimal")) clazz = BriefBean.class;
        else if (StringUtils.containsIgnoreCase(profile, "rich")) clazz = RichBean.class;
        else clazz = ApiBean.class;
        return clazz;
    }

    private Class<? extends IdBeanImpl> getBeanImpl(Class clazz) {
        if (BriefBean.class.equals(clazz)) return BriefBeanImpl.class;
        if (RichBean.class.equals(clazz)) return RichBeanImpl.class;
        return ApiBeanImpl.class;
    }

    @ApiOperation(value = "get autocompletion recommendations for search queries", nickname = "suggestions")
    @RequestMapping(value = "/v2/suggestions.json", method = {RequestMethod.GET}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ModelAndView suggestionsJson(
            @RequestParam(value = "query", required = true) String query,
            @RequestParam(value = "rows", required = false, defaultValue = "10") int count,
            @RequestParam(value = "phrases", required = false, defaultValue = "false") boolean phrases,
            @RequestParam(value = "callback", required = false) String callback,
            HttpServletResponse response) {
        ControllerUtils.addResponseHeaders(response);
        if (LOG.isInfoEnabled()) LOG.info("phrases: " + phrases);
        Suggestions apiResponse = new Suggestions(null);
        try {
            apiResponse.items = searchService.suggestions(query, count);
            apiResponse.itemsCount = apiResponse.items.size();
        } catch (SolrTypeException e) {
            return JsonUtils.toJson(new ApiError(null, e.getMessage()), callback);
        }
        return JsonUtils.toJson(apiResponse, callback);
    }

    @SuppressWarnings("unchecked")
    private <T extends IdBean> SearchResults<T> createResults(
            String apiKey,
            String profile,
            Query query,
            Class<T> clazz)
            throws SolrTypeException {
        FacetWrangler wrangler = new FacetWrangler();
        SearchResults<T> response = new SearchResults<>(apiKey);
        ResultSet<T>     resultSet;
        if (StringUtils.containsIgnoreCase(profile, "debug")) {
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
                if (b instanceof RichBean) beans.add((T) new RichView((RichBean) b, profile, apiKey));
                else if (b instanceof ApiBean) beans.add((T) new ApiView((ApiBean) b, profile, apiKey));
                else if (b instanceof BriefBean) beans.add((T) new BriefView((BriefBean) b, profile, apiKey));
            }

            List<FacetField> facetFields = resultSet.getFacetFields();
            if (resultSet.getQueryFacets() != null) {
                List<FacetField> allQueryFacetsMap = SearchUtils.extractQueryFacets(resultSet.getQueryFacets());
                if (!allQueryFacetsMap.isEmpty()) facetFields.addAll(allQueryFacetsMap);
            }

            if (LOG.isDebugEnabled()) LOG.debug("beans: " + beans.size());

            response.items = beans;
        if (StringUtils.containsIgnoreCase(profile, "facets") ||
            StringUtils.containsIgnoreCase(profile, "portal")) {
            response.facets = wrangler.consolidateFacetList(resultSet.getFacetFields(),
                    query.getTechnicalFacets(), query.isDefaultFacetsRequested(),
                    query.getTechnicalFacetLimits(), query.getTechnicalFacetOffsets());
        }
            if (StringUtils.containsIgnoreCase(profile, "breadcrumb") ||
                    StringUtils.containsIgnoreCase(profile, "portal")) {
                response.breadCrumbs = NavigationUtils.createBreadCrumbList(query);
            }
            if (StringUtils.containsIgnoreCase(profile, "spelling") ||
                    StringUtils.containsIgnoreCase(profile, "portal")) {
                response.spellcheck = ModelUtils.convertSpellCheck(resultSet.getSpellcheck());
            }
        if (StringUtils.containsIgnoreCase(profile, "debug")) {
            response.debug = resultSet.getSolrQueryString();
        }
//        if (StringUtils.containsIgnoreCase(profile, "params")) {
//            response.addParam("sort", resultSet.getSortField());
//        }
//        if (StringUtils.containsIgnoreCase(profile, "suggestions") ||
//            StringUtils.containsIgnoreCase(profile, "portal")) {
//        }
        return response;
    }

	/**
	 *
	 * @return the JSON response
     * @deprecated 2018-01-09 search with coordinates functionality
	 */
	@SwaggerIgnore
	@RequestMapping(value = "/v2/search.kml", method = {RequestMethod.GET}, produces = {"application/vnd.google-earth.kml+xml", MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_XHTML_XML_VALUE})
    @ResponseBody
    @Deprecated
    public KmlResponse searchKml(
			@RequestParam(value = "query", required = true) String queryString,
			@RequestParam(value = "qf", required = false) String[] refinementArray,
			@RequestParam(value = "start", required = false, defaultValue = "1") int start,
			@RequestParam(value = "wskey", required = true) String wskey,
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
            // Disabled while awaiting better implementation (ticket #1742)
            // apiLogService.logApiRequest(wskey, query.getQuery(), RecordType.SEARCH_KML, "kml");
        } catch (SolrTypeException e) {
            response.setStatus(500);
            throw new Exception(e);
        }
        return kmlResponse;
    }

    /**
     * Handles an opensearch query (see also https://en.wikipedia.org/wiki/OpenSearch)
     *
     * @param queryString the search terms used to query the Europeana repository; similar to the query parameter in the search method.
     * @param start the first object in the search result set to start with (first item = 1), e.g., if a result set is made up of 100 objects, you can set the first returned object to the 22nd object in the set [optional parameter, default = 1]
     * @param count the number of search results to return; possible values can be any integer up to 100 [optional parameter, default = 12]
     * @return rss response of the query
     */
	@ApiOperation(value = "basic search function following the OpenSearch specification", nickname = "openSearch")
	@RequestMapping(value = "/v2/opensearch.rss", method = {RequestMethod.GET}, produces = {"application/rss+xml", MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_XHTML_XML_VALUE})
    @ResponseBody
    public RssResponse openSearchRss(
			@RequestParam(value = "searchTerms", required = true) String queryString,
			@RequestParam(value = "startIndex", required = false, defaultValue = "1") int start,
			@RequestParam(value = "count", required = false, defaultValue = "12") int count) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("openSearch query with terms: " + queryString);
        }
		RssResponse rss = new RssResponse();
		Channel channel = rss.channel;
		channel.startIndex.value = start;
		channel.itemsPerPage.value = count;
		channel.query.searchTerms = queryString;
		channel.query.startPage = start;

        try {
            Query query =
                    new Query(SearchUtils.rewriteQueryFields(queryString)).setApiQuery(true)
                            .setPageSize(count).setStart(start - 1).setFacetsAllowed(false)
                            .setSpellcheckAllowed(false);
            ResultSet<BriefBean> resultSet = searchService.search(BriefBean.class, query);
            channel.totalResults.value = resultSet.getResultSize();
            for (BriefBean bean : resultSet.getResults()) {
                Item item = new Item();
                item.guid = urlService.getPortalRecord(bean.getId()).toString();
                item.title = getTitle(bean);
                item.description = getDescription(bean);
                item.link = item.guid;
                channel.items.add(item);
            }
        } catch (SolrTypeException e) {
            LOG.error("Error executing opensearch query", e);
            channel.totalResults.value = 0;
            Item item = new Item();
            item.title = "Error";
            item.description = e.getMessage();
            channel.items.add(item);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Returning rss result: "+rss);
        }
        return rss;
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
	@ApiOperation(value = "Google Fieldtrip formatted RSS of selected collections", nickname = "fieldTrip")
	@RequestMapping(value = "/v2/search.rss", method = {RequestMethod.GET}, produces = {MediaType.APPLICATION_XML_VALUE, MediaType.ALL_VALUE})
	public ModelAndView fieldTripRss(
			@RequestParam(value = "query", required = true) String queryTerms,
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
            channel.description = gftChannelAttributes.get(reqLanguage + "_description") == null || gftChannelAttributes.get(reqLanguage + "_description").equalsIgnoreCase("")
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
			} catch (SolrTypeException|MissingResourceException e) {
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
        return queryTerms.substring(queryTerms.indexOf(":"), queryTerms.indexOf("*")).replaceAll(
                "\\D+", "");
    }
}