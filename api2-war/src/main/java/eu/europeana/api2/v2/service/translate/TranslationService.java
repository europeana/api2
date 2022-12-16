package eu.europeana.api2.v2.service.translate;

import eu.europeana.api2.v2.exceptions.TranslationException;
import eu.europeana.api2.v2.model.translate.Language;

import java.util.List;

/**
 * Generic translation service interface
 */
public interface TranslationService {

    /**
     * Translate multiple texts and leave it to the translation engine to detect the source language
     * @param texts to translate
     * @param targetLanguage language into which the texts are translated
     * @return translations of the provided texts
     * @throws TranslationException when there is a problem sending the translation request
     */
    List<String> translate(List<String> texts, String targetLanguage, Language edmLang) throws TranslationException;

    /**
     * Translate multiple texts and leave it to the translation engine to detect the source language
     * @param texts to translate
     * @param targetLanguage language into which the texts are translated
     * @param sourceLanguage language of the source texts
     * @return translations of the provided texts
     * @throws TranslationException when there is a problem sending the translation request
     */
    List<String> translate(List<String> texts, String targetLanguage, String sourceLanguage) throws TranslationException;

    /**
     * Shutdown the (connection to) the used translation engine
     */
    void close();
}
