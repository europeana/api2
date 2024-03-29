package eu.europeana.api2.v2.model.translate;

/**
 * Generate multi-lingual search query
 */
import eu.europeana.corelib.web.exception.EuropeanaException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 *  * For now it's only translating from English to Spanish
 */
@Service
public class MultilingualQueryGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(MultilingualQueryGenerator.class);

    @Value("#{europeanaProperties['translation.search.query']}")
    private boolean isQueryTranslationEnabled; // feature flag to enable/disable query translate

    private QueryTranslator queryTranslator;

    @Autowired
    public MultilingualQueryGenerator(QueryTranslator queryTranslator) {
        this.queryTranslator = queryTranslator;
    }

    /**
     *
     * @param queryString
     * @param targetLanguage required
     * @param sourceLanguage optional, if null Google Translate will try and detect the language
     * @return
     */
    public String getMultilingualQuery(String queryString, String targetLanguage, String sourceLanguage, String authToken) throws EuropeanaException {
        return getMultilingualQuery(new eu.europeana.api2.v2.model.translate.Query(queryString), targetLanguage, sourceLanguage, authToken);
    }

    private String getMultilingualQuery(Query query, String targetLanguage, String sourceLanguage, String authToken) throws EuropeanaException, IndexOutOfBoundsException {
        LOG.debug("target language {}, source language {}", targetLanguage, sourceLanguage);
        QueryParser qParser = new QueryParser();
        query = qParser.parse(query);
        String translation = queryTranslator.translate(query, targetLanguage, sourceLanguage, authToken);
        /**
         * if translation is not blank, check if the translation and original query are same
         *       if Yes : return the original
         *       if No : return a multilingual query
         */
        if (StringUtils.isNotBlank(translation)) {
            if (StringUtils.equals(query.getText(), translation)) {
                return query.getText();
            } else {
                return "(" + query.getText() + ")" + " OR " + "(" + translation + ")"; //TODO: basic multilingual query
            }
        }
        return query.getText(); // fallback, in case we don't get translation
    }

    /**
     * @return true if there is a translation engine configured and query translation configuration option is enabled
     */
    public boolean isEnabled() {
        return isQueryTranslationEnabled && queryTranslator.isServiceConfigured();
    }

}
