package eu.europeana.api2.v2.service.translate;

import eu.europeana.api2.v2.exceptions.TranslationServiceLimitException;
import eu.europeana.api2.v2.model.translate.Language;
import eu.europeana.corelib.definitions.edm.beans.BriefBean;
import eu.europeana.corelib.edm.utils.EdmUtils;
import eu.europeana.corelib.utils.EuropeanaUriUtils;
import eu.europeana.corelib.web.exception.EuropeanaException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 *  Service that provides a translation of search results.
 *  We translate all LangAware fields in the minimal profile (dcTitleLangAware, dcDescriptionLangAware, dcCreatorLangAware)
 *
 * @author Patrick Ehlert
 * Created June 2022
 */
@Service
public class SearchResultTranslateService {

    private static final Logger LOG = LogManager.getLogger(SearchResultTranslateService.class);

    private static final Set<String> FIELDS_TO_TRANSLATE = Set.of(
            "dcTitleLangAware", "dcDescriptionLangAware", "dcCreatorLangAware");

    private final TranslationService translationService;


    /**
     * Create a new service for translating search results
     *
     * @param translationService underlying translation service to use for translations
     */
    public SearchResultTranslateService(TranslationService translationService) {
        this.translationService = translationService;
    }

    /**
     * @return true if there is a translation service available
     */
    public boolean isEnabled() {
        return translationService != null;
    }


    /**
     * Translate specific fields in the search results to the provided langauge
     * @param results the search results to translate
     * @param targetLang the language we should translate to
     * @return modified search results
     * @throws EuropeanaException when there is a problem sending/retrieving translations to/from the translation engine
     */
    public List<BriefBean> translateSearchResults(List<BriefBean> results, String targetLang) throws EuropeanaException {
        throw new TranslationServiceLimitException();
//        long startTime = System.currentTimeMillis();
//
//        // gather all translations
//        TranslationsMap textsToTranslate = new TranslationsMap(getTextsToTranslate(results, targetLang));
//        if (LOG.isDebugEnabled()) {
//            LOG.debug("Translate results - Gathering data took {} ms", (System.currentTimeMillis() - startTime));
//        }
//
//        // actual translation
//        long startTimeTranslate = System.currentTimeMillis();
//        FieldValuesLanguageMap translations = textsToTranslate.translate(translationService, targetLang, null);
//        if (LOG.isDebugEnabled()) {
//            LOG.debug("Translate results - Send/receive translation request took {} ms", (System.currentTimeMillis() - startTimeTranslate));
//        }
//
//        // add translations to result
//        long startTimeOutput = System.currentTimeMillis();
//        for (Map.Entry<String, List<String>> entry : translations.entrySet()) {
//            TranslationUtils.addTranslationsToList(results, entry.getKey(), getSolrKeyName(entry.getKey(), targetLang), entry.getValue());
//        }
//        if (LOG.isDebugEnabled()) {
//            LOG.debug("Translate results - Generating output took {} ms", (System.currentTimeMillis() - startTimeOutput));
//        }
//
//        if (LOG.isDebugEnabled()) {
//            LOG.debug("Translate results - Total time {} ms", (System.currentTimeMillis() - startTime));
//        }
//        return results;
    }

    private String getSolrKeyName(String fieldName, String lang) {
        // Actually it we don't need to generate the precise solr fieldname. Just providing null.<lang> also works fine
        // because the null and dot get filtered out by EdmUtils.cloneMap() method.
        return null + "." + lang.toLowerCase(Locale.ROOT);
    }

    private List<FieldValuesLanguageMap> getTextsToTranslate(List<BriefBean> searchResults, String targetLang) {
        List<FieldValuesLanguageMap> result = new ArrayList<>();

        ReflectionUtils.FieldFilter resultFieldFilter = field -> field.getType().isAssignableFrom(Map.class) &&
                FIELDS_TO_TRANSLATE.contains(field.getName());

        int index = 0;
        for (BriefBean resultItem : searchResults) {
            LOG.debug("Check search result {}...", index);
            int finalIndex = index;
            ReflectionUtils.doWithFields(resultItem.getClass(), field -> {
                ReflectionUtils.makeAccessible(field);
                LOG.debug("  Inspecting field {}..", field.getName());
                FieldValuesLanguageMap toTranslate = getValuesToTranslate(resultItem, finalIndex, field, targetLang);
                if (toTranslate != null && !toTranslate.isEmpty()) {
                    LOG.trace("    Adding value {}", toTranslate);
                    result.add(toTranslate);
                }
            }, resultFieldFilter);
            index++;
        }
        return result;
    }

