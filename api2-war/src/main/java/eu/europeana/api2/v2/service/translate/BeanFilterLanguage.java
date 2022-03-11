package eu.europeana.api2.v2.service.translate;

import eu.europeana.api2.v2.model.translate.Language;
import eu.europeana.corelib.definitions.edm.beans.FullBean;
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
public final class BeanFilterLanguage {

    private static final Logger LOG = LogManager.getLogger(BeanFilterLanguage.class);

    private BeanFilterLanguage() {
        // empty constructor to prevent initialization
    }

    /**
     * Filter all the language maps in the provided fullbean. Note that if a languagemap only has 1 value nothing is
     * filtered, nor are any "def" values in the map removed.
     * @param bean the fullbean to filter
     * @param targetLangs the languages that need to remain in the fullbean
     * @return filtered fullbean
     */
    public static FullBean filter(FullBean bean, List<Language> targetLangs) {
        long startTime = System.currentTimeMillis();
        iterativeFilterFields(bean, targetLangs);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Filtering record language data took {} ms", (System.currentTimeMillis() - startTime));
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
            LOG.debug("Filtering - object {}", o.getClass().getName());
        }
        // we only want to look at fields that are language maps or can contain them
        ReflectionUtils.FieldFilter fieldFilter = field ->
                field.getType().isAssignableFrom(Map.class) ||
                        field.getType().isAssignableFrom(List.class) ||
                        field.getType().isAssignableFrom(EuropeanaAggregation.class);

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
            // keep all def keys and keep all uri values
            if ("def".equals(keyValue.getKey()) || EuropeanaUriUtils.isUriExt(keyValue.getValue().toString())) {
                LOG.debug("      Keeping key def, value {}", keyValue.getValue());
                continue;
            }
            // remove all unsupported languages and languages not requested
            String keyLang = keyValue.getKey().toString();
            // fetch the first two ISO letters, and validate that
            String isoLanguageToCheck = TranslationUtils.getISOLanguage(keyLang);
            if (!Language.isSupported(isoLanguageToCheck) || !targetLangs.contains(Language.valueOf(isoLanguageToCheck.toUpperCase(Locale.ROOT)))) {
                LOG.debug("      Removing key {}, value {}", keyLang, keyValue.getValue());
                keysToRemove.add(keyLang); // add the original key language for removal
            } else {
                LOG.debug("      Keeping key {}, value {}", keyLang, keyValue.getValue());
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
