package eu.europeana.api2.v2.model.translate;

/**
 * Google translate + autodetect language
 */

import eu.europeana.api2.v2.service.translate.TranslationService;
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

    private static final Logger LOG = LoggerFactory.getLogger(QueryTranslator.class);

    private TranslationService translationService;

    @Autowired
    public MultilingualQueryGenerator(TranslationService translationService) {
        this.translationService = translationService;
        LOG.info("MultilingualQueryGenerator initialised with {} service", translationService);
    }

    public String getMultilingualQuery(String queryString, String targetLanguage, String sourceLanguage) {
        return getMultilingualQuery(new eu.europeana.api2.v2.model.translate.Query(queryString), targetLanguage, sourceLanguage);
    }

    public String getMultilingualQuery(Query query, String targetLanguage, String sourceLanguage) throws IndexOutOfBoundsException {
        String mQuery = null;
        QueryParser qParser = new QueryParser();
        query = qParser.parse(query);
        // TODO creating a new QueryTranslator object seem rather expensive to do for each request
        QueryTranslator queryTranslator = new QueryTranslator(this.translationService);
        String translation = queryTranslator.translate(query,targetLanguage, sourceLanguage);
        mQuery = "(" +  query.getText() + ")" + " OR " + "(" + translation + ")"; //TODO: basic multilingual query
        return  mQuery;
    }

    @PreDestroy
    public void close(){
        if (this.translationService != null) {
            this.translationService.close();
        }
    }

}
