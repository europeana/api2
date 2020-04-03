package eu.europeana.api2.v2.web.controller;

import com.github.jsonldjava.core.JSONLD;
import com.github.jsonldjava.core.JSONLDProcessingError;
import com.github.jsonldjava.core.Options;
import com.github.jsonldjava.impl.JenaRDFParser;
import com.github.jsonldjava.utils.JSONUtils;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import eu.europeana.api2.model.json.ApiError;
import eu.europeana.api2.model.utils.Api2UrlService;
import eu.europeana.api2.utils.JsonUtils;
import eu.europeana.api2.v2.model.ItemFix;
import eu.europeana.api2.v2.model.json.ObjectResult;
import eu.europeana.api2.v2.model.json.view.FullView;
import eu.europeana.api2.v2.utils.ApiKeyUtils;
import eu.europeana.api2.v2.utils.HttpCacheUtils;
import eu.europeana.api2.v2.web.swagger.SwaggerIgnore;
import eu.europeana.api2.v2.web.swagger.SwaggerSelect;
import eu.europeana.corelib.db.entity.enums.RecordType;
import eu.europeana.corelib.definitions.edm.beans.FullBean;
import eu.europeana.corelib.edm.utils.EdmUtils;
import eu.europeana.corelib.edm.utils.SchemaOrgUtils;
import eu.europeana.corelib.search.SearchService;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.corelib.utils.EuropeanaUriUtils;
import eu.europeana.corelib.web.exception.EuropeanaException;
import eu.europeana.corelib.web.utils.RequestUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static eu.europeana.api2.v2.utils.HttpCacheUtils.IFMATCH;
import static eu.europeana.api2.v2.utils.HttpCacheUtils.IFNONEMATCH;

/**
 * Provides record information in all kinds of formats; json, json-ld and rdf
 *
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 */
@Controller
@Api(tags = {"Record"})
@RequestMapping(value = "/v2/record")
@SwaggerSelect
public class ObjectController {

    private static final Logger LOG                     = LogManager.getLogger(ObjectController.class);
    private static final String MEDIA_TYPE_RDF_UTF8     = "application/rdf+xml; charset=UTF-8";
    private static final String MEDIA_TYPE_JSONLD_UTF8  = "application/ld+json; charset=UTF-8";
    private static Object       jsonldContext           = new Object();

    private SearchService   searchService;
    private ApiKeyUtils     apiKeyUtils;
    private HttpCacheUtils httpCacheUtils;

    @Resource
    private Api2UrlService urlService;

    /**
     * Create a static Object for JSONLD Context. This will read the file once during initialization
     *
     * @param jsonldContext
     * @throws IOException
     */

    static {
        try {
            InputStream in = ObjectController.class.getResourceAsStream("/jsonld/context.jsonld");
            jsonldContext = JSONUtils.fromInputStream(in);
        } catch (IOException e) {
            LOG.error("Error reading context.jsonld", e);
        }
    }

    /**
     * Create a new ObjectController
     *
     * @param searchService
     * @param apiKeyUtils
     * @param httpCacheUtils
     */
    @Autowired
    public ObjectController(SearchService searchService, ApiKeyUtils apiKeyUtils, HttpCacheUtils httpCacheUtils) {
        this.searchService = searchService;
        this.apiKeyUtils = apiKeyUtils;
        this.httpCacheUtils = httpCacheUtils;
    }

