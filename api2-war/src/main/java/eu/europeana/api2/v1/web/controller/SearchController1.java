package eu.europeana.api2.v1.web.controller;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.solr.common.SolrException;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import eu.europeana.api2.model.json.ApiError;
import eu.europeana.api2.utils.JsonUtils;
import eu.europeana.api2.v1.model.json.SearchResults;
import eu.europeana.api2.v1.model.json.view.BriefDoc;
import eu.europeana.api2.v2.model.xml.rss.Channel;
import eu.europeana.api2.v2.model.xml.rss.Enclosure;
import eu.europeana.api2.v2.model.xml.rss.Item;
import eu.europeana.api2.v2.model.xml.rss.RssResponse;
import eu.europeana.corelib.db.service.ApiKeyService;
import eu.europeana.corelib.db.service.UserService;
import eu.europeana.corelib.definitions.solr.beans.ApiBean;
import eu.europeana.corelib.definitions.solr.beans.IdBean;
import eu.europeana.corelib.definitions.solr.model.Query;
import eu.europeana.corelib.logging.Log;
import eu.europeana.corelib.solr.exceptions.SolrTypeException;
import eu.europeana.corelib.solr.model.ResultSet;
import eu.europeana.corelib.solr.service.SearchService;
import eu.europeana.corelib.solr.utils.SolrUtils;
import eu.europeana.corelib.utils.service.OptOutService;

@Controller
public class SearchController1 {

	@Log
	private Logger log;

	@Resource(name = "corelib_db_userService")
	private UserService userService;

	@Resource
	private SearchService searchService;

	@Resource
	private ApiKeyService apiService;

	@Resource
	private OptOutService optOutService;

	private static final int RESULT_ROWS_PER_PAGE = 12;

	private static final String DESCRIPTION_SUFFIX = " - Europeana Open Search";

	@Value("#{europeanaProperties['portal.server']}")
	private String portalServer;

	@Value("#{europeanaProperties['portal.name']}")
	private String portalName;

	@Value("#{europeanaProperties['api2.url']}")
	private String apiUrl;

	private String path;

	@RequestMapping(value = { "/opensearch.json", "/v1/search.json" }, produces = MediaType.APPLICATION_JSON_VALUE)
	// method=RequestMethod.GET
	public ModelAndView search2Json(@RequestParam(value = "wskey", required = true) String wskey,
			@RequestParam(value = "searchTerms", required = true) String queryString,
			@RequestParam(value = "startPage", required = false, defaultValue = "1") int start,
			@RequestParam(value = "callback", required = false) String callback, HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		path = fixPath(request.getContextPath());
		int rows = 12;

		ModelAndView mov;

		if (StringUtils.isBlank(wskey)) {
			response.setStatus(401);
			mov = JsonUtils.toJson(new ApiError(wskey, "search.json", "No API authorisation key."), callback);
		} else

		if ((userService.findByApiKey(wskey) == null && apiService.findByID(wskey) == null)) {
			response.setStatus(401);
			mov = JsonUtils.toJson(new ApiError(wskey, "search.json", "Unregistered user"), callback);
		} else {

			log.info("opensearch.json");
			// Query query = new
			// Query(q).setApiQuery(true).setRefinements(refinements).setPageSize(rows).setStart(start
			// - 1);
			Query query = new Query(SolrUtils.translateQuery(queryString)).setApiQuery(true).setPageSize(rows)
					.setStart(start - 1).setAllowSpellcheck(false).setAllowFacets(false);
			Class<? extends IdBean> clazz = ApiBean.class;
			try {
				SearchResults<Map<String, Object>> result = createResultsForApi1(wskey, query, clazz);
				result.startIndex = start;
				result.description = queryString + DESCRIPTION_SUFFIX;
				result.link = String.format("%s?searchTerms=%s&startPage=%d", apiUrl,
						URLEncoder.encode(queryString, "UTF-8"), start);
				log.info("got response " + result.items.size());
				mov = JsonUtils.toJson(result, callback);

			} catch (SolrTypeException e) {
				logException(e);
				mov = JsonUtils.toJson(
						new ApiError(wskey, "search.json", "Internal Server Error. Something is broken."), callback);
				response.setStatus(500);
			} catch (Exception e) {
				logException(e);
				mov = JsonUtils.toJson(
						new ApiError(wskey, "search.json", "Internal Server Error. Something is broken."), callback);
				response.setStatus(500);
			}
		}

		return mov;
	}

