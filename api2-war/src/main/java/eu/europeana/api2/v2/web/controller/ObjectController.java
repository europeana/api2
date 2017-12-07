/*
 * Copyright 2007-2015 The Europeana Foundation
 *
 * Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 * by the European Commission;
 * You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 * http://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 * any kind, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under
 * the Licence.
 */

package eu.europeana.api2.v2.web.controller;

import com.github.jsonldjava.core.JSONLD;
import com.github.jsonldjava.core.JSONLDProcessingError;
import com.github.jsonldjava.core.Options;
import com.github.jsonldjava.impl.JenaRDFParser;
import com.github.jsonldjava.utils.JSONUtils;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import eu.europeana.api2.ApiLimitException;
import eu.europeana.api2.model.json.ApiError;
import eu.europeana.api2.model.xml.srw.SrwResponse;
import eu.europeana.api2.utils.JsonUtils;
import eu.europeana.api2.v2.model.LimitResponse;
import eu.europeana.api2.v2.model.enums.Profile;
import eu.europeana.api2.v2.model.json.ObjectResult;
import eu.europeana.api2.v2.model.json.view.BriefView;
import eu.europeana.api2.v2.model.json.view.FullDoc;
import eu.europeana.api2.v2.model.json.view.FullView;
import eu.europeana.api2.v2.model.xml.srw.Record;
import eu.europeana.api2.v2.utils.ApiKeyUtils;
import eu.europeana.api2.v2.utils.ControllerUtils;
import eu.europeana.api2.v2.web.swagger.SwaggerIgnore;
import eu.europeana.api2.v2.web.swagger.SwaggerSelect;
import eu.europeana.corelib.db.entity.enums.RecordType;
import eu.europeana.corelib.definitions.edm.beans.BriefBean;
import eu.europeana.corelib.definitions.edm.beans.FullBean;
import eu.europeana.corelib.edm.exceptions.BadDataException;
import eu.europeana.corelib.neo4j.exception.Neo4JException;
import eu.europeana.corelib.web.exception.EuropeanaException;
import eu.europeana.corelib.edm.exceptions.MongoDBException;
import eu.europeana.corelib.edm.utils.EdmUtils;
import eu.europeana.corelib.search.SearchService;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.corelib.edm.exceptions.MongoRuntimeException;
import eu.europeana.corelib.utils.EuropeanaUriUtils;
import eu.europeana.corelib.web.utils.RequestUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.*;

/**
 * Provides record information in all kinds of formats; json, json-ld, rdf and srw
 *
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 */
@Controller
@Api(tags = {"Record"})
@RequestMapping(value = "/v2/record")
@SwaggerSelect
public class ObjectController {

    private static final Logger LOG = Logger.getLogger(ObjectController.class);

    private SearchService searchService;

    private ApiKeyUtils apiKeyUtils;

    /**
     * Create a new ObjectController
     *
     * @param searchService
     * @param apiKeyUtils
     */
    @Autowired
    public ObjectController(SearchService searchService, ApiKeyUtils apiKeyUtils) {
        this.searchService = searchService;
        this.apiKeyUtils = apiKeyUtils;
    }

