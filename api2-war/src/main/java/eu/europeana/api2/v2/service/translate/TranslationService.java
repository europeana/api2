package eu.europeana.api2.v2.service.translate;

public interface TranslationService {

    String[] getSupportedLanguages();
    public String detectLanguage(String text); //the language with most confidence
    public String translate(String text, String targetLanguage);
    public String translate(String text, String targetLanguage, String sourceLanguage);
    public void close();
}
