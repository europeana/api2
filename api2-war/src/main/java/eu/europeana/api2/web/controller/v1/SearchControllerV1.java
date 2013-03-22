package eu.europeana.api2.web.controller.v1;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.solr.common.SolrException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import eu.europeana.api2.web.model.json.Api1SearchResults;
import eu.europeana.api2.web.model.json.ApiError;
import eu.europeana.api2.web.model.json.api1.BriefDoc;
import eu.europeana.api2.web.model.xml.rss.Channel;
import eu.europeana.api2.web.model.xml.rss.Enclosure;
import eu.europeana.api2.web.model.xml.rss.Item;
import eu.europeana.api2.web.model.xml.rss.RssResponse;
import eu.europeana.corelib.db.service.ApiKeyService;
import eu.europeana.corelib.db.service.UserService;
import eu.europeana.corelib.definitions.solr.beans.ApiBean;
import eu.europeana.corelib.definitions.solr.beans.IdBean;
import eu.europeana.corelib.definitions.solr.model.Query;
import eu.europeana.corelib.solr.exceptions.SolrTypeException;
import eu.europeana.corelib.solr.model.ResultSet;
import eu.europeana.corelib.solr.service.SearchService;
import eu.europeana.corelib.solr.utils.SolrUtils;
import eu.europeana.corelib.utils.OptOutDatasetsUtil;

@Controller
public class SearchControllerV1 {

	private final Logger log = Logger.getLogger(getClass().getName());

	@Resource(name="corelib_db_userService") private UserService userService;

	@Resource private SearchService searchService;

	@Resource private ApiKeyService apiService;

	private static final int RESULT_ROWS_PER_PAGE = 12;

	private static final String DESCRIPTION_SUFFIX = " - Europeana Open Search";

	@Value("#{europeanaProperties['portal.server']}") private String portalServer;

	@Value("#{europeanaProperties['portal.name']}") private String portalName;

	@Value("#{europeanaProperties['api2.url']}") private String apiUrl;

	@Value("#{europeanaProperties['api.optOutList']}") private String optOutList;

	private String path;

