package eu.europeana.api2.v2.web.controller;

import eu.europeana.api.commons.utils.RiotRdfUtils;
import eu.europeana.api.commons.utils.TurtleRecordWriter;
import eu.europeana.api2.config.SwaggerConfig;
import eu.europeana.api2.model.json.ApiError;
import eu.europeana.api2.utils.JsonUtils;
import eu.europeana.api2.v2.exceptions.InvalidConfigurationException;
import eu.europeana.api2.v2.exceptions.TranslationServiceDisabledException;
import eu.europeana.api2.v2.model.RecordType;
import eu.europeana.api2.v2.model.json.ObjectResult;
import eu.europeana.api2.v2.model.json.view.FullView;
import eu.europeana.api2.v2.model.translate.Language;
import eu.europeana.api2.v2.service.RouteDataService;
import eu.europeana.api2.v2.service.translate.BeanFilterLanguage;
import eu.europeana.api2.v2.service.translate.BeanTranslateService;
import eu.europeana.api2.v2.utils.ApiKeyUtils;
import eu.europeana.api2.v2.utils.HttpCacheUtils;
import eu.europeana.api2.v2.utils.ModelUtils;
import eu.europeana.api2.v2.web.swagger.SwaggerIgnore;
import eu.europeana.api2.v2.web.swagger.SwaggerSelect;
import eu.europeana.corelib.definitions.edm.beans.FullBean;
import eu.europeana.corelib.edm.utils.EdmUtils;
import eu.europeana.corelib.record.BaseUrlWrapper;
import eu.europeana.corelib.record.DataSourceWrapper;
import eu.europeana.corelib.record.RecordService;
import eu.europeana.corelib.record.schemaorg.utils.SchemaOrgUtils;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.corelib.utils.EuropeanaUriUtils;
import eu.europeana.corelib.web.exception.EuropeanaException;
import eu.europeana.corelib.web.exception.ProblemType;
import eu.europeana.corelib.web.utils.RequestUtils;
import eu.europeana.metis.mongo.dao.RecordDao;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.JsonLDWriteContext;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFWriter;
import org.apache.jena.riot.RDFWriterBuilder;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static eu.europeana.api2.v2.utils.HttpCacheUtils.IFMATCH;
import static eu.europeana.api2.v2.utils.HttpCacheUtils.IFNONEMATCH;

/**
 * Provides record information in all kinds of formats; json, json-ld and rdf
 *
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 */
@Controller
@Api(tags = {SwaggerConfig.RECORD_TAG})
@RequestMapping(value = {
        "/api/v2/record",
        "/v2/record",
        "/record/v2",
        "/record",
})
@SwaggerSelect
@Import(BeanTranslateService.class) // to enable title and description translation
public class ObjectController {

    private static final Logger LOG                     = LogManager.getLogger(ObjectController.class);
    private static final String MEDIA_TYPE_RDF_UTF8     = "application/rdf+xml; charset=UTF-8";
    private static final String MEDIA_TYPE_JSONLD_UTF8  = "application/ld+json; charset=UTF-8";
    private static final String MEDIA_TYPE_TURTLE_TEXT  = "text/turtle";
    private static final String MEDIA_TYPE_TURTLE       = "application/turtle";
    private static final String MEDIA_TYPE_TURTLE_X     = "application/x-turtle";

    private static Object       jsonldContext           = new Object();

    private RouteDataService        routeService;
    private RecordService           recordService;
    private BeanTranslateService    translateFilterService;
    private ApiKeyUtils             apiKeyUtils;
    private HttpCacheUtils          httpCacheUtils;

    /**
     * Create a static Object for JSONLD Context. This will read the file once during initialization
     *
     * @param jsonldContext
     * @throws IOException
     */
    static {
        try {
            InputStream in = ObjectController.class.getResourceAsStream("/jsonld/context.jsonld");
            jsonldContext = com.github.jsonldjava.utils.JsonUtils.fromInputStream(in);
        } catch (IOException e) {
            LOG.error("Error reading context.jsonld", e);
        }
    }

