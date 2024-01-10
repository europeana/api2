package eu.europeana.api2.v2.model.translate;

import eu.europeana.api.translation.client.TranslationApiClient;
import eu.europeana.api.translation.client.exception.ExternalServiceException;
import eu.europeana.api.translation.client.exception.TranslationApiException;
import eu.europeana.api2.v2.exceptions.TranslationException;
import eu.europeana.api2.v2.exceptions.TranslationServiceLimitException;
import eu.europeana.api2.v2.service.translate.TranslationUtils;
import eu.europeana.corelib.web.exception.EuropeanaException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private String translate(String text, String targetLanguage, String sourceLanguage, boolean enclose) throws TranslationException, TranslationServiceLimitException {
        StringBuilder sb =  new StringBuilder();
        String toTranslate = text.trim();
        if (!toTranslate.isEmpty()) {
            String translation;
            long start = System.nanoTime(); //DEBUG
            try {
                translation = this.translationClient.translate(
                        TranslationUtils.createTranslationRequest(List.of(toTranslate), targetLanguage, sourceLanguage))
                        .getTranslations().get(0);
            } catch(TranslationApiException e) {
                // For 502 status , Client throws ExternalServiceException.
                // Translation api throws 502 status for google exhuasted exception or if the external service had some issue.
                // Hence we need to check for the message as well as we have a redirect functionality based on it.
                if (e instanceof ExternalServiceException && StringUtils.containsIgnoreCase(e.getMessage(), "quota limit reached")) {
                    throw new TranslationServiceLimitException(e);
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

    public String translate(Query query, String targetLanguage, String sourceLanguage) throws EuropeanaException {
        QueryPartType previous = null;
        StringBuilder outputQuery = new StringBuilder();
        for (QueryPart queryPart : query.getQueryPartList()) {
            String originalText = queryPart.getText();
            QueryPartType type = queryPart.getPartType();
            if (type == QueryPartType.TEXT && previous == QueryPartType.UNARY_OPERATOR) {

                Matcher matcher = FIRST_WORD_PATTERN.matcher(originalText);
                if (matcher.find()) {
                    String firstWord = originalText.substring(0,matcher.end());
                    outputQuery.append(translate(firstWord, targetLanguage, sourceLanguage, true)); //translation first word unary operator in brackets
                    String rest = originalText.substring(matcher.end());
                    outputQuery.append(translate(rest, targetLanguage, sourceLanguage,false));
                } else {
                    outputQuery.append(translate(originalText, targetLanguage, sourceLanguage,false));
                }

            } else if (type == QueryPartType.QUOTED || type == QueryPartType.TEXT) {
                outputQuery.append(translate(originalText, targetLanguage, sourceLanguage,false));
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
