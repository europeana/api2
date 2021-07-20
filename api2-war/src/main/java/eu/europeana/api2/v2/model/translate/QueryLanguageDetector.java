package eu.europeana.api2.v2.model.translate;

import eu.europeana.api2.v2.service.translate.TranslationService;

import java.util.ArrayList;
import java.util.List;

public class QueryLanguageDetector {

    private TranslationService translationService;

    public QueryLanguageDetector(TranslationService translationService){
        this.translationService = translationService;
    }

    public List<String> detectLanguages(Query query){
        List<String> languages = new ArrayList<>();
        for (QueryPart queryPart : query.getQueryPartList()) {
            String originalText = queryPart.getText();
            QueryPartType type = queryPart.getPartType();
            if (type == QueryPartType.QUOTED || type == QueryPartType.TEXT) {
                String text = originalText.trim();
                if (!text.isEmpty()) {
                    languages.add(this.translationService.detectLanguage(originalText));
                }
            }
        }
        return languages;
    }

}
