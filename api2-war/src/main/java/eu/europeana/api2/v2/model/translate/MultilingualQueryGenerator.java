package eu.europeana.api2.v2.model.translate;

/**
 * Generate multi-lingual search query
 */

import eu.europeana.api2.v2.exceptions.TranslationException;
import eu.europeana.corelib.web.exception.EuropeanaException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;

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
    public String getMultilingualQuery(String queryString, String targetLanguage, String sourceLanguage) throws EuropeanaException {
        return getMultilingualQuery(new eu.europeana.api2.v2.model.translate.Query(queryString), targetLanguage, sourceLanguage);
    }

    private String getMultilingualQuery(Query query, String targetLanguage, String sourceLanguage) throws EuropeanaException, IndexOutOfBoundsException {
        LOG.debug("target language {}, source language {}", targetLanguage, sourceLanguage);
        QueryParser qParser = new QueryParser();
        query = qParser.parse(query);
        String translation = queryTranslator.translate(query, targetLanguage, sourceLanguage);
        if (!StringUtils.isBlank(translation)) { // this is to prevent issues with Pangeanic returning empty result sometimes
            return "(" + query.getText() + ")" + " OR " + "(" + translation + ")"; //TODO: basic multilingual query
        }
        return query.getText(); // fallback, in case we don't get translation
    }

    /**
     * @return true if there is a translation engine configured and query translation configuration option is enabled
     */
    public boolean isEnabled() {
        return isQueryTranslationEnabled && queryTranslator.isServiceConfigured();
    }

    @PreDestroy
    public void close(){
        if (this.queryTranslator != null) {
            this.queryTranslator.close();
        }
    }

}
