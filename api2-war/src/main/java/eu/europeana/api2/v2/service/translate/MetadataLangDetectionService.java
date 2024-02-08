package eu.europeana.api2.v2.service.translate;

import eu.europeana.api.translation.client.TranslationApiClient;
import eu.europeana.api.translation.client.exception.TranslationApiException;
import eu.europeana.api.translation.definitions.language.Language;
import eu.europeana.api.translation.definitions.model.LangDetectRequest;
import eu.europeana.api2.v2.exceptions.TranslationException;
import eu.europeana.api2.v2.model.translate.LanguageValueFieldMap;
import eu.europeana.corelib.definitions.edm.beans.BriefBean;
import eu.europeana.corelib.definitions.edm.beans.FullBean;
import eu.europeana.corelib.definitions.edm.beans.IdBean;
import eu.europeana.corelib.definitions.edm.entity.Proxy;
import eu.europeana.corelib.edm.utils.EdmUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.*;

public class MetadataLangDetectionService extends BaseService {

    private static final Logger LOG = LogManager.getLogger(MetadataLangDetectionService.class);

    public MetadataLangDetectionService(TranslationApiClient translationApiClient) {
        super(translationApiClient);
    }

    /**
     * If does not match any of the languages Europeana supports or
     * if not supported by the language detection endpoint (ie. calling the isSupported method)
     * then there will be no hint supplied (this means that ‘mul’ is ignored)
     *
     * @param bean bean that extends IdBean See: {@link IdBean}
     * @return
     */
    private <T extends IdBean> String getHintForLanguageDetect(T bean, boolean searchResults) {
        List<Language> edmLanguages = LanguageDetectionUtils.getEdmLanguage(bean, searchResults);
        if (!edmLanguages.isEmpty()) {
            String edmLang = edmLanguages.get(0).name().toLowerCase(Locale.ROOT);
            if (getTranslationApiClient().isSupported(edmLang)) {
                LOG.debug("For record {}, hint for lang-detection is {} ", searchResults ? bean.getId() : ((FullBean) bean).getAbout(), edmLang);
                return edmLang;
            } else {
                LOG.debug("For record {}, edmLanguage - {} , is NOT supported by lang detection service", bean.getId(), edmLang);

            }
        }
        return null;
    }


    /**
     * Apply a language detection workflow to all whitelisted properties (dc:title, dc:description, dc:creator)
     * that do not have any language tagged values. (only literals, references such as URLs or URIs must be ignored)
     * Use edm:language (“language” field) as hint.
     * Add all new language qualified values into the record (ie. Bean)
     * @param briefBeans
     * @return
     */
    public List<BriefBean> detectLanguageForSearchResults(List<BriefBean> briefBeans, String authToken) throws TranslationException, TranslationApiException {
        long start = System.currentTimeMillis();

        int index = 0;
        for (BriefBean bean : briefBeans) {
            LOG.debug("Check search result {}...", index);
            // 1. gather values for the whitelisted fields that do not have any language tagged values
            List<LanguageValueFieldMap> langValueFieldMapForDetection = new ArrayList<>();
            ReflectionUtils.doWithFields(bean.getClass(), field -> {
                LanguageValueFieldMap fieldValuesLanguageMap = getLiteralsForSearchFields(bean, field);
                if (fieldValuesLanguageMap != null) {
                    langValueFieldMapForDetection.add(fieldValuesLanguageMap);
                }
            }, BaseService.searchFieldFilter);

            if (!langValueFieldMapForDetection.isEmpty()) {
                String langHint = getHintForLanguageDetect(bean, true);
                detectLanguageAndUpdate(langValueFieldMapForDetection, bean, langHint, true, start, authToken);
            }
            index++;
        }
        return briefBeans;
    }

    /**
     * Gather all non-language tagged values (for all whitelisted properties) of the (non-Europeana) Proxies
     * NOTE :: Only if there isn't a language tagged value already spelled exactly the same
     *
     * Run through language detection (ie. call lang detect method) and assign (or correct) language attributes for the values
     *
     * Responses indicating that the language is not supported or the inability to recognise the language should
     * retain the language attribute provided in the source
     *
     * Add all corrected language attributes to the Europeana Proxy (duplicating the value and assigning the new language attribute)
     *
     * @param bean
     * @throws TranslationException
     */
    public FullBean detectLanguageForProxy(FullBean bean, String authToken) throws TranslationException, TranslationApiException {
        long start = System.currentTimeMillis();
        List<Proxy> proxies = new ArrayList<>(bean.getProxies()); // make sure we clone first so we can edit the list to our needs.

        // Data/santity check
        if (proxies.size() < 2) {
            LOG.error("Unexpected data - expected at least 2 proxies, but found only {}!", proxies.size());
            return bean;
        }
        String langHint = getHintForLanguageDetect(bean, false);

        // remove europeana proxy from the list
        Proxy europeanaProxy = BaseService.getEuropeanaProxy(proxies, bean.getAbout());
        proxies.remove(europeanaProxy);

        // 1. gather all the "def" values for the whitelisted fields
        for (Proxy proxy : proxies) {
            List<LanguageValueFieldMap> langValueFieldMapForDetection = new ArrayList<>();

            ReflectionUtils.doWithFields(proxy.getClass(), field -> {
                LanguageValueFieldMap fieldValuesLanguageMap = getProxyFieldsValues(proxy, field, bean);
                if (fieldValuesLanguageMap != null) {
                    langValueFieldMapForDetection.add(fieldValuesLanguageMap);
                }

            }, BaseService.proxyFieldFilter);

            if (!langValueFieldMapForDetection.isEmpty()) {
                LOG.debug("For record {} gathered {} fields non-language tagged values for detection. ", bean.getAbout(), langValueFieldMapForDetection.size());
                detectLanguageAndUpdate(langValueFieldMapForDetection, bean, langHint, false, start, authToken);
            }
        }
        return bean;
    }

