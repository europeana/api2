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
     * Add a translation of the dcTitle and dcDescription to a record, if it does not already have this in the
     * requested language
     *
     * @param bean        the record to be used
     * @param targetLangs the requested languages
     * @return modified record
     */
    public FullBean translateTitleDescription(FullBean bean, List<Language> targetLangs) {
        // TODO for now we only translate into the first language in the list, the rest is only used for filtering
        String targetLang = targetLangs.get(0).name().toLowerCase(Locale.ROOT);
        Map<String, List<String>> toTranslate = TranslationUtils.initNewTranslationMap();

        ToTranslate title = getTitleToTranslate(bean, targetLang);
        ToTranslate description = getDescriptionToTranslate(bean, targetLang);

        String sourceLang = selectSourceLanguage(title, description);
        if (sourceLang == null || sourceLang.equals(targetLang)) {
            LOG.debug("No sources found that need translation");
        } else {
            if (title != null) {
                toTranslate.put(KEY_TITLE, title.textToTranslate);
            }
            if (description != null) {
                toTranslate.put(KEY_DESCRIPTION, description.textToTranslate);
            }

            Map<String, List<String>> translations = TranslationUtils.translate(translationService, toTranslate,
                    targetLang, Language.DEF.equals(sourceLang) ? null : sourceLang);

            addTranslatedTitle(bean, translations.get(KEY_TITLE), targetLang);
            addTranslatedDescription(bean, translations.get(KEY_DESCRIPTION), targetLang);
        }
        return bean;
    }

    //TODO for now we simply select the first sourceLange we find and assume the rest has the same language
    private String selectSourceLanguage(ToTranslate... toTranslates) {
        String result = null;
        for (ToTranslate toTranslate : toTranslates) {
            if (toTranslate != null) {
                result = toTranslate.sourceLang;
                LOG.debug("Selected sourceLanguage is {}", toTranslate.sourceLang);
                break;
            }
        }
        return result;
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
    private ToTranslate getTitleToTranslate(FullBean bean, String targetLang) {
        ToTranslate result = getTitleForLang(bean, targetLang);
        if (result == null) {
            if (!Language.DEFAULT.equals(targetLang.toLowerCase(Locale.ROOT))) {
                LOG.trace("No title found for record {} in lang {}, searching for English...", bean.getAbout(), targetLang);
                result = getTitleForLang(bean, Language.DEFAULT);
            }
            if (result == null) {
                LOG.trace("No English title found for record {}, searching for any title...", bean.getAbout());
                result = getDefOrFirstTitle(bean);
            }
        }

        if (LOG.isDebugEnabled()) {
            if (result == null) {
                LOG.debug("No title found record {} in lang {}", bean.getAbout(), targetLang);
            } else {
                LOG.debug("Found title in lang {} for record {}", result.sourceLang, bean.getAbout());
            }
        }
        return result;
    }

    private ToTranslate getTitleForLang(FullBean bean, String lang) {
        ToTranslate result = null;
        for (Proxy p : bean.getProxies()) {
            if (p.getDcTitle() != null && p.getDcTitle().containsKey(lang)) {
                result = new ToTranslate(lang, p.getDcTitle().get(lang));
                break;
            }
        }
        return result;
    }

    private ToTranslate getDefOrFirstTitle(FullBean bean) {
        ToTranslate result = null;
        ToTranslate firstValue = null;
        for (Proxy p : bean.getProxies()) {
            if (p.getDcTitle() != null) {
                List<String> defValue = p.getDcTitle().get(Language.DEF);
                if (defValue != null) {
                    result = new ToTranslate(Language.DEF, defValue);
                    break;
                } else if (firstValue == null && !p.getDcTitle().isEmpty()) {
                    // set any found value, in case we find nothing in other proxies
                    String sourceLang = p.getDcTitle().keySet().iterator().next();
                    firstValue = new ToTranslate(sourceLang, p.getDcTitle().get(sourceLang));
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

    private ToTranslate getDescriptionToTranslate(FullBean bean, String targetLang) {
        ToTranslate result = getDescriptionForLang(bean, targetLang);
        if (result == null) {
            if (!Language.DEFAULT.equals(targetLang.toLowerCase(Locale.ROOT))) {
                LOG.trace("No description found for record {} in lang {}, searching for English...", bean.getAbout(), targetLang);
                result = getDescriptionForLang(bean, Language.DEFAULT);
            }
            if (result == null) {
                LOG.trace("No English description found for record {}, searching for any result...", bean.getAbout());
                result = getDefOrFirstDescription(bean);
            }
        }

        if (LOG.isDebugEnabled()) {
            if (result == null) {
                LOG.debug("No description found record {} in lang {}", bean.getAbout(), targetLang);
            } else {
                LOG.debug("Found description in lang {} for record {}", result.sourceLang, bean.getAbout());
            }
        }
        return result;
    }

    private ToTranslate getDescriptionForLang(FullBean bean, String lang) {
        ToTranslate result = null;
        for (Proxy p : bean.getProxies()) {
            if (p.getDcDescription() != null && p.getDcDescription().containsKey(lang)) {
                result = new ToTranslate(lang, p.getDcTitle().get(lang));
                break;
            }
        }
        return result;
    }

    private ToTranslate getDefOrFirstDescription(FullBean bean) {
        ToTranslate result = null;
        ToTranslate firstValue = null;
        for (Proxy p : bean.getProxies()) {
            if (p.getDcDescription() != null) {
                List<String> defValue = p.getDcDescription().get(Language.DEF);
                if (defValue != null) {
                    result = new ToTranslate(Language.DEF, defValue);
                    break;
                } else if (firstValue == null && !p.getDcDescription().isEmpty()) {
                    // set any found value, in case we find nothing in other proxies
                    String sourceLang = p.getDcDescription().keySet().iterator().next();
                    firstValue = new ToTranslate(sourceLang, p.getDcDescription().get(sourceLang));
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

    /**
     * Stores source language and the text to translate
     */
    private static class ToTranslate {

        private final String sourceLang;
        private final List<String> textToTranslate;

        public ToTranslate(String sourceLang, List<String>textToTranslate) {
            this.sourceLang = sourceLang;
            this.textToTranslate = textToTranslate;
        }

    }
}
