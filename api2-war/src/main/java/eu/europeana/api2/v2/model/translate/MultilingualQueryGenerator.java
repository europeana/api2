package eu.europeana.api2.v2.model.translate;

/**
 * Google translate + autodetect language
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;

/**
 *  * For now it's only translating from English to Spanish
 */
@Service
public class MultilingualQueryGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(MultilingualQueryGenerator.class);

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
    public String getMultilingualQuery(String queryString, String targetLanguage, String sourceLanguage) {
        return getMultilingualQuery(new eu.europeana.api2.v2.model.translate.Query(queryString), targetLanguage, sourceLanguage);
    }

    private String getMultilingualQuery(Query query, String targetLanguage, String sourceLanguage) throws IndexOutOfBoundsException {
        LOG.debug("target language {}, source language {}", targetLanguage, sourceLanguage);
        QueryParser qParser = new QueryParser();
        query = qParser.parse(query);
        String translation = queryTranslator.translate(query, targetLanguage, sourceLanguage);
        return "(" +  query.getText() + ")" + " OR " + "(" + translation + ")"; //TODO: basic multilingual query
    }

    @PreDestroy
    public void close(){
        if (this.queryTranslator != null) {
            this.queryTranslator.close();
        }
    }

}
