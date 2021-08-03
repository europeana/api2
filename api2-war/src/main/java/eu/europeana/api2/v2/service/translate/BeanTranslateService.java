package eu.europeana.api2.v2.service.translate;

import eu.europeana.api2.v2.model.translate.Language;
import eu.europeana.corelib.definitions.edm.beans.FullBean;
import eu.europeana.corelib.definitions.edm.entity.Proxy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 *  Service that provides a translation of the title and description fields if the requested language is not already
 *  present in the CHO. Eventually we'll also filter out non-requested languages (see EA-2627 that will be implemented soon)
 *
 * @author P. Ehlert
 * Created June 2021
 */
@Service
@Import(GoogleTranslationService.class)
public class BeanTranslateService {

    private static final Logger LOG = LogManager.getLogger(BeanTranslateService.class);

    private static final String KEY_TITLE = "dcTitle";
    private static final String KEY_DESCRIPTION = "dcDescription";

    private final TranslationService translationService;

    /**
     * Create a new service for translating title and description in a particular language and filtering out other
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
     * Add a translation of the dcTitle and dcDescription to a record, if it does not already have this in the
     * requested language
     *
     * @param bean        the record to be used
     * @param targetLangs the requested languages
     * @return modified record
     */
    public FullBean translateTitleDescription(FullBean bean, List<Language> targetLangs) {
        // TODO for now we only translate into the first language in the list, the rest is used for filtering only
        String targetLang = targetLangs.get(0).name().toLowerCase(Locale.ROOT);

        // gather all translations
        TranslationsMap toTranslate = new TranslationsMap();
        toTranslate.add(getTitleToTranslate(bean, targetLang));
        toTranslate.add(getDescriptionToTranslate(bean, targetLang));

        // send a requests for each of the languages and merge all results into 1 map
        TranslationMap translated = new TranslationMap(targetLang);
        for (TranslationMap mapToTranslate : toTranslate.values()) {
            translated.merge(TranslationUtils.translate(translationService, mapToTranslate, targetLang));
        }

        // add translations to Europeana proxy
        for (Map.Entry<String, List<String>> entry : translated.entrySet()) {
            if (KEY_TITLE.equals(entry.getKey())) {
                addTranslatedTitle(bean, entry.getValue(), targetLang);
            } else if (KEY_DESCRIPTION.equals(entry.getKey())) {
                addTranslatedDescription(bean, entry.getValue(), targetLang);
            }
        }
        return bean;
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
    private TranslationMap getTitleToTranslate(FullBean bean, String targetLang) {
        TranslationMap result = getTitleForLang(bean, targetLang);
        if (result == null) {
            if (!Language.DEFAULT.equals(targetLang.toLowerCase(Locale.ROOT))) {
                LOG.trace("No title found for record {} in lang {}, searching for English...", bean.getAbout(), targetLang);
                result = getTitleForLang(bean, Language.DEFAULT);
            }
            if (result == null) {
                LOG.trace("No English title found for record {}, searching for any title...", bean.getAbout());
                result = getDefOrFirstTitle(bean);
            }
            if (LOG.isDebugEnabled()) {
                if (result == null) {
                    LOG.debug("No title found record {} in lang {}", bean.getAbout(), targetLang);
                } else {
                    LOG.debug("Found title in lang {} for record {}", result.getLanguage(), bean.getAbout());
                }
            }
        } else {
            LOG.debug("Title found in lang {} for record {}, no translation required", targetLang, bean.getAbout());
            result = null;
        }

        return result;
    }

    private TranslationMap getTitleForLang(FullBean bean, String lang) {
        TranslationMap result = null;
        for (Proxy p : bean.getProxies()) {
            if (p.getDcTitle() != null && p.getDcTitle().containsKey(lang)) {
                result = new TranslationMap(lang, KEY_TITLE, p.getDcTitle().get(lang));
                break;
            }
        }
        return result;
    }

    private TranslationMap getDefOrFirstTitle(FullBean bean) {
        TranslationMap result = null;
        TranslationMap firstValue = null;
        for (Proxy p : bean.getProxies()) {
            if (p.getDcTitle() != null) {
                List<String> defValue = p.getDcTitle().get(Language.DEF);
                if (defValue != null) {
                    result = new TranslationMap(Language.DEF, KEY_TITLE, defValue);
                    break;
                } else if (firstValue == null && !p.getDcTitle().isEmpty()) {
                    // set any found value, in case we find nothing in other proxies
                    String sourceLang = p.getDcTitle().keySet().iterator().next();
                    firstValue = new TranslationMap(sourceLang, KEY_TITLE, p.getDcTitle().get(sourceLang));
                }
            }
        }
        if (result == null) {
            return firstValue;
        }
        return result;
    }

    private void addTranslatedTitle(FullBean bean, List<String> translation, String targetLang) {
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

    private TranslationMap getDescriptionToTranslate(FullBean bean, String targetLang) {
        TranslationMap result = getDescriptionForLang(bean, targetLang);
        if (result == null) {
            if (!Language.DEFAULT.equals(targetLang.toLowerCase(Locale.ROOT))) {
                LOG.trace("No description found for record {} in lang {}, searching for English...", bean.getAbout(), targetLang);
                result = getDescriptionForLang(bean, Language.DEFAULT);
            }
            if (result == null) {
                LOG.trace("No English description found for record {}, searching for any result...", bean.getAbout());
                result = getDefOrFirstDescription(bean);
            }
            if (LOG.isDebugEnabled()) {
                if (result == null) {
                    LOG.debug("No description found record {} in lang {}", bean.getAbout(), targetLang);
                } else {
                    LOG.debug("Found description in lang {} for record {}", result.getLanguage(), bean.getAbout());
                }
            }
        }  else {
            LOG.debug("Description found in lang {} for record {}, no translation required", targetLang, bean.getAbout());
            result = null;
        }

        return result;
    }

    private TranslationMap getDescriptionForLang(FullBean bean, String lang) {
        TranslationMap result = null;
        for (Proxy p : bean.getProxies()) {
            if (p.getDcDescription() != null && p.getDcDescription().containsKey(lang)) {
                result = new TranslationMap(lang, KEY_DESCRIPTION, p.getDcDescription().get(lang));
                break;
            }
        }
        return result;
    }

    private TranslationMap getDefOrFirstDescription(FullBean bean) {
        TranslationMap result = null;
        TranslationMap firstValue = null;
        for (Proxy p : bean.getProxies()) {
            if (p.getDcDescription() != null) {
                List<String> defValue = p.getDcDescription().get(Language.DEF);
                if (defValue != null) {
                    result = new TranslationMap(Language.DEF, KEY_DESCRIPTION, defValue);
                    break;
                } else if (firstValue == null && !p.getDcDescription().isEmpty()) {
                    // set any found value, in case we find nothing in other proxies
                    String sourceLang = p.getDcDescription().keySet().iterator().next();
                    firstValue = new TranslationMap(sourceLang, KEY_DESCRIPTION, p.getDcDescription().get(sourceLang));
                }
            }
        }
        if (result == null) {
            return firstValue;
        }
        return result;
    }

    private void addTranslatedDescription(FullBean bean, List<String> translation, String targetLang) {
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
