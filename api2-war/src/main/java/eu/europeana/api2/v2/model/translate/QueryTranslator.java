package eu.europeana.api2.v2.model.translate;

import eu.europeana.api.translation.client.TranslationApiClient;
import eu.europeana.api.translation.definitions.model.TranslationObj;
import eu.europeana.api2.v2.exceptions.TranslationException;
import eu.europeana.api2.v2.exceptions.TranslationServiceNotAvailableException;
import eu.europeana.api2.v2.service.translate.TranslationUtils;
import eu.europeana.corelib.web.exception.EuropeanaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static eu.europeana.api2.v2.utils.ControllerUtils.is5xxError;

@Component
public class QueryTranslator {

    private static final Logger LOG = LoggerFactory.getLogger(QueryTranslator.class);
    private static final String FIRST_WORD_REGEX = "^\\s*\\S+";
    private static final Pattern FIRST_WORD_PATTERN = Pattern.compile(FIRST_WORD_REGEX);

    private TranslationApiClient translationClient;

    @Autowired
    public QueryTranslator(TranslationApiClient translationClient) {
        this.translationClient = translationClient;
        LOG.info("QueryTranslator initialised with Translation Api client");
    }

    private String translate(String text, String targetLanguage, String sourceLanguage, boolean enclose, String authToken) throws TranslationException, TranslationServiceNotAvailableException {
        StringBuilder sb =  new StringBuilder();
        String toTranslate = text.trim();
        if (!toTranslate.isEmpty()) {
            String translation;
            long start = System.nanoTime(); //DEBUG
            try {
                this.translationClient.setAuthToken(authToken);
                List<TranslationObj> translationObjs =  TranslationUtils.createTranslationRequest(List.of(toTranslate), targetLanguage, sourceLanguage);
                this.translationClient.getTranslationService().translate(translationObjs);
                translation =    translationObjs.get(0).getTranslation();
            } catch (eu.europeana.api.translation.service.exception.TranslationException e) {
                // For all 5xx error return a TranslationServiceNotAvailableException.
                if (is5xxError(e.getRemoteStatusCode())) {
                    throw new TranslationServiceNotAvailableException(e.getMessage(), e);
                }
                // keep in mind once we have token being passed that should be valid for
                // translation api as well and we will never receive Unauthorised error here as it is validated in the beginning.
                throw new TranslationException(e);
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("<TRANSLATION> text: {} time: {}", text.replaceAll("\t", " "), (System.nanoTime() - start) / 1_000_000);
            }
            if (text.startsWith(" ")) {
                sb.append(" ");
            }
            if (enclose) {
                sb.append("(").append(translation).append(")");
            } else {
                sb.append(translation);
            }
            if (text.endsWith(" ")) {
                sb.append(" ");
            }
        } else {
            return text;
        }
        return sb.toString();
    }

    public String translate(Query query, String targetLanguage, String sourceLanguage, String authToken) throws EuropeanaException {
        QueryPartType previous = null;
        StringBuilder outputQuery = new StringBuilder();
        for (QueryPart queryPart : query.getQueryPartList()) {
            String originalText = queryPart.getText();
            QueryPartType type = queryPart.getPartType();
            if (type == QueryPartType.TEXT && previous == QueryPartType.UNARY_OPERATOR) {

                Matcher matcher = FIRST_WORD_PATTERN.matcher(originalText);
                if (matcher.find()) {
                    String firstWord = originalText.substring(0,matcher.end());
                    outputQuery.append(translate(firstWord, targetLanguage, sourceLanguage, true, authToken)); //translation first word unary operator in brackets
                    String rest = originalText.substring(matcher.end());
                    outputQuery.append(translate(rest, targetLanguage, sourceLanguage,false, authToken));
                } else {
                    outputQuery.append(translate(originalText, targetLanguage, sourceLanguage,false, authToken));
                }

            } else if (type == QueryPartType.QUOTED || type == QueryPartType.TEXT) {
                outputQuery.append(translate(originalText, targetLanguage, sourceLanguage,false, authToken));
            } else {
                outputQuery.append(originalText);
            }
            previous = type;
        }
        return outputQuery.toString();
    }

    /**
     * @return true if there is a translation engine configured
     */
    public boolean isServiceConfigured() {
        return translationClient != null;
    }

}
