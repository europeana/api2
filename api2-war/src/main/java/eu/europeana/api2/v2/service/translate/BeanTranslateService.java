package eu.europeana.api2.v2.service.translate;

import eu.europeana.api2.v2.model.translate.Language;
import eu.europeana.corelib.definitions.edm.beans.FullBean;
import eu.europeana.corelib.definitions.edm.entity.ContextualClass;
import eu.europeana.corelib.definitions.edm.entity.Proxy;
import eu.europeana.corelib.solr.entity.ProxyImpl;
import eu.europeana.corelib.utils.EuropeanaUriUtils;
import eu.europeana.corelib.web.exception.EuropeanaException;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.*;

/**
 *  Service that provides a translation of various proxy fields if the field doesn't already have a value in the
 *  requested language.
 *
 * @author P. Ehlert
 * Created June - August 2021
 */
@Service
@Import(GoogleTranslationService.class)
public class BeanTranslateService {

    private static final Logger LOG = LogManager.getLogger(BeanTranslateService.class);

    // TODO check if we should also include dcIdentifier to the exclude list
    private static final Set<String> EXCLUDE_PROXY_MAP_FIELDS = Set.of("dcLanguage", "year", "userTags", "edmRights");
    private static final List<String> ENTITIES = List.of("agents", "concepts", "places", "timespans");

    private final TranslationService translationService;


    /**
     * Create a new service for translating proxy fields in a particular language
     *
     * @param translationService underlying translation service to use for translations
     */
    public BeanTranslateService(TranslationService translationService) {
        this.translationService = translationService;
    }

    /**
     * Returns the default language list of the edm:languages
     * Only returns the supported official languages,See: {@link Language}
     * Default translation and filtering for non-official language
     * is not supported
     *
     * @param bean
     * @return
     */
    public List<Language> getDefaultTranslationLanguage(FullBean bean) {
        List<Language> lang = new ArrayList<>();
        Map<String,List<String>> edmLanguage = bean.getEuropeanaAggregation().getEdmLanguage();
        for (Map.Entry<String, List<String>> entry : edmLanguage.entrySet()) {
            for (String languageAbbreviation : entry.getValue()) {
                if (Language.isSupported(languageAbbreviation)) {
                   lang.add(Language.valueOf(languageAbbreviation.trim().toUpperCase(Locale.ROOT)));
                } else {
                    LOG.warn("edm:language '{}' is not supported for default translation and filtering ", languageAbbreviation);
                }
            }
        }
        if (!lang.isEmpty()) {
            LOG.debug("Default translation and filtering applied for language : {} ", lang);
        }
        return lang;

    }

    /**
     * Add a translation of various proxy fields to the provided record, but only if that field does not already have
     * values in the requested language
     *
     * @param bean        the record to be modified
     * @param targetLangs the requested languages (only first language provided is used)
     * @return modified record
     * @throws EuropeanaException when there is a problem sending/retrieving data from the translation service
     */
    public FullBean translateProxyFields(FullBean bean, List<Language> targetLangs) throws EuropeanaException {
        long startTime = System.currentTimeMillis();
        // For the time being we only translate into the first language in the list. Any other provided language in the
        // list is used for filtering only
        String targetLang = targetLangs.get(0).name().toLowerCase(Locale.ROOT);

        // gather all translations
        TranslationsMap textsToTranslate = new TranslationsMap(getProxyFieldsToTranslate(bean, targetLang));
        if (LOG.isDebugEnabled()) {
            LOG.debug("Translate - Gathering data took {} ms", (System.currentTimeMillis() - startTime));
        }

        FieldValuesLanguageMap translations = textsToTranslate.translate(translationService, targetLang);

        // add translations to Europeana proxy
        long startTimeOutput = System.currentTimeMillis();
        for (Map.Entry<String, List<String>> entry : translations.entrySet()) {
            generateTranslatedField(bean, entry.getKey(), entry.getValue(), targetLang);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Translate - Generating output took {} ms", (System.currentTimeMillis() - startTimeOutput));
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Translate - Total time {} ms", (System.currentTimeMillis() - startTime));
        }
        return bean;
    }

