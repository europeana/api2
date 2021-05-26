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

/**
 * Note that this requires the GOOGLE_APPLICATION_CREDENTIALS environment variable to be available as well as a projectId
 * (defined in the application properties).
 */
@Service
public class GoogleTranslationService implements TranslationService {

    private static final Logger LOG = LogManager.getLogger(GoogleTranslationService.class);

    @Value("${google.translate.projectId}")
    private String projectId;

    private TranslationServiceClient client;

    public GoogleTranslationService() throws IOException {
        // gRPC doesn't like communication via the socks proxy (throws an error) and also doesn't support the
        // socksNonProxyHosts settings, so this is to tell it to by-pass the configured proxy
        TransportChannelProvider transportChannelProvider = CloudTasksStubSettings
                .defaultGrpcTransportProviderBuilder()
                .setChannelConfigurator(managedChannelBuilder -> managedChannelBuilder.proxyDetector(socketAddress -> null))
                .build();
        TranslationServiceSettings tss = TranslationServiceSettings.newBuilder()
                .setTransportChannelProvider(transportChannelProvider).build();
        this.client = TranslationServiceClient.create(tss);
    }

    @PostConstruct
    public void init() {
        LOG.info("GoogleTranslationService initialised, projectId = {}", projectId);
    }

    @PreDestroy
    @Override
    public void close() {
        if (this.client != null) {
            this.client.close();
        }
    }

    @Override
    public String[] getSupportedLanguages() {
        return null;
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
        TranslateTextRequest request = this.createTranslateRequest(text, targetLanguage);
        TranslateTextResponse response = this.client.translateText(request);
        LOG.debug("String {} -> language detected is {}", text, response.getTranslationsList().get(0).getDetectedLanguageCode());
        return response.getTranslationsList().get(0).getTranslatedText();
    }

    @Override
    public String translate(String text, String targetLanguage, String sourceLanguage) {
        TranslateTextRequest request = this.createTranslateRequest(text, targetLanguage, sourceLanguage);
        TranslateTextResponse response = this.client.translateText(request);
        return response.getTranslationsList().get(0).getTranslatedText();
    }

    private TranslateTextRequest createTranslateRequest(String text, String targetLanguage){
        LocationName parent = LocationName.of(this.projectId, "global");
        return TranslateTextRequest.newBuilder()
                        .setParent(parent.toString())
                        .setMimeType("text/plain")
                        .setTargetLanguageCode(targetLanguage)
                        .addContents(text)
                        .build();
    }

    private TranslateTextRequest createTranslateRequest(String text, String targetLanguage, String sourceLanguage){
        LocationName parent = LocationName.of(this.projectId, "global");
        return TranslateTextRequest.newBuilder()
                .setParent(parent.toString())
                .setMimeType("text/plain")
                .setTargetLanguageCode(targetLanguage)
                .setSourceLanguageCode(sourceLanguage)
                .addContents(text)
                .build();
    }

    private DetectLanguageRequest createDetectRequest(String text){
        LocationName parent = LocationName.of(this.projectId, "global");
        return DetectLanguageRequest.newBuilder()
                .setParent(parent.toString())
                .setMimeType("text/plain")
                .setContent(text)
                .build();
    }
}

