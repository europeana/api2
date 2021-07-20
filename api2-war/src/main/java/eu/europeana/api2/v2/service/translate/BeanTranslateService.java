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

    private static final String ENGLISH = "en";
    private static final String DEF = "def";

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
        translateTitle(bean, targetLangs.get(0).name().toLowerCase(Locale.ROOT));
        translateDescription(bean, targetLangs.get(0).name().toLowerCase(Locale.ROOT));
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
