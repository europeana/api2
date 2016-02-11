/*
 * Copyright 2007-2012 The Europeana Foundation
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.response.FacetField;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

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
import eu.europeana.api2.v2.utils.ControllerUtils;
import eu.europeana.api2.v2.utils.FacetParameterUtils;
import eu.europeana.api2.v2.utils.ModelUtils;
import eu.europeana.api2.v2.web.swagger.SwaggerIgnore;
import eu.europeana.api2.v2.web.swagger.SwaggerSelect;
import eu.europeana.corelib.db.entity.enums.RecordType;
import eu.europeana.corelib.db.exception.DatabaseException;
import eu.europeana.corelib.db.exception.LimitReachedException;
import eu.europeana.corelib.db.service.ApiKeyService;
import eu.europeana.corelib.definitions.db.entity.relational.ApiKey;
import eu.europeana.corelib.definitions.edm.beans.ApiBean;
import eu.europeana.corelib.definitions.edm.beans.BriefBean;
import eu.europeana.corelib.definitions.edm.beans.IdBean;
import eu.europeana.corelib.definitions.edm.beans.RichBean;
import eu.europeana.corelib.definitions.exception.ProblemType;
import eu.europeana.corelib.definitions.solr.Facet;
import eu.europeana.corelib.definitions.solr.model.Query;
import eu.europeana.corelib.edm.exceptions.SolrTypeException;
import eu.europeana.corelib.logging.Log;
import eu.europeana.corelib.logging.Logger;
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
import eu.europeana.crf_faketags.utils.FakeTagsUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 * @author Maike (edits)
 */
@Controller
@SwaggerSelect
@Api(tags = {"Search"}, description = " ")
public class SearchController {

	final static public int FACET_LIMIT = 16;

	@Log
	private Logger log;

    @Resource
    private SearchService searchService;

    @Resource
    private ApiKeyService apiService;

    @Resource
    private Configuration configuration;

    @Resource
    private EuropeanaUrlService urlService;

    @Resource
    private ControllerUtils controllerUtils;

    @Resource(name = "api2_mvc_xmlUtils")
    private XmlUtils xmlUtils;

    @Resource
    private MessageSource messageSource;

