package eu.europeana.api2.v2.service.translate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Keeps track of all the data from a single record that we want to translate. The keys are the different languages,
 * the values are TranslationMaps (maps of fieldnames and texts to translate)
 *
 * @author P. Ehlert
 * Created 22 jul 2021
 */
public class TranslationsMap extends HashMap<String, TranslationMap> {

    private static final long serialVersionUID = 3953283538425288592L;

    /**
     * Adds a map of a particular translation
     * @param map the translationmap to store, if null nothing is added
     */
    public void add(TranslationMap map) {
        if (map != null) {
            if (this.containsKey(map.getLanguage())) {
                // copy everything to existing TranslationMap value
                for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                    get(map.getLanguage()).put(entry.getKey(), entry.getValue());
                }
            } else {
                this.put(map.getLanguage(), map);
            }
        }
    }
    /**
     * Add a new entry to the map
     * @param language the language to which the text should be translated
     * @param fieldName the field name containing the text that should be translated
     * @param textToTranslate the texts that should be translated
     */
    public void put(String language, String fieldName, List<String> textToTranslate) {
        if (this.containsKey(language)) {
            get(language).put(fieldName, textToTranslate);
        } else {
            put(language, new TranslationMap(language, fieldName, textToTranslate));
        }
    }
}