    /**
     * Handles record.json GET requests. Each request should consists of at least a collectionId, a recordId and an api-key (wskey)
     *
     * @param collectionId   ID of data collection or data set
     * @param recordId       ID of record, item - a.k.a. 'localId'
     * @param apikey         formerly known as 'wskey'
     * @param profile        supported types are 'params' and 'similar'
     * @param callback       repeats whatever you supply
     * @param webRequest
     * @param servletRequest
     * @param response
     * @return
     * @throws EuropeanaException
     */
    @ApiOperation(value = "get a single record in JSON format", nickname = "getSingleRecordJson")
    @GetMapping(value = "/{collectionId}/{recordId}.json", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ModelAndView record(@PathVariable String collectionId,
                               @PathVariable String recordId,
                               @RequestParam(value = "profile", required = false, defaultValue = "full") String profile,
                               @RequestParam(value = "wskey") String apikey,
                               @RequestParam(value = "callback", required = false) String callback,
                               @ApiIgnore WebRequest webRequest,
                               @ApiIgnore HttpServletRequest servletRequest,
                               @ApiIgnore HttpServletResponse response) throws EuropeanaException {
        RequestData data = new RequestData(collectionId, recordId, apikey, profile, callback, webRequest, servletRequest);
        return (ModelAndView) handleRecordRequest(RecordType.OBJECT, data, response);
    }

    /**
     * @param callback  repeats whatever you supply
     * @return only the context part of a json-ld record
     */
    @SwaggerIgnore
    @GetMapping(value = {"/context.jsonld", "/context.json-ld"}, produces = { MEDIA_TYPE_JSONLD_UTF8 , MediaType.APPLICATION_JSON_UTF8_VALUE })
    public ModelAndView contextJSONLD(@RequestParam(value = "callback", required = false) String callback) {
        String jsonld = JSONUtils.toString(jsonldContext);
        return JsonUtils.toJson(jsonld, callback);
    }

    /**
     * Retrieve a record in JSON-LD format (hidden alias for record.jsonld request)
     *
     * @param collectionId   ID of data collection or data set
     * @param recordId       ID of record, item - a.k.a. 'localId'
     * @param apikey         formerly known as 'wskey'
     * @param format         specifies the layout: supported types are 'compacted', 'flattened' and 'normalized'
     * @param callback       repeats whatever you supply
     * @param webRequest
     * @param servletRequest
     * @param response
     * @return
     * @throws EuropeanaException
     */ // produces = MEDIA_TYPE_JSONLD_UTF8)
    @SwaggerIgnore
    @GetMapping(value = "/{collectionId}/{recordId}.json-ld", produces = { MEDIA_TYPE_JSONLD_UTF8, MediaType.APPLICATION_JSON_UTF8_VALUE })
    public ModelAndView recordJSON_LD(@PathVariable String collectionId,
                                      @PathVariable String recordId,
                                      @RequestParam(value = "wskey") String apikey,
                                      @RequestParam(value = "format", required = false, defaultValue = "compacted") String format,
                                      @RequestParam(value = "callback", required = false) String callback,
                                      @ApiIgnore WebRequest webRequest,
                                      @ApiIgnore HttpServletRequest servletRequest,
                                      @ApiIgnore HttpServletResponse response) throws EuropeanaException {
        return recordJSONLD(collectionId, recordId, apikey, format, callback, webRequest, servletRequest, response);
    }

    /***
     * Retrieve a record in JSON-LD format.
     * @param collectionId   ID of data collection or data set
     * @param recordId       ID of record, item - a.k.a. 'localId'
     * @param apikey         formerly known as 'wskey'
     * @param format         specifies the layout: supported types are 'compacted', 'flattened' and 'normalized'
     * @param callback       repeats whatever you supply
     * @param webRequest
     * @param servletRequest
     * @param response
     * @return
     * @throws EuropeanaException
     */ // produces = MEDIA_TYPE_JSONLD_UTF8)
    @ApiOperation(value = "get single record in JSON LD format", nickname = "getSingleRecordJsonLD")
    @GetMapping(value = "/{collectionId}/{recordId}.jsonld", produces = { MEDIA_TYPE_JSONLD_UTF8 , MediaType.APPLICATION_JSON_UTF8_VALUE })
    public ModelAndView recordJSONLD(@PathVariable String collectionId,
                                     @PathVariable String recordId,
                                     @RequestParam(value = "wskey") String apikey,
                                     @RequestParam(value = "format", required = false, defaultValue = "compacted") String format,
                                     @RequestParam(value = "callback", required = false) String callback,
                                     @ApiIgnore WebRequest webRequest,
                                     @ApiIgnore HttpServletRequest servletRequest,
                                     @ApiIgnore HttpServletResponse response) throws EuropeanaException {

        RequestData data = new RequestData(collectionId, recordId, apikey, format, callback, webRequest, servletRequest);
        return (ModelAndView) handleRecordRequest(RecordType.OBJECT_JSONLD, data, response);
    }

    /***
     * Retrieve a record in Schema.org JSON-LD format.
     * @param collectionId   ID of data collection or data set
     * @param recordId       ID of record, item - a.k.a. 'localId'
     * @param apikey         formerly known as 'wskey'
     * @param format         specifies the layout: supported types are 'compacted', 'flattened' and 'normalized'
     * @param callback       repeats whatever you supply
     * @param webRequest
     * @param servletRequest
     * @param response
     * @return
     * @throws EuropeanaException
     */ // produces = MEDIA_TYPE_JSONLD_UTF8)
    @ApiOperation(value = "get single record in Schema.org JSON LD format", nickname = "getSingleRecordSchemaOrg")
    @GetMapping(value = "/{collectionId}/{recordId}.schema.jsonld", produces = { MEDIA_TYPE_JSONLD_UTF8 , MediaType.APPLICATION_JSON_UTF8_VALUE })
    public ModelAndView recordSchemaOrg(@PathVariable String collectionId,
                                        @PathVariable String recordId,
                                        @RequestParam(value = "wskey", required = true) String apikey,
                                        @RequestParam(value = "format", required = false, defaultValue = "compacted") String format,
                                        @RequestParam(value = "callback", required = false) String callback,
                                        @ApiIgnore WebRequest webRequest,
                                        @ApiIgnore HttpServletRequest servletRequest,
                                        @ApiIgnore HttpServletResponse response) throws EuropeanaException {

        RequestData data = new RequestData(collectionId, recordId, apikey, format, callback, webRequest, servletRequest);
        return (ModelAndView) handleRecordRequest(RecordType.OBJECT_SCHEMA_ORG, data, response);
    }

    /**
     * Retrieve a record in RDF format
     *
     * @param collectionId   ID of data collection or data set
     * @param recordId       ID of record, item - a.k.a. 'localId'
     * @param apikey         formerly known as 'wskey'
     * @param webRequest
     * @param servletRequest
     * @param response
     * @return
     * @throws EuropeanaException
     */
    @ApiOperation(value = "get single record in RDF format)", nickname = "getSingleRecordRDF")
    @GetMapping(value = "/{collectionId}/{recordId}.rdf", produces = MEDIA_TYPE_RDF_UTF8)
    public ModelAndView recordRdf(@PathVariable String collectionId,
                                  @PathVariable String recordId,
                                  @RequestParam(value = "wskey") String apikey,
                                  @ApiIgnore WebRequest webRequest,
                                  @ApiIgnore HttpServletRequest servletRequest,
                                  @ApiIgnore HttpServletResponse response) throws EuropeanaException {
        RequestData data = new RequestData(collectionId, recordId, apikey, null, null, webRequest, servletRequest);
        return (ModelAndView) handleRecordRequest(RecordType.OBJECT_RDF, data, response);
    }

    /**
     * The larger part of handling a record is the same for all types of output, so this method handles all the
     * common functionality such as setting CORS headers, checking API key, retrieving the record for mongo
     * and setting 301 or 404 if necessary
     */
    private Object handleRecordRequest(RecordType recordType, RequestData data, HttpServletResponse response)
            throws EuropeanaException {
        ModelAndView result;

        // 1) Check if HTTP method is supported, HTTP 405 if not
        if (!StringUtils.equalsIgnoreCase("GET", data.servletRequest.getMethod()) &&
                !StringUtils.equalsIgnoreCase("HEAD", data.servletRequest.getMethod())){
            response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return null; // figure out what to return exactly in these cases
        }

        long startTime = System.currentTimeMillis();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Retrieving record with id " + data.europeanaObjectId + ", type = " + recordType);
        }

        apiKeyUtils.validateApiKey(data.apikey);

        FullBean bean = searchService.fetchFullBean(data.europeanaObjectId);

        // 3) Check if record exists, HTTP 404 if not
        if (Objects.isNull(bean)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            if (recordType == RecordType.OBJECT_RDF) {
                Map<String, Object> model = new HashMap<>();
                model.put("error", "Non-existing record identifier");
                result = new ModelAndView("rdf", model);
            } else {
                result = JsonUtils.toJson(new ApiError(data.apikey, "Invalid record identifier: "
                        + data.europeanaObjectId), data.callback);
            }
            return result;
        }

        /*
         * 2017-07-06 PE: the code below was implemented as part of ticket #662. However as collections does not support this
         * yet activation of this functionality is postponed.
         *        if (!bean.getAbout().equals(data.europeanaObjectId)) {
         *            response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
         *            response.setHeader("Location", generateRedirectUrl(data.servletRequest, data.europeanaObjectId, bean.getAbout()));
         *            return null;
         *        }
         *        */

        // ETag is created from timestamp + api version.
        String tsUpdated = httpCacheUtils.dateToRFC1123String(bean.getTimestampUpdated());
        String eTag      = httpCacheUtils.generateETag(data.europeanaObjectId+tsUpdated, true, true);

        // If If-None-Match is present: check if it contains a matching eTag OR == '*"
        // Yes: return HTTP 304 + cache headers. Ignore If-Modified-Since (RFC 7232)
        if (StringUtils.isNotBlank(data.servletRequest.getHeader(IFNONEMATCH))){
            if (httpCacheUtils.doesAnyIfNoneMatch(data.servletRequest, eTag)) {
                response = httpCacheUtils.addDefaultHeaders(response, eTag, tsUpdated);
                response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                return null;
            }
            // If If-Match is present: check if it contains a matching eTag OR == '*"
            // Yes: proceed. No: return HTTP 412, no cache headers
        } else if (StringUtils.isNotBlank(data.servletRequest.getHeader(IFMATCH))){
            if (httpCacheUtils.doesPreconditionFail(data.servletRequest, eTag)){
                response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
                return null;
            }
            // check if If-Modified-Since is present and on or after timestamp_updated
            // yes: return HTTP 304 no: continue
        } else if (httpCacheUtils.isNotModifiedSince(data.servletRequest, bean.getTimestampUpdated())){
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED); // no cache headers
            return null;
        }