    /**
     * Iterate over all the proxies fields and return a TranslationMap for each field that has data we want to translate
     */
    private List<FieldValuesLanguageMap> getProxyFieldsToTranslate(FullBean bean, String targetLang) {
        List<FieldValuesLanguageMap> result = new ArrayList<>();
        List<Proxy> proxies = new ArrayList<>(bean.getProxies()); // make sure we clone first so we can edit the list to our needs.

        // Data/santity check
        if (proxies.size() < 2) {
            LOG.error("Unexpected data - expected at least 2 proxies, but found only {}!", proxies.size());
            return result;
        }
        // No need to look at the Europeana proxy, so we remove that from the list (for now, we may need to include it
        // later, if the way we do translations changes).
        Proxy europeanaProxy = proxies.remove(0);
        if (!europeanaProxy.isEuropeanaProxy()) {
            LOG.error("Unexpected data - first proxy is not Europeana proxy!");
            return result;
        }

        Proxy mainProxy = proxies.get(0);
        boolean moreThanOneProxy = proxies.size() > 1;
        ReflectionUtils.FieldFilter proxyFieldFilter = field -> field.getType().isAssignableFrom(Map.class) &&
                !EXCLUDE_PROXY_MAP_FIELDS.contains(field.getName());

        ReflectionUtils.doWithFields(mainProxy.getClass(), field -> {
            boolean hasStaticTranslation = moreThanOneProxy && hasStaticTranslations(mainProxy, field);
            LOG.trace("Processing field {}, hasStaticTranslation {}...", field.getName(), hasStaticTranslation);
            List<FieldValuesLanguageMap> toTranslate = getProxyFieldToTranslate(proxies, field, hasStaticTranslation, targetLang);

            if (!toTranslate.isEmpty()) {
                for (FieldValuesLanguageMap map : toTranslate) {
                    result.addAll(checkValuesForUris(bean, map, hasStaticTranslation, targetLang));
                }
            }
        }, proxyFieldFilter);
        return result;
    }

    /**
     * If the aggregator proxy has language values other than "def" for the requested field, then we
     * can assume there's already a translation present.
     */
    boolean hasStaticTranslations(Proxy aggregatorProxy, Field field) {
        ReflectionUtils.makeAccessible(field);
        Object o = ReflectionUtils.getField(field, aggregatorProxy);
        if (o instanceof Map) {
            Map map = (Map) o;
            // if there are more than 1 keys, then surely one of them is not def
            return map.keySet().size() > 1 || (map.keySet().size() == 1 && !Language.DEF.equals(map.get(0)));
        }
        return false;
    }

    /**
     * Check all proxies if they have the requested field and if so have a value in the target language or in any of
     * the "fallback" languages.
     * Also, for target-lang and 'en' we also check for uri values in def.
     * If present, uri values are also sent for translations
     * If there is a static translation, we don't have to check for def or other values (step 3 and 4)
     * Note that the returned translationMap contains data for only 1 field
     */
    private List<FieldValuesLanguageMap> getProxyFieldToTranslate(List<Proxy> proxies, Field field, boolean hasStaticTranslations, String targetLang) {
        // 1. Check if we have targetLang value
        List<FieldValuesLanguageMap> result = getProxyValueForLang(proxies, field, targetLang, true);
        if (!result.isEmpty()) {
            // check if def tag is found (uri values) and send that for translations alone.
            if (result.size() > 1 && result.get(1).getSourceLanguage().equals(Language.DEF)) {
                LOG.debug("  Found uri value in def for {},translation needed", field.getName());
                return Collections.singletonList(result.get(1));
            } else {
                LOG.debug("  Found value with target language for {}, no translation needed", field.getName());
                return Collections.emptyList();
            }
        }

        // 2. If targetLang is not English, check if there's an English translation
        if (!Language.ENGLISH.equals(targetLang)) {
            result = getProxyValueForLang(proxies, field, Language.ENGLISH, true);
        }
        if (result.isEmpty() && !hasStaticTranslations) {
            // 3. Check if there is a default value
            result = getProxyValueForLang(proxies, field, Language.DEF, false);
            if (result.isEmpty()) {
                // 4. Pick any language
                // TODO we could optimize later to use a language we already have for translation
                result = getProxyValueForLang(proxies, field, null, false);
            }
        }

        if (result.isEmpty()) {
            LOG.trace("  Found no values for field {}", field.getName());
        } else {
            LOG.debug("  Found value for field {} with {} language", field.getName(), result.get(0).getSourceLanguage());
        }
        return result;
    }

    /**
     * Checks all proxies to see if there is any proxy that has that field with a value in the requested language
     * If language is null, then it will return the first language available (if any).
     * Note that the returned translationMap contains data for only 1 field
     */
    private List<FieldValuesLanguageMap> getProxyValueForLang(List<Proxy> proxies, Field field, String lang, boolean checkForUriInDef) {
        List<FieldValuesLanguageMap> result = new ArrayList<>();
        for (Proxy proxy : proxies) {
            ReflectionUtils.makeAccessible(field);
            Object value = ReflectionUtils.getField(field, proxy);
            if (value instanceof Map) {
                HashMap<String, List<String>> origFieldData = (HashMap<String, List<String>>) value;
                // make sure we make a deep copy of the map so we can modify data later without affecting original record data
                result = getValueFromLanguageMap(SerializationUtils.clone(origFieldData), field.getName(), lang, checkForUriInDef);
            } else if (value != null) {
                LOG.warn("Unexpected data - field {} did not return a map", field.getName());
            }

            if (!result.isEmpty()) {
                break;
            }
        }
        return result;
    }

