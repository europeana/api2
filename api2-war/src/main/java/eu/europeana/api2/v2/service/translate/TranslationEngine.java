package eu.europeana.api2.v2.service.translate;

import org.apache.logging.log4j.LogManager;

import java.util.Locale;

/**
 * The type of translation engine to use
 *
 * @author Patrick Ehlert
 */
public enum TranslationEngine {

    GOOGLE, PANGEANIC, NONE;

    /**
     * Uses case-insensitive matching to find the corresponding enum value
     * @param s string to check
     * @return corresponding enum value, or NONE if there is no match
     */
    public static TranslationEngine fromString(String s) {
        if (s == null) {
            return NONE;
        }

        String toCheck = s.trim().toUpperCase(Locale.ROOT);
        for (TranslationEngine value : TranslationEngine.values()) {
            if (value.name().equals(toCheck)) {
                return value;
            }
        }
        LogManager.getLogger(TranslationEngine.class).warn("Unknown translation engine value '{}'. Using NONE", s);
        return NONE;
    }
}
