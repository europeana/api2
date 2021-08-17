package eu.europeana.api2.v2.service.translate;

import eu.europeana.api2.v2.model.translate.Language;
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
     */
    public static FieldValuesLanguageMap translate(TranslationService translationService, FieldValuesLanguageMap toTranslate, String targetLanguage) {
        // We don't use delimiters because we want to keep the number of characters we sent low.
        // Instead we use line counts to determine start and end of a field.
        Map<String, Integer> linesPerField = new LinkedHashMap<>();
        List<String> linesToTranslate = new ArrayList<>();
        long nrCharacters = 0;
        for (Map.Entry<String, List<String>> entry : toTranslate.entrySet()) {
            List<String> lines = entry.getValue();
            for (String line : lines) {
                // we trim lines to reduce the number of characters we sent
                nrCharacters = nrCharacters + line.length();
            }
            linesPerField.put(entry.getKey(), lines.size());
            linesToTranslate.addAll(lines);
            if (LOG.isTraceEnabled()) {
                LOG.trace("Key {}, values to translate {}", entry.getKey(), lines);
            }
        }


        // Actual translation
        // We temporarily log the number of characters for ticket EA-2633 / 2661
        LOG.info("{}", nrCharacters);
        List<String> translations;
        if (Language.DEF.equals(toTranslate.getSourceLanguage())) {
            LOG.debug("Sending translate query with language detect...");
            translations = translationService.translate(linesToTranslate, targetLanguage);
        } else {
            LOG.debug("Sending translate query with source language {} and target language {}...", toTranslate.getSourceLanguage(), targetLanguage);
            translations = translationService.translate(linesToTranslate, targetLanguage, toTranslate.getSourceLanguage());
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

}