    private List<FieldValuesLanguageMap> getValueFromLanguageMap(Map<String, List<String>> map, String fieldName, String lang, boolean checkForUriInDef) {
        if (lang == null && !map.keySet().isEmpty()) {
            // return any value if available, but only if it's a supported language
            for (String key : map.keySet()) {
                if (Language.isSupported(key)) {
                    return Collections.singletonList(new FieldValuesLanguageMap(key, fieldName, map.get(key)));
                } else {
                    LOG.debug("  Found value for field {} in unsupported language {}", fieldName, key);
                }
            }
        } else if (lang != null && map.containsKey(lang)) {
            return getValuesFromLanguageMap(map, fieldName, lang, checkForUriInDef);
        }
        return Collections.emptyList();
    }

    /**
     * Returns the values for the language. Also checks if checkForUriInDef is true,
     * adds the def values for translations (only if they are uri's).
     * hence, the list of two FieldValuesLanguageMap are returned :
     * one for language and one for def (if checkForUriInDef is true)
     *
     * Note: that the returned translationMap contains data for only 1 field
     * @param map
     * @param fieldName
     * @param lang language for which value is fetched
     * @param checkForUriInDef true, if we want to check the def values too
     * @return
     */
    private List<FieldValuesLanguageMap> getValuesFromLanguageMap(Map<String, List<String>> map, String fieldName, String lang, boolean checkForUriInDef) {
        FieldValuesLanguageMap valueMap = new FieldValuesLanguageMap(lang, fieldName, map.get(lang));
        // if checkForUriInDef is true and map contains def tag, add all the uri's present in def
        if (checkForUriInDef && map.containsKey(Language.DEF)) {
            FieldValuesLanguageMap defMapWithUriValues = getUriValuesFromDef(map.get(Language.DEF), fieldName);
            if (defMapWithUriValues != null) {
                LOG.debug("  Found uri for field {} in def tag for language {}", fieldName, lang);
                return Arrays.asList(valueMap, defMapWithUriValues);
            }
        }
        // otherwise, return value for 1 particular language
        return Collections.singletonList(valueMap);
    }

    /**
     * Returns the uri values from def
     * @param valuesToCheck list of values for def
     * @param fieldName
     * @return
     */
    private FieldValuesLanguageMap getUriValuesFromDef(List<String> valuesToCheck, String fieldName) {
        List<String> valuesToTranslate = new ArrayList<>();
        for (String value : valuesToCheck) {
            if (EuropeanaUriUtils.isUriExt(value)) {
                valuesToTranslate.add(value);
            }
        }
        if (!valuesToTranslate.isEmpty()) {
            return new FieldValuesLanguageMap(Language.DEF, fieldName, valuesToTranslate);
        }
        return null;
    }

    /**
     * See if any of the values in the maps are uris. If so, we will try to see if we can find a corresponding entity
     * and replace the uri with the entities preflabel (if available, otherwise just remove the uri).
     */
    private List<FieldValuesLanguageMap> checkValuesForUris(FullBean bean, FieldValuesLanguageMap map, boolean hasStaticTranslation,
                                                            String targetLang) {
        // map should have only 1 field at this point
        if (map.keySet().size() != 1) {
            throw new IllegalArgumentException("Resolving uri's is only supported for maps with 1 key");
        }
        String field = map.keySet().iterator().next();
        List<String> valuesToCheck = map.get(field);

        List<String> urisToRemove = new ArrayList<>();
        List<FieldValuesLanguageMap> prefLabelsToTranslate = new ArrayList<>();
        for (String value : valuesToCheck) {
            if (EuropeanaUriUtils.isUriExt(value)) {
                urisToRemove.add(value);
                if (hasStaticTranslation) {
                    // we assume uris are resolved as part of static translation, no need to find entity preflabels
                    LOG.debug("Removing uri {}...", value);
                    continue;
                } else {
                    LOG.debug("Checking uri {}...", value);
                }

                // find entity
                ContextualClass entity = findEntity(bean, value);
                if (entity == null) {
                    LOG.debug("  No entity found");
                    continue;
                }

                // find entity preflabel
                FieldValuesLanguageMap prefLabel = getEntityPrefLabel(entity, field, targetLang);
                if (prefLabel == null) {
                    LOG.debug("  Entity found, but no preflabel to translate");
                } else {
                    LOG.debug("  Entity found, adding prefLabel with {} language to translate", prefLabel.getSourceLanguage());
                    prefLabelsToTranslate.add(prefLabel);
                }
            }
        }

        // delete all uris
        map.remove(field, urisToRemove);

        // gather final results
        List<FieldValuesLanguageMap> result = new ArrayList<>();
        for (FieldValuesLanguageMap prefLabelMap : prefLabelsToTranslate) {
            // if any of the prefLabel maps have the same source language as the original we merge it into the original map
            if (prefLabelMap.getSourceLanguage().equals(map.getSourceLanguage())) {
                map.merge(prefLabelMap);
            } else {
                result.add(prefLabelMap);
            }
        }
        List<String> originalValues = map.get(field);
        if (!originalValues.isEmpty()) {
            result.add(0, map); // return also original remaining values in map if it's not empty
        }
        return result;
    }