    /**
     * Handles record.json GET requests. Each request should consists of at least a collectionId, a recordId and an api-key (wskey)
     *
     * @param collectionId
     * @param recordId
     * @param profile        supported types are 'params' and 'similar'
     * @param wskey
     * @param callback
     * @param webRequest
     * @param servletRequest
     * @param response
     * @return
     * @throws ApiLimitException
     */
    @ApiOperation(value = "get a single record in JSON format", nickname = "getSingleRecordJson")
    @RequestMapping(value = "/{collectionId}/{recordId}.json", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ModelAndView record(@PathVariable String collectionId,
                               @PathVariable String recordId,
                               @RequestParam(value = "profile", required = false, defaultValue = "full") String profile,
                               @RequestParam(value = "wskey", required = true) String wskey,
                               @RequestParam(value = "callback", required = false) String callback,
                               WebRequest webRequest,
                               HttpServletRequest servletRequest,
                               HttpServletResponse response) throws ApiLimitException {
        RequestData data = new RequestData(collectionId, recordId, wskey, profile, callback, webRequest, servletRequest);
        try {
            return (ModelAndView) handleRecordRequest(RecordType.OBJECT, data, response);
        } catch (EuropeanaException e) {
            LOG.error("Error retrieving record JSON", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return JsonUtils.toJson(new ApiError(wskey, e.getClass().getSimpleName() + ": " + e.getMessage(), data.apikeyCheckResponse.getRequestNumber()), data.callback);
        }
    }

    /**
     * @param callback
     * @return only the context part of a json-ld record
     */
    @SwaggerIgnore
    @RequestMapping(value = {"/context.jsonld", "/context.json-ld"}, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ModelAndView contextJSONLD(@RequestParam(value = "callback", required = false) String callback) {
        String jsonld = JSONUtils.toString(getJsonContext());
        return JsonUtils.toJson(jsonld, callback);
    }

    /**
     * Retrieve a record in JSON-LD format (hidden alias for record.jsonld request)
     *
     * @param collectionId
     * @param recordId
     * @param wskey
     * @param format
     * @param callback
     * @param webRequest
     * @param servletRequest
     * @param response
     * @return
     * @throws ApiLimitException
     */
    @SwaggerIgnore
    @RequestMapping(value = "/{collectionId}/{recordId}.json-ld", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ModelAndView recordJSON_LD(@PathVariable String collectionId,
                                      @PathVariable String recordId,
                                      @RequestParam(value = "wskey", required = true) String wskey,
                                      @RequestParam(value = "format", required = false, defaultValue = "compacted") String format,
                                      @RequestParam(value = "callback", required = false) String callback,
                                      WebRequest webRequest,
                                      HttpServletRequest servletRequest,
                                      HttpServletResponse response) throws ApiLimitException {
        return recordJSONLD(collectionId, recordId, wskey, format, callback, webRequest, servletRequest, response);
    }

    /***
     * Retrieve a record in JSON-LD format.
     * @param collectionId
     * @param recordId
     * @param wskey
     * @param format supported types are 'compacted', 'flattened' and 'normalized'
     * @param callback
     * @param webRequest
     * @param servletRequest
     * @param response
     * @return
     * @throws ApiLimitException
     */
    @ApiOperation(value = "get single record in JSON LD format", nickname = "getSingleRecordJsonLD")
    @RequestMapping(value = "/{collectionId}/{recordId}.jsonld", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ModelAndView recordJSONLD(@PathVariable String collectionId,
                                     @PathVariable String recordId,
                                     @RequestParam(value = "wskey", required = true) String wskey,
                                     @RequestParam(value = "format", required = false, defaultValue = "compacted") String format,
                                     @RequestParam(value = "callback", required = false) String callback,
                                     WebRequest webRequest,
                                     HttpServletRequest servletRequest,
                                     HttpServletResponse response) throws ApiLimitException {

        RequestData data = new RequestData(collectionId, recordId, wskey, format, callback, webRequest, servletRequest);
        try {
            return (ModelAndView) handleRecordRequest(RecordType.OBJECT_JSONLD, data, response);
        } catch (EuropeanaException e) {
            LOG.error("Error retrieving record JSON-LD", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return JsonUtils.toJson(new ApiError(wskey, e.getClass().getSimpleName() + ": " + e.getMessage(), data.apikeyCheckResponse.getRequestNumber()), data.callback);
        }
    }

    /**
     * Retrieve a record in RDF format
     *
     * @param collectionId
     * @param recordId
     * @param wskey
     * @param webRequest
     * @param servletRequest
     * @param response
     * @return
     * @throws ApiLimitException
     */
    @ApiOperation(value = "get single record in RDF format)", nickname = "getSingleRecordRDF")
    @RequestMapping(value = "/{collectionId}/{recordId}.rdf", method = RequestMethod.GET, produces = "application/rdf+xml")
    public ModelAndView recordRdf(@PathVariable String collectionId,
                                  @PathVariable String recordId,
                                  @RequestParam(value = "wskey", required = true) String wskey,
                                  WebRequest webRequest,
                                  HttpServletRequest servletRequest,
                                  HttpServletResponse response) throws ApiLimitException {
        RequestData data = new RequestData(collectionId, recordId, wskey, null, null, webRequest, servletRequest);
        try {
            return (ModelAndView) handleRecordRequest(RecordType.OBJECT_RDF, data, response);
        } catch (EuropeanaException e) {
            LOG.error("Error retrieving record RDF", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return JsonUtils.toJson(new ApiError(wskey, e.getClass().getSimpleName() + ": " + e.getMessage(), data.apikeyCheckResponse.getRequestNumber()), data.callback);
        }
    }

    /**
     * Provides records in SRU/SRW (XML) format.
     *
     * @param collectionId
     * @param recordId
     * @param wskey
     * @param webRequest
     * @param servletRequest
     * @param response
     * @return
     * @throws Exception
     */
    // 2017-06-16 This code hasn't been used for a long time (not a single .srw request was logged in Kibana)
    // However, depending on the results of a to-be-held survey among developers we may bring this back to life again
    @SwaggerIgnore
    @RequestMapping(value = "/{collectionId}/{recordId}.srw", method = RequestMethod.GET, produces = MediaType.TEXT_XML_VALUE)
    public @ResponseBody
    SrwResponse recordSrw(@PathVariable String collectionId,
                          @PathVariable String recordId,
                          @RequestParam(value = "wskey", required = false) String wskey,
                          WebRequest webRequest,
                          HttpServletRequest servletRequest,
                          HttpServletResponse response) throws ApiLimitException, EuropeanaException {
        RequestData data = new RequestData(collectionId, recordId, wskey, null, null, webRequest, servletRequest);
        // output can be an SrwResponse (status 200)
        Object out = handleRecordRequest(RecordType.OBJECT_SRW, data, response);
        if (out instanceof SrwResponse) {
            return (SrwResponse) out;
        }
        // or output is a ModelAndView and status 404 or 301
        return null;
    }


    /**
     * The larger part of handling a record is the same for all types of output, so this method handles all the common
     * functionality like setting CORS headers, checking API key, retrieving the record for mongo and setting 301 or 404 if necessary
     */
    private Object handleRecordRequest(RecordType recordType, RequestData data, HttpServletResponse response)
            throws ApiLimitException, EuropeanaException {
        long startTime = System.currentTimeMillis();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Retrieving record with id " + data.europeanaObjectId + ", type = " + recordType);
        }

        // check apikey and add default headers
        data.apikeyCheckResponse = apiKeyUtils.checkLimit(data.wskey, data.servletRequest.getRequestURL().toString(), recordType, data.profile);
        ControllerUtils.addResponseHeaders(response);

        // retrieve record data
        FullBean bean = retrieveRecord(data.europeanaObjectId);
        if (bean == null) {
            ModelAndView result;
            // record not found, check if we can redirect
            String newId = findNewId(data.europeanaObjectId);

            // 2017-07-06 code PE: inserted as temp workaround until we resolve #662 (see also comment below)
            if (newId != null) {
                bean = retrieveRecord(newId);
            }
            if (bean == null) {
                // -- end of insert


                //if (newId == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                if (recordType == RecordType.OBJECT_RDF) {
                    Map<String, Object> model = new HashMap<>();
                    model.put("error", "Non-existing record identifier");
                    result = new ModelAndView("rdf", model);
                } else if (recordType == RecordType.OBJECT_SRW) {
                    // no official supported way to return xml error message yet
                    result = null;
                } else {
                    result = JsonUtils.toJson(new ApiError(data.wskey, "Invalid record identifier: "
                            + data.europeanaObjectId, data.apikeyCheckResponse.getRequestNumber()), data.callback);
                }
                return result;
                // 2017-07-06 PE: Code below was implemented as part of ticket #662. However as collections does not support his yet,
                // activation of this functionality is postponed
                //} else {
                //  response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
                //  response.setHeader("Location", generateRedirectUrl(data.servletRequest, data.europeanaObjectId, newId));
                //  result = null;
                //}
            }
            //return result;
        }

        // check modified
        // 2017-07-10 PE: Decided to postpone the modified check for now (see also ticket 676)
        //if (bean.getTimestampUpdated() != null && data.webRequest.checkNotModified(bean.getTimestampUpdated().getTime()))
        {
            // checkNotModified method will set LastModified header automatically and will return 304 - Not modified if necessary
            // (but only when clients include the If_Modified_Since header in their request)
        }

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
            case OBJECT_SRW:
                output = generateSrw(bean);
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
        ObjectResult objectResult = new ObjectResult(data.wskey, data.apikeyCheckResponse.getRequestNumber());

        if (StringUtils.containsIgnoreCase(data.profile, "params")) {
            objectResult.addParams(RequestUtils.getParameterMap(data.servletRequest), "wskey");
            objectResult.addParam("profile", data.profile);
        }

        if (StringUtils.containsIgnoreCase(data.profile, Profile.SIMILAR.getName())) {
            objectResult.similarItems = getSimilarItems(data.europeanaObjectId, data.wskey);
        }

        objectResult.object = new FullView(bean, data.profile, data.wskey);
        objectResult.statsDuration = System.currentTimeMillis() - startTime;
        return JsonUtils.toJson(objectResult, data.callback);
    }

    private ModelAndView generateJsonLd(FullBean bean, RequestData data, HttpServletResponse response) {
        String jsonld = null;
        String rdf    = EdmUtils.toEDM((FullBeanImpl) bean, false);
        try (InputStream rdfInput = IOUtils.toInputStream(rdf)) {
            Model  modelResult = ModelFactory.createDefaultModel().read(rdfInput, "", "RDF/XML");
            Object raw         = JSONLD.fromRDF(modelResult, new JenaRDFParser());
            if (StringUtils.equalsIgnoreCase(data.profile, "compacted")) {
                raw = JSONLD.compact(raw, getJsonContext(), new Options());
            } else if (StringUtils.equalsIgnoreCase(data.profile, "flattened")) {
                raw = JSONLD.flatten(raw);
            } else if (StringUtils.equalsIgnoreCase(data.profile, "normalized")) {
                raw = JSONLD.normalize(raw);
            }
            jsonld = JSONUtils.toString(raw);
        } catch (IOException | JSONLDProcessingError e) {
            LOG.error("Error parsing JSON-LD data", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return JsonUtils.toJson(new ApiError(data.wskey, e.getClass().getSimpleName() + ": " + e.getMessage()), data.callback);
        }
        return JsonUtils.toJson(jsonld, data.callback);
    }

    private ModelAndView generateRdf(FullBean bean) {
        Map<String, Object> model = new HashMap<>();
        model.put("record", EdmUtils.toEDM((FullBeanImpl) bean, false));
        return new ModelAndView("rdf", model);
    }

    private SrwResponse generateSrw(FullBean bean) {
        SrwResponse srwResponse = new SrwResponse();
        FullDoc     doc         = new FullDoc(bean); // TODO this will generate date ParseExceptions, need to investigate
        Record      record      = new Record();
        record.recordData.dc = doc;
        srwResponse.records.record.add(record);
        try {
            createXml(srwResponse);
        } catch (JAXBException e) {
            LOG.error("Error generating xml", e);
        }
        return srwResponse;
    }

    private FullBean retrieveRecord(String europeanaId) throws MongoRuntimeException, MongoDBException, Neo4JException {
        long     startTime = System.currentTimeMillis();
        FullBean result    = searchService.findById(europeanaId, false);
        if (LOG.isDebugEnabled()) {
            LOG.debug("SearchService findByID took " + (System.currentTimeMillis() - startTime) + " ms");
        }
        return result;
    }

    private String findNewId(String europeanaId) throws BadDataException {
        long   startTime = System.currentTimeMillis();
        String newId     = searchService.resolveId(europeanaId);
        if (LOG.isDebugEnabled()) {
            LOG.debug("SearchService find newId took " + (System.currentTimeMillis() - startTime) + " ms");
        }
        return newId;
    }

    /**
     * Reconstruct the original url and instead of the old EuropeanaId inserts the new provided one.
     * <p>
     * Original code snippet was copied from https://stackoverflow.com/a/5212336 and slightly adjusted.
     *
     * @param req
     * @param oldId
     * @param newId
     * @return
     */
    private String generateRedirectUrl(HttpServletRequest req, String oldId, String newId) {

        String scheme      = req.getScheme();             // http
        String serverName  = req.getServerName();     // www.europeana.eu
        int    serverPort  = req.getServerPort();        // 80
        String requestUri  = req.getRequestURI();     // /api/v2/record/90402/BK_1978_399.json
        String pathInfo    = req.getPathInfo();         //
        String queryString = req.getQueryString();   // wskey=....

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

    private List<BriefView> getSimilarItems(String europeanaId, String wskey) {
        List<BriefView> result = new ArrayList<>();
        try {
            long            startTime    = System.currentTimeMillis();
            List<BriefBean> similarItems = searchService.findMoreLikeThis(europeanaId);
            if (LOG.isDebugEnabled()) {
                LOG.debug("SearchService find similar items took " + (System.currentTimeMillis() - startTime) + " ms");
            }
            for (BriefBean b : similarItems) {
                String    similarItemsProfile = "minimal";
                BriefView view                = new BriefView(b, similarItemsProfile, wskey);
                result.add(view);
            }
        } catch (SolrServerException e) {
            LOG.error("Error retrieving similar items: " + e.getLocalizedMessage(), e);
        }
        return result;
    }

    private Object getJsonContext() {
        try (InputStream in = this.getClass().getResourceAsStream("/jsonld/context.jsonld")) {
            return JSONUtils.fromInputStream(in);
        } catch (IOException e) {
            LOG.error("Error reading context.jsonld", e);
        }
        return null;
    }

    private void createXml(SrwResponse response) throws JAXBException {
        final JAXBContext  context      = JAXBContext.newInstance(SrwResponse.class);
        final Marshaller   marshaller   = context.createMarshaller();
        final StringWriter stringWriter = new StringWriter();
        marshaller.marshal(response, stringWriter);
    }

    /**
     * Helper class so we can pass all data around in 1 object (and not specify many parameters)
     */
    private static class RequestData {
        protected String             europeanaObjectId;
        protected String             profile; // called format in json-ld
        protected String             wskey;
        protected LimitResponse      apikeyCheckResponse;
        protected String             callback;
        protected WebRequest         webRequest;
        protected HttpServletRequest servletRequest;

        public RequestData(String collectionId, String recordId, String wskey, String profile, String callback, WebRequest webRequest, HttpServletRequest servletRequest) {
            this.europeanaObjectId = EuropeanaUriUtils.createEuropeanaId(collectionId, recordId);
            this.wskey = wskey;
            this.profile = profile;
            this.callback = callback;
            this.webRequest = webRequest;
            this.servletRequest = servletRequest;
        }
    }
}