    /**
     * Create a new ObjectController
     * @param routeService for
     * @param recordService for retrieving data from Mongo
     * @param tfService for translating data
     * @param apiKeyUtils for api key validation
     * @param httpCacheUtils for request caching
     */
    @Autowired
    public ObjectController(RouteDataService routeService, RecordService recordService, BeanTranslateService tfService,
                            ApiKeyUtils apiKeyUtils, HttpCacheUtils httpCacheUtils) {
        this.recordService = recordService;
        this.apiKeyUtils = apiKeyUtils;
        this.httpCacheUtils = httpCacheUtils;
        this.routeService = routeService;
        this.translateFilterService = tfService;
    }

    /**
     * Handles record.json GET requests. Each request should consists of at least a collectionId, a recordId and an api-key (wskey)
     *
     * @param collectionId   ID of data collection or data set
     * @param recordId       ID of record, item - a.k.a. 'localId'
     * @param apikey         formerly known as 'wskey'
     * @param profile        one or more profile values to enhance the record (see also RecordProfile class)
     * @param lang           language in which record data should be displayed
     * @param callback       repeats whatever you supply
     * @param request        incoming request
     * @param response       generated response
     * @return
     * @throws EuropeanaException
     */
    @ApiOperation(value = "get a single record in JSON format", nickname = "getSingleRecordJson")
    @GetMapping(value = "/{collectionId}/{recordId}.json", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ModelAndView record(@PathVariable String collectionId,
                               @PathVariable String recordId,
                               @RequestParam(value = "wskey") String apikey,
                               @RequestParam(value = "profile", required = false, defaultValue = "standard") String profile,
                               @RequestParam(value = "lang", required = false) String lang,
                               @RequestParam(value = "callback", required = false) String callback,
                               @ApiIgnore HttpServletRequest request,
                               @ApiIgnore HttpServletResponse response) throws EuropeanaException {
        RequestData data = new RequestData(collectionId, recordId, apikey, profile, lang, callback, request);
        return (ModelAndView) handleRecordRequest(RecordType.OBJECT_JSON, data, response);
    }

    /**
     * @param callback  repeats whatever you supply
     * @return only the context part of a json-ld record
     */
    @SwaggerIgnore
    @GetMapping(value = {"/context.jsonld", "/context.json-ld"}, produces = { MEDIA_TYPE_JSONLD_UTF8 , MediaType.APPLICATION_JSON_UTF8_VALUE })
    public ModelAndView contextJSONLD(@RequestParam(value = "callback", required = false) String callback) throws IOException {
        String jsonld = com.github.jsonldjava.utils.JsonUtils.toString(jsonldContext);
        return JsonUtils.toJson(jsonld, callback);
    }

    /**
     * Retrieve a record in JSON-LD format (hidden alias for record.jsonld request)
     *
     * @param collectionId   ID of data collection or data set
     * @param recordId       ID of record, item - a.k.a. 'localId'
     * @param apikey         formerly known as 'wskey'
     * @param profile        one or more profile values to enhance the record (see also RecordProfile class)
     * @param lang           language in which record data should be displayed
     * @param callback       repeats whatever you supply
     * @param request        incoming request
     * @param response       generated response
     * @return
     * @throws EuropeanaException
     */ // produces = MEDIA_TYPE_JSONLD_UTF8)
    @SwaggerIgnore
    @GetMapping(value = "/{collectionId}/{recordId}.json-ld", produces = { MEDIA_TYPE_JSONLD_UTF8, MediaType.APPLICATION_JSON_UTF8_VALUE })
    public ModelAndView recordJSONLD2(@PathVariable String collectionId,
                                      @PathVariable String recordId,
                                      @RequestParam(value = "wskey") String apikey,
                                      @RequestParam(value = "profile", required = false, defaultValue = "standard") String profile,
                                      @RequestParam(value = "lang", required = false) String lang,
                                      @RequestParam(value = "callback", required = false) String callback,
                                      @ApiIgnore HttpServletRequest request,
                                      @ApiIgnore HttpServletResponse response) throws EuropeanaException {
        return recordJSONLD(collectionId, recordId, apikey, profile, lang, callback, request, response);
    }

    /***
     * Retrieve a record in JSON-LD format.
     * @param collectionId   ID of data collection or data set
     * @param recordId       ID of record, item - a.k.a. 'localId'
     * @param apikey         formerly known as 'wskey'
     * @param profile        one or more profile values to enhance the record (see also RecordProfile class)
     * @param lang           language in which record data should be displayed
     * @param callback       repeats whatever you supply
     * @param request        incoming request
     * @param response       generated response
     * @return
     * @throws EuropeanaException
     */ // produces = MEDIA_TYPE_JSONLD_UTF8)
    @ApiOperation(value = "get single record in JSON LD format", nickname = "getSingleRecordJsonLD")
    @GetMapping(value = "/{collectionId}/{recordId}.jsonld", produces = { MEDIA_TYPE_JSONLD_UTF8 , MediaType.APPLICATION_JSON_UTF8_VALUE })
    public ModelAndView recordJSONLD(@PathVariable String collectionId,
                                     @PathVariable String recordId,
                                     @RequestParam(value = "wskey") String apikey,
                                     @RequestParam(value = "profile", required = false, defaultValue = "standard") String profile,
                                     @RequestParam(value = "lang", required = false) String lang,
                                     @RequestParam(value = "callback", required = false) String callback,
                                     @ApiIgnore HttpServletRequest request,
                                     @ApiIgnore HttpServletResponse response) throws EuropeanaException {
        RequestData data = new RequestData(collectionId, recordId, apikey, profile, lang, callback, request);
        return (ModelAndView) handleRecordRequest(RecordType.OBJECT_JSONLD, data, response);
    }

    /***
     * Retrieve a record in Schema.org JSON-LD format.
     * @param collectionId   ID of data collection or data set
     * @param recordId       ID of record, item - a.k.a. 'localId'
     * @param apikey         formerly known as 'wskey'
     * @param profile        one or more profile values to enhance the record (see also RecordProfile class)
     * @param lang           language in which record data should be displayed
     * @param callback       repeats whatever you supply
     * @param request        incoming request
     * @param response       generated response
     * @return
     * @throws EuropeanaException
     */ // produces = MEDIA_TYPE_JSONLD_UTF8)
    @ApiOperation(value = "get single record in Schema.org JSON LD format", nickname = "getSingleRecordSchemaOrg")
    @GetMapping(value = "/{collectionId}/{recordId}.schema.jsonld", produces = { MEDIA_TYPE_JSONLD_UTF8 , MediaType.APPLICATION_JSON_UTF8_VALUE })
    public ModelAndView recordSchemaOrg(@PathVariable String collectionId,
                                        @PathVariable String recordId,
                                        @RequestParam(value = "wskey", required = true) String apikey,
                                        @RequestParam(value = "profile", required = false, defaultValue = "standard") String profile,
                                        @RequestParam(value = "lang", required = false) String lang,
                                        @RequestParam(value = "callback", required = false) String callback,
                                        @ApiIgnore HttpServletRequest request,
                                        @ApiIgnore HttpServletResponse response) throws EuropeanaException {
        RequestData data = new RequestData(collectionId, recordId, apikey, profile, lang, callback, request);
        return (ModelAndView) handleRecordRequest(RecordType.OBJECT_SCHEMA_ORG, data, response);
    }

    /**
     * Retrieve a record in RDF format
     *
     * @param collectionId   ID of data collection or data set
     * @param recordId       ID of record, item - a.k.a. 'localId'
     * @param apikey         formerly known as 'wskey'
     * @param profile        one or more profile values to enhance the record (see also RecordProfile class)
     * @param lang           language in which record data should be displayed
     * @param request        incoming request
     * @param response       generated response
     * @return
     * @throws EuropeanaException
     */
    @ApiOperation(value = "get single record in RDF format)", nickname = "getSingleRecordRDF")
    @GetMapping(value = "/{collectionId}/{recordId}.rdf", produces = MEDIA_TYPE_RDF_UTF8)
    public ModelAndView recordRdf(@PathVariable String collectionId,
                                  @PathVariable String recordId,
                                  @RequestParam(value = "wskey") String apikey,
                                  @RequestParam(value = "profile", required = false, defaultValue = "standard") String profile,
                                  @RequestParam(value = "lang", required = false) String lang,
                                  @ApiIgnore HttpServletRequest request,
                                  @ApiIgnore HttpServletResponse response) throws EuropeanaException {
        RequestData data = new RequestData(collectionId, recordId, apikey, profile, lang, null, request);
        return (ModelAndView) handleRecordRequest(RecordType.OBJECT_RDF, data, response);
    }

    /**
     * Retrieve a record in Turtle format
     *
     * @param collectionId   ID of data collection or data set
     * @param recordId       ID of record, item - a.k.a. 'localId'
     * @param wskey          pre-api term for 'apikey'
     * @param profile        one or more profile values to enhance the record (see also RecordProfile class)
     * @param lang           language in which record data should be displayed
     * @param request        incoming request
     * @param response       generated response
     * @return matching records in the turtle format
     * @throws EuropeanaException
     */
    @ApiOperation(value = "get single record in turtle format)", nickname = "getSingleRecordTurtle")
    @GetMapping(value = "/{collectionId}/{recordId}.ttl", produces = {MEDIA_TYPE_TURTLE, MEDIA_TYPE_TURTLE_TEXT, MEDIA_TYPE_TURTLE_X})
    public ModelAndView recordTurtle(@PathVariable String collectionId,
                             @PathVariable String recordId,
                             @RequestParam(value = "wskey") String wskey,
                             @RequestParam(value = "profile", required = false, defaultValue = "standard") String profile,
                             @RequestParam(value = "lang", required = false) String lang,
                             @ApiIgnore HttpServletRequest request,
                             @ApiIgnore HttpServletResponse response) throws EuropeanaException {
        RequestData data = new RequestData(collectionId, recordId, wskey, profile, lang, null, request);
        return (ModelAndView) handleRecordRequest(RecordType.OBJECT_TURTLE, data, response);
    }

    /**
     * The larger part of handling a record is the same for all types of output, so this method handles all the common
     * functionality like setting CORS headers, checking API key, retrieving the record for mongo and setting 301 or 404 if necessary
     */
    private Object handleRecordRequest(RecordType recordType, RequestData data, HttpServletResponse response)
            throws EuropeanaException {
        long startTime = System.currentTimeMillis();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Retrieving record with id {}, type = {}", data.europeanaId, recordType);
        }

        // 1) Check if HTTP method is supported, HTTP 405 if not
        if (!StringUtils.equalsIgnoreCase("GET", data.servletRequest.getMethod()) &&
                !StringUtils.equalsIgnoreCase("HEAD", data.servletRequest.getMethod())){
            response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return null;
        }

        // 2) check API key & routing
        apiKeyUtils.validateApiKey(data.wskey);
        Optional<DataSourceWrapper> dataSource = routeService.getRecordServerForRequest(data.servletRequest.getServerName());
        if (dataSource.isEmpty() || dataSource.get().getRecordDao().isEmpty()) {
            LOG.error("Error while retrieving record id {}, type= {}. No record server configured for route {}", 
                      data.europeanaId, recordType, data.servletRequest.getServerName());
            throw new InvalidConfigurationException(ProblemType.CONFIG_ERROR, "No CHO database configured for request route");
        }

        // 3) validate other common params
        if (!translateFilterService.isEnabled() && RecordProfile.TRANSLATE.isActive(data.profile)) {
            throw new TranslationServiceDisabledException();
        }
        if (data.lang != null) {
            data.setLanguages(Language.validateMultiple(data.lang));
        }

        // 4) get the fullbean
        FullBean bean = recordService.fetchFullBean(dataSource.get(), data.europeanaId, true);

        // 5) Check if record exists, return 404 if not
        ModelAndView result;
        if (Objects.isNull(bean)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            if (recordType == RecordType.OBJECT_RDF) {
                Map<String, Object> model = new HashMap<>();
                model.put("error", "Non-existing record identifier");
                result = new ModelAndView("rdf", model);
            } else {
                result = JsonUtils.toJson(new ApiError(data.wskey, "Invalid record identifier: "
                        + data.europeanaId), data.callback);
            }
            return result;
        }

       /*
        * 2017-07-06 PE: the code below was implemented as part of ticket #662. However as collections does not support this
        * yet activation of this functionality is postponed.
        *        if (!bean.getAbout().equals(data.europeanaId)) {
        *            response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
        *            response.setHeader("Location", generateRedirectUrl(data.servletRequest, data.europeanaId, bean.getAbout()));
        *            return null;
        *        }
        */

        // 6) Handle caching
        // ETag is created from timestamp + api version.
        String tsUpdated = httpCacheUtils.dateToRFC1123String(bean.getTimestampUpdated());
        String eTag      = httpCacheUtils.generateETag(data.europeanaId +tsUpdated, true, true);

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
        } else if (StringUtils.isNotBlank(data.servletRequest.getHeader(IFMATCH))) {
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

        // 7) Process bean further (adding webresource meta info, set proper urls)
        // cannot be null here, as method has already checked for record dao
        RecordDao recordDao = dataSource.get().getRecordDao().get();

        BaseUrlWrapper baseUrls = routeService.getBaseUrlsForRequest(data.servletRequest.getServerName());
        bean = recordService.enrichFullBean(recordDao, bean, baseUrls);

        // 8) When translation profile is active, do translation
        if (RecordProfile.TRANSLATE.isActive(data.profile)) {
            if (data.languages == null || data.languages.isEmpty()) {
                // Get the edm:language for default translation and filtering (if we find a default language)
                data.setLanguages(translateFilterService.getDefaultTranslationLanguage(bean));
            }
            if (data.languages != null && !data.languages.isEmpty()) {
                bean = translateFilterService.translateProxyFields(bean, data.languages);
            }
        }

        // 9) When lang profile is provided, do filtering
        if (data.languages != null && !data.languages.isEmpty()) {
            bean = BeanFilterLanguage.filter(bean, data.languages);
        }

        // 10) Generate output
        // add headers, except Content-Type (that differs per recordType)
        response = httpCacheUtils.addDefaultHeaders(response, eTag, tsUpdated);

        Object output;
        switch (recordType) {
            case OBJECT_JSON:
                output = generateJson(bean, data, startTime);
                break;
            case OBJECT_JSONLD:
                output = generateJsonLd(bean, data, response);
                break;
            case OBJECT_RDF:
                output = generateRdf(bean);
                break;
            case OBJECT_SCHEMA_ORG:
                output = generateSchemaOrg(bean, data, response);
                break;
            case OBJECT_TURTLE:
                output = generateTurtle(bean, data, response);
                break;
            default:
                throw new IllegalArgumentException("Unknown record output type: " + recordType);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Done generating record output in {} ms ", (System.currentTimeMillis() - startTime));
        }
        return output;
    }

