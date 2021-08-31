package eu.europeana.api2.v2.service.translate;

import eu.europeana.api2.v2.exceptions.TranslationException;
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
     * Adds a map of a particular translation
     * @param map the translationmap to store, if null nothing is added
     */
    private void add(FieldValuesLanguageMap map) {
        if (map != null) {
            if (this.containsKey(map.getSourceLanguage())) {
                // copy everything to existing TranslationMap value
                for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                    get(map.getSourceLanguage()).put(entry.getKey(), entry.getValue());
                }
            } else {
                this.put(map.getSourceLanguage(), map);
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
    public FieldValuesLanguageMap translate(TranslationService translationService, String targetLanguage) throws TranslationException {
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
