package eu.europeana.api2.v2.service.translate;

import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.cloud.tasks.v2.stub.CloudTasksStubSettings;
import com.google.cloud.translate.v3.*;
import eu.europeana.api2.v2.exceptions.TranslationException;
import eu.europeana.api2.v2.model.translate.Language;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Note that this requires the GOOGLE_APPLICATION_CREDENTIALS environment variable to be available as well as a projectId
 * (defined in the application properties).
 */
//@Service
@PropertySource("classpath:europeana.properties")
@PropertySource(value = "classpath:europeana.user.properties", ignoreResourceNotFound = true)
public class GoogleTranslationService implements TranslationService {

    private static final Logger LOG = LogManager.getLogger(GoogleTranslationService.class);
    private static final String MIME_TYPE_TEXT = "text/plain";

    @Value("#{europeanaProperties['translation.google.projectId']}")
    private String projectId;

    private TranslationServiceClient client;
    private LocationName locationName;

    /**
     * Creates a new client that can send translation requests to Google Cloud Translate. Note that the client needs
     * to be closed when it's not used anymore
     * @throws IOException when there is a problem creating the client
     */
    @PostConstruct
    private void init() throws IOException {
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
    public List<String> translate(List<String> texts, String targetLanguage, Language edmLang) {
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

}

