package eu.europeana.api2.v2.service.translate;

import eu.europeana.api.translation.client.TranslationApiClient;
import eu.europeana.api.translation.definitions.language.Language;
import eu.europeana.corelib.definitions.edm.beans.FullBean;
import eu.europeana.corelib.definitions.edm.beans.IdBean;
import eu.europeana.corelib.definitions.edm.entity.Proxy;
import eu.europeana.corelib.edm.utils.EdmUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class MetadataChosenLanguageService extends BaseService {

    private static final Logger LOG = LogManager.getLogger(MetadataChosenLanguageService.class);

    public MetadataChosenLanguageService(TranslationApiClient translationApiClient) {
        super(translationApiClient);
    }

    public <T extends IdBean> String getMostRepresentativeLanguage(T bean, String targetLanguage, boolean searchResults) {
        Map<String, Integer> langCountMap = new HashMap<>();
        if (searchResults) {
            ReflectionUtils.doWithFields(bean.getClass(), field -> getLanguageAndCount(bean, field, langCountMap, targetLanguage, searchResults), BaseService.searchFieldFilter);
        } else  {
            List<? extends Proxy> proxies = ((FullBean)bean).getProxies();
            for (Proxy proxy : proxies) {
                ReflectionUtils.doWithFields(proxy.getClass(), field -> getLanguageAndCount(proxy, field, langCountMap, targetLanguage, searchResults), BaseService.proxyFieldFilter);
            }
        }
        // if there is no language available for translation workflow, do nothing
        if (langCountMap.isEmpty()) {
            LOG.error("Most representative languages NOT present for search results. " +
                    "Languages present are either zxx or def or not-supported by the translation engine");
            return null;
        }
        return getMostRepresentativeLanguage(langCountMap);
    }

    /**
     * Will fetch the most representative language
     * @param langCountMap
     */
    private String getMostRepresentativeLanguage(Map<String, Integer> langCountMap) {
        //reverse map - as values might not be unique so using grouping method
        Map<Integer, List<String>> reverseMap =
                langCountMap.entrySet()
                        .stream()
                        .collect(Collectors.groupingBy(Map.Entry::getValue, Collectors.mapping(Map.Entry::getKey, Collectors.toList())));
        List<String> languagesWithMostvalues = reverseMap.get(Collections.max(reverseMap.keySet()));

        LOG.debug("Language with most values {}" , languagesWithMostvalues);

        // if there is a tie between more than one language, choose based on the precedance list
        if (languagesWithMostvalues.size() > 1) {
            Optional<String> langWithHigherPrecedance =  BaseService.PRECENDANCE_LIST.stream().filter(languagesWithMostvalues :: contains).findFirst();
            if (langWithHigherPrecedance.isPresent()) {
                return langWithHigherPrecedance.get();
            } else {
                LOG.warn("Language not found in the precedence list. Hence, will return the first language out of - {} ", languagesWithMostvalues);
            }
        }
        // will only have one value here, hence by default or any else case return the first language.
        // Also if we had multiple values and those languages were not present in the precedence list (this is an exceptional case, should not happen)
        // but in those cases as well just any random value is acceptable( we will return the first language)
        return languagesWithMostvalues.get(0);
    }

    /**
     * Fetches the Language value map for the field for Proxy OR
     *  search result bean map.
     *  NOTE -  Map keys we get from Solr have compound names with a dot (e.g. {proxy_dc_title.ro=[Happy-end]})
     *         The EdmUtils.cloneMap functionality makes sure that is transformed into something we can use (e.g. {ro=[Happy-end]})
     * @param object
     * @param field
     * @return
     */
    private  Map<String, List<String>> getLanguageValueMap(Object object, Field field, boolean searchResults) {
        if (searchResults) {
            return EdmUtils.cloneMap(BaseService.getValueOfTheField(object, false).apply(field.getName()));
        } else{
            return BaseService.getValueOfTheField(object, false).apply(field.getName());
        }
    }

    private void getLanguageAndCount(Object object, Field field, Map<String, Integer> langCountMap, String targetLang, boolean searchResults) {
        Map<String, List<String>> langValueMap = getLanguageValueMap(object, field, searchResults);
        if (!langValueMap.isEmpty()) {
            for (Map.Entry<String, List<String>> langValue : langValueMap.entrySet()) {
                String key = Language.stripLangStringIfRegionPresent(langValue.getKey()); // if region codes present get the first two ISO letters
                if (languageToBeChosen(key, targetLang)) {
                    Integer value = langValue.getValue().size();
                    if (langCountMap.containsKey(key)) {
                        value += langCountMap.get(key);
                    }
                    langCountMap.put(key, value);
                }
            }
        }
    }

    /**
     * Identifies if Language should be selected as most representative
     * Ignores the values with language code “zxx” or "def" and unsupported languages (ie. call the isSupported method)
     * <p>
     * NOTE : We check if the translation is supported for the language pair.
     *
     * @param lang value
     * @return true if language should be chosen
     */

    private boolean languageToBeChosen(String lang, String targetLanguage) {
        return !(StringUtils.equals(lang, Language.NO_LINGUISTIC_CONTENT) || StringUtils.equals(lang, Language.DEF))
                && getTranslationApiClient().isSupported(lang, targetLanguage);
    }

}
