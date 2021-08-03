package eu.europeana.api2.v2.service.translate;

import eu.europeana.api2.v2.model.translate.Language;
import eu.europeana.corelib.definitions.edm.beans.FullBean;
import eu.europeana.corelib.definitions.edm.entity.EuropeanaAggregation;
import eu.europeana.corelib.utils.EuropeanaUriUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Utility class to filter the language dependent data in a FullBean
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
     * @param useReflectiveMethods whether to use the faster but less safe reflection on fields, or slower but safer
     *                            reflection on methods
     * @return filtered fullbean
     */
    public static FullBean filter(FullBean bean, List<Language> targetLangs, boolean useReflectiveMethods) {
        long startTime = System.currentTimeMillis();
        if (useReflectiveMethods) {
            iterativeFilterMethods(bean, targetLangs);
        } else {
            iterativeFilterFields(bean, targetLangs);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Filtering record language data using {} took {} ms", (useReflectiveMethods ? "methods" : "fields"),
                    (System.currentTimeMillis() - startTime));
        }
        return bean;
    }

    /**
     * We can iterate over all the getDeclaredFields() in an object or use the getMethods(), but both methods have pros
     * and cons. For testing and making a decision in the future, we support both for the time being.
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
     * 2) it is much slower than the getDeclaredFields approach
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

    /**
     * Search for methods that return a map
     */
    private static void iterativeFilterMethods(Object obj, List<Language> targetLangs)  {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Filtering - object {}", obj.getClass().getName());
        }
        // we only want to look at fields that are language maps or can contain them
        ReflectionUtils.MethodFilter methodFilter = method -> method.getName().startsWith("get") && (
                method.getReturnType().isAssignableFrom(Map.class) ||
                        method.getReturnType().isAssignableFrom(List.class) ||
                        method.getReturnType().isAssignableFrom(EuropeanaAggregation.class)
        );

        ReflectionUtils.doWithMethods(obj.getClass(), method -> {
            LOG.debug("  Method {} has returnType {}", method.getName(), method.getReturnType());

            Object methodValue = ReflectionUtils.invokeMethod(method, obj);
            if (methodValue instanceof Map<?, ?>) {
                methodValue = filterLanguageMap(method.getName(), (Map<?, ?>) methodValue, targetLangs);
                if (methodValue == null) {
                    deleteMap(obj, method);
                }
            } else if (methodValue instanceof List<?>) {
                List<?> list = (List<?>) ReflectionUtils.invokeMethod(method, obj);
                if (list != null) {
                    for (Object item : list) {
                        iterativeFilterMethods(item, targetLangs);
                    }
                }
            } else if (methodValue instanceof EuropeanaAggregation) {
                iterativeFilterMethods(ReflectionUtils.invokeMethod(method, obj), targetLangs);
            } else {
                assert methodValue == null : "Unknown field class " + methodValue.getClass() + ". Checks do not match method filter";
            }
        }, methodFilter);

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
            if ("def".equals(keyValue.getKey()) || EuropeanaUriUtils.isUri(keyValue.getValue().toString())) {
                LOG.debug("      Keeping key def, value {}", keyValue.getValue());
                continue;
            }
            // remove all unsupported languages and languages not requested
            String keyLang = keyValue.getKey().toString();
            if (!Language.isSupported(keyLang) || !targetLangs.contains(Language.valueOf(keyLang.toUpperCase(Locale.ROOT)))) {
                LOG.debug("      Removing key {}, value {}", keyLang, keyValue.getValue());
                keysToRemove.add(keyLang);
            } else {
                LOG.debug("      Keeping key {}, value {}", keyLang, keyValue.getValue());
            }
        }
        // do actual removal
        if (map.keySet().size() == keysToRemove.size()) {
            return null; // note that the map still has to be deleted!
        }
        for (String keyToRemove : keysToRemove) {
            map.remove(keyToRemove);
        }

        return map;
    }


    private static void deleteMap(Object obj, Method method) {
        String setterMethodName = method.getName().replace("get", "set");
        Method setter = ReflectionUtils.findMethod(method.getDeclaringClass(), setterMethodName, Map.class);
        if (setter == null) {
            LOG.error("Unable to delete map. Setter method {} not found in class {}", setterMethodName,
                    method.getDeclaringClass());
        } else {
            LOG.debug("    Deleting map {} entirely", method.getName());
            try {
                Object arg = null; // if we set null directly in the invokeMethod() below, an error is thrown!
                ReflectionUtils.invokeMethod(setter, obj, arg);
            } catch (IllegalArgumentException e) {
                LOG.error("Unable to delete map. Unexpected number of arguments for method {}", setter, e);
            }
        }
    }

}