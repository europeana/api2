package eu.europeana.api2.v2.utils;

import eu.europeana.api2.v2.model.translate.Language;
import eu.europeana.corelib.definitions.edm.beans.IdBean;
import eu.europeana.corelib.definitions.edm.entity.EuropeanaAggregation;
import eu.europeana.corelib.utils.EuropeanaUriUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.ReflectionUtils;

import java.util.*;

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
        iterativeFilterFields(bean, targetLangs);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Filtering language data took {} ms", (System.currentTimeMillis() - startTime));
        }
        return bean;
    }

    /**
     * We can iterate over all the getDeclaredFields() in an object or use the getMethods(), but both approaches have
     * pros and cons. We implemented both, but kept only the getDeclaredFields() approach because that's faster.
     *
     * For the getDeclaredFields() approach the main downside is:
     * 1) since all/most fields in FullBean are protected we first used Field.setAccessible() to access them. This
     * raised a warning that this may not be allowed in JDKs newer than version 11 (see also sonarqube warning java:S3011).
     * Later we switched to using ReflectionUtils.makeAccessible() which doesn't trigger a warning, but the question
     * is still if this is allowed in future JDKs as that still uses Field.setAccessible under the hood
     *
     * For getMethods() the downsides are:
     * 1) only works if the getters return the real objects and not if they return a copy (or else we need to
     * also invoke the getters)
     * 2) it is a bit slower than the getDeclaredFields approach
     */
    @SuppressWarnings("java:S3011") // suppress the setAccessibility(true) or ReflectionUtils.makeAccessible warning
    private static void iterativeFilterFields(Object o, List<Language> targetLangs) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Inspecting object {}", o.getClass().getName());
        }
        // we only want to look at fields that are language maps or can contain them
        ReflectionUtils.FieldFilter fieldFilter = field ->
                (field.getType().isAssignableFrom(Map.class) ||
                        field.getType().isAssignableFrom(List.class) ||
                        field.getType().isAssignableFrom(EuropeanaAggregation.class)) &&
                !"fieldMap".equals(field.getName()); // search resuls have a special field called fieldMap (see IdBeanImpl.getFields())

        ReflectionUtils.doWithFields(o.getClass(), field -> {
            ReflectionUtils.makeAccessible(field); // this is needed to access protected fields, may not be allowed in JDKs newer than 11!
            Object fieldValue = field.get(o);
            if (LOG.isDebugEnabled()) {
                LOG.debug("  Field {} has class {}", field.getName(), fieldValue == null ? null : fieldValue.getClass());
            }
            if (fieldValue instanceof Map<?, ?>) {
                fieldValue = filterLanguageMap(field.getName(), (Map<?, ?>) fieldValue, targetLangs);
                if (fieldValue == null) {
                    LOG.debug("    Deleting map {} entirely", field.getName());
                    field.set(o, null);
                }
            } else if (fieldValue instanceof List) {
                List<?> list = (List<?>) fieldValue;
                for (Object item : list) {
                    iterativeFilterFields(item, targetLangs);
                }
            } else if (fieldValue instanceof EuropeanaAggregation) {
                iterativeFilterFields(fieldValue, targetLangs);
            } else {
                assert fieldValue == null : "Unknown field class " + fieldValue.getClass() + ". Checks do not match field filter";
            }
        }, fieldFilter);
    }

    private static Map filterLanguageMap(String fieldName, Map<?,?> map, List<Language> targetLangs) {
        if (map == null) {
            return null;
        }

        LOG.debug("    Map {} has {} keys and {} values", fieldName, map.keySet().size(), map.values().size());
        Set<? extends Map.Entry<?,?>> set = map.entrySet();
        List<String> keysToRemove = new ArrayList<>();
        for (Map.Entry<?,?> keyValue : set) {
            String origKey = keyValue.getKey().toString();
            // Language keys of search results are compound and exist of <solrFieldName>.<lang>, so we need to filter
            // the language from the key name
            String keyLang = origKey.substring(origKey.indexOf(".") + 1);

            // keep all def keys and keep all uri values
            if ("def".equals(keyLang) || EuropeanaUriUtils.isUri(origKey)) {
                LOG.debug("      Keeping key def, value {}", keyValue.getValue());
                continue;
            }
            // remove all unsupported languages and languages not requested
            if (Language.isNoLinguisticContent(keyLang) ||
                    (Language.isSupported(keyLang) && targetLangs.contains(Language.getLanguage(keyLang)))) {
                LOG.debug("      Keeping key {}, value {}", origKey, keyValue.getValue());
            } else {
                LOG.debug("      Removing key {}, value {}", origKey, keyValue.getValue());
                keysToRemove.add(origKey); // add the original key language for removal
            }
        }
        // do actual removal
        if (map.keySet().size() == keysToRemove.size()) {
            return null; // everything removed
        }
        for (String keyToRemove : keysToRemove) {
            map.remove(keyToRemove);
        }

        return map;
    }

}
