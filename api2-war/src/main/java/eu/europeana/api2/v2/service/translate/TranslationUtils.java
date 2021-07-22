package eu.europeana.api2.v2.service.translate;

import eu.europeana.api2.v2.model.translate.Language;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * Helper class for sending translations request
 */
public final class TranslationUtils {

    private static final Logger LOG = LogManager.getLogger(TranslationUtils.class);

    private TranslationUtils() {
        // empty constructor to prevent initialization
    }

    /**
     * @return a new map that can be used to gather fields and texts that need to be translated
     */
    public static Map<String, List<String>> initNewTranslationMap() {
        // This needs to be a linkedHashMap to guarantee fixed ordering!
        return new LinkedHashMap<>();
    }

    /**
     * Combines all provided lists of strings into 1 single list so we can send it to the provided translation service
     * in 1 query. When the results are returned, all translation parts are put back in the appropriate map under
     * the corresponding key.
     * @param translationService the translation service to use
     * @param keysValues map of field name or id and values to translate
     * @param targetLanguage the language to translate to
     * @return map with the same field names and ids, but now containing translated values
     */
    public static Map<String, List<String>> translate(TranslationService translationService,
                                                      Map<String, List<String>> keysValues, String targetLanguage) {
        return translate(translationService, keysValues, targetLanguage, null);
    }

    /**
     * Combines all provided lists of strings into 1 single list so we can send it to the provided translation service
     * in 1 query. When the results are returned, all translation parts are put back in the appropriate map under
     * the corresponding key.
     * @param translationService the translation service to use
     * @param keysValues map of field name or id and values to translate
     * @param targetLanguage the language to translate to
     * @param sourceLanguage the language of the sources (values in the map)
     * @return map with the same field names and ids, but now containing translated values
     */
    public static Map<String, List<String>> translate(TranslationService translationService,
                Map<String, List<String>> keysValues, String targetLanguage, String sourceLanguage) {
        // We don't use delimiters because we want to keep the number of character we sent low.
        // Instead we use line counts to determine start and end of a field.
        Map<String, Integer> linesPerField = new LinkedHashMap<>();
        List<String> toTranslate = new ArrayList<>();
        int nrLinesToTranslate = 0;
        long nrCharacters = 0;
        for (Map.Entry<String, List<String>> entry : keysValues.entrySet()) {
            if (entry.getValue() == null || entry.getValue().isEmpty()) {
                linesPerField.put(entry.getKey(), 0);
            } else {
                List<String> lines = entry.getValue();
                for (String line : lines) {
                    // we trim lines to reduce the number of characters we sent
                    line = line.trim();
                    nrCharacters = nrCharacters + line.length();
                }
                linesPerField.put(entry.getKey(), lines.size());
                toTranslate.addAll(lines);
                nrLinesToTranslate = nrLinesToTranslate + lines.size();
            }
        }

        // Actual translation
        // We temporarily log the number of characters for ticket EA-
        LOG.info("{}", nrCharacters);
        List<String> translations;
        if (sourceLanguage == null || Language.DEF.equals(sourceLanguage)) {
            LOG.debug("Sending translate query with language detect...");
            translations = translationService.translate(toTranslate, targetLanguage);
        } else {
            LOG.debug("Sending translate query with source language {} and target language {}...", sourceLanguage, targetLanguage);
            translations = translationService.translate(toTranslate, targetLanguage, sourceLanguage);
        }
        // Sanity check
        if (translations.size() != nrLinesToTranslate) {
            throw new IllegalStateException("Expected " + nrLinesToTranslate + " of translated text, but received " + translations.size());
        }

        // Reconstruct data
        Map<String, List<String>> result = new HashMap<>();
        int counter = 0;
        for (String key : keysValues.keySet()) {
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
