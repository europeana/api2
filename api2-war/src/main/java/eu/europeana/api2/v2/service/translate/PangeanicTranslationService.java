package eu.europeana.api2.v2.service.translate;

import com.auth0.jwt.JWT;
import eu.europeana.api2.v2.exceptions.TranslationException;
import eu.europeana.api2.v2.model.translate.Language;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Service to send data to translate to (old) Pangeanic Translate API
 * Pangeanic Translate API requires a token, so this is issued first and on each request we check if the token is
 * still valid for at least 30 seconds
 * @author Patrick Ehlert
 */
@Service
@PropertySource("classpath:europeana.properties")
@PropertySource(value = "classpath:europeana.user.properties", ignoreResourceNotFound = true)
public class PangeanicTranslationService implements TranslationService {

    private static final Logger LOG = LogManager.getLogger(PangeanicTranslationService.class);

    @Value("${translation.pangeanic.endpoint.translate:}")
    private String translateEndpoint;
    @Value("${translation.pangeanic.endpoint.auth:}")
    private String tokenEndpoint;
    @Value("${translation.pangeanic.username:}")
    private String userName;
    @Value("${translation.pangeanic.passwd:}")
    private String password;

    private String token;
    private long tokenExpiration;

    private CloseableHttpClient translateClient;

    /**
     * Creates a new client that can send translation requests to Google Cloud Translate. Note that the client needs
     * to be closed when it's not used anymore
     * @throws IOException when there is a problem retrieving the first token
     * @throws JSONException when there is a problem decoding the received token
     */
    @PostConstruct
    private void init() throws IOException, JSONException {
        this.token = getNewToken(tokenEndpoint, userName, password);
        this.tokenExpiration = JWT.decode(token).getExpiresAt().getTime();

        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(MetadataTranslationUtils.MAX_CONNECTIONS);
        cm.setDefaultMaxPerRoute(MetadataTranslationUtils.MAX_CONNECTIONS_PER_ROUTE);
        translateClient = HttpClients.custom().setConnectionManager(cm).build();
        LOG.info("Pangeanic translation service is initialized. Endpoint is {}", translateEndpoint);
    }

    private String getNewToken(String tokenEndpoint, String username, String password) throws IOException, JSONException {
        LOG.info("Requesting token from Pangeanic API...");
        HttpPost post = new HttpPost(tokenEndpoint);
        JSONObject body = new JSONObject();
        body.put("user", username);
        body.put("password", password);
        post.setEntity(new StringEntity(body.toString()));
        post.setHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        post.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        try (CloseableHttpClient client = HttpClients.createDefault();
            CloseableHttpResponse response = client.execute(post)) {
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new IOException("Unable to retrieve token from Pangeanic Translation API: " +
                        response.getStatusLine().getStatusCode() + " - " + response.getStatusLine().getReasonPhrase());
            } else {
                String json = EntityUtils.toString(response.getEntity());
                JSONObject obj = new JSONObject(json);
                LOG.info("Token received");
                return obj.getString("access_token");
            }
        }
    }

    /**
     * Return a token that is valid for at least 30 seconds
     */
    private synchronized String getValidToken() throws JSONException, IOException {
        // return true if token expires in less than 30 seconds
        if (tokenExpiration - new Date().getTime() < MetadataTranslationUtils.TOKEN_MIN_AGE) {
            this.token = getNewToken(tokenEndpoint, userName, password);
            this.tokenExpiration = JWT.decode(token).getExpiresAt().getTime();
        }
        return this.token;
    }

    @Override
    public List<String> translate(List<String> texts, String targetLanguage, Language sourceLangHint) throws TranslationException {
        String hint = (sourceLangHint != null ? sourceLangHint.name().toLowerCase(Locale.ROOT) : null);
        return translate(texts, targetLanguage, hint);
    }

    @Override
    public List<String> translate(List<String> texts, String targetLanguage, String sourceLanguage) throws TranslationException {
        try {
            HttpPost post = createTranslateRequest(texts, targetLanguage, sourceLanguage);
            return sendTranslateRequestAndParse(post);
        } catch (JSONException|IOException e) {
            throw new TranslationException(e);
        }
    }

    private HttpPost createTranslateRequest(List<String> texts, String targetLanguage, String sourceLanguage) throws JSONException, IOException {
        HttpPost post = new HttpPost(translateEndpoint);
        JSONObject body = MetadataTranslationUtils.createTranslateRequestBody(texts, targetLanguage, sourceLanguage, null, false);
        post.setEntity(new StringEntity(body.toString(), StandardCharsets.UTF_8));
        post.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + getValidToken());
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

    private List<String> sendTranslateRequestAndParse(HttpPost post) throws IOException, JSONException {
        try (CloseableHttpResponse response = translateClient.execute(post)) {
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new IOException("Error from Pangeanic Translation API: " +
                        response.getStatusLine().getStatusCode() + " - " + response.getStatusLine().getReasonPhrase());
            } else {
                String json = EntityUtils.toString(response.getEntity());

                JSONObject obj = new JSONObject(json);
                // TODO check confidence (once this is working properly)
                List<String> result = new ArrayList<>();
                JSONArray translations = obj.getJSONArray("translation");
                for (int i = 0; i < translations.length(); i++) {
                    result.add(translations.getString(i));
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