	//
	@RequestMapping(value = { "/opensearch.rss", "/v1/opensearch.rss" }, produces = "application/rss+xml")
	public @ResponseBody
	RssResponse openSearchControllerRSS(@RequestParam(value = "searchTerms", required = false) String queryString,
			@RequestParam(value = "startPage", required = false, defaultValue = "1") String startPage,
			@RequestParam(value = "wskey", required = false, defaultValue = "") String wskey,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		path = fixPath(request.getContextPath());
		log.info("===== openSearchControllerRSS =====");
		response.setCharacterEncoding("UTF-8");

		String cannonicalLink = "http://europeana.eu";
		String baseLink = getPortalServer() + "/" + path + "/v1/opensearch.rss";
		String href = baseLink + "?searchTerms=" + URLEncoder.encode(queryString, "UTF-8") + "&startPage=" + startPage;

		RssResponse rss = new RssResponse();
		Channel channel = rss.channel;
		channel.startIndex.value = Integer.parseInt(startPage);
		channel.itemsPerPage.value = RESULT_ROWS_PER_PAGE;
		channel.query.searchTerms = queryString;
		channel.query.startPage = Integer.parseInt(startPage);
		channel.setLink(cannonicalLink);
		channel.atomLink.href = href;
		channel.updateDescription();

		try {
			if (log.isInfoEnabled()) {
				log.info(queryString + ", " + RESULT_ROWS_PER_PAGE + ", " + (Integer.parseInt(startPage) - 1));
			}
			Query query = new Query(SolrUtils.translateQuery(queryString)).setApiQuery(true)
					.setPageSize(RESULT_ROWS_PER_PAGE).setStart(Integer.parseInt(startPage) - 1)
					.setAllowSpellcheck(false).setAllowFacets(false);
			Class<? extends IdBean> clazz = ApiBean.class;
			SearchResults<BriefDoc> resultSet = createResultsForRSS(wskey, query, clazz);

			channel.totalResults.value = resultSet.totalResults;

			for (BriefDoc bean : resultSet.items) {

				Item item = new Item();
				item.guid = bean.getGuid();
				item.title = getTitle(bean);
				item.link = bean.getLink(BriefDoc.SRW);
				item.description = bean.getDescription();
				String enclosure = bean.getThumbnail();
				if (enclosure != null) {
					item.enclosure = new Enclosure(enclosure);
				}
				item.dcCreator = bean.getCreator();
				item.dcTermsHasPart = bean.getDcTermsHasPart();
				item.dcTermsIsPartOf = bean.getDcTermsIsPartOf();
				item.europeanaYear = bean.getYear();
				item.europeanaLanguage = bean.getLanguage();
				item.europeanaType = bean.getType();
				item.europeanaProvider = bean.getProvider();
				item.europeanaDataProvider = bean.getDataProvider();
				item.europeanaRights = bean.getEuropeanaRights();
				item.enrichmentPlaceLatitude = bean.getEnrichmentPlaceLatitude();
				item.enrichmentPlaceLongitude = bean.getEnrichmentPlaceLongitude();
				item.enrichmentPlaceTerm = bean.getEnrichmentPlaceTerm();
				item.enrichmentPlaceLabel = bean.getEnrichmentPlaceLabel();
				item.enrichmentPeriodTerm = bean.getEnrichmentPeriodTerm();
				item.enrichmentPeriodLabel = bean.getEnrichmentPeriodLabel();
				item.enrichmentPeriodBegin = bean.getEnrichmentPeriodBegin();
				item.enrichmentPeriodEnd = bean.getEnrichmentPeriodEnd();
				item.enrichmentAgentTerm = bean.getEnrichmentAgentLabel();
				item.enrichmentAgentLabel = bean.getEnrichmentAgentLabel();
				item.enrichmentConceptTerm = bean.getEnrichmentConceptTerm();
				item.enrichmentConceptLabel = bean.getEnrichmentConceptLabel();

				channel.items.add(item);
			}
			response.setStatus(200);
		} catch (SolrTypeException e) {
			log.error(e.getMessage());
			channel.totalResults.value = 0;
			Item item = new Item();
			item.title = "Error";
			item.description = e.getMessage();
			channel.items.add(item);
		} catch (SolrException e) {
			log.error(e.getMessage());
			channel.totalResults.value = 0;
			Item item = new Item();
			item.title = "Error";
			item.description = e.getMessage();
			channel.items.add(item);
		}
		return rss;
	}

