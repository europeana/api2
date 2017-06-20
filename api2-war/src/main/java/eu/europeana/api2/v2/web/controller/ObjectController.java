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
import eu.europeana.api2.model.json.ApiNotImplementedYet;
import eu.europeana.api2.model.json.abstracts.ApiResponse;
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
import eu.europeana.corelib.db.exception.DatabaseException;
import eu.europeana.corelib.db.exception.LimitReachedException;
import eu.europeana.corelib.db.service.ApiKeyService;
import eu.europeana.corelib.definitions.db.entity.relational.ApiKey;
import eu.europeana.corelib.definitions.edm.beans.BriefBean;
import eu.europeana.corelib.definitions.edm.beans.FullBean;
import eu.europeana.corelib.neo4j.exception.Neo4JException;
import eu.europeana.corelib.web.exception.ProblemType;
import eu.europeana.corelib.edm.exceptions.EuropeanaQueryException;
import eu.europeana.corelib.edm.exceptions.MongoDBException;
import eu.europeana.corelib.edm.exceptions.SolrTypeException;
import eu.europeana.corelib.edm.utils.EdmUtils;
import eu.europeana.corelib.search.SearchService;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.corelib.edm.exceptions.MongoRuntimeException;
import eu.europeana.corelib.utils.EuropeanaUriUtils;
import eu.europeana.corelib.web.service.EuropeanaUrlService;
import eu.europeana.corelib.web.utils.RequestUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
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
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 */
@Controller
@Api(tags = {"Record"}, description = " ")
@RequestMapping(value = "/v2/record")
@SwaggerSelect
public class ObjectController {

    private Logger log = Logger.getLogger(ObjectController.class);

    @Resource
    private SearchService searchService;

    @Resource
    private ApiKeyService apiService;

    @Resource
    private EuropeanaUrlService urlService;

    @Resource
    private ApiKeyUtils apiKeyUtils;

