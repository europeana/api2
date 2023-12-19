package eu.europeana.api2.v2.service.translate;

import eu.europeana.api.translation.definitions.language.Language;
import eu.europeana.api2.v2.exceptions.TranslationException;
import eu.europeana.api2.v2.utils.MetadataTranslationUtils;
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
@Deprecated
public class PangeanicV2TranslationService implements TranslationService  {

    private static final Logger LOG = LogManager.getLogger(PangeanicV2TranslationService.class);

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
        cm.setMaxTotal(MetadataTranslationUtils.MAX_CONNECTIONS);
        cm.setDefaultMaxPerRoute(MetadataTranslationUtils.MAX_CONNECTIONS_PER_ROUTE);
        translateClient = HttpClients.custom().setConnectionManager(cm).build();
        LOG.info("Pangeanic translation service is initialized. Translate Endpoint is {}. Detect language Endpoint is {}", translateEndpoint, detectEndpoint);
    }

    @Override
    public List<String> translate(List<String> texts, String targetLanguage, Language sourceLangHint) throws TranslationException {
        String hint = (sourceLangHint != null ? sourceLangHint.name().toLowerCase(Locale.ROOT) : null);
        return translateWithLangDetect(texts, targetLanguage, hint);
    }

    @Override
    public List<String> translate(List<String> texts, String targetLanguage, String sourceLanguage) throws TranslationException {
        try {
            HttpPost post = createTranslateRequest(texts, targetLanguage, sourceLanguage, "" );
            return MetadataTranslationUtils.getResults(texts, sendTranslateRequestAndParse(post), false);
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
            List<String> detectedLanguages = sendDetectRequestAndParse(post);
            // create lang-value map for translation
            Map<String, List<String>> detectedLangValueMap = MetadataTranslationUtils.getDetectedLangValueMap(texts, detectedLanguages);
            LOG.debug("Pangeanic detect lang request with hint {} is executed. Detected languages are {} ", hint, detectedLangValueMap.keySet());

            Map<String, String> translations = new LinkedHashMap<>();
            for (Map.Entry<String, List<String>> entry : detectedLangValueMap.entrySet()) {
                if (MetadataTranslationUtils.noTranslationRequired(entry.getKey())) {
                    if (entry.getKey().equals(Language.DEF)) {
                        LOG.debug("NOT translating data for empty lang detected values {} ", entry.getValue());
                    } else {
                        LOG.debug("NOT translating data for lang '{}' and values {} ", entry.getKey(), entry.getValue());
                    }
                } else {
                    HttpPost translateRequest = createTranslateRequest(entry.getValue(), targetLanguage, entry.getKey(), "");
                    translations.putAll(sendTranslateRequestAndParse(translateRequest));
                }
            }
            return MetadataTranslationUtils.getResults(texts, translations, MetadataTranslationUtils.nonTranslatedDataExists(detectedLanguages));
        } catch (JSONException | IOException e) {
            throw  new TranslationException(e);
        }
    }

    private HttpPost createTranslateRequest(List<String> texts, String targetLanguage, String sourceLanguage, String apikey) throws JSONException {
        HttpPost post = new HttpPost(translateEndpoint);
        JSONObject body = MetadataTranslationUtils.createTranslateRequestBody(texts, targetLanguage, sourceLanguage, apikey, true);
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
        JSONObject body = MetadataTranslationUtils.createDetectRequestBody(texts, hint, apikey);
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
                // there are cases where we get an empty response
                if (!obj.has(MetadataTranslationUtils.TRANSLATIONS)) {
                    throw new TranslationException("Pangeanic Translation API returned empty response");
                }
                JSONArray translations = obj.getJSONArray(MetadataTranslationUtils.TRANSLATIONS);
                for (int i = 0; i < translations.length(); i++) {
                    // TODO Pangeanic changed the object model. Still need to verify this change
                    JSONObject object = (JSONObject) ((JSONArray)translations.get(i)).get(0);
                   results.put(object.getString(MetadataTranslationUtils.TRANSLATE_SOURCE), object.getString(MetadataTranslationUtils.TRANSLATE_TARGET));

                }
                // response should not be empty
                if (results.isEmpty()) {
                    throw new TranslationException("Translation failed for source language - " +obj.get(MetadataTranslationUtils.SOURCE_LANG));
                }
                return  results;
            }
        }
    }

    // TODO score logic still pending
    public List<String> sendDetectRequestAndParse(HttpPost post) throws IOException, JSONException, TranslationException {
        try (CloseableHttpResponse response = translateClient.execute(post)) {
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new IOException("Error from Pangeanic Translation API: " +
                        response.getStatusLine().getStatusCode() + " - " + response.getStatusLine().getReasonPhrase());
            } else {
                String json = EntityUtils.toString(response.getEntity());
                // sometimes language detect sends 200 ok status with empty response data
                if (json.isEmpty()) {
                    throw new TranslationException("Language detect returned an empty response");
                }
                JSONObject obj = new JSONObject(json);
                List<String> result = new ArrayList<>();
                JSONArray detectedLangs = obj.getJSONArray(MetadataTranslationUtils.DETECTED_LANGUAGE);
                for (int i = 0; i < detectedLangs.length(); i++) {
                    JSONObject object = (JSONObject) detectedLangs.get(i);
                    if (object.has(MetadataTranslationUtils.SOURCE_DETECTED)) {
                        result.add(object.getString(MetadataTranslationUtils.SOURCE_DETECTED));
                    } else {
                        // when no detected lang is returned. Ideally, this should not happen
                         // But this is for just-in cases when src_detected is
                        // not returned. These values as well will not be translated and returned as it is
                        // TODO adding "def" for now utill Pangeanic finds a way for the
                        //  tool to return that it simply doesn't know the language
                        result.add(Language.DEF);
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
