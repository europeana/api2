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
import java.util.Map;
import java.util.Locale;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.response.FacetField;
import org.springframework.http.MediaType;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.context.support.AbstractMessageSource;

import com.mongodb.Mongo;

import eu.europeana.api2.model.enums.ApiLimitException;
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
import eu.europeana.corelib.db.entity.enums.RecordType;
import eu.europeana.corelib.db.exception.DatabaseException;
import eu.europeana.corelib.db.exception.LimitReachedException;
import eu.europeana.corelib.db.service.ApiKeyService;
import eu.europeana.corelib.db.service.ApiLogService;
import eu.europeana.corelib.db.service.UserService;
import eu.europeana.corelib.definitions.db.entity.relational.ApiKey;
import eu.europeana.corelib.definitions.solr.Facet;
import eu.europeana.corelib.definitions.solr.beans.ApiBean;
import eu.europeana.corelib.definitions.solr.beans.BriefBean;
import eu.europeana.corelib.definitions.solr.beans.IdBean;
import eu.europeana.corelib.definitions.solr.beans.RichBean;
import eu.europeana.corelib.definitions.solr.model.Query;
import eu.europeana.corelib.logging.Log;
import eu.europeana.corelib.logging.Logger;
import eu.europeana.corelib.solr.exceptions.SolrTypeException;
import eu.europeana.corelib.solr.model.ResultSet;
import eu.europeana.corelib.solr.service.SearchService;
import eu.europeana.corelib.solr.utils.SolrUtils;
import eu.europeana.corelib.utils.StringArrayUtils;
import eu.europeana.corelib.utils.service.OptOutService;
import eu.europeana.corelib.web.model.rights.RightReusabilityCategorizer;
import eu.europeana.corelib.web.service.EuropeanaUrlService;
import eu.europeana.corelib.web.support.Configuration;
import eu.europeana.corelib.web.utils.NavigationUtils;
import eu.europeana.corelib.web.utils.RequestUtils;

/**
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 */
@Controller
public class SearchController {

	@Log
	private Logger log;

	@Resource(name = "corelib_db_mongo")
	private Mongo mongo;

	@Resource
	private SearchService searchService;

	@Resource
	private ApiKeyService apiService;

	@Resource
	private UserService userService;

	@Resource
	private ApiLogService apiLogService;

	@Resource
	private OptOutService optOutService;

	@Resource
	private Configuration configuration;

	@Resource
	private EuropeanaUrlService urlService;

	@Resource
	private ControllerUtils controllerUtils;

	@Resource(name = "api2_mvc_views_jaxbmarshaller")
	private Jaxb2Marshaller marshaller;

	@Resource(name = "api2_mvc_xmlUtils")
	private XmlUtils xmlUtils;
        
        @Resource
        private AbstractMessageSource messageSource;

	final static public int FACET_LIMIT = 16;

