package eu.europeana.api2.v2.service.translate;

import eu.europeana.api2.v2.exceptions.TranslationException;
import eu.europeana.api2.v2.model.translate.Language;
import eu.europeana.api2.v2.utils.PangeanicUtils;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Service to send data to translate to Pangeanic Translate API V2
 * @author Srishti Singh
 */
@Service
@PropertySource("classpath:europeana.properties")
@PropertySource(value = "classpath:europeana.user.properties", ignoreResourceNotFound = true)
public class PangeanicTranslationServiceV2 implements TranslationService  {

    private static final Logger LOG = LogManager.getLogger(PangeanicTranslationServiceV2.class);

    @Value("${translation.pangeanic.endpoint.translate:}")
    private String translateEndpoint;

    @Value("${translation.pangeanic.endpoint.detect:}")
    private String detectEndpoint;

    private CloseableHttpClient translateClient;

    /**
     * Creates a new client that can send translation requests to Google Cloud Translate. Note that the client needs
     * to be closed when it's not used anymore
     * @throws IOException when there is a problem retrieving the first token
     * @throws JSONException when there is a problem decoding the received token
     */
    @PostConstruct
    private void init() {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(PangeanicUtils.MAX_CONNECTIONS);
        cm.setDefaultMaxPerRoute(PangeanicUtils.MAX_CONNECTIONS_PER_ROUTE);
        translateClient = HttpClients.custom().setConnectionManager(cm).build();
        LOG.info("Pangeanic translation service is initialized. Translate Endpoint is {}. Detect language Endpoint is {}", translateEndpoint, detectEndpoint);
    }

    @Override
    public List<String> translate(List<String> texts, String targetLanguage, Language edmLang) throws TranslationException {
        String hint = edmLang != null ? edmLang.name().toLowerCase(Locale.ROOT) : null ;
        return translateWithLangDetect(texts, targetLanguage, hint);
    }

    @Override
    public List<String> translate(List<String> texts, String targetLanguage, String sourceLanguage) throws TranslationException {
        try {
            HttpPost post = createTranslateRequest(texts, targetLanguage, sourceLanguage, "" );
            return PangeanicUtils.getResults(texts, sendTranslateRequestAndParse(post), false);
        } catch (JSONException|IOException e) {
            throw new TranslationException(e);
        }
    }


    /**
     * Translates the texts with no source language.
     * First a lang detect request is sent to identify the source language
     * Later translations are performed
     *
     * @param texts
     * @param targetLanguage
     * @return
     * @throws TranslationException
     */
    private List<String> translateWithLangDetect(List<String> texts, String targetLanguage, String hint) throws TranslationException {
        try {
            // TODO Get apikey
            HttpPost post = createDetectlanguageRequest(texts, hint, "");
            List<String> lang = sendDetectRequestAndParse(post);
            // create lang-value map for translation
            Map<String, List<String>> detectedLangValueMap = PangeanicUtils.getDetectedLangValueMap(texts, lang);
            LOG.debug("Pangeanic detect lang request with hint {} is executed. Detected languages are {} ", hint, detectedLangValueMap.keySet());
            Map<String, String> translations = new LinkedHashMap<>();
            for (Map.Entry<String, List<String>> entry : detectedLangValueMap.entrySet()) {
                if (!PangeanicUtils.noTranslationRequired(entry.getKey())) {
                    HttpPost translateRequest = createTranslateRequest(entry.getValue(), targetLanguage, entry.getKey(), "");
                    translations.putAll(sendTranslateRequestAndParse(translateRequest));
                }
            }
            return PangeanicUtils.getResults(texts, translations, PangeanicUtils.isSortingRequired(detectedLangValueMap));
        } catch (JSONException | IOException e) {
            throw  new TranslationException(e);
        }
    }

