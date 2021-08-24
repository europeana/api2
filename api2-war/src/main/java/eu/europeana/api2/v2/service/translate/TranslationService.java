package eu.europeana.api2.v2.service.translate;

import java.util.List;

/**
 * Generic translation service interface
 */
public interface TranslationService {

    String[] getSupportedLanguages();


    String detectLanguage(String text); //the language with most confidence

    /**
     * Translate a particular text and leave it to the translation engine to detect the source language
     * @param text to translate
     * @param targetLanguage language into which the text is translated
     * @return translation of the provided text
     */
    String translate(String text, String targetLanguage);

    /**
     * Translate multiple texts and leave it to the translation engine to detect the source language
     * @param texts to translate
     * @param targetLanguage language into which the texts are translated
     * @return translations of the provided texts
     */
    List<String> translate(List<String> texts, String targetLanguage);

    /**
     * Translate a particular text and leave it to the translation engine to detect the source language
     * @param text to translate
     * @param targetLanguage language into which the text is translated
     * @param sourceLanguage language of the source text
     * @return translation of the provided text
     */
    String translate(String text, String targetLanguage, String sourceLanguage);

    /**
     * Translate multiple texts and leave it to the translation engine to detect the source language
     * @param texts to translate
     * @param targetLanguage language into which the texts are translated
     * @param sourceLanguage language of the source texts
     * @return translations of the provided texts
     */
    List<String> translate(List<String> texts, String targetLanguage, String sourceLanguage);

    /**
     * Shutdown the (connection to) the used translation engine
     */
    void close();
}
