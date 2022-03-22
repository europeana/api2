package eu.europeana.api2.v2.model.translate;

import eu.europeana.api2.v2.exceptions.InvalidParamValueException;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Supported languages for filtering record data
 *
 * @author P. Ehlert
 * Created 6 jul 2021
 */
public enum Language {

    EN, NL, FR, DE, ES, SV, IT, FI, DA, EL, CS, SK, SL, PT, HU, LT, PL, RO, BG, HR, LV, GA, MT, ET, NO, CA, RU;

    private static final Set<String> LANGUAGES = new HashSet<>(Stream.of(Language.values())
                    .map(Enum::name)
                    .collect(Collectors.toList()));

    private static final String SEPARATOR = ",";

    public static final String DEF = "def";
    public static final String ENGLISH = Language.EN.name().toLowerCase(Locale.ROOT);

    /**
     * Validate if the provided string is a single 2-letter ISO-code language abbreviation
     * @param languageAbbrevation the string to check
     * @return Language that was found
     * @throws InvalidParamValueException if the string did not match any supported language
     */
    public static Language validateSingle(String languageAbbrevation) throws InvalidParamValueException {
        if (StringUtils.isBlank(languageAbbrevation)) {
            throw new InvalidParamValueException("Empty language value");
        }

        Language result;
        try {
            result = Language.valueOf(languageAbbrevation.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new InvalidParamValueException("Language value '" + languageAbbrevation + "' is not valid");
        }
        return result;
    }

    /**
     * Checks if the provided string consists of one or more 2 letter abbreviation of the supported languages.
     * @param languageAbbrevations String containing one or more two letter ISO-code abbreviation of a language, separated
     *                             by a comma (and optionally also a space)
     * @return a list of one or more found languages
     * @throws InvalidParamValueException if one of the values is incorrect
     */
    public static List<Language> validateMultiple(String languageAbbrevations) throws InvalidParamValueException {
        if (StringUtils.isBlank(languageAbbrevations)) {
            throw new InvalidParamValueException("Empty language value");
        }

        List<Language> result = new ArrayList<>();
        String[] languages = languageAbbrevations.split(SEPARATOR);
        for (String language: languages) {
            result.add(validateSingle(language));
        }
        if (result.isEmpty()) {
            throw new InvalidParamValueException("Language value '" + languageAbbrevations + "' is not valid");
        }
        return result;
    }

    public static Language getLanguage(String lang) {
        return Language.valueOf(stripLangStringIfRegionPresent(lang).toUpperCase(Locale.ROOT));
    }

    /**
     * Check if a particular string is one of the supported languages
     * @param lang 2 letter ISO-code abbrevation of a language
     * @return true if we support it, otherwise false
     */
    public static boolean isSupported(String lang) {
        return LANGUAGES.contains(stripLangStringIfRegionPresent(lang).toUpperCase(Locale.ROOT));
    }

    /**
     * Return true, if lang value is with regions ex: en-GB
     * @param lang
     * @return
     */
    private static boolean isLanguageWithRegionLocales(String lang) {
        return lang.length() > 2 && lang.contains("-") ;
    }

    /**
     * returns the substring  before '-' if lang value is with region locales
     * @param lang
     * @return
     */
    private static String stripLangStringIfRegionPresent(String lang) {
        if (isLanguageWithRegionLocales(lang)) {
            return StringUtils.substringBefore(lang, "-");
        }
        return lang;
    }
}