    private HttpPost createTranslateRequest(List<String> texts, String targetLanguage, String sourceLanguage, String apikey) throws JSONException {
        HttpPost post = new HttpPost(translateEndpoint);
        JSONObject body = PangeanicUtils.createTranslateRequestBody(texts, targetLanguage, sourceLanguage, apikey, true);
        post.setEntity(new StringEntity(body.toString(), StandardCharsets.UTF_8));
        post.setHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        post.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        if (LOG.isTraceEnabled()) {
            LOG.trace("Sending POST {}", post.getURI());
            LOG.trace("  body {}", body);
            LOG.trace("  headers:");
            for (Header header :post.getAllHeaders()) {
                LOG.trace("  {}: {}", header.getName(), header.getValue());
            }
        }
        return post;
    }


    private HttpPost createDetectlanguageRequest(List<String> texts, String hint, String apikey) throws JSONException {
        HttpPost post = new HttpPost(detectEndpoint);
        JSONObject body = PangeanicUtils.createDetectRequestBody(texts, hint, apikey);
        post.setEntity(new StringEntity(body.toString(), StandardCharsets.UTF_8));
        post.setHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        post.setHeader(HttpHeaders.CONTENT_TYPE,MediaType.APPLICATION_JSON_VALUE);
        if (LOG.isTraceEnabled()) {
            LOG.trace("Sending POST {}", post.getURI());
            LOG.trace("  body {}", body);
            LOG.trace("  headers:");
            for (Header header :post.getAllHeaders()) {
                LOG.trace("  {}: {}", header.getName(), header.getValue());
            }
        }
        return post;
    }


    // TODO score logic still pending
    private Map<String, String> sendTranslateRequestAndParse(HttpPost post) throws IOException, JSONException, TranslationException {
        try (CloseableHttpResponse response = translateClient.execute(post)) {
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new IOException("Error from Pangeanic Translation API: " +
                        response.getStatusLine().getStatusCode() + " - " + response.getStatusLine().getReasonPhrase());
            } else {
                String json = EntityUtils.toString(response.getEntity());
                JSONObject obj = new JSONObject(json);
                Map<String, String> results = new LinkedHashMap<>();
                JSONArray translations = obj.getJSONArray(PangeanicUtils.TRANSLATIONS);
                for (int i = 0; i < translations.length(); i++) {
                    JSONObject object = (JSONObject) translations.get(i);
                   results.put(object.getString(PangeanicUtils.TRANSLATE_SOURCE), object.getString(PangeanicUtils.TRANSLATE_TARGET));

                }
                // response should not be empty
                if (results.isEmpty()) {
                    throw new TranslationException("Pangeanic Translation API translation failed for source lang " +obj.getString(PangeanicUtils.SOURCE_LANG));
                }
                return  results;
            }
        }
    }

    // TODO score logic still pending
    public List<String> sendDetectRequestAndParse(HttpPost post) throws IOException, JSONException {
        try (CloseableHttpResponse response = translateClient.execute(post)) {
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new IOException("Error from Pangeanic Translation API: " +
                        response.getStatusLine().getStatusCode() + " - " + response.getStatusLine().getReasonPhrase());
            } else {
                String json = EntityUtils.toString(response.getEntity());
                JSONObject obj = new JSONObject(json);
                List<String> result = new ArrayList<>();
                JSONArray detectedLangs = obj.getJSONArray(PangeanicUtils.DETECTED_LANGUAGE);
                for (int i = 0; i < detectedLangs.length(); i++) {
                    JSONObject object = (JSONObject) detectedLangs.get(i);
                    if (object.has(PangeanicUtils.SOURCE_DETECTED)) {
                        result.add(object.getString(PangeanicUtils.SOURCE_DETECTED));
                    }
                    else {// when no deceted lang is returned
                        result.add(PangeanicUtils.LANG_NA);
                    }
                }
                return result;
            }
        }
    }

    @Override
    public void close() {
        if (translateClient != null) {
            try {
                this.translateClient.close();
            } catch (IOException e) {
                LOG.error("Error closing connection to Pangeanic Translation API", e);
            }
        }
    }
}