	private String getTitle(BriefDoc bean) {
		if (!StringUtils.isEmpty(bean.getTitle())) {
			return bean.getTitle();
		}
		return bean.getDataProvider() + " " + bean.getUrl();
	}

	private <T extends IdBean> SearchResults<Map<String, Object>> createResultsForApi1(String wskey,
			Query q, Class<T> clazz) throws SolrTypeException {
		SearchResults<Map<String, Object>> response = new SearchResults<Map<String, Object>>(wskey, "search.json");
		ResultSet<T> resultSet = searchService.search(clazz, q);
		response.totalResults = resultSet.getResultSize();
		response.itemsPerPage = resultSet.getResults().size();

		BriefDoc.setPortalServer(getPortalServer());
		BriefDoc.setPortalName(portalName);
		BriefDoc.setPath(path);
		List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
		for (Object o : resultSet.getResults()) {
			ApiBean bean = (ApiBean) o;
			BriefDoc doc = new BriefDoc(bean, optOutService.check(bean.getId()));
			doc.setWskey(wskey);
			items.add(doc.asMap());
		}
		response.items = items;
		if (log.isInfoEnabled()) {
			log.info("response: " + response);
		}
		return response;
	}

	private SearchResults<BriefDoc> createResultsForRSS(String wskey, Query q,
			Class<? extends IdBean> clazz) throws SolrTypeException {
		SearchResults<BriefDoc> response = new SearchResults<BriefDoc>(wskey, "search.json");
		ResultSet<? extends IdBean> resultSet = searchService.search(clazz, q);
		response.totalResults = resultSet.getResultSize();
		response.itemsPerPage = resultSet.getResults().size();

		BriefDoc.setPortalServer(getPortalServer());
		BriefDoc.setPortalName(portalName);
		BriefDoc.setPath(path);
		List<BriefDoc> items = new ArrayList<BriefDoc>();
		for (Object o : resultSet.getResults()) {
			ApiBean bean = (ApiBean) o;
			BriefDoc doc = new BriefDoc(bean, optOutService.check(bean.getId()));
			doc.setWskey(wskey);
			items.add(doc);
		}
		response.items = items;
		return response;
	}

	private void logException(Exception e) {
		if (log.isErrorEnabled()) {
			log.error(ExceptionUtils.getRootCauseMessage(e));
			log.error(ExceptionUtils.getFullStackTrace(e));
		}
	}

	public String getPortalServer() {
		if (portalServer.endsWith("/")) {
			portalServer = portalServer.substring(0, portalServer.length() - 1);
		}
		return portalServer;
	}

	private String fixPath(String path) {
		if (path.startsWith("/")) {
			path = path.substring(1);
		}
		if (path.endsWith("/")) {
			path = path.substring(0, path.length() - 1);
		}
		return path;
	}
}
