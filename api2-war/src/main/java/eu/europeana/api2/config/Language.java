package eu.europeana.api2.config;

import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

/**
 * Define static language values
 */
public class Language {

    private Language() {
        // empty constructor to prevent initialization
    }

    public static final String DEF = "def";
    public static final String NO_LINGUISTIC_CONTENT = "zxx";

    /**
     * Check if the provided language code indicates no linguistic content
     * (see also <a href="https://en.wikipedia.org/wiki/Zxx">Wikipedia</a>)
     * @param lang language code to check
     * @return true if provided language is zxx, else false
     */
    public static boolean isNoLinguisticContent(String lang) {
        return Language.NO_LINGUISTIC_CONTENT.equalsIgnoreCase(lang);
    }

    /**
     * @param lang the language abbreviation to check
     * @return return true, if lang value has a region specified, e.g en-GB, otherwise false
     */
    public static boolean isLanguageWithRegionLocales(String lang) {
        return lang.length() > 2 && lang.contains("-") ;
    }

    /**
     * @param lang the language abbreviation to check
     * @return the substring  before '-' if the provided lang value has a region locales, otherwise the unchanged lang
     * value
     */
    public static String stripRegionIfPresent(String lang) {
        if (isLanguageWithRegionLocales(lang)) {
            return StringUtils.substringBefore(lang, "-");
        }
        return lang;
    }

    /**
     * @param lang the language abbreviation to check
     * @return a language string without region in uppercase
     */
    public static String getCleanedLangAbbreviation(String lang) {
        return stripRegionIfPresent(lang).trim().toLowerCase(Locale.ROOT);
    }
}