    private ModelAndView generateJson(FullBean bean, RequestData data, long startTime) {
        ObjectResult objectResult = new ObjectResult(data.wskey);
        // add schemaOrg in the response if profile = schemaOrg
        if (RecordProfile.SCHEMAORG.isActive(data.profile)) {
            try {
                objectResult.schemaOrg = SchemaOrgUtils.toSchemaOrg((FullBeanImpl) bean);
            } catch (IOException e) {
                LOG.error("Error generating schema.org data", e);
            }
        }
        if (RecordProfile.PARAMS.isActive(data.profile)) {
            objectResult.addParams(RequestUtils.getParameterMap(data.servletRequest), "wskey");
            objectResult.addParam("profile", data.profile);
        }
        objectResult.object = new FullView(bean);
        objectResult.statsDuration = System.currentTimeMillis() - startTime;
        return JsonUtils.toJson(objectResult, data.callback);
    }

    private ModelAndView generateSchemaOrg(FullBean bean, RequestData data, HttpServletResponse response) {
        try {
            String jsonld = SchemaOrgUtils.toSchemaOrg((FullBeanImpl) bean);
            return JsonUtils.toJsonLd(jsonld, data.callback);
        } catch (IOException e) {
            LOG.error("Error generating schema.org data", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return JsonUtils.toJson(new ApiError(data.wskey, e.getClass().getSimpleName() + ": " + e.getMessage()), data.callback);
        }
    }

    private ModelAndView generateJsonLd(FullBean bean, RequestData data, HttpServletResponse response) {
        // get the web resources order
        List<String> orderOfWebResource = new ArrayList<>();
        bean.getAggregations().get(0).getWebResources().stream().forEach(webResource -> orderOfWebResource.add(webResource.getAbout()));

        String rdf    = EdmUtils.toEDM((FullBeanImpl) bean);
        try (InputStream rdfInput = IOUtils.toInputStream(rdf, StandardCharsets.UTF_8);
             OutputStream outputStream = new ByteArrayOutputStream()) {
                 RiotRdfUtils.disableErrorForSpaceURI();
                 Model  modelResult = ModelFactory.createDefaultModel().read(rdfInput, "", "RDF/XML");
                 DatasetGraph graph = DatasetFactory.wrap(modelResult).asDatasetGraph();
                 JsonLDWriteContext ctx = new JsonLDWriteContext();
                 ctx.setJsonLDContext(ObjectController.jsonldContext);
                 RDFWriterBuilder writerBuilder = RDFWriter.create();
                 RDFWriter writer = writerBuilder.source(graph).format(RDFFormat.JSONLD10_FLAT).context(ctx).build();
                 writer.output(outputStream);
                 // Jena model sorts the data with it's own logic. We can not manipulate the order there.
                // Hence, we will sort the web resources with JsonObject that is created by RDFWriter.
                String orderedJsonLd = ModelUtils.sortWebResources(orderOfWebResource, outputStream.toString());
                return JsonUtils.toJsonLd(orderedJsonLd, data.callback);
        } catch (IOException | IllegalAccessException | NoSuchFieldException e) {
            LOG.error("Error parsing JSON-LD data", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return JsonUtils.toJson(new ApiError(data.wskey, e.getClass().getSimpleName() + ": " + e.getMessage()), data.callback);
        }
    }

    private ModelAndView generateRdf(FullBean bean) {
        Map<String, Object> model = new HashMap<>();
        model.put("record", EdmUtils.toEDM((FullBeanImpl) bean));
        return new ModelAndView("rdf", model);
    }

    private ModelAndView generateTurtle(FullBean bean, RequestData data, HttpServletResponse response) {
        Map<String, Object> model = new HashMap<>();
        String rdf    = EdmUtils.toEDM((FullBeanImpl) bean);
        try (OutputStream outputStream = new ByteArrayOutputStream();
             InputStream rdfInput = IOUtils.toInputStream(rdf);
             TurtleRecordWriter writer= new TurtleRecordWriter(outputStream)) {
             Model modelResult = ModelFactory.createDefaultModel().read(rdfInput, "", "RDF/XML");
             writer.write(modelResult);
             model.put("record", outputStream);
             return new ModelAndView("ttl", model);
        } catch (IOException | IllegalAccessException | NoSuchFieldException e) {
            LOG.error("Error parsing Turtle data for record {}", bean.getAbout(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return JsonUtils.toJson(new ApiError(data.wskey, e.getClass().getSimpleName() + ": " + e.getMessage()), data.callback);
        }
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
        String             europeanaId;
        String             wskey;
        String             profile;
        String             lang;
        List<Language>     languages;
        String             callback;
        HttpServletRequest servletRequest;

        RequestData(String collectionId, String recordId, String wskey, String profile, String lang, String callback,
                    HttpServletRequest servletRequest) {
            this.europeanaId    = EuropeanaUriUtils.createEuropeanaId(collectionId, recordId);
            this.wskey          = wskey;
            this.profile        = profile;
            this.lang           = lang;
            this.callback       = callback;
            this.servletRequest = servletRequest;
        }

        void setLanguages(List<Language> languages) {
            this.languages = languages;
        }
    }
}