	@RequestMapping(value = {"/opensearch.json", "/v1/search.json"}, method=RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ModelAndView search2Json(
		@RequestParam(value = "wskey", required = true) String wskey,
		@RequestParam(value = "searchTerms", required = true) String queryString,
		// @RequestParam(value = "qf", required = false) String[] refinements,
		// @RequestParam(value = "profile", required = false, defaultValue="standard") String profile,
		@RequestParam(value = "startPage", required = false, defaultValue="1") int start,
		// @RequestParam(value = "rows", required = false, defaultValue="12") int rows,
		// @RequestParam(value = "sort", required = false) String sort,
		HttpServletRequest request, HttpServletResponse response
			) throws Exception {

		path = fixPath(request.getContextPath());
		String profile = "standard";
		int rows = 12;
		OptOutDatasetsUtil.setOptOutDatasets(optOutList);

		Map<String, Object> model = new HashMap<String, Object>();
		Api1Utils utils = new Api1Utils();

		boolean hasResult = false;
		if (!hasResult && StringUtils.isBlank(wskey)) {
			model.put("json", utils.toJson(new ApiError(wskey, "search.json", "No API authorisation key.")));
			response.setStatus(401);
			hasResult = true;
		}

		if (!hasResult && (userService.findByApiKey(wskey) == null && apiService.findByID(wskey) == null)) {
			model.put("json", utils.toJson(new ApiError(wskey, "search.json", "Unregistered user")));
			response.setStatus(401);
			hasResult = true;
		}

		if (!hasResult) {
			log.info("opensearch.json");
			// Query query = new Query(q).setApiQuery(true).setRefinements(refinements).setPageSize(rows).setStart(start - 1);
			Query query = new Query(SolrUtils.translateQuery(queryString))
					.setApiQuery(true)
					.setPageSize(rows)
					.setStart(start - 1)
					.setAllowSpellcheck(false)
					.setAllowFacets(false);
			Class<? extends IdBean> clazz = ApiBean.class;
			try {
				Api1SearchResults<Map<String, Object>> result = createResultsForApi1(wskey, profile, query, clazz);
				result.startIndex = start;
				result.description = queryString + DESCRIPTION_SUFFIX;
				result.link = String.format("%s?searchTerms=%s&startPage=%d", apiUrl, URLEncoder.encode(queryString), start);
				if (result != null) {
					log.info("got response " + result.items.size());
					log.info("itemsPerPage: " + utils.toJson(result));
					model.put("json", utils.toJson(result));
				}
				model.put("result", result);
			} catch (SolrTypeException e) {
				logException(e);
				model.put("json", utils.toJson(new ApiError(wskey, "search.json", "Internal Server Error. Something is broken.")));
				response.setStatus(500);
			} catch (Exception e) {
				logException(e);
				model.put("json", utils.toJson(new ApiError(wskey, "search.json", "Internal Server Error. Something is broken.")));
				response.setStatus(500);
			}
		}

		ModelAndView page = new ModelAndView("search", model);
		return page;
	}

	// 
	@RequestMapping(value = {"/opensearch.rss", "/v1/opensearch.rss"}, produces = "application/rss+xml")
	public @ResponseBody RssResponse openSearchControllerRSS(
			@RequestParam(value = "searchTerms", required = false) String queryString,
			@RequestParam(value = "startPage", required = false, defaultValue = "1") String startPage,
			@RequestParam(value = "wskey", required = false, defaultValue = "") String wskey,
			HttpServletRequest request, 
			HttpServletResponse response
				) throws Exception {
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
			log.info(queryString + ", " + RESULT_ROWS_PER_PAGE + ", " + (Integer.parseInt(startPage) - 1));
			Query query = new Query(SolrUtils.translateQuery(queryString))
					.setApiQuery(true)
					.setPageSize(RESULT_ROWS_PER_PAGE)
					.setStart(Integer.parseInt(startPage) - 1)
					.setAllowSpellcheck(false)
					.setAllowFacets(false);
			Class<? extends IdBean> clazz = ApiBean.class;
			Api1SearchResults<BriefDoc> resultSet = createResultsForRSS(wskey, null, query, clazz);

			channel.totalResults.value = resultSet.totalResults;

			for (BriefDoc bean : resultSet.items) {

				Item item = new Item();
				item.guid = bean.getGuid();
				item.title = bean.getTitle();
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
			log.severe(e.getMessage());
			channel.totalResults.value = 0;
			Item item = new Item();
			item.title = "Error";
			item.description = e.getMessage();
			channel.items.add(item);
		} catch (SolrException e) {
			log.severe(e.getMessage());
			channel.totalResults.value = 0;
			Item item = new Item();
			item.title = "Error";
			item.description = e.getMessage();
			channel.items.add(item);
		}
		return rss;
	}

	private <T extends IdBean> Api1SearchResults<Map<String, Object>> createResultsForApi1(String wskey, String profile, Query q, 
			Class<T> clazz) 
			throws SolrTypeException {
		Api1SearchResults<Map<String, Object>> response = new Api1SearchResults<Map<String, Object>>(wskey, "search.json");
		ResultSet<T> resultSet = searchService.search(clazz, q);
		response.totalResults = resultSet.getResultSize();
		response.itemsPerPage = resultSet.getResults().size();

		BriefDoc.setPortalServer(getPortalServer());
		BriefDoc.setPortalName(portalName);
		BriefDoc.setPath(path);
		List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
		for (Object o : resultSet.getResults()) {
			BriefDoc doc = new BriefDoc((ApiBean)o);
			doc.setWskey(wskey);
			items.add(doc.asMap());
		}
		response.items = items;
		log.info("response: " + response);
		return response;
	}

	private Api1SearchResults<BriefDoc> createResultsForRSS(String wskey, String profile, Query q, 
			Class<? extends IdBean> clazz) 
			throws SolrTypeException {
		Api1SearchResults<BriefDoc> response = new Api1SearchResults<BriefDoc>(wskey, "search.json");
		ResultSet<? extends IdBean> resultSet = searchService.search(clazz, q);
		response.totalResults = resultSet.getResultSize();
		response.itemsPerPage = resultSet.getResults().size();

		BriefDoc.setPortalServer(getPortalServer());
		BriefDoc.setPortalName(portalName);
		BriefDoc.setPath(path);
		List<BriefDoc> items = new ArrayList<BriefDoc>();
		for (Object o : resultSet.getResults()) {
			BriefDoc doc = new BriefDoc((ApiBean)o);
			doc.setWskey(wskey);
			items.add(doc);
		}
		response.items = items;
		return response;
	}

	private void logException(Exception e) {
		log.severe(ExceptionUtils.getRootCauseMessage(e));
		log.severe(ExceptionUtils.getFullStackTrace(e));
	}

	public String getPortalServer() {
		if (portalServer.endsWith("/")) {
			portalServer = portalServer.substring(0, portalServer.length()-1);
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
