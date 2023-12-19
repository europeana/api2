package eu.europeana.api2.v2.service.translate;

import eu.europeana.api.translation.client.exception.ExternalServiceException;
import eu.europeana.api.translation.client.exception.TranslationApiException;
import eu.europeana.api.translation.record.service.MetadataLangDetectionService;
import eu.europeana.api.translation.record.service.MetadataTranslationService;
import eu.europeana.api.translation.definitions.language.Language;
import eu.europeana.api2.v2.exceptions.TranslationException;
import eu.europeana.api2.v2.exceptions.TranslationServiceLimitException;
import eu.europeana.corelib.definitions.edm.beans.FullBean;
import eu.europeana.corelib.definitions.edm.entity.Proxy;
import eu.europeana.corelib.utils.EuropeanaUriUtils;
import eu.europeana.corelib.web.exception.EuropeanaException;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.*;

import static eu.europeana.api.translation.record.service.BaseService.*;

// TODO rename to RecordTranslationService once we have tested everything and deleted the previous code
@Service
public class RecordTranslations {

    private static final Logger LOG = LogManager.getLogger(RecordTranslations.class);

    private MetadataTranslationService metadataTranslationService;

    MetadataLangDetectionService metadataLangDetectionService;

    @Autowired
    public RecordTranslations(MetadataTranslationService metadataTranslationService, MetadataLangDetectionService metadataLangDetectionService) {
        this.metadataLangDetectionService = metadataLangDetectionService;
        this.metadataTranslationService = metadataTranslationService;
    }

    public FullBean translate(FullBean bean, String targetLanguage) throws EuropeanaException {
        try {
            Proxy europeanaProxy = getEuropeanaProxy(bean.getProxies(), bean.getAbout());

            // check if the record was translated during ingestion
            List<Boolean> translatedDuringIngestion = new ArrayList<>();
            ReflectionUtils.doWithFields(europeanaProxy.getClass(), field ->
                translatedDuringIngestion.add(pivotLanguageTaggedValueIsPresent(europeanaProxy, field)), proxyFieldFilter);

            if (translatedDuringIngestion.contains(Boolean.TRUE)) {
                LOG.debug("Record was translated during the ingestion.. ");
                if (StringUtils.equals(targetLanguage, Language.PIVOT)) {
                    return bean; // do nothing
                } else {
                    // call translation workflow
                    return metadataTranslationService.translationWorkflow(bean, targetLanguage);
                }
            } else {
                // if not translated during ingestion - apply detection + translation
                return metadataTranslationService.translationWorkflow(metadataLangDetectionService.detectLanguageForProxy(bean), targetLanguage);
            }
        } catch (TranslationApiException e) {
            if (e instanceof ExternalServiceException && StringUtils.containsIgnoreCase(e.getMessage(), "quota limit reached")) {
                throw new TranslationServiceLimitException(e);
            }
            // keep in mind once we have token being passed that should be valid for
            // translation api as well and we will never receive Unauthorised error here as it is validated in the beginning.
            throw new TranslationException(e);
        }
    }

    /**
     * Returns true if Pivot language tagged ("en") value is present for the field.
     * @param proxy
     * @param field
     * @return
     */
    private boolean pivotLanguageTaggedValueIsPresent(Proxy proxy, Field field) {
        HashMap<String, List<String>> origFieldData = (HashMap<String, List<String>>) getValueOfTheField(proxy, false).apply(field.getName());
        if (origFieldData != null && !origFieldData.isEmpty()) {
            boolean hasLangTaggedLiterals = false;
            for (Map.Entry<String, List<String>> entry : origFieldData.entrySet()) {
                if (Language.isSupported(entry.getKey()) && hasLiterals(entry.getValue())){
                    hasLangTaggedLiterals = true;
                    break;
                }
            }
            if (hasLangTaggedLiterals) {
                return true;
            }
        }
        return false;
    }

    private boolean hasLiterals(List<String> values) {
       return values.stream().anyMatch(value -> !EuropeanaUriUtils.isUri(value));
    }

    /**
     * @return true if there is a translation services available
     */
    public boolean isEnabled() {
        return metadataTranslationService != null && metadataLangDetectionService != null;
    }

    /**
     * Returns the default language list of the edm:languages
     * NOTE : For region locales values, if present in edm:languages
     * the first two ISO letters will be picked up.
     *
     * Only returns the supported official languages,See: {@link Language}
     * Default translation and filtering for non-official language
     * is not supported
     *
     * @param bean the fullbean to inspect
     * @return the default language as specified in Europeana Aggregation edmLanguage field (if the language found there
     * is one of the EU languages we support in this application for translation)
     */
    public List<Language> getDefaultTranslationLanguage(FullBean bean) {
        List<Language> lang = new ArrayList<>();
        Map<String,List<String>> edmLanguage = bean.getEuropeanaAggregation().getEdmLanguage();
        for (Map.Entry<String, List<String>> entry : edmLanguage.entrySet()) {
            for (String languageAbbreviation : entry.getValue()) {
                if (Language.isSupported(languageAbbreviation)) {
                    lang.add(Language.getLanguage(languageAbbreviation));
                } else {
                    LOG.warn("edm:language '{}' is not supported for default translation and filtering ", languageAbbreviation);
                }
            }
        }
        if (!lang.isEmpty()) {
            LOG.debug("Default translation and filtering applied for language : {} ", lang);
        }
        return lang;
    }
}
