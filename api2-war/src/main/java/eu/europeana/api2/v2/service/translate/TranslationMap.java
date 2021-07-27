package eu.europeana.api2.v2.service.translate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * Keeps track of all translations in one particular language. The keys are the field names, the values the texts that
 * should be translated
 *
 * @author P. Ehlert
 * Created 22 jul 2021
 */
public class TranslationMap extends LinkedHashMap<String, List<String>> {

    private static final long serialVersionUID = 7857857025275959529L;

    private static final Logger LOG = LogManager.getLogger(TranslationMap.class);

    public static final String APPENDIX = "-";

    @Nonnull
    private final String language;

    /**
     * Create a new empty map for a particular language
     * @param language the language to which all added objects should be translated
     */
    public TranslationMap(String language) {
        this.language = language;
    }

    /**
     * Create a new map and add first values
     * @param language the language to which all texts should be translated
     * @param fieldName String containing the field name of the texts that should be translated
     * @param textToTranslate List of String containing the actual texts that should be translated
     */
    public TranslationMap(String language, String fieldName, List<String> textToTranslate) {
        super.put(fieldName, textToTranslate);
        this.language = language;
    }

    @Override
    public List<String> put(String fieldName, List<String> textToTranslate) {
        // check if a field name was already added, as a sanity check
        if (containsKey(fieldName)) {
            throw new IllegalArgumentException("Key " + fieldName + " already exists for language " + language +
                    " with value " + textToTranslate);
        }
        return super.put(fieldName, textToTranslate);
    }

    /**
     * Merges one translation map into another. Note that duplicate keys are ignored only if their value is exactly
     * the same. If not, then a new key name is created (the key name will then get an appendix based on the hash of
     * the value)
     * @param map the map to merge into this map
     */
    public void merge(TranslationMap map) {
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            List<String> value = entry.getValue();
            String key = entry.getKey();
            String keyAndValueHash = getKeyAndValueHash(key, value);
            if (this.containsKey(keyAndValueHash)) {
                // to detect cases where there's more than 1 duplicate
                LOG.debug("Skipping another duplicate key {} and value {}", key, value);
            } else if (this.containsKey(key)) {
                if (this.get(key).equals(value)) {
                    LOG.debug("Skipping duplicate key {} and value {}", key, value);
                } else {
                    LOG.debug("Found duplicate key {} with value {} so renaming key to {} (existing value is {}) ",
                            key, value, keyAndValueHash, this.get(key));
                    this.put(keyAndValueHash, value);
                }
            } else {
                this.put(key, value);
            }
        }
    }

    private String getKeyAndValueHash(String origKeyName, List<String> value) {
        return origKeyName + APPENDIX + value.hashCode();
    }

    @Nonnull
    public String getLanguage() {
        return language;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        TranslationMap that = (TranslationMap) o;
        return language.equals(that.language);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), language);
    }


}
