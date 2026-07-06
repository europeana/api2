package eu.europeana.api2.config;

import eu.europeana.api2.v2.exceptions.InvalidParamValueException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Supported languages for translation and language filtering
 */
@Configuration
@PropertySource("classpath:europeana.properties")
@PropertySource(value = "classpath:europeana.user.properties", ignoreResourceNotFound = true)
public class SupportedLanguages {

    private static final Logger LOG = LogManager.getLogger(SupportedLanguages.class);

    private static final String SEPARATOR = ",";

    private Set<String> supportedLang;

    @Value("${translation.supported.languages}")
    String languagesConfig; // also used for output in error messages

    /**
     * Default constructor
     */
    public SupportedLanguages() {
        // empty construction
    }

    /**
     * Constructor used for testing
     * @param languagesConfig
     */
    public SupportedLanguages(String languagesConfig) {
        this.languagesConfig = languagesConfig;
        this.loadSupportedLanguages();
    }

    @PostConstruct
    void loadSupportedLanguages() {
        if (languagesConfig == null) {
            LOG.warn("No supported languages configured");
        } else {
            supportedLang = new HashSet<>();
            for (String language : languagesConfig.split(SEPARATOR)) {
                supportedLang.add(language.trim());
            }
        }
        LOG.info("Loaded supported languages: {}", languagesConfig);
    }

    /**
     * Check if a particular string is one of the supported languages
     * @param lang the language string to check
     * @return true if we support it, otherwise false
     */
    public boolean isSupported(String lang) {
         return supportedLang.contains(Language.getCleanedLangAbbreviation(lang));
    }

    /**
     * Validate if the provided string is a single 2-letter ISO-code language abbreviation
     * @param languageAbbreviation the string to check
     * @return the validated language abbreviation, trimmed and converted to lowercase
     * @throws InvalidParamValueException if the string did not match any supported language
     */
    public String validateSingle(String languageAbbreviation) throws InvalidParamValueException {
        if (StringUtils.isEmpty(languageAbbreviation)) {
            throw new InvalidParamValueException("Empty language parameter value is not allowed");
        }

        String result = Language.getCleanedLangAbbreviation(languageAbbreviation);
        if (!supportedLang.contains(result)) {
            throw new InvalidParamValueException("Language value '" + languageAbbreviation + "' is not valid. "
            + "Supported values are: " + languagesConfig);
        }
        return result;
    }

    /**
     * Checks if the provided string consists of one or more 2 letter abbreviation of the supported languages.
     * @param languageAbbreviations String containing one or more two letter ISO-code abbreviation of a language, separated
     *                             by a comma (and optionally also a space)
     * @return List of validated languages, trimmed and converted to lowercase
     * @throws InvalidParamValueException if one of the values is incorrect
     */
    public List<String> validateMultiple(String languageAbbreviations) throws InvalidParamValueException {
        if (StringUtils.isEmpty(languageAbbreviations)) {
            throw new InvalidParamValueException("Empty language parameter value is not allowed");
        }

        List<String> result = new ArrayList<>();
        for (String language : languageAbbreviations.split(SEPARATOR)) {
            String validatedLang = validateSingle(language);
            result.add(validatedLang);
        }
        return result;
    }

}