    /**
     * Handles record.json GET requests. Each request should consists of at least a collectionId, a recordId and an api-key (wskey)
     * @param collectionId
     * @param recordId
     * @param profile
     * @param wskey
     * @param callback
     * @param request
     * @param response
     * @return
     * @throws MongoRuntimeException
     */
    @ApiOperation(value = "get a single record in JSON format", nickname = "getSingleRecordJson")
    @RequestMapping(value = "/{collectionId}/{recordId}.json", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ModelAndView record(
            @PathVariable String collectionId,
            @PathVariable String recordId,
            @RequestParam(value = "profile", required = false, defaultValue = "full") String profile,
            @RequestParam(value = "wskey", required = true) String wskey,
            @RequestParam(value = "callback", required = false) String callback,
            HttpServletRequest request,
            HttpServletResponse response) throws MongoRuntimeException {
        if (log.isDebugEnabled()) { log.debug("Retrieving record with id "+collectionId+"/"+recordId); }
        ControllerUtils.addResponseHeaders(response);

        LimitResponse limitResponse;

        long t9 = System.currentTimeMillis();
        try {
            limitResponse = apiKeyUtils.checkLimit(wskey, request.getRequestURL().toString(),
                    RecordType.OBJECT, profile);
            if (log.isDebugEnabled()) { log.debug("Apikey checklimit took: " + (System.currentTimeMillis() - t9) + " milliseconds"); }
        } catch (ApiLimitException e) {
            response.setStatus(e.getHttpStatus());
            return JsonUtils.toJson(new ApiError(e), callback);
        }

        ObjectResult objectResult = new ObjectResult(wskey, limitResponse.getRequestNumber());
        if (StringUtils.containsIgnoreCase(profile, "params")) {
            objectResult.addParams(RequestUtils.getParameterMap(request), "wskey");
            objectResult.addParam("profile", profile);
        }

        String europeanaObjectId = EuropeanaUriUtils.createResolveEuropeanaId(collectionId, recordId);
        String originalObjectId = europeanaObjectId;
        try {
            long t0 = (new Date()).getTime();
            long t2 = System.currentTimeMillis();
            // first try to retrieve the bean directly
            FullBean bean = searchService.findById(europeanaObjectId, false);
            if (log.isDebugEnabled()) { log.debug("SearchService findByID took: " + (System.currentTimeMillis() - t2) + " milliseconds"); }
            if (bean == null) {
                // if the bean is null, the record id may have changed so check for that
                t2 = System.currentTimeMillis();
                europeanaObjectId = searchService.resolveId(europeanaObjectId);
                if (log.isDebugEnabled()) { log.debug("Bean = null; SearchService resolveID took: " + (System.currentTimeMillis() - t2) + " milliseconds"); }
                // retry retrieving the bean if we have a new id
                if (europeanaObjectId != null) {
                    t2 = System.currentTimeMillis();
                    bean = searchService.findById(europeanaObjectId, false);
                    if (log.isDebugEnabled()) { log.debug("Bean = null; retrying SearchService findByID now took: " + (System.currentTimeMillis() - t2) + " milliseconds"); }
                    if (bean == null) { // detect potential errors in record redirect data, we log it because we're not sure how often this happens
                        log.warn("Retrieved new recordId "+europeanaObjectId+" but still unable to find record.");
                    }
                }
            }
//            if (bean != null && bean.isOptedOut()) {
//                bean.getAggregations().get(0).setEdmObject("");
//            }
            if (bean == null) {
                response.setStatus(404);
                return JsonUtils.toJson(new ApiError(wskey, "Invalid record identifier: "
                        + originalObjectId, limitResponse.getRequestNumber()), callback);
            }

            if (StringUtils.containsIgnoreCase(profile, Profile.SIMILAR.getName())) {
                List<BriefBean> similarItems;
                List<BriefView> beans = new ArrayList<>();
                try {
                    t2 = System.currentTimeMillis();
                    similarItems = searchService.findMoreLikeThis(europeanaObjectId);
                    if (log.isDebugEnabled()) { log.debug("SearchService find similar items took: " + (System.currentTimeMillis() - t2) + " milliseconds"); }
                    for (BriefBean b : similarItems) {
                        String similarItemsProfile = "minimal";
                        BriefView view = new BriefView(b, similarItemsProfile, wskey);

                        beans.add(view);
                    }
                } catch (SolrServerException e) {
                    log.error("Error during getting similar items: " + e.getLocalizedMessage(), e);
                }
                objectResult.similarItems = beans;
            }
            objectResult.object = new FullView(bean, profile, wskey);
            long t1 = (new Date()).getTime();
            objectResult.statsDuration = (t1 - t0);
            if (log.isDebugEnabled()) { log.debug("Record retrieval took: " + (System.currentTimeMillis() - t9) + " milliseconds"); }
        } catch (Exception e) {
            response.setStatus(500);
            return JsonUtils.toJson(new ApiError(wskey, e.getClass().getSimpleName() + ": "+ e.getMessage(), limitResponse.getRequestNumber()), callback);
        }

//        final ObjectMapper objectMapper = new ObjectMapper();
//        objectMapper.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
//        final ObjectWriter ow = objectMapper.writer().withDefaultPrettyPrinter();

        return JsonUtils.toJson(objectResult, callback);
    }

    @SuppressWarnings("unused")
    @SwaggerIgnore
    @RequestMapping(value = "/{collectionId}/{recordId}.kml", method = RequestMethod.GET, produces = "application/vnd.google-earth.kml+xml")
    @ResponseBody
    public ApiResponse searchKml(
            @PathVariable String collectionId,
            @PathVariable String recordId,
            @RequestParam(value = "apikey", required = true) String apiKey,
            @RequestParam(value = "sessionhash", required = true) String sessionHash) {
        return new ApiNotImplementedYet(apiKey);
    }

    @SwaggerIgnore
    @RequestMapping(value = {"/context.jsonld", "/context.json-ld"}, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ModelAndView contextJSONLD(
            @RequestParam(value = "callback", required = false) String callback
    ) {
        String jsonld = JSONUtils.toString(getJsonContext());
        return JsonUtils.toJson(jsonld, callback);
    }

    @SwaggerIgnore
    @RequestMapping(value = "/{collectionId}/{recordId}.json-ld",
            method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ModelAndView recordJSON_LD(
            @PathVariable String collectionId,
            @PathVariable String recordId,
            @RequestParam(value = "wskey", required = true) String wskey,
            @RequestParam(value = "format", required = false, defaultValue = "compacted") String format,
            @RequestParam(value = "callback", required = false) String callback,
            HttpServletRequest request, HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");
        return recordJSONLD(collectionId, recordId, wskey, format, callback, request, response);
    }


    @ApiOperation(value = "get single record in JSON LD format", nickname = "getSingleRecordJsonLD")
    @RequestMapping(value = "/{collectionId}/{recordId}.jsonld",
            method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ModelAndView recordJSONLD(
            @PathVariable String collectionId,
            @PathVariable String recordId,
            @RequestParam(value = "wskey", required = true) String wskey,
            @RequestParam(value = "format", required = false, defaultValue = "compacted") String format,
            @RequestParam(value = "callback", required = false) String callback,
            HttpServletRequest request, HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");

        try {
            apiKeyUtils.checkLimit(wskey,
                    request.getRequestURL().toString(), RecordType.OBJECT_JSONLD, null);
        } catch (ApiLimitException e) {
            response.setStatus(e.getHttpStatus());
            return JsonUtils.toJson(new ApiError(e), callback);
        }

        String europeanaObjectId = "/" + collectionId + "/" + recordId;

        String jsonld = null;

        FullBeanImpl bean = null;
        try {
            bean = (FullBeanImpl) searchService.findById(europeanaObjectId, false);
            if (bean == null) {
                bean = (FullBeanImpl) searchService.resolve(europeanaObjectId, false);
            }
        } catch (SolrTypeException | MongoDBException | MongoRuntimeException e) {
            log.error(ExceptionUtils.getFullStackTrace(e));
        } catch (Neo4JException e) {
            log.error("Neo4JException thrown: " + e.getMessage());
            log.error("Cause: " + e.getCause());
        }

        if (bean != null) {
            String rdf = EdmUtils.toEDM(bean, false);
            InputStream rdfInput = null;
            try {
                rdfInput = IOUtils.toInputStream(rdf);
                Model modelResult = ModelFactory.createDefaultModel().read(rdfInput, "", "RDF/XML");
                JenaRDFParser parser = new JenaRDFParser();
                Object raw = JSONLD.fromRDF(modelResult, parser);
                if (StringUtils.equalsIgnoreCase(format, "compacted")) {
                    raw = JSONLD.compact(raw, getJsonContext(), new Options());
                } else if (StringUtils.equalsIgnoreCase(format, "flattened")) {
                    raw = JSONLD.flatten(raw);
                } else if (StringUtils.equalsIgnoreCase(format, "normalized")) {
                    raw = JSONLD.normalize(raw);
                }
                jsonld = JSONUtils.toString(raw);
            } catch (JSONLDProcessingError e) {
                log.error(e.getMessage(), e);
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
            finally {
                IOUtils.closeQuietly(rdfInput);
            }
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }

        return JsonUtils.toJson(jsonld, callback);
    }

    @ApiOperation(value = "get single record in RDF format)", nickname = "getSingleRecordRDF")
    @RequestMapping(value = "/{collectionId}/{recordId}.rdf", method = RequestMethod.GET, produces = "application/rdf+xml")
    public ModelAndView recordRdf(@PathVariable String collectionId,
                                  @PathVariable String recordId,
                                  @RequestParam(value = "wskey", required = true) String wskey,
                                  HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> model = new HashMap<>();
        model.put("error", "");

        String europeanaObjectId = "/" + collectionId + "/" + recordId;

        ApiKey apiKey;
        try {
            apiKey = apiService.findByID(wskey);
            if (apiKey == null) {
                response.setStatus(401);
                model.put("error", "Unregistered API key");
                return new ModelAndView("rdf", model);
            }
            apiKey.getUsageLimit();
            apiService.checkReachedLimit(apiKey);
        } catch (DatabaseException e) {
            // Disabled while awaiting better implementation (ticket #1742)
            // apiLogService.logApiRequest(wskey, requestUri, RecordType.OBJECT_RDF, profile);
            model.put("error", e.getMessage());
            response.setStatus(401);
            return new ModelAndView("rdf", model);
            // return JsonUtils.toJson(new ApiError(wskey, "record.json", e.getMessage(), requestNumber));
        } catch (LimitReachedException e) {
            // Disabled while awaiting better implementation (ticket #1742)
            // apiLogService.logApiRequest(wskey, requestUri, RecordType.LIMIT, profile);
            log.error(e.getMessage());
            model.put("error", e.getMessage());
            response.setStatus(429);
            return new ModelAndView("rdf", model);
            // return JsonUtils.toJson(new ApiError(wskey, "record.json", e.getMessage(), e.getRequested()));
        }

        FullBeanImpl bean = null;
        try {
            bean = (FullBeanImpl) searchService.findById(europeanaObjectId, false);
            if (bean == null) {
                bean = (FullBeanImpl) searchService.resolve(europeanaObjectId, false);
            }
        } catch (SolrTypeException | MongoDBException | MongoRuntimeException e) {
            log.error(ExceptionUtils.getFullStackTrace(e));
        } catch (Neo4JException e) {
            log.error("Neo4JException thrown: " + e.getMessage());
            log.error("Cause: " + e.getCause());
        }

        if (bean != null) {
            model.put("record", EdmUtils.toEDM(bean, false));
        } else {
            response.setStatus(404);
            model.put("error", "Non-existing record identifier");
        }

        // Disabled while awaiting better implementation (ticket #1742)
        // apiLogService.logApiRequest(wskey, requestUri, RecordType.OBJECT_RDF, profile);
        return new ModelAndView("rdf", model);
    }

    private Object getJsonContext() {
        InputStream in = this.getClass().getResourceAsStream("/jsonld/context.jsonld");
        try {
            return JSONUtils.fromInputStream(in);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(in);
        }
        return null;
    }

    @SwaggerIgnore
    @RequestMapping(value = "/{collectionId}/{recordId}.srw", method = RequestMethod.GET, produces = MediaType.TEXT_XML_VALUE)
    public @ResponseBody SrwResponse recordSrw(
            @PathVariable String collectionId,
          @PathVariable String recordId,
          @RequestParam(value = "wskey", required = false) String wskey,
          HttpServletResponse response)
            throws Exception {
        log.info("====== /v2/record/{collectionId}/{recordId}.srw ======");

        if (StringUtils.isBlank(wskey)) {
            // model.put("json", utils.toJson(new ApiError(wskey, "search.json", "No API authorisation key.")));
            throw new EuropeanaQueryException(ProblemType.NO_PASSWORD);
        }

        ApiKey apiKey = apiService.findByID(wskey);
        if (apiKey == null) {
            // model.put("json", utils.toJson(new ApiError(wskey, "search.json", "Unregistered user")));
            throw new EuropeanaQueryException(ProblemType.NO_PASSWORD);
            // hasResult = true;
        }

        String europeanaObjectId = "/" + collectionId + "/" + recordId;
        FullBean bean = searchService.findById(europeanaObjectId, true);
        if (bean == null) {
            bean = searchService.resolve(europeanaObjectId, true);
        }

        SrwResponse srwResponse = new SrwResponse();
        FullDoc doc;
        if (bean != null) {
            doc = new FullDoc(bean);
            Record record = new Record();
            record.recordData.dc = doc;
            srwResponse.records.record.add(record);
            log.info("record added");
        } else {
            String url = urlService.getPortalRecord(true, collectionId, recordId).toString();
            response.setStatus(302);
            response.setHeader("Location", url);
            return null;
        }
        createXml(srwResponse);
        log.info("xml created");
        return srwResponse;
    }

    private void createXml(SrwResponse response) {
        try {
            final JAXBContext context = JAXBContext.newInstance(SrwResponse.class);
            final Marshaller marshaller = context.createMarshaller();
            final StringWriter stringWriter = new StringWriter();
            marshaller.marshal(response, stringWriter);
            if (log.isInfoEnabled()) {
                log.info("result: " + stringWriter.toString());
            }
        } catch (JAXBException e) {
            log.error("JAXBException: " + e.getMessage() + ", " + e.getCause().getMessage(), e);
        }
    }
}