    private FieldValuesLanguageMap getValuesToTranslate(BriefBean resultItem, int index, Field field, String targetLang) {
        // 1. Check if the field has values in the target language
        FieldValuesLanguageMap result = getFieldValuesForLang(resultItem, index, field, targetLang);
        if (result != null) {
            LOG.debug("  Found value(s) with target language {} for item {}, field {}, no translation needed", targetLang, index, field.getName());
            return null; // no need to translate
        }

        // 2. Check if there's a English value (if target is not English)
        if (!Language.ENGLISH.equals(targetLang)) {
            result = getFieldValuesForLang(resultItem, index, field, Language.ENGLISH);
        }

        // 3. Check if there are non-uri def values
        if (result == null) {
            result = getFieldValuesForLang(resultItem, index, field, Language.DEF);
        }

        // 4. Pick any language
        if (result == null) {
            result = getFieldValuesForLang(resultItem, index, field, null);
        }

        if (result == null) {
            LOG.trace("  Found no values for item {}, field {}", index, field.getName());
        } else {
            LOG.debug("  Found value for item {}, field {} with {} language", index, field.getName(), result.getSourceLanguage());
        }
        return result;
    }

    /**
     * Use reflection to get the values of the provided field in a particular language (if available)
     * @return null if not available
     */
    private FieldValuesLanguageMap getFieldValuesForLang(BriefBean resultItem, int index, Field field, String lang) {
        FieldValuesLanguageMap result = null;
        ReflectionUtils.makeAccessible(field);
        Object value = ReflectionUtils.getField(field, resultItem);

        if (value instanceof Map) {
            // Map keys we get from Solr have compound names with a dot (e.g. proxy_dc_description.def)
            // The EdmUtils.cloneMap functionality makes sure that is transformed into something we can use (e.g. def)
            Map<String, List<String>> fieldData = EdmUtils.cloneMap((Map<String, List<String>>) value);
            // generate proper fieldName for list (we append item index)
            String fieldName = index + TranslationUtils.FIELD_NAME_INDEX_SEPARATOR + field.getName();
            result = getNonUriValuesFromLanguageMap(fieldData, fieldName, lang);
        } else if (value != null) {
            LOG.warn("Unexpected data - field {} did not return a map", field.getName());
        }
        LOG.trace("    Item {}, field {}, lang {}, result = {}", index, field.getName(), lang, result);
        return result;
    }

    /**
     * Given a language map, return the non-uri values for the provided string and put them in a FieldvaluesLanguageMap
     * with the provided fieldName.
     * @return null if not available
     */
    protected FieldValuesLanguageMap getNonUriValuesFromLanguageMap(Map<String, List<String>> map, String fieldName, String lang) {
        FieldValuesLanguageMap result = null;
        if (lang != null) {
            List<String> values = filterOutUris(TranslationUtils.getValuesToTranslateFromMultilingualMap(map, lang));
            if (values != null && !values.isEmpty()) {
                result = new FieldValuesLanguageMap(lang, fieldName, values);
            }
        } else if (!map.keySet().isEmpty()) {
            // return any value if available, but only if it's a supported language
            for (String key : map.keySet()) {
                if (Language.isSupported(key)) {
                    List<String> values = filterOutUris(TranslationUtils.getValuesToTranslateFromMultilingualMap(map, key));
                    result = new FieldValuesLanguageMap(Language.getLanguage(key).name().toLowerCase(Locale.ROOT), fieldName, values);
                    if (result != null) {
                        break;
                    }
                } else {
                    LOG.debug("  Skipping unsupported language {} in field {}", key, fieldName);
                }
            }
        }
        return result;
    }

    private List<String> filterOutUris(List<String> values) {
        if (values == null) {
            return Collections.emptyList();
        }
        return values.stream().filter(v -> !EuropeanaUriUtils.isUri(v)).collect(Collectors.toList());
    }

}

