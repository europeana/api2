package eu.europeana.api2.v2.service.translate;

import eu.europeana.corelib.utils.ComparatorUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * Keeps track of all texts we want to translate in one particular source language.
 * The keys are the field names, the values the texts that should be translated
 *
 * @author P. Ehlert
 * Created 22 jul 2021
 */
public class FieldValuesLanguageMap extends LinkedHashMap<String, List<String>> {

    private static final long serialVersionUID = 7857857025275959529L;

    private static final Logger LOG = LogManager.getLogger(FieldValuesLanguageMap.class);

    @Nonnull
    private final String sourceLanguage;

    /**
     * Create a new map for a particular language
     * @param sourceLanguage the source language of all added objects
     */
    public FieldValuesLanguageMap(String sourceLanguage) {
        this.sourceLanguage = sourceLanguage;
    }

    /**
     * Create a new map for a particular language and provided map
     * @param sourceLanguage the source language of all added objects
     * @param map the map to be added
     */
    public FieldValuesLanguageMap(String sourceLanguage, Map<String, List<String>> map) {
        super.putAll(map);
        this.sourceLanguage = sourceLanguage;
    }

    /**
     * Create a new map and populate with a field and values to translate
     * @param sourceLanguage the source language of all all added objects
     * @param fieldName String containing the field name of the texts that should be translated
     * @param textsToTranslate List of String containing the actual texts that should be translated
     */
    public FieldValuesLanguageMap(String sourceLanguage, String fieldName, List<String> textsToTranslate) {
        super.put(fieldName, textsToTranslate);
        this.sourceLanguage = sourceLanguage;
    }

    /**
     * Adds a new field and values to this map. Note that if the map already contains this field, an error is thrown
     * @param fieldName field to add
     * @param textsToTranslate list of values to translate
     * @return any previous values for this key (will always be null)
     */
    @Override
    public List<String> put(String fieldName, List<String> textsToTranslate) {
        // Sanity check - we expect only 1 put operation per field in a particular language
        if (containsKey(fieldName)) {
            throw new IllegalArgumentException("Key " + fieldName + " already exists for language " + sourceLanguage +
                    " with value " + this.get(fieldName) + ". Use merge function instead!");
        }
        return super.put(fieldName, textsToTranslate);
    }

    public boolean remove(String fieldName, List<String> valuesToRemove) {
        List<String> values = this.get(fieldName);
        for (String valueToRemove : valuesToRemove) {
            if (!values.remove(valueToRemove)) {
                throw new IllegalArgumentException("Could not find and remove value " + valueToRemove);
            }
        }
        return true;
    }

    /**
     * Temporarily functionality for ticket EA-2633 / EA-2661
     * @return the number of characters of all values in this map
     */
    public long getNrCharacters() {
        long result = 0;
        for (List<String> values : this.values()) {
            for (String value : values) {
                result = result + value.length();
            }
        }
        return result;
    }

    /**
     * Merges one translation map into another if it has the same source language. Note that duplicate keys are ignored.
     * @param map the map to merge into this map
     */
    public void merge(FieldValuesLanguageMap map) {
        if (!this.getSourceLanguage().equals(map.getSourceLanguage())) {
            throw new IllegalArgumentException("Maps with different source languages should not be merged");
        }
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            String key = entry.getKey();
            List<String> values = entry.getValue();
            if (this.containsKey(key)) {
                List<String> mergedValues = mergeValues(this.get(key), values, key);
                this.remove(key);
                this.put(key, mergedValues);
            } else {
                // add new field
                this.put(key, values);
            }
        }
    }

    /**
     * Any string from values2 is merged into values1 if that doesn't already contain it
     */
    private List<String> mergeValues(List<String> values1, List<String> values2, String fieldName) {
        // to avoid a weird issue with ConcurrentModificationExceptions we create and return a new List here
        List<String> mergedValues = new ArrayList<>(values1);
        for (String value2 : values2) {
            boolean valuePresent = false;
            for (String value1 : values1) {
                if (ComparatorUtils.sameValueWithoutSpace(value1, value2)) {
                    valuePresent = true;
                    break;
                }
            }
            if (valuePresent) {
                LOG.debug("Field {} - Found duplicate value {}", fieldName, value2);
            } else {
                LOG.debug("Field {} - Merging value {}", fieldName, value2);
                mergedValues.add(value2);
            }
        }
        return mergedValues;
    }

    @Nonnull
    public String getSourceLanguage() {
        return sourceLanguage;
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
        FieldValuesLanguageMap that = (FieldValuesLanguageMap) o;
        return sourceLanguage.equals(that.sourceLanguage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), sourceLanguage);
    }


}
