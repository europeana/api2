package eu.europeana.api2.v2.service.translate;

import eu.europeana.api2.v2.model.translate.Language;
import eu.europeana.corelib.definitions.edm.beans.FullBean;
import eu.europeana.corelib.definitions.edm.entity.EuropeanaAggregation;
import eu.europeana.corelib.definitions.edm.entity.Proxy;
import eu.europeana.corelib.utils.EuropeanaUriUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import java.util.*;

/**
 *  Service that provides a translation of the title and description fields if the requested language is not already
 *  present in the CHO. Eventually we'll also filter out non-requested languages (see EA-2627 that will be implemented soon)
 */
@Service
@Import(GoogleTranslationService.class)
public class TranslateFilterService {

    private static final Logger LOG = LogManager.getLogger(TranslateFilterService.class);

    private static final String ENGLISH = "en";
    private static final String DEF = "def";

    private TranslationService translationService;

    /**
     * Create a new service for translating title and description in a particular language and filtering out other
     *
     * @param translationService underlying translation service to use for translations
     */
    public TranslateFilterService(TranslationService translationService) {
        this.translationService = translationService;
    }

    /**
     * Add a translation of the dcTitle and dcDescription to a record, if it does not already have this in the
     * requested language
     *
     * @param bean        the record to be used
     * @param targetLangs the requested languages
     * @return modified record
     */
    public FullBean translateTitleDescription(FullBean bean, List<Language> targetLangs) {
        // TODO for now we only translate into the first language in the list, the rest is only used for filtering
        translateTitle(bean, targetLangs.get(0).name().toLowerCase(Locale.ROOT));
        translateDescription(bean, targetLangs.get(0).name().toLowerCase(Locale.ROOT));
        return bean;
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
    public FullBean filter(FullBean bean, List<Language> targetLangs, boolean useReflectiveMethods) {
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
     * We can iterate over all the getDeclaredFields() in an object or use the getMethods(), but both methods have pros and
     * cons. For testing and making a decision in the future, we support both for the time being-
     *
     * For the getDeclaredFields() approach the main downside is:
     * 1) since all/most fields in FullBean are protected we need to use makeAccessible() to access them and this may
     * not be allowed in JDKs newer than version 11 (see also sonarqube warning java:S3011)
     *
     * For getMethods() the downsides are:
     * 1) only works if the getters return the real objects and not if they return a copy (or else we need to
     * also invoke the getters)
     * 2) it is much slower than the getDeclaredFields approach
     */
    @SuppressWarnings("java:S3011") // suppress the setAccessibility(true) or ReflectionUtils.makeAccessible warning
    private void iterativeFilterFields(Object o, List<Language> targetLangs) {
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
                filterLanguageMap(field.getName(), (Map<?, ?>) fieldValue, targetLangs);
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
    private void iterativeFilterMethods(Object obj, List<Language> targetLangs)  {
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
                filterLanguageMap(method.getName(), (Map<?, ?>) methodValue, targetLangs);
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

    private void filterLanguageMap(String fieldName, Map<?,?> map, List<Language> targetLangs) {
        if (map == null) {
            return;
        }
        LOG.debug("    Map {} has {} keys and {} values", fieldName, map.keySet().size(), map.values().size());
        // if there's only 1 key in the map we do not filter
        if (map.keySet().size() > 1) {
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
                // we should not remove all keys in a map, so if we are about to do that we keep only the first
                String keyToKeep = keysToRemove.remove(0);
                LOG.debug("      All keys are about to be filtered, keeping only the first key {}", keyToKeep);
            }
            for (String keyToRemove : keysToRemove) {
                map.remove(keyToRemove);
            }
        }
    }

    /**
     * Logic is a follows:
     * <pre>
     * 1.    iterate over all proxies to try and find title in requested language
     * 2.1     if no title in requested language was found AND target language at step 1 was not English, try to find English title
     * 2.3.    if there's no English title, try to find def title (or else just any title)
     * 2.3.1     if there is no title whatsoever, do nothing
     * 2.3.2     if there is some title, get translation and add to record
     * 2.4.    if there is an English title, get translation and add to record
     * 3.    if there's a title for the requested language, do nothing
     * </pre>
     */
    private void translateTitle(FullBean bean, String targetLang) {
        List<String> title = getTitleForLang(bean, targetLang);
        if (title == null) {
            if (!ENGLISH.equals(targetLang.toLowerCase(Locale.ROOT))) {
                LOG.debug("No title found for record {} in lang {}, searching for English...", bean.getAbout(), targetLang);
                title = getTitleForLang(bean, ENGLISH);
            }
            if (title == null) {
                LOG.debug("No English title found for record {}, searching for any title...", bean.getAbout());
                title = getDefOrFirstTitle(bean);
                if (title == null) {
                    LOG.warn("No title in any language found for record {}", bean.getAbout());
                    // do nothing
                } else {
                    List<String> translation = translationService.translate(title, targetLang);
                    addTitleTranslation(bean, translation, targetLang);
                    LOG.debug("Found a title for record {}, translation added", bean.getAbout());
                }
            } else {
                addTitleTranslation(bean, translationService.translate(title, targetLang, ENGLISH), targetLang);
                LOG.debug("Found English title found for record {}, translation added", bean.getAbout());
            }
        } else {
            LOG.debug("Found title for record {} in lang {}", bean.getAbout(), targetLang);
            // do nothing
        }
    }

    private List<String> getTitleForLang(FullBean bean, String lang) {
        List<String> result = null;
        for (Proxy p : bean.getProxies()) {
            if (p.getDcTitle() != null && p.getDcTitle().containsKey(lang)) {
                result = p.getDcTitle().get(lang);
                break;
            }
        }
        return result;
    }

    private List<String> getDefOrFirstTitle(FullBean bean) {
        List<String> defValue = null;
        List<String> firstValue = null;
        for (Proxy p : bean.getProxies()) {
            if (p.getDcTitle() != null) {
                defValue = p.getDcTitle().get(DEF);
                if (defValue != null) {
                    break;
                } else if (firstValue == null && !p.getDcTitle().isEmpty()) {
                    // set any found value, in case we find nothing in other proxies
                    firstValue = p.getDcTitle().entrySet().iterator().next().getValue();
                }
            }
        }
        if (defValue == null) {
            return firstValue;
        }
        return defValue;
    }

    private void addTitleTranslation(FullBean bean, List<String> translation, String targetLang) {
        // we assume the first proxy is always the Europeana proxy, so we add translations there
        Proxy p = bean.getProxies().get(0);
        if (p == null || !p.isEuropeanaProxy()) {
            LOG.error("First proxy of record {} is not an EuropeanaProxy!", bean.getAbout());
        } else {
            if (p.getDcTitle() == null) {
                p.setDcTitle(new LinkedHashMap<>());
            }
            p.getDcTitle().put(targetLang, translation);
        }
    }

    private void translateDescription(FullBean bean, String targetLang) {
        List<String> description = getDescriptionForLang(bean, targetLang);
        if (description == null) {
            if (!ENGLISH.equals(targetLang.toLowerCase(Locale.ROOT))) {
                LOG.debug("No description found for record {} in lang {}, searching for English...", bean.getAbout(), targetLang);
                description = getDescriptionForLang(bean, ENGLISH);
            }
            if (description == null) {
                LOG.warn("No English description found for record {}, searching for any description...", bean.getAbout());
                description = getDefOrFirstDescription(bean);
                if (description == null) {
                    LOG.debug("No description in any language found for record {}", bean.getAbout());
                    // do nothing
                } else {
                    List<String> translation = translationService.translate(description, targetLang);
                    addDescriptionTranslation(bean, translation, targetLang);
                    LOG.debug("Found a description for record {}, translation added", bean.getAbout());
                }
            } else {
                addDescriptionTranslation(bean, translationService.translate(description, targetLang, ENGLISH), targetLang);
                LOG.debug("Found English description found for record {}, translation added", bean.getAbout());
            }
        } else {
            LOG.debug("Found description for record {} in lang {}", bean.getAbout(), targetLang);
            // do nothing
        }
    }

    private List<String> getDescriptionForLang(FullBean bean, String lang) {
        List<String> result = null;
        for (Proxy p : bean.getProxies()) {
            if (p.getDcDescription() != null && p.getDcDescription().containsKey(lang)) {
                result = p.getDcDescription().get(lang);
                break;
            }
        }
        return result;
    }

    private List<String>getDefOrFirstDescription(FullBean bean) {
        List<String> defValue = null;
        List<String> firstValue = null;
        for (Proxy p : bean.getProxies()) {
            if (p.getDcDescription() != null) {
                defValue = p.getDcDescription().get(DEF);
                if (defValue != null) {
                    break;
                } else if (firstValue == null && !p.getDcDescription().isEmpty()) {
                    // set any found value, in case we find nothing in other proxies
                    firstValue = p.getDcDescription().entrySet().iterator().next().getValue();
                }
            }
        }
        if (defValue == null) {
            return firstValue;
        }
        return defValue;
    }

    private void addDescriptionTranslation(FullBean bean, List<String> translation, String targetLang) {
        // we assume the first proxy is always the Europeana proxy, so we add translations there
        Proxy p = bean.getProxies().get(0);
        if (p == null || !p.isEuropeanaProxy()) {
            LOG.error("First proxy of record {} is not an EuropeanaProxy!", bean.getAbout());
        } else {
            if (p.getDcDescription() == null) {
                p.setDcDescription(new LinkedHashMap<>());
            }
            p.getDcDescription().put(targetLang, translation);
        }
    }
}