    private <T extends IdBean> void detectLanguageAndUpdate(List<LanguageValueFieldMap> langValueFieldMapForDetection, T bean,
                                                            String langHint, boolean searchResults, long start, String authToken) throws TranslationException, TranslationApiException {
        Map<String, Integer> textsPerField = new LinkedHashMap<>(); // to maintain the order of the fields
        List<String> textsForDetection = new ArrayList<>();

        // 3. collect all the values in one list for single lang-detection request per proxy/brief bean
        LanguageDetectionUtils.getTextsForDetectionRequest(textsForDetection, textsPerField, langValueFieldMapForDetection);

        LOG.debug("Gathering detection values for record {} took {} ms ", searchResults ? bean.getId() : ((FullBean)bean).getAbout(),
                (System.currentTimeMillis() - start));

        // 4. send lang-detect request
        List<String> detectedLanguages = getTranslationApiClient().detectLang(createLangDetectRequest(textsForDetection, langHint), authToken).getLangs();
        LOG.debug("Detected languages - {} ", detectedLanguages);

        // if only nulls , nothing is detected. no need to process further.
        if (!LanguageDetectionUtils.onlyNulls(detectedLanguages)) {
            // 5. assign language attributes to the values. This map may contain "def" tag values.
            // As for the unidentified languages or unacceptable threshold values the service returns null
            // and the source value is retained which is "def" in our case
            List<LanguageValueFieldMap> correctLangValueMap = LanguageDetectionUtils.getLangDetectedFieldValueMap(textsPerField, detectedLanguages, textsForDetection);

            // 6. add all the new language tagged values to europeana proxy/ brief bean results
            if (searchResults) {
                updateObject(bean, correctLangValueMap);
                } else {
                Proxy europeanProxy = BaseService.getEuropeanaProxy(((FullBean)bean).getProxies(), ((FullBean)bean).getAbout());
                updateObject(europeanProxy, correctLangValueMap);
            }
            LOG.debug("Language detection for {} took {} ms", searchResults ? bean.getId() : ((FullBean)bean).getAbout(),
                    (System.currentTimeMillis() - start));
        }
    }

    private LanguageValueFieldMap getProxyFieldsValues(Proxy proxy, Field field, FullBean bean) {
        HashMap<String, List<String>> origFieldData = (HashMap<String, List<String>>) BaseService.getValueOfTheField(proxy, false).apply(field.getName());
        return LanguageDetectionUtils.getValueFromLanguageMap(SerializationUtils.clone(origFieldData), field.getName(), bean, false);
    }


    /**
     * Return the LanguageValueFieldMap only if non-lang tagged values are present
     * For search results - only literals, references such as URLs or URIs must be ignored
     * @param bean
     * @param field
     * @return
     */
    private LanguageValueFieldMap getLiteralsForSearchFields(BriefBean bean, Field field) {
        HashMap<String, List<String>> origFieldData = (HashMap<String, List<String>>) BaseService.getValueOfTheField(bean, false).apply(field.getName());
        // Map keys we get from Solr have compound names with a dot (e.g. {proxy_dc_title.ro=[Happy-end]})
        // The EdmUtils.cloneMap functionality makes sure that is transformed into something we can use (e.g. def)
        Map<String, List<String>> fieldData = EdmUtils.cloneMap( origFieldData);
        // only gather values if there is no language tags present (Only "def" values are present)
        if (fieldData != null && !fieldData.isEmpty() && !LanguageDetectionUtils.mapHasOtherLanguagesThanDef(fieldData.keySet())) {
            return LanguageDetectionUtils.getValueFromLanguageMap(fieldData, field.getName(), bean, true);
        }
        return null;
    }


    private LangDetectRequest createLangDetectRequest(List<String> textsForDetection, String langHint) {
        LangDetectRequest langDetectRequest = new LangDetectRequest();
        langDetectRequest.setText(textsForDetection);
        langDetectRequest.setLang(langHint);
        return langDetectRequest;
    }

    /**
     * Updates the proxy / Brief Bean object field values by adding the new map values
     *
     * NOTE For search results Beans - the map key values contains solr field names
     *             like - {proxy_dc_creator.def=[http://data.europeana.eu/agent/test, happy]}
     *             this method will create the map without solr field name - {en=[happy], proxy_dc_creator.def=[http://data.europeana.eu/agent/test, happy]}
     *             For BriefBeans results EdmUtils takes care of the maps with solr fields {@link EdmUtils#cloneMap(Map)}
     * NOTE : Only add language tagged values.
     * @param object - Object to be updated
     * @param correctLangMap
     */
    private void updateObject(Object object, List<LanguageValueFieldMap> correctLangMap) {
        correctLangMap.stream().forEach(value -> {
            Map<String, List<String>> map = BaseService.getValueOfTheField(object, true).apply(value.getFieldName());
            // Now add the new lang-value map in the proxy
            for (Map.Entry<String, List<String>> entry : value.entrySet()) {
                if (!StringUtils.equals(entry.getKey(), Language.DEF)) {
                    if (map.containsKey(entry.getKey())) {
                        map.get(entry.getKey()).addAll(entry.getValue());
                    } else {
                        map.put(entry.getKey(), entry.getValue());
                    }
                }
            }
        });
    }
}