	@RequestMapping(value = "/v2/search.json", method = {RequestMethod.GET, RequestMethod.POST}, produces = MediaType.APPLICATION_JSON_VALUE)
	public ModelAndView searchJson(
			@RequestParam(value = "query", required = true) String queryString,
            @RequestParam(value = "qf", required = false) String[] refinements,
			@RequestParam(value = "reusability", required = false) String[] aReusability,
			@RequestParam(value = "profile", required = false, defaultValue = "standard") String profile,
			@RequestParam(value = "start", required = false, defaultValue = "1") int start,
			@RequestParam(value = "rows", required = false, defaultValue = "12") int rows,
			@RequestParam(value = "facet", required = false) String[] aFacet,
			@RequestParam(value = "wskey", required = false) String wskey,
			@RequestParam(value = "callback", required = false) String callback,
			@RequestParam(value = "colourpalette", required = false) String[] colorPalette,

            @RequestParam(value = "text_fulltext", required = false) Boolean isFulltext,
            @RequestParam(value = "thumbnail", required = false) Boolean thumbnail,
            @RequestParam(value = "media", required = false) Boolean media,
			HttpServletRequest request,
			HttpServletResponse response) {
        // workaround of a Spring issue
        // (https://jira.springsource.org/browse/SPR-7963)
        String[] _qf = (String[]) request.getParameterMap().get("qf");
        if (_qf != null && _qf.length != refinements.length) {
            refinements = _qf;
        }


        final List<String> mediaTypes = new ArrayList<>();
        final List<String> mimeTypes = new ArrayList<>();

        final List<String> imageSizes = new ArrayList<>();
        final List<Boolean> imageColors = new ArrayList<>();
        final List<Boolean> imageGrayScales = new ArrayList<>();
        final List<String> imageAspectRatios = new ArrayList<>();
        final List<String> imageColorsPalette = new ArrayList<>();

        final List<Boolean> soundHQs = new ArrayList<>();
        final List<String> soundDurations = new ArrayList<>();

        final List<Boolean> videoHQs = new ArrayList<>();
        final List<String> videoDurations = new ArrayList<>();

        final Integer imageFilterTag = imageFilterTags(mimeTypes, imageSizes, imageColors, imageGrayScales, imageAspectRatios).get(0);
        final Integer soundFilterTag = soundFilterTags(mimeTypes, soundHQs, soundDurations).get(0);
        final Integer videoFilterTag = videoFilterTags(mimeTypes, videoHQs, videoDurations).get(0);

        if (null != colorPalette) {
            imageColorsPalette.addAll(Arrays.asList(colorPalette));
        }

        final List<String> extra = new ArrayList<>();
        if(refinements != null) {
            for (String qf : refinements) {
                log.info("QF: " + qf);
                final Integer colonIndex = qf.indexOf(":");
                if (colonIndex == null || colonIndex == -1) {
                    continue;
                }
                final String prefix = qf.substring(0, colonIndex).toLowerCase();
                final String suffix = qf.substring(colonIndex + 1).toLowerCase();

                log.info("prefix: " + prefix);
                log.info("suffix: " + suffix);

                if (prefix.equals("text_fulltext")) {
                    isFulltext = (null == isFulltext ? false : isFulltext) || Boolean.parseBoolean(suffix);
                    extra.add(qf);
                }

                if (prefix.equals("has_thumbnail")) {
                    thumbnail = (null == thumbnail ? false : thumbnail) || Boolean.parseBoolean(suffix);
                    extra.add(qf);
                }

                if (prefix.equals("has_media")) {
                    media = (null == media ? false : media) || Boolean.parseBoolean(suffix);
                    extra.add(qf);
                }

                if (prefix.equals("onetagpercolour")) {
                    imageColorsPalette.add(suffix);
                    extra.add(qf);
                }

                if (prefix.equals("type")) {
                    mediaTypes.add(suffix);
                }
                if (prefix.equals("mime_type")) {
                    mimeTypes.add(suffix);
                    extra.add(qf);
                }

                if (prefix.equals("image_size")) {
                    imageSizes.add(suffix);
                    extra.add(qf);
                }
                if (prefix.equals("image_colour") || prefix.equals("image_color")) {
                    imageColors.add(Boolean.valueOf(suffix));
                    extra.add(qf);
                }
                if (prefix.equals("image_greyscale") || prefix.equals("image_grayscale")) {
                    imageGrayScales.add(Boolean.valueOf(suffix));
                    extra.add(qf);
                }
                if (prefix.equals("image_aspectratio")) {
                    imageAspectRatios.add(suffix);
                    extra.add(qf);
                }

                if (prefix.equals("sound_hq")) {
                    soundHQs.add(Boolean.valueOf(suffix));
                    extra.add(qf);
                }
                if (prefix.equals("sound_duration")) {
                    soundDurations.add(suffix);
                    extra.add(qf);
                }

                if (prefix.equals("video_hd")) {
                    videoHQs.add(Boolean.valueOf(suffix));
                    extra.add(qf);
                }
                if (prefix.equals("video_duration")) {
                    videoDurations.add(suffix);
                    extra.add(qf);
                }
            }
        }

        if (isFulltext != null) {
            final String filterQuery = "is_fulltext:" + isFulltext;
            if (queryString.equals("")) {
                queryString = filterQuery;
            } else {
                queryString = queryString + " AND " + filterQuery;
            }
        }

        // FilterTagGeneration
        if (thumbnail != null) {
            final String filterQuery = "has_thumbnails:" + thumbnail;
            if (queryString.equals("")) {
                queryString = filterQuery;
            } else {
                queryString = queryString + " AND " + filterQuery;
            }
        }

        if (media != null) {
            final String filterQuery = "has_media:" + media;
            if (queryString.equals("")) {
                queryString = filterQuery;
            } else {
                queryString = queryString + " AND " + filterQuery;
            }
        }

        if (!imageColorsPalette.isEmpty()) {
            String filterQuery = "";
            for (String color : imageColorsPalette) {
                log.info("Color palette: " + color);
                final Integer filterTag = searchService.search(1, null, null, null, null, null, color, null, null, null, null);
                log.info("Color palette: " + filterTag);
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
        filterTags.addAll(imageFilterTags(mimeTypes, imageSizes, imageColors, imageGrayScales, imageAspectRatios));
        filterTags.addAll(soundFilterTags(mimeTypes, soundHQs, soundDurations));
        filterTags.addAll(videoFilterTags(mimeTypes, videoHQs, videoDurations));

        Boolean image = false, sound = false, video = false;
        for (final String type : mediaTypes) {
            if (type.equals("image")) {
                image = true;
            }
            if (type.equals("sound")) {
                sound = true;
            }
            if (type.equals("video")) {
                video = true;
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
            log.info("filterTag: " + filterTag);
            if (filterTag % 33554432 != 0) {
                filterTagQuery = filterTagQuery + "filter_tags:" + filterTag + " OR ";
            }
        }

        log.info("filtertagquery: " + filterTagQuery);

        if (filterTagQuery.contains("OR")) {
            filterTagQuery = filterTagQuery.substring(0, filterTagQuery.lastIndexOf("OR"));
            filterTagQuery = filterTagQuery.trim();

            if (queryString.equals("")) {
                queryString = filterTagQuery;
            } else {
                filterTagQuery = "(" + filterTagQuery + ")";
                queryString = queryString + " AND " + filterTagQuery;
            }
        }

        queryString = queryString.trim();
        log.info("QUERY: |" + queryString + "|");

        // =================================================================================================
        final List<String> newRefinements = new ArrayList<>();
        if(refinements != null) {
            newRefinements.addAll(Arrays.asList(refinements));
            for (String extraQF : extra) {
                newRefinements.remove(extraQF);
            }

            refinements = new String[newRefinements.size()];
            refinements = newRefinements.toArray(refinements);

            for (String qf : refinements) {
                log.info("ref: " + qf);
            }
        }

        boolean isFacetsRequested = isFacetsRequested(profile);
		String[] reusability = StringArrayUtils.splitWebParameter(aReusability);
		String[] facets = StringArrayUtils.splitWebParameter(aFacet);
		boolean isDefaultFacetsRequested = isDefaultFacetsRequested(profile, facets);
		facets = limitFacets(facets, isDefaultFacetsRequested);

		boolean isDefaultOrReusabilityFacetRequested = isDefaultOrReusabilityFacetRequested(profile, facets);
		Map<String, Integer> facetLimits = null;
		Map<String, Integer> facetOffsets = null;
		if (isFacetsRequested) {
			Map<String,String[]> parameterMap = request.getParameterMap();
			facetLimits = FacetParameterUtils.getFacetParams("limit", aFacet, parameterMap, isDefaultFacetsRequested);
			facetOffsets = FacetParameterUtils.getFacetParams("offset", aFacet, parameterMap, isDefaultFacetsRequested);
		}

		controllerUtils.addResponseHeaders(response);
		rows = Math.min(rows, configuration.getApiRowLimit());

		Map<String, String> valueReplacements = new HashMap<String, String>();
		if (ArrayUtils.isNotEmpty(reusability)) {
			valueReplacements = RightReusabilityCategorizer.mapValueReplacements(reusability, true);

			refinements = (String[]) ArrayUtils.addAll(
					refinements,
					valueReplacements.keySet().toArray(new String[valueReplacements.keySet().size()])
			);
		}

        log.info("Query: " + queryString);
		Query query = new Query(SolrUtils.rewriteQueryFields(queryString))
				.setApiQuery(true)
				.setRefinements(refinements)
				.setPageSize(rows)
				.setStart(start - 1)
				.setParameter("facet.mincount", "1")
				.setParameter("fl", "*,score")
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
			query.setFacetQueries(RightReusabilityCategorizer.getQueryFacets());
		}

		if (StringUtils.containsIgnoreCase(profile, "portal") || StringUtils.containsIgnoreCase(profile, "spelling")) {
			query.setAllowSpellcheck(true);
		}

		if (isFacetsRequested) {
			query.setAllowFacets(true);
			if (!query.hasParameter("f.DATA_PROVIDER.facet.limit")
					&& (ArrayUtils.contains(facets, "DATA_PROVIDER") || ArrayUtils.contains(facets, "DEFAULT"))) {
				query.setParameter("f.DATA_PROVIDER.facet.limit", "3000");
			}
		}

		LimitResponse limitResponse = null;
		try {
			limitResponse = controllerUtils.checkLimit(wskey, request.getRequestURL().toString(),
					"search.json", RecordType.SEARCH, profile);
		} catch (ApiLimitException e) {
			response.setStatus(e.getHttpStatus());
			return JsonUtils.toJson(new ApiError(e), callback);
		}

		Class<? extends IdBean> clazz = selectBean(profile);

		try {
			SearchResults<? extends IdBean> result = createResults(wskey, profile, 
					query, clazz, limitResponse.getApiKey().getUser().getId());
			result.requestNumber = limitResponse.getRequestNumber();
			if (StringUtils.containsIgnoreCase(profile, "params")) {
				result.addParams(RequestUtils.getParameterMap(request), "wskey");
				result.addParam("profile", profile);
				result.addParam("start", start);
				result.addParam("rows", rows);
			}

			if (log.isInfoEnabled()) {
				log.info("got response " + result.items.size());
			}
			return JsonUtils.toJson(result, callback);
		} catch (SolrTypeException e) {
			log.error(wskey + " [search.json] ", e);
			response.setStatus(500);
			return JsonUtils.toJson(new ApiError(wskey, "search.json", e.getMessage()), callback);
		} catch (Exception e) {
			log.error(wskey + " [search.json] " + e.getClass().getSimpleName(), e);
			response.setStatus(500);
			return JsonUtils.toJson(new ApiError(wskey, "search.json", e.getMessage()), callback);
		}
	}

    private List<Integer> imageFilterTags(List<String> mimeTypes, List<String> imageSizes, List<Boolean> imageColors, List<Boolean> imageGrayScales, List<String> imageAspectRatios) {
        final List<Integer> filterTags = new ArrayList<>();
        Integer i = 0, j, k, l, m;

        log.info("Size: " + mimeTypes.size());
        log.info("Size: " + imageSizes.size());
        log.info("Size: " + imageColors.size());
        log.info("Size: " + imageGrayScales.size());
        log.info("Size: " + imageAspectRatios.size());

        do {
            String mimeType = null;
            if(mimeTypes.size() != 0) {
                mimeType = mimeTypes.get(i);
            }
            j = 0;
            log.info("mimetype: " + mimeType);
            do {
                String imageSize = null;
                if(imageSizes.size() != 0) {
                    imageSize = imageSizes.get(j);
                }
                k = 0;
                log.info("imageSize: " + imageSize);
                do {
                    Boolean imageColor = null;
                    if(imageColors.size() != 0) {
                        imageColor = imageColors.get(k);
                    }
                    l = 0;
                    log.info("imageColor: " + imageColor);
                    do {
                        Boolean imageGrayScale = null;
                        if(imageGrayScales.size() != 0) {
                            imageGrayScale = imageGrayScales.get(l);
                        }
                        m = 0;
                        log.info("imageGrayScale: " + imageGrayScale);
                        do {
                            String imageAspectRatio = null;
                            if(imageAspectRatios.size() != 0) {
                                imageAspectRatio = imageAspectRatios.get(m);
                            }
                            log.info("imageAspectRatio: " + imageAspectRatio);

                            final Integer filterTag = searchService.search(1, mimeType, imageSize, imageColor, imageGrayScale, imageAspectRatio, null, null, null, null, null);
                            log.info("image filtertag: " + filterTag);
                            filterTags.add(filterTag);

                            m += 1;
                        } while (m < imageAspectRatios.size());

                        l += 1;
                    } while (l < imageGrayScales.size());

                    k += 1;
                } while (k < imageColors.size());

                j += 1;
            } while (j < imageSizes.size());

            i += 1;
        } while (i < mimeTypes.size());

        return filterTags;
    }

    private List<Integer> soundFilterTags(List<String> mimeTypes, List<Boolean> soundHQs, List<String> soundDurations) {
        final List<Integer> filterTags = new ArrayList<>();

        Integer i = 0, j, k;

        do {
            String mimeType = null;
            if(mimeTypes.size() != 0) {
                mimeType = mimeTypes.get(i);
            }
            j = 0;
            do {
                Boolean soundHQ = null;
                if(soundHQs.size() != 0) {
                    soundHQ = soundHQs.get(j);
                }
                k = 0;
                do {
                    String soundDuration = null;
                    if(soundDurations.size() != 0) {
                        soundDuration = soundDurations.get(k);
                    }

                    final Integer filterTag = searchService.search(2, mimeType, null, null, null, null, null, soundHQ, soundDuration, null, null);
                    log.info("sound filtertag: " + filterTag);
                    filterTags.add(filterTag);

                    k += 1;
                } while (k < soundDurations.size());

                j += 1;
            } while (j < soundHQs.size());

            i += 1;
        } while (i < mimeTypes.size());

        return filterTags;
    }

    private List<Integer> videoFilterTags(List<String> mimeTypes, List<Boolean> videoHQs, List<String> videoDurations) {
        final List<Integer> filterTags = new ArrayList<>();

        Integer i = 0, j, k;

        do {
            String mimeType = null;
            if(mimeTypes.size() != 0) {
                mimeType = mimeTypes.get(i);
            }
            j = 0;
            do {
                Boolean videoHQ = null;
                if(videoHQs.size() != 0) {
                    videoHQ = videoHQs.get(j);
                }
                k = 0;
                do {
                    String videoDuration = null;
                    if(videoDurations.size() != 0) {
                        videoDuration = videoDurations.get(k);
                    }

                    final Integer filterTag = searchService.search(3, mimeType, null, null, null, null, null, null, null, videoHQ, videoDuration);
                    log.info("video filtertag: " + filterTag);
                    filterTags.add(filterTag);

                    k += 1;
                } while (k < videoDurations.size());

                j += 1;
            } while (j < videoHQs.size());

            i += 1;
        } while (i < mimeTypes.size());

        return filterTags;
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

	/**
	 * Limits the number of facets
	 * @param facets
	 *   The user entered facet names list
	 * @param isDefaultFacetsRequested
	 *   Flag if default facets should be returned
	 * @return
	 *   The limited set of facet names
	 */
	public static String[] limitFacets(String[] facets, boolean isDefaultFacetsRequested) {
		List<String> requestedFacets = Arrays.asList(facets);
		List<String> allowedFacets = new ArrayList<String>();

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

	@RequestMapping(value = "/v2/suggestions.json", produces = MediaType.APPLICATION_JSON_VALUE)
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
		Suggestions apiResponse = new Suggestions(null, "suggestions.json");
		try {
			apiResponse.items = searchService.suggestions(query, count);
			apiResponse.itemsCount = apiResponse.items.size();
		} catch (SolrTypeException e) {
			return JsonUtils.toJson(new ApiError(null, "suggestions.json", e.getMessage()), callback);
		}
		return JsonUtils.toJson(apiResponse, callback);
	}

	@SuppressWarnings("unchecked")
	private <T extends IdBean> SearchResults<T> createResults(
			String apiKey,
			String profile,
			Query query,
			Class<T> clazz,
			long uid)
					throws SolrTypeException {
		SearchResults<T> response = new SearchResults<T>(apiKey, "search.json");
		ResultSet<T> resultSet = searchService.search(clazz, query);
		response.totalResults = resultSet.getResultSize();
		response.itemsCount = resultSet.getResults().size();
		response.items = resultSet.getResults();

		List<T> beans = new ArrayList<T>();
		for (T b : resultSet.getResults()) {
			if (b instanceof RichBean) {
				beans.add((T) new RichView((RichBean) b, profile, apiKey, uid, optOutService.check(b.getId())));
			} else if (b instanceof ApiBean) {
				beans.add((T) new ApiView((ApiBean) b, profile, apiKey, uid, optOutService.check(b.getId())));
			} else if (b instanceof BriefBean) {
				beans.add((T) new BriefView((BriefBean) b, profile, apiKey, uid, optOutService.check(b.getId())));
			}
		}

		List<FacetField> facetFields = resultSet.getFacetFields();
		if (resultSet.getQueryFacets() != null) {
			List<FacetField> allQueryFacetsMap = SolrUtils.extractQueryFacets(resultSet.getQueryFacets());
			if (allQueryFacetsMap != null && !allQueryFacetsMap.isEmpty()) {
				facetFields.addAll(allQueryFacetsMap);
			}
		}

		if (log.isInfoEnabled()) {
			log.info("beans: " + beans.size());
		}
		response.items = beans;
		if (StringUtils.containsIgnoreCase(profile, "facets") || StringUtils.containsIgnoreCase(profile, "portal")) {
			response.facets = ModelUtils.conventFacetList(resultSet.getFacetFields());
		}
		if (StringUtils.containsIgnoreCase(profile, "breadcrumb") || StringUtils.containsIgnoreCase(profile, "portal")) {
			response.breadCrumbs = NavigationUtils.createBreadCrumbList(query);
		}
		if (StringUtils.containsIgnoreCase(profile, "spelling") || StringUtils.containsIgnoreCase(profile, "portal")) {
			response.spellcheck = ModelUtils.convertSpellCheck(resultSet.getSpellcheck());
		}
		// if (StringUtils.containsIgnoreCase(profile, "suggestions") ||
		// StringUtils.containsIgnoreCase(profile, "portal")) {
		// }
		return response;
	}

	@RequestMapping(value = "/v2/search.kml", produces = MediaType.APPLICATION_XML_VALUE)
	// @RequestMapping(value = "/v2/search.kml", produces =
	// "application/vnd.google-earth.kml+xml")
	public @ResponseBody
	KmlResponse searchKml(
			@RequestParam(value = "query", required = true) String queryString,
			@RequestParam(value = "qf", required = false) String[] refinements,
			@RequestParam(value = "start", required = false, defaultValue = "1") int start,
			@RequestParam(value = "wskey", required = true) String wskey,
			HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		// workaround of a Spring issue
		// (https://jira.springsource.org/browse/SPR-7963)
		String[] _qf = (String[]) request.getParameterMap().get("qf");
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
		Query query = new Query(SolrUtils.rewriteQueryFields(queryString))
				.setRefinements(refinements)
				.setApiQuery(true)
				.setAllowSpellcheck(false)
				.setAllowFacets(false);
		query.setRefinements("pl_wgs84_pos_lat_long:[* TO *]");
		try {
			ResultSet<BriefBean> resultSet = searchService.search(BriefBean.class, query);
			kmlResponse.document.extendedData.totalResults.value = Long.toString(resultSet.getResultSize());
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

	@RequestMapping(value = "/v2/opensearch.rss", produces = MediaType.APPLICATION_XML_VALUE)
	public @ResponseBody
	RssResponse openSearchRss(
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
			Query query = new Query(SolrUtils.rewriteQueryFields(queryString)).setApiQuery(true).setPageSize(count)
					.setStart(start - 1).setAllowFacets(false).setAllowSpellcheck(false);
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
	 * @param queryTerms the collection ID, e.g. "europeana_collectionName:91697*"
	 * @param offset     list items from this index on
	 * @param limit      max number of items to list
	 * @param profile    should be "FieldTrip"
	 * @param request    servlet request object
	 * @param response   servlet response object
	 * @return ModelAndView instance
	 *   
	 */
	@RequestMapping(value = "/v2/search.rss", produces = MediaType.APPLICATION_XML_VALUE)
	public ModelAndView fieldTripRss(
			@RequestParam(value = "query", required = true) String queryTerms,
			@RequestParam(value = "offset", required = false, defaultValue = "1") int offset,
			@RequestParam(value = "limit", required = false, defaultValue = "12") int limit,
			@RequestParam(value = "profile", required = false, defaultValue = "FieldTrip") String profile,
			HttpServletRequest request,
			HttpServletResponse response) {
		controllerUtils.addResponseHeaders(response);

                String collectionID = getIdFromQueryTerms(queryTerms);
                Map<String, String> gftChannelAttributes = configuration.getGftChannelAttributes(collectionID);
                
		FieldTripResponse rss = new FieldTripResponse();
		FieldTripChannel channel = rss.channel;
		
                if (gftChannelAttributes.isEmpty() || gftChannelAttributes.size() < 5) {
                    log.error("error: one or more attributes are not defined in europeana.properties for [INSERT COLLECTION ID HERE]");
                    channel.title = "error retrieving attributes";
                    channel.description = "error retrieving attributes";
                    channel.language = "--";
                    channel.link = "error retrieving attributes";
                    channel.image = null;
                } else {
                    channel.title = gftChannelAttributes.get("title") == null 
                            || gftChannelAttributes.get("title").equalsIgnoreCase("") ? "no title defined" : gftChannelAttributes.get("title");
                    channel.description = gftChannelAttributes.get("description") == null 
                            || gftChannelAttributes.get("description").equalsIgnoreCase("") ? "no description defined" : gftChannelAttributes.get("description");
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
		FieldTripUtils fieldTripUtils = new FieldTripUtils(urlService);
		try {
			Query query = new Query(SolrUtils.rewriteQueryFields(queryTerms)).setApiQuery(true).setPageSize(limit)
					.setStart(offset - 1).setAllowFacets(false).setAllowSpellcheck(false);
			ResultSet<RichBean> resultSet = searchService.search(RichBean.class, query);
			for (RichBean bean : resultSet.getResults()) {
				channel.items.add(fieldTripUtils.createItem(bean, getTranslatedEdmIsShownAtLabel(bean, channel.language)));
			}
		} catch (SolrTypeException e) {
			log.error("error: " + e.getLocalizedMessage());
			FieldTripItem item = new FieldTripItem();
			item.title = "Error";
			item.description = e.getMessage();
			channel.items.add(item);
		}

		String xml = fieldTripUtils.cleanRss(xmlUtils.toString(rss));

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("rss", xml);

		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/xml");

		return new ModelAndView("rss", model);
	}

	/**
	 * Retrieves the title from the bean if not null; otherwise, returns
         * a concatenation of the Data Provier and ID fields. 
         * <p>! FIX ME ! Note that this method will yield unwanted results when 
         * there is more than one Title field!
	 * @param  bean mapped pojo bean
	 * @return String containing the concatenated fields
	 *   
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
	 * @param  bean mapped pojo bean
	 * @return String containing the fields, separated by semicolons
	 *   
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

	private boolean isFacetsRequested(String profile) {
		if (StringUtils.containsIgnoreCase(profile, "portal") || StringUtils.containsIgnoreCase(profile, "facets")) {
			return true;
		}
		return false;
	}

	private boolean isDefaultFacetsRequested(String profile, String[] facets) {
		if (StringUtils.containsIgnoreCase(profile, "portal") || 
			(StringUtils.containsIgnoreCase(profile, "facets") 
				&& (    ArrayUtils.isEmpty(facets)
					||  ArrayUtils.contains(facets, "DEFAULT")
			))) {
			return true;
		}
		return false;
	}

	private boolean isDefaultOrReusabilityFacetRequested(String profile, String[] facets) {
		if (StringUtils.containsIgnoreCase(profile, "portal")
			|| (
				StringUtils.containsIgnoreCase(profile, "facets")
				&& (
						ArrayUtils.isEmpty(facets)
					||  ArrayUtils.contains(facets, "DEFAULT")
					||  ArrayUtils.contains(facets, "REUSABILITY")
			))) {
			return true;
		}
		return false;
	}
        
        /**
	 * Gives a translation of the 'EdmIsShownAt' label in the appropriate 
         * language. 
         * <p>The 'appropriate language' is arrived at as follows: first it tries 
         * to retrieve the language code from the bean and look up the translation
         * in this language. 
         * <p>If this doesn't yield a string (either because the bean contains 
         * no language settings or there is no translation provided for that
         * language), it tries to retrieve the translation based on the language
         * code provided in the 'channelLanguage' parameter.
         * <p>If that fails as well, it looks up the English translation of the
         * label. And if that fails too, it returns a hardcoded error message.
	 * @param bean containing language code
         * @param channelLanguage String containing the channel's language code 
	 * @return String containing the label translation
	 *   
	 */    
        private String getTranslatedEdmIsShownAtLabel(BriefBean bean, String channelLanguage){
            String translatedEdmIsShownAtLabel = "";
            // first try with the bean language
            translatedEdmIsShownAtLabel = getEdmIsShownAtLabelTranslation(getBeanLocale(bean.getLanguage()));
            // check bean translation
            if (StringUtils.isBlank(translatedEdmIsShownAtLabel)){
                log.error("error: 'edmIsShownAtLabel translation for language code '" + getBeanLocale(bean.getLanguage()) + "' unavailable");
                log.error("falling back on channel language ('" + channelLanguage + "')");
                // if empty, try with channel language instead
                translatedEdmIsShownAtLabel = getEdmIsShownAtLabelTranslation(channelLanguage);
                // check channel translation
                if (StringUtils.isBlank(translatedEdmIsShownAtLabel)){
                    log.error("error: 'fallback edmIsShownAtLabel translation for channel language code '" + channelLanguage + "' unavailable");
                    log.error("falling back on default English translation ..."); 
                    // if empty, try with english instead
                    translatedEdmIsShownAtLabel = getEdmIsShownAtLabelTranslation("en");
                    // check english translation
                    if (StringUtils.isBlank(translatedEdmIsShownAtLabel)){
                        log.error("Default English translation unavailable."); 
                        // if empty, return hardcoded message
                        return "error: 'edmIsShownAtLabel' english fallback translation unavailable";
                    }
                }
            }
            return translatedEdmIsShownAtLabel;
        }
         
        /**
	 * Gives the translation of the 'EdmIsShownAt' label in the language that
         * the provided String specifies
	 * @param  language containing language code
	 * @return String containing the label translation
	 *   
	 */    
        private String getEdmIsShownAtLabelTranslation(String language){
            return messageSource.getMessage("edm_isShownAtLabel_t", null, new Locale(language));
        }

        /**
	 * Gives the translation of the 'EdmIsShownAt' label in the language of
         * the provided Locale
	 * @param  locale Locale instance initiated with the desired language
	 * @return String containing the label translation
	 *   
	 */
        private String getEdmIsShownAtLabelTranslation(Locale locale){
            return messageSource.getMessage("edm_isShownAtLabel_t", null, locale);
        }
        
        /**
	 * Initiates and returns a Locale instance for the language specified by 
         * the language code found in the input. 
         * <p>Checks for NULL values, and whether or not the found code is two 
         * characters long; if not, it returns a locale initiated to English
	 * @param  beanLanguage String Array containing language code
	 * @return Locale instance
	 *   
	 */
	private Locale getBeanLocale(String[] beanLanguage) {
            if (!ArrayUtils.isEmpty(beanLanguage)
             && !StringUtils.isBlank(beanLanguage[0])
             && beanLanguage[0].length() == 2){
                return new Locale(beanLanguage[0]);
            } else {
		log.error("error: language code unavailable or malformed (e.g. not two characters long)");
                return new Locale("en");
            }
	}
        
	/**
	 * retrieves the numerical part of the substring between the ':' and '*' 
         * characters. 
         * <p>e.g. "europeana_collectionName:91697*" will result in "91697"
	 * @param  queryTerms provided String
	 * @return String containing the Europeana collection ID only
	 *   
	 */
        private String getIdFromQueryTerms(String queryTerms){
            return queryTerms.substring(queryTerms.indexOf(":"), queryTerms.indexOf("*")).replaceAll("\\D+","");
        }
}
