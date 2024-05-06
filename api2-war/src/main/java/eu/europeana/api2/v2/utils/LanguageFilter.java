package eu.europeana.api2.v2.utils;

import eu.europeana.api.translation.definitions.language.Language;
import eu.europeana.api2.v2.service.translate.BaseService;
import eu.europeana.corelib.definitions.edm.beans.IdBean;
import eu.europeana.corelib.definitions.edm.entity.EuropeanaAggregation;
import eu.europeana.corelib.solr.entity.ProxyImpl;
import eu.europeana.corelib.utils.EuropeanaUriUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.ReflectionUtils;

import java.util.*;
import org.springframework.util.ReflectionUtils.FieldFilter;

/**
 * Utility class to filter the language-dependent data in a FullBean
 *
 * @author P. Ehlert
 * Created 20 July 2021
 */
public final class LanguageFilter {

    private static final Logger LOG = LogManager.getLogger(LanguageFilter.class);

    private LanguageFilter() {
        // empty constructor to prevent initialization
    }

    /**
     * Filter all the language maps in the provided search result or fullbean. Note that if a languagemap only has 1
     * value nothing is filtered, nor are any "def" values in the map removed.
     * @param bean search result or fullbean to filter
     * @param targetLangs the languages that need to remain in the search result
     * @return filtered search result of fullbean
     */
    public static IdBean filter(IdBean bean, List<Language> targetLangs) {
        long startTime = System.currentTimeMillis();
        Set<String> proxyFieldsWithTargetLang = new TreeSet<>();
        iterativeFilterFields(bean, targetLangs, proxyFieldsWithTargetLang);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Filtering language data took {} ms", (System.currentTimeMillis() - startTime));
        }
        proxyFieldsWithTargetLang.clear();
        return bean;
    }

    /**
     * We can iterate over all the getDeclaredFields() in an object or use the getMethods(), but both approaches have
     * pros and cons. We implemented both, but kept only the getDeclaredFields() approach because that's faster.
     * -
     * For the getDeclaredFields() approach the main downside is:
     * 1) since all/most fields in FullBean are protected we first used Field.setAccessible() to access them. This
     * raised a warning that this may not be allowed in JDKs newer than version 11 (see also sonarqube warning java:S3011).
     * Later we switched to using ReflectionUtils.makeAccessible() which doesn't trigger a warning, but the question
     * is still if this is allowed in future JDKs as that still uses Field.setAccessible under the hood
     * -
     * For getMethods() the downsides are:
     * 1) only works if the getters return the real objects and not if they return a copy (or else we need to
     * also invoke the getters)
     * 2) it is a bit slower than the getDeclaredFields approach
     */
    @SuppressWarnings("java:S3011") // suppress the setAccessibility(true) or ReflectionUtils.makeAccessible warning
    private static void iterativeFilterFields(Object o, List<Language> targetLangs,  Set<String> proxyFieldsWithTargetLang) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Inspecting object {}", o.getClass().getName());
        }
        // we only want to look at fields that are language maps or can contain them
        FieldFilter fieldFilter = getFieldFilter();
        ReflectionUtils.doWithFields(o.getClass(), field -> {
            ReflectionUtils.makeAccessible(field); // this is needed to access protected fields, may not be allowed in JDKs newer than 11!
            Object fieldValue = field.get(o);
            if (LOG.isDebugEnabled()) {
                LOG.debug("  Field {} has class {}", field.getName(), fieldValue == null ? null : fieldValue.getClass());
            }
            if (fieldValue instanceof Map<?, ?>) {
                 filterLanguageMap(field.getName(), (Map<?, ?>) fieldValue, targetLangs,
                    filterOnlyTargetLanguage(o, proxyFieldsWithTargetLang, field.getName()));

                // if object is proxy and we have already gathered the fieldValue in target language then add it in the proxyFieldsWithTargetLang set
                if (o.getClass().isAssignableFrom(ProxyImpl.class)
                    && ifFieldHasTargetLangValue((Map<?, ?>) fieldValue, targetLangs)) {
                    proxyFieldsWithTargetLang.add(field.getName());
                }
            } else if (fieldValue instanceof List) {
                for (Object item : ((List<?>) fieldValue)) {
                    iterativeFilterFields(item, targetLangs, proxyFieldsWithTargetLang);
                }
            } else if (fieldValue instanceof EuropeanaAggregation) {
                iterativeFilterFields(fieldValue, targetLangs, proxyFieldsWithTargetLang);
            } else {
                assert fieldValue == null : "Unknown field class " + fieldValue.getClass() + ". Checks do not match field filter";
            }
        }, fieldFilter);
    }

    @NotNull
    private static ReflectionUtils.FieldFilter getFieldFilter() {
         return (field ->
                (field.getType().isAssignableFrom(Map.class) ||
                        field.getType().isAssignableFrom(List.class) ||
                        field.getType().isAssignableFrom(EuropeanaAggregation.class)) &&
                !"fieldMap".equals(field.getName())); // search resuls have a special field called fieldMap (see IdBeanImpl.getFields())

    }


    /**
     * If proxyFieldsWithTargetLang contains teh field name (this means it already has found the value in target language)
     * then for the field in other proxies we should only fetch the target language values if present.
     * We should not do the default filtering like retaining def values or displaying everything.
     *
     * @param o Object being filtered
     * @param proxyFieldsWithTargetLang set of values with field names which already have been filtered with target lang
     * @param fieldName field name
     * @return boolean value
     */
    private static boolean filterOnlyTargetLanguage(Object o, Set<String> proxyFieldsWithTargetLang, String fieldName) {
        return  o.getClass().isAssignableFrom(ProxyImpl.class) && proxyFieldsWithTargetLang.contains(fieldName);
    }

    /**
     * If the field value after filtering has any of the target language value then return true
     * @param map multilingual map after filtering
     * @param targetLangs target languages for filtering
     * @return boolean value
     */
    private static boolean ifFieldHasTargetLangValue(Map<?,?> map, List<Language> targetLangs) {
        Set<String> keyset = (Set<String>) map.keySet();
        for (Language language : targetLangs) {
             if (keyset.contains(language.name().toLowerCase(Locale.ROOT))) {
                 return  true; // if any of the target language found return true
             }
        }
        return false;
    }

    /**
     * Filter the map for the traget language values
     *  Default filtering - we keep the def and uri values
     *                    - remove the invalid languages that are not supported
     *                    -  keep the 'zxx' values as well
     *                    - if value in target language is found keep that as well
     *-
     * After this if no value is returned (after filtering) for a given property, in that case,
     * all language tagged values should be returned (EA-3727)
     *-
     *  But for Proxy object we need to consider them as one object hence -
     *    if filterOnlyTargetLanguage is true - only filter the target language values and remove everything else.
     *-
     * NOTE - filterOnlyTargetLanguage is true - this means that the field has already been filtered with the target lang in one of the proxy
     *
     * @param fieldName field name
     * @param map original map for the field
     * @param targetLangs target language for filtering
     * @param filterOnlyTargetLanguage if true, only look for the target language values and don't perform the default filtering.
     */
    private static void filterLanguageMap(String fieldName, Map<?, ?> map,
        List<Language> targetLangs, boolean filterOnlyTargetLanguage) {
        LOG.debug("    Map {} has {} keys and {} values", fieldName, map.keySet().size(),
            map.values().size());
        List<String> keysToRemove = new ArrayList<>();

        for (Map.Entry<?, ?> keyValue : map.entrySet()) {
            String origKey = keyValue.getKey().toString();
            // Language keys of search results are compound and exist of <solrFieldName>.<lang>, so we need to filter
            // the language from the key name
            String keyLang = origKey.substring(origKey.indexOf(".") + 1);
            if (filterOnlyTargetLanguage && (!Language.isSupported(keyLang) || !targetLangs.contains(Language.getLanguage(keyLang)))) {
                // 'zxx' and 'def' will be removed as they are non supported languages
                LOG.debug("     Proxy field {} is already filtered for target lang. Removing key {}, value {}",
                    fieldName, origKey, keyValue.getValue());
                keysToRemove.add(keyLang);
            } else {
                // keep all def keys and keep all uri values
                if ("def".equals(keyLang) || EuropeanaUriUtils.isUri(origKey)) {
                    LOG.debug("      Keeping key def, value {}", keyValue.getValue());
                    continue;
                }
                // remove all unsupported languages and languages not requested
                if (Language.isNoLinguisticContent(keyLang) || (Language.isSupported(keyLang) && targetLangs.contains(Language.getLanguage(keyLang)))) {
                    LOG.debug("      Keeping key {}, value {}", origKey, keyValue.getValue());
                } else {
                    LOG.debug("      Removing key {}, value {}", origKey, keyValue.getValue());
                    keysToRemove.add(origKey); // add the original key language for removal
                }
                // If no value is returned (after filtering) for a given property, in which case,
                // all language tagged values should be returned EA-3727
                if (map.keySet().size() == keysToRemove.size()) {
                    LOG.debug("Keys to remove {}. Return all lang-tagged values",keysToRemove.size());
                    return; // everything returned
                }
            }
        }
        // do actual removal
        keysToRemove.forEach(map::remove);
    }


    /**
     * Removes the non lang aware fields.
     * See - {@link BaseService#searchFieldRemovalFilter}
     * @param bean of type IdBean
     */
    public static void removeNonLanguageAwareFields(IdBean bean) {
        ReflectionUtils.doWithFields(bean.getClass(), field -> {
            ReflectionUtils.makeAccessible(field);
            Object fieldValue = field.get(bean);
            if (fieldValue != null) {
                ReflectionUtils.setField(field, bean, null);
            }
            }, BaseService.searchFieldRemovalFilter);
    }

}
