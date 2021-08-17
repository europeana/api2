package eu.europeana.api2.v2.service.translate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Keeps track of all the data from a single record that we want to translate. The keys are the different languages,
 * the values are maps consisting of fieldnames and texts to translate.
 *
 * @author P. Ehlert
 * Created 22 jul 2021
 */
public class TranslationsMap extends LinkedHashMap<String, FieldValuesLanguageMap> {

    private static final long serialVersionUID = 3953283538425288592L;

    /**
     * Adds a map of a particular translation
     * @param map the translationmap to store, if null nothing is added
     */
    public void add(FieldValuesLanguageMap map) {
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
    public void addAll(List<FieldValuesLanguageMap> maps) {
        for (FieldValuesLanguageMap map : maps) {
            this.add(map);
        }
    }


}