	/**
	 * Limits the number of facets
	 *
	 * @param facets                   The user entered facet names list
	 * @param isDefaultFacetsRequested Flag if default facets should be returned
	 * @return                         The limited set of facet names
	 */
	public static String[] limitFacets(String[] facets, boolean isDefaultFacetsRequested) {
		List<String> requestedFacets = Arrays.asList(facets);
		List<String> allowedFacets = new ArrayList<>();

		int count = 0;
		if (isDefaultFacetsRequested && !requestedFacets.contains("DEFAULT")) {
			count = Facet.values().length;
		}

		int increment;
		for (String facet : requestedFacets) {
			increment = 1;
			if (StringUtils.equals(facet, "DEFAULT")) {
				increment = Facet.values().length;
			}
			if (count + increment <= FACET_LIMIT) {
				allowedFacets.add(facet);
				count += increment;
			} else {
				break;
			}
		}

		return allowedFacets.toArray(new String[allowedFacets.size()]);
	}

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
			@RequestParam(value = "query", required = false) String queryString,
            @RequestParam(value = "qf", required = false) String[] refinements,
            @RequestParam(value = "reusability", required = false) String[] aReusability,
            @RequestParam(value = "profile", required = false, defaultValue = "standard") String profile,
            @RequestParam(value = "start", required = false, defaultValue = "1") int start,
            @RequestParam(value = "rows", required = false, defaultValue = "12") int rows,
            @RequestParam(value = "facet", required = false) String[] aFacet,
            @RequestParam(value = "sort", required = false) String sort,
            @RequestParam(value = "colourpalette", required = false) String[] colorPalette,
            @RequestParam(value = "text_fulltext", required = false) Boolean isFulltext,
            @RequestParam(value = "thumbnail", required = false) Boolean thumbnail,
            @RequestParam(value = "media", required = false) Boolean media,
            @RequestParam(value = "sound_duration", required = false) String[] sound_duration,
            @RequestParam(value = "sound_hq", required = false) Boolean sound_hq,
            @RequestParam(value = "video_duration", required = false) String[] video_duration,
            @RequestParam(value = "video_hd", required = false) Boolean video_hd,
            @RequestParam(value = "image_colour", required = false) Boolean image_colour,
            @RequestParam(value = "image_aspectratio", required = false) String[] image_aspectratio,
            @RequestParam(value = "image_size", required = false) String[] image_size,
            @RequestParam(value = "has_landingpage", required = false) Boolean hasLandingPage,
            @RequestParam(value = "cursor", required = false) String cursorMark,
            @RequestParam(value = "callback", required = false) String callback,
            HttpServletRequest request,
            HttpServletResponse response) {

        LimitResponse limitResponse;
        try {
            limitResponse = controllerUtils.checkLimit(wskey, request.getRequestURL().toString(),
                    RecordType.SEARCH, profile);
        } catch (ApiLimitException e) {
            response.setStatus(e.getHttpStatus());
            return JsonUtils.toJson(new ApiError(e), callback);
        }

        if (StringUtils.isBlank(queryString)) {
            response.setStatus(400);
            return JsonUtils.toJson(new ApiError("", (queryString == null ? "Missing" : "Invalid") + " query parameter"), callback);
        }
        queryString = queryString.trim();
        log.info("QUERY: |" + queryString + "|");

        // workaround of a Spring issue
        // (https://jira.springsource.org/browse/SPR-7963)
        String[] _qf = request.getParameterMap().get("qf");
        if (_qf != null && _qf.length != refinements.length) {
            refinements = _qf;
        }

        final List<String> newRefinements = new ArrayList<>();
        final List<String> mediaTypes = new ArrayList<>();
        final List<String> mimeTypes = new ArrayList<>();
        final List<String> imageSizes = new ArrayList<>();
        final List<Boolean> imageColors = new ArrayList<>();
        final List<Boolean> imageGrayScales = new ArrayList<>();
        final List<String> imageAspectRatios = new ArrayList<>();
        final List<String> imageColorsPalette = new ArrayList<>();
        final List<Boolean> soundHQs = new ArrayList<>();
        final List<String> soundDurations = new ArrayList<>();
        final List<Boolean> videoHDs = new ArrayList<>();
        final List<String> videoDurations = new ArrayList<>();
        List<Integer> imageFilterTags = FakeTagsUtils.imageFilterTags(mimeTypes, imageSizes, imageColors, imageGrayScales, imageAspectRatios);
        final Integer imageFilterTag = imageFilterTags.isEmpty() ? null : imageFilterTags.get(0);
        List<Integer> soundFilterTags = FakeTagsUtils.soundFilterTags(mimeTypes, soundHQs, soundDurations);
        final Integer soundFilterTag = soundFilterTags.isEmpty() ? null : soundFilterTags.get(0);
        List<Integer> videoFilterTags = FakeTagsUtils.videoFilterTags(mimeTypes, videoHDs, videoDurations);
        final Integer videoFilterTag = videoFilterTags.isEmpty() ? null : videoFilterTags.get(0);

        if (cursorMark != null) {
            if (start > 1) {
                response.setStatus(400);
                return JsonUtils.toJson(new ApiError("", "Parameters 'start' and 'cursorMark' cannot be used together"), callback);
            }
        }
        // exclude sorting on timestamp, #238
        if (sort != null && (sort.equalsIgnoreCase("timestamp") || sort.toLowerCase().startsWith("timestamp "))){
            sort = "";
        }

        StringArrayUtils.addToList(soundDurations, sound_duration);
        if (sound_hq != null) {
            soundHQs.add(sound_hq);
        }

        StringArrayUtils.addToList(videoDurations, video_duration);
        if (video_hd != null) {
            videoHDs.add(video_hd);
        }

        StringArrayUtils.addToList(imageAspectRatios, image_aspectratio);
        if (image_colour != null) {
            if (image_colour) {
                imageColors.add(true);
            } else {
                imageGrayScales.add(true);
            }
        }
        StringArrayUtils.addToList(imageSizes, image_size);
        StringArrayUtils.addToList(imageColorsPalette, colorPalette);

        if (refinements != null) {
            for (String qf : refinements) {
                log.info("QF: " + qf);
                final Integer colonIndex = qf.indexOf(":");
                if (colonIndex == -1) {
                    newRefinements.add(qf);
                    continue;
                }
                final String prefix = qf.substring(0, colonIndex).toLowerCase();
                final String suffix = qf.substring(colonIndex + 1).toLowerCase();

                log.info("prefix: " + prefix);
                log.info("suffix: " + suffix);

                switch (prefix.toLowerCase()) {
                    case "text_fulltext":
                        isFulltext = (isFulltext == null ? false : isFulltext) || Boolean.parseBoolean(suffix);
                        break;
                    case "has_thumbnail":
                        thumbnail = (thumbnail == null ? false : thumbnail) || Boolean.parseBoolean(suffix);
                        break;
                    case "has_media":
                        media = (media == null ? false : media) || Boolean.parseBoolean(suffix);
                        break;
//                    case "onetagpercolour":
//                        imageColorsPalette.add(suffix);
//                        break;
                    case "type":
                        mediaTypes.add(suffix);
                        newRefinements.add(qf);
                        break;
                    case "mime_type":
                        mimeTypes.add(suffix);
                        break;
                    case "image_size":
                        imageSizes.add(suffix);
                        break;
                    case "image_colour":
                    case "image_color":
                        if (Boolean.valueOf(suffix)) {
                            imageColors.add(true);
                        } else {
                            imageGrayScales.add(true);
                        }
                        break;
                    case "image_greyscale":
                    case "image_grayscale":
                        imageGrayScales.add(Boolean.valueOf(suffix));
                        break;
                    case "image_aspectratio":
                        imageAspectRatios.add(suffix);
                        break;
                    case "sound_hq":
                        soundHQs.add(Boolean.valueOf(suffix));
                        break;
                    case "sound_duration":
                        soundDurations.add(suffix);
                        break;
                    case "video_hd":
                        videoHDs.add(Boolean.valueOf(suffix));
                        break;
                    case "video_duration":
                        videoDurations.add(suffix);
                        break;
                    default:
                        newRefinements.add(qf);

                }
            }
        }

        if (isFulltext != null) {
            newRefinements.add("is_fulltext:" + isFulltext);
        }

        // FilterTagGeneration
        if (thumbnail != null) {
            newRefinements.add("has_thumbnails:" + thumbnail);
        }

        if (media != null) {
            newRefinements.add("has_media:" + media);
        }

        if (hasLandingPage != null) {
            newRefinements.add("has_landingpage:" + hasLandingPage);
        }

        refinements = newRefinements.toArray(new String[newRefinements.size()]);
        log.info("New Refinements: " + Arrays.toString(refinements));

        if (!imageColorsPalette.isEmpty()) {
            String filterQuery = "";
            for (String color : imageColorsPalette) {
                log.debug("Color palette: " + color);
                final Integer filterTag =
                        FakeTagsUtils.calculateTag(1, null, null, null, null, null, color, null, null, null, null);
                log.debug("Color palette: " + filterTag);
                filterQuery += "filter_tags:" + filterTag + " AND ";
            }
            if (!filterQuery.equals("")) {
                filterQuery = filterQuery.substring(0, filterQuery.lastIndexOf("AND"));
                filterQuery = filterQuery.trim();

                if (queryString.equals("")) {
                    queryString = filterQuery;
                } else {
                    queryString = queryString + " AND " + filterQuery;
                }
            }
        }

        final List<Integer> filterTags = new ArrayList<>();
        filterTags.addAll(FakeTagsUtils.imageFilterTags(mimeTypes, imageSizes, imageColors, imageGrayScales,
                imageAspectRatios));
        filterTags.addAll(FakeTagsUtils.soundFilterTags(mimeTypes, soundHQs, soundDurations));
        filterTags.addAll(FakeTagsUtils.videoFilterTags(mimeTypes, videoHDs, videoDurations));
        boolean image = false, sound = false, video = false;
        for (final String type : mediaTypes) {
            switch (type.toLowerCase()) {
                case "image":
                    image = true;
                    break;
                case "sound":
                    sound = true;
                    break;
                case "video":
                    video = true;
                    break;
            }
        }
        if (!image) {
            filterTags.remove(imageFilterTag);
        }
        if (!sound) {
            filterTags.remove(soundFilterTag);
        }
        if (!video) {
            filterTags.remove(videoFilterTag);
        }

        String filterTagQuery = "";
        for (final Integer filterTag : filterTags) {
            if (filterTag % 33554432 != 0) {
                filterTagQuery = filterTagQuery + "filter_tags:" + filterTag + " OR ";
            }
        }

        if (filterTagQuery.contains("OR")) {
            filterTagQuery = filterTagQuery.substring(0, filterTagQuery.lastIndexOf("OR"));
            filterTagQuery = filterTagQuery.trim();

            if (StringUtils.isBlank(queryString)) {
                queryString = filterTagQuery;
            } else {
                filterTagQuery = "(" + filterTagQuery + ")";
                queryString = queryString + " AND " + filterTagQuery;
            }
        }


        boolean isFacetsRequested = isFacetsRequested(profile);
        String[] reusability = StringArrayUtils.splitWebParameter(aReusability);
        String[] facets = expandFacetNames(StringArrayUtils.splitWebParameter(aFacet));
        boolean isDefaultFacetsRequested = isDefaultFacetsRequested(profile, facets);
        facets = limitFacets(facets, isDefaultFacetsRequested);

        boolean isDefaultOrReusabilityFacetRequested = isDefaultOrReusabilityFacetRequested(profile, facets);
        Map<String, Integer> facetLimits = null;
        Map<String, Integer> facetOffsets = null;
        if (isFacetsRequested) {
            Map<String, String[]> parameterMap = request.getParameterMap();
            facetLimits = FacetParameterUtils.getFacetParams("limit", aFacet, parameterMap, isDefaultFacetsRequested);
            facetOffsets = FacetParameterUtils.getFacetParams("offset", aFacet, parameterMap, isDefaultFacetsRequested);
        }

		controllerUtils.addResponseHeaders(response);
		rows = Math.min(rows, configuration.getApiRowLimit());

		Map<String, String> valueReplacements = new HashMap<>();
		if (ArrayUtils.isNotEmpty(reusability)) {
			valueReplacements = RightReusabilityCategorizer.mapValueReplacements(reusability, true);

            refinements = (String[]) ArrayUtils.addAll(
                    refinements,
                    valueReplacements.keySet().toArray(new String[valueReplacements.keySet().size()])
            );
        }

        Class<? extends IdBean> clazz = selectBean(profile);
        Query query = new Query(SearchUtils.rewriteQueryFields(queryString))
                .setApiQuery(true)
                .setRefinements(refinements)
                .setPageSize(rows)
                .setStart(start - 1)
                .setSort(sort)
                .setCurrentCursorMark(cursorMark)
                .setParameter("facet.mincount", "1")
                .setParameter("fl", IdBeanImpl.getFields(getBeanImpl(clazz)))
                .setAllowSpellcheck(false)
                .setAllowFacets(false);

		if (ArrayUtils.isNotEmpty(facets)) {
			query.setFacets(facets);
			if (facetLimits != null && !facetLimits.isEmpty()) {
				for (Map.Entry<String, Integer> entry : facetLimits.entrySet()) {
					query.setParameter(entry.getKey(), String.valueOf(entry.getValue()));
				}
			}
			if (facetOffsets != null && !facetOffsets.isEmpty()) {
				for (Map.Entry<String, Integer> entry : facetOffsets.entrySet()) {
					query.setParameter(entry.getKey(), String.valueOf(entry.getValue()));
				}
			}
		}

		query.setValueReplacements(valueReplacements);

		// reusability facet settings
		if (isDefaultOrReusabilityFacetRequested) {
			//System
			query.setFacetQueries(RightReusabilityCategorizer.getQueryFacets());
		}

		if (StringUtils.containsIgnoreCase(profile, "portal") || StringUtils.containsIgnoreCase(profile, "spelling")) {
			query.setAllowSpellcheck(true);
		}

        if (isFacetsRequested) {
            query.setAllowFacets(true);
            if (!query.hasParameter("f.DATA_PROVIDER.facet.limit")
                    && (ArrayUtils.contains(facets, "DATA_PROVIDER") || ArrayUtils
                    .contains(facets, "DEFAULT"))) {
                query.setParameter("f.DATA_PROVIDER.facet.limit", "3000");
            }
        }

        try {
            SearchResults<? extends IdBean> result = createResults(wskey, profile,
                    query, clazz);
            result.requestNumber = limitResponse.getRequestNumber();
            if (StringUtils.containsIgnoreCase(profile, "params")) {
                result.addParams(RequestUtils.getParameterMap(request), "wskey");
                result.addParam("profile", profile);
                result.addParam("start", start);
                result.addParam("rows", rows);
                result.addParam("sort", sort);
            }

            if (log.isInfoEnabled()) {
                log.info("got response " + result.items.size());
            }
            return JsonUtils.toJson(result, callback);
        } catch (SolrTypeException e) {
            if(e.getProblem().equals(ProblemType.SEARCH_LIMIT_REACHED)){
                log.error(wskey + " [search.json] " + ProblemType.SEARCH_LIMIT_REACHED.getMessage());
            } else {
                log.error(wskey + " [search.json] ", e);
            }
            response.setStatus(400);
            return JsonUtils.toJson(new ApiError(wskey, e.getMessage()), callback);
        } catch (Exception e) {
            log.error(wskey + " [search.json] " + e.getClass().getSimpleName(), e);
            response.setStatus(400);
            return JsonUtils.toJson(new ApiError(wskey, e.getMessage()), callback);
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

    @ApiOperation(value = "get autocompletion recommendations for search queries", nickname = "suggestions")
    @RequestMapping(value = "/v2/suggestions.json", method = {RequestMethod.GET}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ModelAndView suggestionsJson(
            @RequestParam(value = "query", required = true) String query,
            @RequestParam(value = "rows", required = false, defaultValue = "10") int count,
            @RequestParam(value = "phrases", required = false, defaultValue = "false") boolean phrases,
            @RequestParam(value = "callback", required = false) String callback,
            HttpServletResponse response) {
        controllerUtils.addResponseHeaders(response);
        if (log.isInfoEnabled()) {
            log.info("phrases: " + phrases);
        }
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
        SearchResults<T> response = new SearchResults<>(apiKey);
        ResultSet<T> resultSet = searchService.search(clazz, query);
        response.totalResults = resultSet.getResultSize();
        if (StringUtils.isNotBlank(resultSet.getCurrentCursorMark()) && StringUtils.isNotBlank(resultSet.getNextCursorMark())
                && !resultSet.getNextCursorMark().equalsIgnoreCase(resultSet.getCurrentCursorMark())) {
            response.nextCursor = resultSet.getNextCursorMark();
        }
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
        if (resultSet.getQueryFacets() != null) {
            List<FacetField> allQueryFacetsMap =
                    SearchUtils.extractQueryFacets(resultSet.getQueryFacets());
            if (!allQueryFacetsMap.isEmpty()) {
                facetFields.addAll(allQueryFacetsMap);
            }
        }

        if (log.isInfoEnabled()) {
            log.info("beans: " + beans.size());
        }
        response.items = beans;
        if (StringUtils.containsIgnoreCase(profile, "facets")
                || StringUtils.containsIgnoreCase(profile, "portal")) {
            response.facets = ModelUtils.conventFacetList(resultSet.getFacetFields());
        }
        if (StringUtils.containsIgnoreCase(profile, "breadcrumb")
                || StringUtils.containsIgnoreCase(profile, "portal")) {
            response.breadCrumbs = NavigationUtils.createBreadCrumbList(query);
        }
        if (StringUtils.containsIgnoreCase(profile, "spelling")
                || StringUtils.containsIgnoreCase(profile, "portal")) {
            response.spellcheck = ModelUtils.convertSpellCheck(resultSet.getSpellcheck());
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
	 */
	@SwaggerIgnore
	@RequestMapping(value = "/v2/search.kml", method = {RequestMethod.GET}, produces = {"application/vnd.google-earth.kml+xml", MediaType.ALL_VALUE})
    @ResponseBody
    public KmlResponse searchKml(
			@RequestParam(value = "query", required = true) String queryString,
			@RequestParam(value = "qf", required = false) String[] refinements,
			@RequestParam(value = "start", required = false, defaultValue = "1") int start,
			@RequestParam(value = "wskey", required = true) String wskey,
			HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		// workaround of a Spring issue
		// (https://jira.springsource.org/browse/SPR-7963)
		String[] _qf = request.getParameterMap().get("qf");
		if (_qf != null && _qf.length != refinements.length) {
			refinements = _qf;
		}

        try {
            ApiKey apiKey = apiService.findByID(wskey);
            apiService.checkReachedLimit(apiKey);
        } catch (DatabaseException e) {
            response.setStatus(401);
            throw new Exception(e);
        } catch (LimitReachedException e) {
            response.setStatus(429);
            throw new Exception(e);
        }
        KmlResponse kmlResponse = new KmlResponse();
        Query query =
                new Query(SearchUtils.rewriteQueryFields(queryString)).setRefinements(refinements)
                        .setApiQuery(true).setAllowSpellcheck(false).setAllowFacets(false);
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
            response.setStatus(429);
            throw new Exception(e);
        }
        return kmlResponse;
    }

	@ApiOperation(value = "basic search function following the OpenSearch specification", nickname = "suggestions")
	@RequestMapping(value = "/v2/opensearch.rss", method = {RequestMethod.GET}, produces = {MediaType.APPLICATION_XML_VALUE, MediaType.ALL_VALUE})
    @ResponseBody
    public RssResponse openSearchRss(
			@RequestParam(value = "searchTerms", required = true) String queryString,
			@RequestParam(value = "startIndex", required = false, defaultValue = "1") int start,
			@RequestParam(value = "count", required = false, defaultValue = "12") int count) {
		RssResponse rss = new RssResponse();
		Channel channel = rss.channel;
		channel.startIndex.value = start;
		channel.itemsPerPage.value = count;
		channel.query.searchTerms = queryString;
		channel.query.startPage = start;

        try {
            Query query =
                    new Query(SearchUtils.rewriteQueryFields(queryString)).setApiQuery(true)
                            .setPageSize(count).setStart(start - 1).setAllowFacets(false)
                            .setAllowSpellcheck(false);
            ResultSet<BriefBean> resultSet = searchService.search(BriefBean.class, query);
            channel.totalResults.value = resultSet.getResultSize();
            for (BriefBean bean : resultSet.getResults()) {
                Item item = new Item();
                item.guid = urlService.getPortalRecord(false, bean.getId()).toString();
                item.title = getTitle(bean);
                item.description = getDescription(bean);
        /*
         * String enclosure = getThumbnail(bean); if (enclosure != null) { item.enclosure = new
         * Enclosure(enclosure); }
         */
                item.link = item.guid;
                channel.items.add(item);
            }
        } catch (SolrTypeException e) {
            channel.totalResults.value = 0;
            Item item = new Item();
            item.title = "Error";
            item.description = e.getMessage();
            channel.items.add(item);
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
		controllerUtils.addResponseHeaders(response);

		FieldTripResponse rss = new FieldTripResponse();
		FieldTripChannel channel = rss.channel;
		FieldTripUtils fieldTripUtils = new FieldTripUtils(urlService);

		if  (queryTerms == null || "".equalsIgnoreCase(queryTerms) || "".equals(getIdFromQueryTerms(queryTerms))){
			response.setStatus(400);
			String errorMsg = "error: Query ('" + queryTerms + "') is malformed, can't retrieve collection ID";
			log.error(errorMsg);
			FieldTripItem item = new FieldTripItem();
			item.title = "Error";
			item.description = errorMsg;
			channel.items.add(item);
		} else {

			String collectionID = getIdFromQueryTerms(queryTerms);
			Map<String, String> gftChannelAttributes = configuration.getGftChannelAttributes(collectionID);

        if (gftChannelAttributes.isEmpty() || gftChannelAttributes.size() < 5) {
            log.error("error: one or more attributes are not defined in europeana.properties for [INSERT COLLECTION ID HERE]");
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
						.setStart(offset - 1).setAllowFacets(false).setAllowSpellcheck(false);
				ResultSet<RichBean> resultSet = searchService.search(RichBean.class, query);
				for (RichBean bean : resultSet.getResults()) {
					if (reqLanguage == null || getDcLanguage(bean).equalsIgnoreCase(reqLanguage)) {
						channel.items.add(fieldTripUtils.createItem(bean, getTranslatedEdmIsShownAtLabel(bean, reqLanguage == null ? channel.language : reqLanguage)));
					}
				}
			} catch (SolrTypeException|MissingResourceException e) {
				log.error("error: " + e.getLocalizedMessage());
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

    private boolean isFacetsRequested(String profile) {
        return StringUtils.containsIgnoreCase(profile, "portal")
                || StringUtils.containsIgnoreCase(profile, "facets");
    }

	private boolean isDefaultFacetsRequested(String profile, String[] facets) {
		return StringUtils.containsIgnoreCase(profile, "portal") ||
				(StringUtils.containsIgnoreCase(profile, "facets")
						&& (ArrayUtils.isEmpty(facets)
						|| ArrayUtils.contains(facets, "DEFAULT")
				));
	}

	private boolean isDefaultOrReusabilityFacetRequested(String profile, String[] facets) {
		return StringUtils.containsIgnoreCase(profile, "portal")
				|| (
				StringUtils.containsIgnoreCase(profile, "facets")
						&& (
						ArrayUtils.isEmpty(facets)
								|| ArrayUtils.contains(facets, "DEFAULT")
								|| ArrayUtils.contains(facets, "REUSABILITY")
				));
	}

    /**
     * Gives a translation of the 'EdmIsShownAt' label in the appropriate language.
     * <p/>
     * The 'appropriate language' is arrived at as follows: first it tries to retrieve the language
     * code from the bean and look up the translation in this language.
     * <p/>
     * If this doesn't yield a string (either because the bean contains no language settings or there
     * is no translation provided for that language), it tries to retrieve the translation based on
     * the language code provided in the 'language' parameter - which has the value of the 'language'
     * GET parameter if provided, or else the channel language code.
     * <p/>
     * If that fails as well, it looks up the English translation of the label. And if that fails too,
     * it returns a hardcoded error message.
     *
     * @param bean     containing language code
     * @param language String containing the channel's language code
     * @return String containing the label translation
     */
    private String getTranslatedEdmIsShownAtLabel(BriefBean bean, String language) {
        String translatedEdmIsShownAtLabel;
        // first try with the bean language
        try {
            translatedEdmIsShownAtLabel = getEdmIsShownAtLabelTranslation(getBeanLocale(bean.getLanguage()));
        } catch (MissingResourceException e) {
            log.error("error: 'edmIsShownAtLabel' translation for bean language '" + getBeanLocale(bean.getLanguage()) + "' unavailable: " + e.getMessage());
            translatedEdmIsShownAtLabel = "";
        }
        // check if retrieving translation for bean language failed
        if (StringUtils.isBlank(translatedEdmIsShownAtLabel)) {
            // if so, and bean language != channel language, try channel language
            if (!isLanguageEqual(bean.getLanguage(), language)) {
                log.error("falling back on channel language ('" + language + "')");
                try {
                    translatedEdmIsShownAtLabel = getEdmIsShownAtLabelTranslation(getLocale(language));
                } catch (MissingResourceException e) {
                    log.error("error: 'edmIsShownAtLabel' translation for channel language '" + getBeanLocale(bean.getLanguage()) + "' unavailable: " + e.getMessage());
                    translatedEdmIsShownAtLabel = "";
                }
            } else {
                log.error("channel language ('" + language + "') is identical to bean language, skipping ...");
            }
            // check if translation is still empty
            if (StringUtils.isBlank(translatedEdmIsShownAtLabel)) {
                log.error("falling back on default English translation ...");
                // if so, try English translation
                try {
                    translatedEdmIsShownAtLabel = getEdmIsShownAtLabelTranslation(getLocale("en"));
                } catch (MissingResourceException e) {
                    log.error("error: 'edmIsShownAtLabel' English translation unavailable: " + e.getMessage());
                    translatedEdmIsShownAtLabel = "";
                }
                // check if retrieving English translation failed
                if (StringUtils.isBlank(translatedEdmIsShownAtLabel)) {
                    log.error("No translation for 'edmIsShownAtLabel' available.");
                    // if so, return hardcoded message
                    return "error: translations for 'edmIsShownAtLabel' unavailable";
                }
            }
        }
        return translatedEdmIsShownAtLabel;
    }

    /**
     * Gives the translation of the 'EdmIsShownAt' label in the language of
     * the provided Locale
     *
     * @param locale Locale instance initiated with the desired language
     * @return String containing the label translation
     */
    private String getEdmIsShownAtLabelTranslation(Locale locale) {
        return messageSource.getMessage("edm_isShownAtLabel_t", null, locale);
    }

    /**
     * Initiates and returns a Locale instance for the language specified by the language code found
     * in the input.
     * <p/>
     * Checks for NULL values, and whether or not the found code is two characters long; if not, it
     * returns a locale initiated to English
     *
     * @param beanLanguage String Array containing language code
     * @return Locale instance
     */
    private Locale getBeanLocale(String[] beanLanguage) {
        if (!ArrayUtils.isEmpty(beanLanguage) && !StringUtils.isBlank(beanLanguage[0])
                && beanLanguage[0].length() == 2) {
            return new Locale(beanLanguage[0]);
        } else {
            log.error("error: language code unavailable or malformed (e.g. not two characters long)");
            return new Locale("en");
        }
    }

    /**
     * Initiates and returns a Locale instance for the language specified by
     * the language code found in the input.
     * <p>Checks for NULL values, and whether or not the found code is two
     * characters long; if not, it returns a locale initiated to English
     *
     * @param language String containing language code
     * @return Locale instance
     */
    private Locale getLocale(String language) {
        if (!StringUtils.isBlank(language)
                && language.length() == 2) {
            return new Locale(language);
        } else {
            log.error("error: language code unavailable or malformed (e.g. not two characters long)");
            return new Locale("en");
        }
    }


    /**
     * simple utility method to compare the language code contained in a String array
     * with another contained in a String. Also checks for well-formedness, i.e. if they're two characters long
     *
     * @param languageArray String[]
     * @param language String
     * @return boolean TRUE if equal, else FALSE
     */
    private boolean isLanguageEqual(String[] languageArray, String language){
        return (!ArrayUtils.isEmpty(languageArray)
                && !StringUtils.isBlank(languageArray[0])
                && languageArray[0].length() == 2
                && !StringUtils.isBlank(language)
                && language.length() == 2
                && language.equalsIgnoreCase(languageArray[0]));
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

    private String[] expandFacetNames(String[] facet) {
        if (facet == null)
            return null;

        for (int i = 0; i < facet.length; ++i) {
            if ("MEDIA".equalsIgnoreCase(facet[i])) {
                facet[i] = "has_media";
            } else if ("THUMBNAIL".equalsIgnoreCase(facet[i])) {
                facet[i] = "has_thumbnails";
            } else if ("TEXT_FULLTEXT".equalsIgnoreCase(facet[i])) {
                facet[i] = "is_fulltext";
            }
        }
        return facet;
    }
}