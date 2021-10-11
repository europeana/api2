package eu.europeana.api2.v2.service.translate;

import eu.europeana.api2.v2.exceptions.TranslationException;
import eu.europeana.api2.v2.exceptions.TranslationServiceLimitException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Keeps track of all the data from a single record that we want to translate. The keys are the different
 * source languages, the values are maps consisting of fieldnames and texts to translate.
 *
 * @author P. Ehlert
 * Created jul - aug 2021
 */
public class TranslationsMap extends LinkedHashMap<String, FieldValuesLanguageMap> {

    private static final Logger LOG = LogManager.getLogger(TranslationsMap.class);

    private static final long serialVersionUID = 3953283538425288592L;


    public TranslationsMap(List<FieldValuesLanguageMap> textsToTranslate) {
        this.addAll(textsToTranslate);
    }

    /**
     * Adds a map of a particular translation. Note that maps in the same language are merged
     * @param mapToAdd the translationmap to store, if null nothing is added
     */
    private void add(FieldValuesLanguageMap mapToAdd) {
        if (mapToAdd != null) {
            if (this.containsKey(mapToAdd.getSourceLanguage())) {
                // map with same language exists, so we need to check what we already have (see if we need to merge values)
                FieldValuesLanguageMap existingMap = get(mapToAdd.getSourceLanguage());
                for (Map.Entry<String, List<String>> entry : mapToAdd.entrySet()) {
                    if (existingMap.containsKey(entry.getKey())) {
                        // existing field, so merge values
                        existingMap.merge(new FieldValuesLanguageMap(mapToAdd.getSourceLanguage(), entry.getKey(), entry.getValue()));
                    } else {
                        // new field, so we can simply add new key
                        existingMap.put(entry.getKey(), entry.getValue());
                    }
                }
            } else {
                // create new language key
                this.put(mapToAdd.getSourceLanguage(), mapToAdd);
            }
        }
    }

    /**
     * Add multiple translation maps
     * @param maps the maps to add
     */
    private void addAll(List<FieldValuesLanguageMap> maps) {
        for (FieldValuesLanguageMap map : maps) {
            this.add(map);
        }
    }

    /**
     * For each source language in this map send a request to the translation services
     * @param translationService the translation service to use
     * @param targetLanguage the language into which we want to translate
     * @return a FieldValuesLanguageMap containing the fields and translated values
     * @throws TranslationException when there is an error sending/retrieving data from the translation service
     */
    public FieldValuesLanguageMap translate(TranslationService translationService, String targetLanguage) throws TranslationException, TranslationServiceLimitException {
        // send a request for each of the languages
        long startTimeTranslate = System.currentTimeMillis();
        List<FieldValuesLanguageMap> translations = new ArrayList<>();
        long nrCharacters = 0;
        for (FieldValuesLanguageMap mapToTranslate : this.values()) {
            nrCharacters = nrCharacters + mapToTranslate.getNrCharacters();
            translations.add(TranslationUtils.translate(translationService, mapToTranslate, targetLanguage));
        }
        // Temp functionality (to remove later), for EA-2633 / 2661 we need to log the amount of characters that are sent for 1 record
        LOG.info("{}", nrCharacters);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Translate - Sending and receiving requests took {} ms", (System.currentTimeMillis() - startTimeTranslate));
        }

        // merge result values from different languages (we do this separately to avoid ConcurrentModificationExceptions)
        long startTimeMerge = System.currentTimeMillis();
        FieldValuesLanguageMap result = new FieldValuesLanguageMap(targetLanguage);
        for (FieldValuesLanguageMap translation : translations) {
            result.merge(translation);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Translate - Merging results values took {} ms", (System.currentTimeMillis() - startTimeMerge));
        }
        return result;
    }

}
