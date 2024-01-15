package eu.europeana.api2.v2.model.translate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.validation.constraints.NotNull;
import java.util.LinkedHashMap;
import java.util.List;

public class TranslationMap extends LinkedHashMap<String, List<String>> {

    private static final long serialVersionUID = 7857857025275959529L;

    private static final Logger LOG = LogManager.getLogger(TranslationMap.class);

    @NotNull
    private final String sourceLanguage;

    public TranslationMap(@NotNull String sourceLanguage) {
        this.sourceLanguage = sourceLanguage;
    }

    public TranslationMap(@NotNull String sourceLanguage, String fieldName, List<String> values) {
        this.sourceLanguage = sourceLanguage;
        add(fieldName, values);
    }

    /**
     * Adds the fieldname and the list of values for that field in the Translation map
     *
     * @param fieldName
     * @param values
     */
    public void add(String fieldName, List<String> values) {
        if (fieldName != null && !values.isEmpty()) {
            if (this.containsKey(fieldName)) {
                this.get(fieldName).addAll(values);
            } else {
                this.put(fieldName, values);
            }
        }
    }

    @NotNull
    public String getSourceLanguage() {
        return sourceLanguage;
    }
}