        // ugly solution for EA-1257, but it works
        ItemFix.apply(bean);
        // now the FullBean can be processed (adding similar items and initiating the AttributionSnippet)
        bean = searchService.processFullBean(bean, data.europeanaObjectId, false);

        // add headers, except Content-Type (that differs per recordType)
        response = httpCacheUtils.addDefaultHeaders(response, eTag, tsUpdated);


        // generate output depending on type of record
        Object output;
        switch (recordType) {
            case OBJECT:
                output = generateJson(bean, data, startTime);
                break;
            case OBJECT_JSONLD:
                output = generateJsonLd(bean, data, response);
                break;
            case OBJECT_RDF:
                output = generateRdf(bean);
                break;
            case OBJECT_SCHEMA_ORG:
                output = generateSchemaOrg(bean, data);
                break;
            default:
                throw new IllegalArgumentException("Unknown record output type: " + recordType);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Done generating record output in " + (System.currentTimeMillis() - startTime) + " ms");
        }
        return output;
    }

    private ModelAndView generateJson(FullBean bean, RequestData data, long startTime) {
        ObjectResult objectResult = new ObjectResult(data.apikey);

        if (StringUtils.containsIgnoreCase(data.profile, "params")) {
            objectResult.addParams(RequestUtils.getParameterMap(data.servletRequest), "wskey");
            objectResult.addParam("profile", data.profile);
        }

        objectResult.object = new FullView(bean, data.profile, data.apikey);
        objectResult.statsDuration = System.currentTimeMillis() - startTime;
        return JsonUtils.toJson(objectResult, data.callback);
    }

    private ModelAndView generateSchemaOrg(FullBean bean, RequestData data) {
        String jsonld = SchemaOrgUtils.toSchemaOrg((FullBeanImpl) bean);
        return JsonUtils.toJsonLd(jsonld, data.callback);
    }

    private ModelAndView generateJsonLd(FullBean bean, RequestData data, HttpServletResponse response) {
        String jsonld;
        String rdf    = EdmUtils.toEDM((FullBeanImpl) bean);
        try (InputStream rdfInput = IOUtils.toInputStream(rdf)) {
            Model  modelResult = ModelFactory.createDefaultModel().read(rdfInput, "", "RDF/XML");
            Object raw         = JSONLD.fromRDF(modelResult, new JenaRDFParser());
            if (StringUtils.equalsIgnoreCase(data.profile, "compacted")) {
                raw = JSONLD.compact(raw, jsonldContext, new Options());
            } else if (StringUtils.equalsIgnoreCase(data.profile, "flattened")) {
                raw = JSONLD.flatten(raw);
            } else if (StringUtils.equalsIgnoreCase(data.profile, "normalized")) {
                raw = JSONLD.normalize(raw);
            }
            jsonld = JSONUtils.toString(raw);
        } catch (IOException | JSONLDProcessingError e) {
            LOG.error("Error parsing JSON-LD data", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return JsonUtils.toJson(new ApiError(data.apikey, e.getClass().getSimpleName() + ": " + e.getMessage()), data.callback);
        }
        return JsonUtils.toJsonLd(jsonld, data.callback);
    }

    private ModelAndView generateRdf(FullBean bean) {
        Map<String, Object> model = new HashMap<>();
        model.put("record", EdmUtils.toEDM((FullBeanImpl) bean));
        return new ModelAndView("rdf", model);
    }


    /**
     * NOTE this method is called in commented-out code above, check those first before removing this
     *
     * Reconstruct the original url and instead of the old EuropeanaId inserts the new provided one.
     * <p>
     * Original code snippet was copied from https://stackoverflow.com/a/5212336 and slightly adjusted.
     *
     * @param req
     * @param oldId old identifier
     * @param newId new identifier
     * @return String with redirect URL
     */
    private String generateRedirectUrl(HttpServletRequest req, String oldId, String newId) {

        String scheme      = req.getScheme();      // http
        String serverName  = req.getServerName();  // www.europeana.eu
        int    serverPort  = req.getServerPort();  // 80
        String requestUri  = req.getRequestURI();  // /api/v2/record/90402/BK_1978_399.json
        String pathInfo    = req.getPathInfo();    //
        String queryString = req.getQueryString(); // wskey=....

        requestUri = requestUri.replace(oldId, newId);
        // check if we really replaced the id to avoid infinite loops
        if (!requestUri.contains(newId)) {
            throw new IllegalStateException("Error generating record redirect url");
        }

        // Reconstruct original requesting URL
        StringBuilder url = new StringBuilder();
        url.append(scheme).append("://").append(serverName);
        if (serverPort != 80 && serverPort != 443) {
            url.append(':').append(serverPort);
        }
        url.append(requestUri);
        if (pathInfo != null) {
            url.append(pathInfo);
        }
        if (queryString != null) {
            url.append('?').append(queryString);
        }
        return url.toString();
    }

    /**
     * Helper class to pass all data around in 1 object
     */
    private static class RequestData{
        String europeanaObjectId;
        protected String profile;             // called format in json-ld
        String apikey;
        protected String callback;
        WebRequest         webRequest;
        HttpServletRequest servletRequest;

        RequestData(String collectionId, String recordId, String apikey, String profile, String callback,
                    WebRequest webRequest, HttpServletRequest servletRequest) {
            this.europeanaObjectId = EuropeanaUriUtils.createEuropeanaId(collectionId, recordId);
            this.apikey            = apikey;
            this.profile           = profile;
            this.callback          = callback;
            this.webRequest        = webRequest;
            this.servletRequest    = servletRequest;
        }
    }
}