    /**
     * See if there is an entity with the provided uri
     */
    private ContextualClass findEntity(FullBean bean, String uri) {
        final List<ContextualClass> result = new ArrayList<>();

        ReflectionUtils.FieldFilter entityFilter = field -> ENTITIES.contains(field.getName()) && result.isEmpty();
        ReflectionUtils.doWithFields(bean.getClass(), field -> {
                ReflectionUtils.makeAccessible(field);
                Object o = ReflectionUtils.getField(field, bean);
                LOG.trace("Searching for entities with type {}...", field.getName());
                if (o instanceof List) {
                    List<ContextualClass> entities = (List<ContextualClass>) o;
                    for (ContextualClass entity : entities) {
                        if (StringUtils.equalsIgnoreCase(uri, entity.getAbout())) {
                            LOG.debug("  Found matching entity {}", entity.getAbout());
                            result.add(entity);
                            break;
                        }
                    }
                }
            }, entityFilter);

        if (result.isEmpty()) {
            return null;
        }
        return result.get(0);
    }

    private FieldValuesLanguageMap getEntityPrefLabel(ContextualClass entity, String fieldName, String targetLang) {
        FieldValuesLanguageMap entityPrefLabel = null;
        Map<String, List<String>> prefLabels = entity.getPrefLabel();
        if (prefLabels == null) {
            LOG.debug("  Entity {} has no prefLabels", entity.getAbout());
            return null;
        }

        // 1. Check if we have targetLang value
        List<FieldValuesLanguageMap> result = getValueFromLanguageMap(prefLabels, entity.getAbout(), targetLang, false);
        if (!result.isEmpty()) {
            LOG.debug("  Found prefLabel with target language for {}, no translation needed", entity.getAbout());
            return null;
        }

        // 2. If targetLang is not English, check if there's an English translation
        if (!Language.ENGLISH.equals(targetLang)) {
            result = getValueFromLanguageMap(prefLabels, entity.getAbout(), Language.ENGLISH, false);
        }
        if (result.isEmpty()) {
            // 3. Check if there is a default value
            result = getValueFromLanguageMap(prefLabels, entity.getAbout(), Language.DEF, false);
            if (result.isEmpty()) {
                // 4. Pick any language
                // TODO we could optimize later to use a language we already have for translation
                result = getValueFromLanguageMap(prefLabels, entity.getAbout(), null, false);
            }
        }

        // TODO should we check if preflabel values are uri!? If we do that we could get into an infinite loop!

        if (result.isEmpty()) {
            LOG.debug("  Found no preflabels for {}", entity.getAbout());
        } else {
            // as checkForUriInDef is false, result will only contain one value.
            entityPrefLabel = result.get(0);
            LOG.debug("  Found preflabel with language {} for {} ", entityPrefLabel.getSourceLanguage(), entity.getAbout());
            // we need to replace the original uri (key) with the field name where we found the uri
            entityPrefLabel.put(fieldName, entityPrefLabel.remove(entity.getAbout()));
        }
        return entityPrefLabel;
    }

    private void generateTranslatedField(FullBean bean, String key, List<String> values, String lang) {
        Proxy proxy = bean.getProxies().get(0);
        if (proxy == null || !proxy.isEuropeanaProxy()) {
            LOG.error("First proxy of record {} is not an EuropeanaProxy!", bean.getAbout());
            return;
        }

        Field field = ReflectionUtils.findField(ProxyImpl.class, key);
        if (field == null) {
            LOG.error("Cannot find field with name {}", key);
        } else {
            ReflectionUtils.makeAccessible(field);
            Object o = ReflectionUtils.getField(field, proxy);
            if (o instanceof Map) {
                Map<String, List<String>> map = (Map<String, List<String>>) o;
                if (map.containsKey(lang)) {
                    // should not happen!
                    LOG.error("Europeana proxy already has values for field {} and language {}!", key, lang);
                } else {
                    map.put(lang, values);
                }
            } else {
                Map<String, List<String>> newMap = new LinkedHashMap<>();
                newMap.put(lang, values);
                ReflectionUtils.setField(field, proxy, newMap);
            }
        }
    }

}
