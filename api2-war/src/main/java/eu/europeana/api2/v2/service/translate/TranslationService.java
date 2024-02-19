package eu.europeana.api2.v2.service.translate;

import eu.europeana.api.translation.definitions.language.Language;
import eu.europeana.api.translation.service.exception.LanguageDetectionException;
import eu.europeana.api2.v2.exceptions.TranslationException;
import eu.europeana.api2.v2.exceptions.TranslationServiceLimitException;
import eu.europeana.corelib.definitions.edm.beans.BriefBean;
import eu.europeana.corelib.definitions.edm.beans.FullBean;
import eu.europeana.corelib.definitions.edm.entity.Proxy;
import eu.europeana.corelib.utils.EuropeanaUriUtils;
import eu.europeana.corelib.web.exception.EuropeanaException;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.*;

import static eu.europeana.api2.v2.service.translate.BaseService.*;

@Service
public class TranslationService {

    private static final Logger LOG = LogManager.getLogger(TranslationService.class);

    MetadataTranslationService metadataTranslationService;
    MetadataLangDetectionService metadataLangDetectionService;

    @Autowired
    public TranslationService(MetadataTranslationService metadataTranslationService, MetadataLangDetectionService metadataLangDetectionService) {
        this.metadataLangDetectionService = metadataLangDetectionService;
        this.metadataTranslationService = metadataTranslationService;
    }

    /**
     * Search Translation
     *
     * 1. For each record in the search results:
     *       a) Apply a language detection workflow to all whilested properties (dc:title, dc:description, dc:creator)
     *       that do not have any language tagged values (only literals). Use edm:language (“language” field) as hint.
     *       b) Add all new language qualified values into the record (ie. Bean)
     *
     * 2. Determine the most representative language in order to be used as source language for translation
     * 3. Apply metadata translation to all records segmented by source language:
     *      a) If the source language matches the target language do nothing
     *      b) Otherwise, call metadata translation workflow
     *
     * @param beans
     * @param targetLanguage
     * @return
     * @throws EuropeanaException
     */
    public List<BriefBean> translate(List<BriefBean> beans, String targetLanguage, String authToken) throws EuropeanaException {
        try {
            return metadataTranslationService.searchResultsTranslations(metadataLangDetectionService.detectLanguageForSearchResults(beans, authToken), targetLanguage, authToken);
        } catch (LanguageDetectionException | eu.europeana.api.translation.service.exception.TranslationException e) {
            // Client throws Generic exceptions but with status 504 and 500
            // Translation api client throws 504 status for google exhuasted exception or if the external service had some issue.
            // Hence we need to check for the message as well as we have a redirect functionality based on it.
            if (getRemoteStatusCode(e) == HttpStatus.SC_GATEWAY_TIMEOUT && StringUtils.containsIgnoreCase(e.getMessage(), "quota limit reached")) {
                throw new TranslationServiceLimitException(e);
            }
            // keep in mind once we have token being passed that should be valid for
            // translation api as well and we will never receive Unauthorised error here as it is validated in the beginning.
            throw new TranslationException(e);
        }
    }

    /**
     * Record translation
     *
     * 1. Check if there are language tagged values1 present for any of the whitelisted properties in the Europeana Proxy
     *   (this informs us if the record was subject of translation upon ingestion or not)
     *
     * 2. If true and the requested language matches pivot language (ie.  English), do nothing
     * 3. If true and the requested language does not match the pivot language, apply language translation workflow
     *    (from the most representative language) to the requested language. The source language is chosen by MetadataChosenLanguage workflow
     *
     * 4. If false, apply language detection workflow followed by the language translation workflow to the target language
     *
     * @param bean
     * @param targetLanguage
     * @return
     * @throws EuropeanaException
     */
    public FullBean translate(FullBean bean, String targetLanguage, String authToken) throws EuropeanaException {
        try {
            Proxy europeanaProxy = getEuropeanaProxy(bean.getProxies(), bean.getAbout());

            // check if the record was translated during ingestion
            List<Boolean> translatedDuringIngestion = new ArrayList<>();
            ReflectionUtils.doWithFields(europeanaProxy.getClass(), field ->
                translatedDuringIngestion.add(languageTaggedValueIsPresent(europeanaProxy, field)), proxyFieldFilter);

            if (translatedDuringIngestion.contains(Boolean.TRUE)) {
                LOG.info("Record was translated during the ingestion.. ");
                if (StringUtils.equals(targetLanguage, Language.PIVOT)) {
                    return bean; // do nothing
                } else {
                    // call translation workflow
                    return metadataTranslationService.proxyTranslation(bean, targetLanguage, authToken);
                }
            } else {
                // if not translated during ingestion - apply detection + translation
                return metadataTranslationService.proxyTranslation(metadataLangDetectionService.detectLanguageForProxy(bean, authToken), targetLanguage, authToken);
            }
        } catch (LanguageDetectionException | eu.europeana.api.translation.service.exception.TranslationException e) {
            // Client throws Generic exceptions but with status 504 and 500
            // Translation api client throws 504 status for google exhuasted exception or if the external service had some issue.
            // Hence we need to check for the message as well as we have a redirect functionality based on it.
            if (getRemoteStatusCode(e) == HttpStatus.SC_GATEWAY_TIMEOUT && StringUtils.containsIgnoreCase(e.getMessage(), "quota limit reached")) {
                throw new TranslationServiceLimitException(e);
            }
            // keep in mind once we have token being passed that should be valid for
            // translation api as well and we will never receive Unauthorised error here as it is validated in the beginning.
            throw new TranslationException(e);
        }
    }

    /**
     * Returns true if Language tagged literals are present
     * @param proxy
     * @param field
     * @return
     */
    private boolean languageTaggedValueIsPresent(Proxy proxy, Field field) {
        HashMap<String, List<String>> origFieldData = (HashMap<String, List<String>>) getValueOfTheField(proxy, false).apply(field.getName());
        if (origFieldData != null && !origFieldData.isEmpty()) {
            boolean hasLangTaggedLiterals = false;
            for (Map.Entry<String, List<String>> entry : origFieldData.entrySet()) {
                if (Language.isSupported(entry.getKey()) && hasLiterals(entry.getValue())){
                    hasLangTaggedLiterals = true;
                    break;
                }
            }
            // don't check further if found a lang tagged literal
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

    protected int getRemoteStatusCode(Exception e) {
        if (e.getClass().isAssignableFrom(LanguageDetectionException.class)) {
            return ((LanguageDetectionException) e).getRemoteStatusCode();
        }

        if (e.getClass().isAssignableFrom(eu.europeana.api.translation.service.exception.TranslationException.class)) {
            return ((eu.europeana.api.translation.service.exception.TranslationException) e).getRemoteStatusCode();
        }
        return 0;
    }
}
