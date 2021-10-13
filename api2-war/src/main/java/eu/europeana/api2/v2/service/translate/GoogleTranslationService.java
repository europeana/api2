package eu.europeana.api2.v2.service.translate;

import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.cloud.tasks.v2.stub.CloudTasksStubSettings;
import com.google.cloud.translate.v3.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Note that this requires the GOOGLE_APPLICATION_CREDENTIALS environment variable to be available as well as a projectId
 * (defined in the application properties).
 */
@Service
public class GoogleTranslationService implements TranslationService {

    private static final Logger LOG = LogManager.getLogger(GoogleTranslationService.class);
    private static final String MIME_TYPE_TEXT = "text/plain";

    @Value("${google.translate.projectId}")
    private String projectId;
    @Value("${translation.enabled}")
    private Boolean translationEnabled;

    private TranslationServiceClient client;
    private LocationName locationName;

    /**
     * Creates a new client that can send translation requests to Google Cloud Translate. Note that the client needs
     * to be closed when it's not used anymore
     * @throws IOException when there is a problem creating the client
     */
    @PostConstruct
    private void init() throws IOException {
        if (translationEnabled) {
            // gRPC doesn't like communication via the socks proxy (throws an error) and also doesn't support the
            // socksNonProxyHosts settings, so this is to tell it to by-pass the configured proxy
            TransportChannelProvider transportChannelProvider = CloudTasksStubSettings
                    .defaultGrpcTransportProviderBuilder()
                    .setChannelConfigurator(managedChannelBuilder -> managedChannelBuilder.proxyDetector(socketAddress -> null))
                    .build();
            TranslationServiceSettings tss = TranslationServiceSettings.newBuilder()
                    .setTransportChannelProvider(transportChannelProvider).build();
            this.client = TranslationServiceClient.create(tss);
            this.locationName = LocationName.of(this.projectId, "global");
            LOG.info("GoogleTranslationService initialised, projectId = {}", projectId);
        } else{
            LOG.info("GoogleTranslationService is disabled");
        }
    }

    @PreDestroy
    @Override
    public void close() {
        if (this.client != null) {
            LOG.info("Shutting down GoogleTranslationService client...");
            this.client.close();
        }
    }

    @Override
    public String[] getSupportedLanguages() {
        return new String[0];
    }

    @Override
    public String detectLanguage(String text) {
        DetectLanguageRequest request = this.createDetectRequest(text);
        DetectLanguageResponse response = this.client.detectLanguage(request);
        if (response.getLanguagesCount() > 0) { //Google returns a list of languages ordered by confidence
            LOG.debug("String {} is in language {}, confidence {}", text, response.getLanguages(0).getLanguageCode(),
                    response.getLanguages(0).getConfidence());
            return response.getLanguages(0).getLanguageCode();
        }
        return null;
    }

    @Override
    public String translate(String text, String targetLanguage) {
        // create request for synchronous translation
        TranslateTextRequest request = TranslateTextRequest.newBuilder()
                .setParent(locationName.toString())
                .setMimeType(MIME_TYPE_TEXT)
                .setTargetLanguageCode(targetLanguage)
                .addContents(text)
                .build();
        TranslateTextResponse response = this.client.translateText(request);
        LOG.debug("String {} -> language detected is {}", text, response.getTranslationsList().get(0).getDetectedLanguageCode());
        return response.getTranslationsList().get(0).getTranslatedText();
    }

    @Override
    public List<String> translate(List<String> texts, String targetLanguage) {
        TranslateTextRequest request = TranslateTextRequest.newBuilder()
                .setParent(locationName.toString())
                .setMimeType(MIME_TYPE_TEXT)
                .setTargetLanguageCode(targetLanguage)
                .addAllContents(texts)
                .build();
        TranslateTextResponse response = this.client.translateText(request);
        List<String> result = new ArrayList<>();
        for (Translation t : response.getTranslationsList()) {
            result.add(t.getTranslatedText());
        }
        return result;
    }

    @Override
    public String translate(String text, String targetLanguage, String sourceLanguage) {
        TranslateTextRequest request = TranslateTextRequest.newBuilder()
                .setParent(locationName.toString())
                .setMimeType(MIME_TYPE_TEXT)
                .setTargetLanguageCode(targetLanguage)
                .setSourceLanguageCode(sourceLanguage)
                .addContents(text)
                .build();
        TranslateTextResponse response = this.client.translateText(request);
        return response.getTranslationsList().get(0).getTranslatedText();
    }

    @Override
    public List<String> translate(List<String> text, String targetLanguage, String sourceLanguage) {
        TranslateTextRequest request = TranslateTextRequest.newBuilder()
                .setParent(locationName.toString())
                .setMimeType(MIME_TYPE_TEXT)
                .setTargetLanguageCode(targetLanguage)
                .setSourceLanguageCode(sourceLanguage)
                .addAllContents(text)
                .build();
        TranslateTextResponse response = this.client.translateText(request);
        List<String> result = new ArrayList<>();
        for (Translation t : response.getTranslationsList()) {
            result.add(t.getTranslatedText());
        }
        return result;
    }

    private DetectLanguageRequest createDetectRequest(String text){
        return DetectLanguageRequest.newBuilder()
                .setParent(locationName.toString())
                .setMimeType(MIME_TYPE_TEXT)
                .setContent(text)
                .build();
    }
}

