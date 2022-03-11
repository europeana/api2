package eu.europeana.api2.v2.service.translate;

import com.google.api.gax.rpc.ResourceExhaustedException;
import eu.europeana.api2.v2.exceptions.TranslationException;
import eu.europeana.api2.v2.exceptions.TranslationServiceLimitException;
import eu.europeana.api2.v2.model.translate.Language;
import eu.europeana.corelib.utils.ComparatorUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * Helper class for sending translation request
 *
 * @author P. Ehlert
 * Created 22 jul 2021
 */
public final class TranslationUtils {

    private static final Logger LOG = LogManager.getLogger(TranslationUtils.class);

    private TranslationUtils() {
        // empty constructor to prevent initialization
    }

    /**
     * Gathers all texts (values) present in a TranslationMap and puts these in a single query to the translation
     * service. When the results are returned, all translation parts are put back in a similar appropriate map using the
     * same field names (keys).
     * @param translationService the translation service to use
     * @param toTranslate map consisting of field names and texts to translate
     * @param targetLanguage the language to translate to
     * @return map with the same field names, but now mapped to translated values
     * @throws TranslationException when there is a problem sending/retrieving translation data
     */
    public static FieldValuesLanguageMap translate(TranslationService translationService, FieldValuesLanguageMap toTranslate,
                                                   String targetLanguage) throws TranslationException, TranslationServiceLimitException {
        // We don't use delimiters because we want to keep the number of characters we sent low.
        // Instead we use line counts to determine start and end of a field.
        Map<String, Integer> linesPerField = new LinkedHashMap<>();
        List<String> linesToTranslate = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : toTranslate.entrySet()) {
            List<String> lines = entry.getValue();
            linesPerField.put(entry.getKey(), lines.size());
            linesToTranslate.addAll(lines);
            if (LOG.isTraceEnabled()) {
                LOG.trace("Key {}, values to translate {}", entry.getKey(), lines);
            }
        }

        // Actual translation
        List<String> translations;
        try {
            if (Language.DEF.equals(toTranslate.getSourceLanguage())) {
                LOG.debug("Sending translate query with language detect...");
                translations = translationService.translate(linesToTranslate, targetLanguage);
            } else {
                LOG.debug("Sending translate query with source language {} and target language {}...", toTranslate.getSourceLanguage(), targetLanguage);
                translations = translationService.translate(linesToTranslate, targetLanguage, toTranslate.getSourceLanguage());
            }
        } catch (ResourceExhaustedException e) {
            // catch Google StatusRuntimeException: RESOURCE_EXHAUSTED exception
            // this will be thrown if the limit for the day is exceeded
            throw new TranslationServiceLimitException(e);
        } catch (RuntimeException e) {
            // Catch Google Translate issues and wrap in our own exception
            throw new TranslationException(e);
        }
        // Sanity check
        if (translations.size() != linesToTranslate.size()) {
            throw new IllegalStateException("Expected " + linesToTranslate.size() + " lines of translated text, but received " + translations.size());
        }

        // Put received data under appropriate key in new map
        FieldValuesLanguageMap result = new FieldValuesLanguageMap(targetLanguage);
        int counter = 0;
        for (String key : toTranslate.keySet()) {
            int nrLines = linesPerField.get(key);
            if (nrLines == 0) {
                result.put(key, null);
            } else {
                result.put(key, translations.subList(counter, counter + nrLines));
            }
            counter = counter + nrLines;
        }
        return result;
    }

    /**
     * Removes the values from the translated Map,
     * If values in original map are equal to the values in translated map
     * NOTE : Comparison is made for each value ignoring spaces and case.
     *        Values which actually are changed or translated are finally added in the map.
     *
     * @param translatedMap map containing the fields and translated values
     * @param mapToTranslate map containing the fields and original values to be translated
     * @Returns map containing fields and actual-translated values
     */
    public static FieldValuesLanguageMap removeIfOriginalIsSameAsTranslated(FieldValuesLanguageMap translatedMap, FieldValuesLanguageMap mapToTranslate) {
        Map<String , List<String>> actualTranslationMap = new HashMap<>();
        for (Map.Entry<String, List<String>> translated : translatedMap.entrySet()) {
            List<String> actualTranslatedValues = new ArrayList<>();
            // we have already performed the sanity check in translate() method. So the list size will be equal here
            for(int i = 0; i < translated.getValue().size(); i++) {
               if (!ComparatorUtils.sameValueWithoutSpace(mapToTranslate.get(translated.getKey()).get(i), translated.getValue().get(i))) {
                   actualTranslatedValues.add(translated.getValue().get(i));
               }
            }
            if (!actualTranslatedValues.isEmpty()) {
                actualTranslationMap.put(translated.getKey(), actualTranslatedValues);
            }
        }
        if (!actualTranslationMap.isEmpty()) {
            return new FieldValuesLanguageMap(translatedMap.getSourceLanguage(), actualTranslationMap);
        }
        return null;
    }

    /**
     * lang in the map might be present with or without region codes ex : en-GB or only 'en'
     * hence first try the exact match of key. if empty, try the partial match to fetch the values
     *
     * @param map
     * @param lang
     * @return
     */
    public static List<String> getValuesToTranslateFromMultilingualMap(Map<String, List<String>> map, String lang) {
        List<String> valuesToTranslate = map.get(lang);
        if (valuesToTranslate == null || valuesToTranslate.isEmpty()) {
            valuesToTranslate = map.entrySet().stream()
                    .filter(entry -> entry.getKey().startsWith(lang))
                    .map(Map.Entry :: getValue)
                    .findFirst().orElse(null);
        }
        return valuesToTranslate;
    }

    /**
     * Method recognizes the ISO 2 letter when it has only 2 digits
     * or when followed by a regional locale.
     * @param lang
     * @return
     */
    public static String getISOLanguage(String lang) {
        if(lang.length() > 2) {
            LOG.debug("Found regional code language = {}", lang);
            return StringUtils.substringBefore(lang, "-");
        }
        return lang;
    }
}
