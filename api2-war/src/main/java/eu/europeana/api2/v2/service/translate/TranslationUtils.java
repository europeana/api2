package eu.europeana.api2.v2.service.translate;

import eu.europeana.api.translation.definitions.language.Language;
import eu.europeana.api.translation.definitions.model.TranslationObj;
import eu.europeana.api.translation.definitions.model.TranslationRequest;
import eu.europeana.corelib.definitions.edm.beans.FullBean;
import eu.europeana.corelib.definitions.edm.entity.ContextualClass;
import eu.europeana.corelib.utils.ComparatorUtils;
import eu.europeana.corelib.utils.EuropeanaUriUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TranslationUtils {

    private static final Logger LOG = LogManager.getLogger(TranslationUtils.class);

    public static final String FIELD_SEPARATOR = ".";
    public static final String FIELD_SEPARATOR_REGEX = "\\.";

    private static String getPharse = "^.*?(?=[.|?|!])";
    public  static final Pattern getValuesBeforePhrasePattern = Pattern.compile(getPharse);
    public static final String TRUNCATED_INDICATOR = "...";


    /**
     * During ingestion -
     *     For translations only fetch the value from the map if there
     *     is NO "en" language tag already present for the field and there is value present for the sourceLang
     *
     * For other API's translations , fetch the value in the source language if present
     *
     * @param map
     * @param sourceLang
     * @return
     */
    public static boolean ifValuesShouldBePickedForTranslation(Map<String, List<String>> map, String sourceLang, String targetLang, boolean ingestionProcess) {
        if (ingestionProcess) {
            return map != null && !map.isEmpty()  && !map.containsKey(Language.PIVOT) && containsLangOrRegionLang(map, sourceLang);
        }
        return map != null && !map.isEmpty() && !map.containsKey(targetLang) && containsLangOrRegionLang(map, sourceLang);
    }


    /**
     * Return true if the map contains source language or a region code that starts with the source language
     *
     * ex - en-GB or en ;  de or de-NL
     * NOTE : There are cases, the source language matches with "def" (if the language is "de"). See - EA-3530
     * Hence the second condition is more extensive.
     * Performance wise the condition are placed accordingly.
     *
     * @param multilingualMap
     * @param lang
     * @return
     */
    private static boolean containsLangOrRegionLang(Map<String, List<String>> multilingualMap, String lang) {
        return multilingualMap.containsKey(lang) || multilingualMap.keySet().stream().anyMatch(key -> !StringUtils.equals(key, Language.DEF) && key.startsWith(lang));
    }

    /**
     * Creates Translation request
     * @param textsToTranslate
     * @param targetLanguage
     * @param sourceLanguage
     * @return
     */
    public static List<TranslationObj> createTranslationRequest(List<String> textsToTranslate, String targetLanguage, String sourceLanguage) {
        List<TranslationObj> translationObjs = new ArrayList<>(textsToTranslate.size());
        for(String text : textsToTranslate) {
            TranslationObj translationObj = new TranslationObj();
            translationObj.setText(text);
            translationObj.setSourceLang(sourceLanguage);
            translationObj.setTargetLang(targetLanguage);
            translationObjs.add(translationObj);
        }
        return translationObjs;
    }

    /**
     * Returns list of values to be translated.
     * Looks for Contextual entities, if found fetches the prefLabel of the entity in the source language
     *
     * @param origFieldData field lang value map
     * @param sourceLang    the language chosen for translation
     * @param bean          record
     * @return
     */
    public static List<String> getValuesToTranslate(Map<String, List<String>> origFieldData, String sourceLang, FullBean bean, boolean onlyLiterals,
                                                    Integer translationCharLimit, Integer translationCharTolerance) {
        List<String> valuesToTranslate = new ArrayList<>();
        // for search translations we only need literal values
        if (onlyLiterals) {
            valuesToTranslate = LanguageDetectionUtils.filterOutUris(getValueFromMultiLingualMap(origFieldData, sourceLang));
        } else {
            for (String value : getValueFromMultiLingualMap(origFieldData, sourceLang)) {
                // if the value is a URI get the contextual entity pref label in source lang.
                // Also, ignore the other uri values whose entity doesn't exist
                if (EuropeanaUriUtils.isUri(value)) {
                    ContextualClass entity = BaseService.entityExistsWithUrl(bean, value);
                    if (entity != null && entity.getPrefLabel() != null && containsLangOrRegionLang(entity.getPrefLabel(), sourceLang)) {
                        LOG.debug("Entity {} has preflabel in chosen language {} for translation  ", value, sourceLang);
                        valuesToTranslate.addAll(getValueFromMultiLingualMap(entity.getPrefLabel(), sourceLang));
                    }
                } else {
                    valuesToTranslate.add(value); // add non uri values
                }
            }
        }
        // remove duplicate values
       List<String> cleanValues = ComparatorUtils.removeDuplicates(valuesToTranslate);

        // truncate if neccesary
        if (translationCharLimit != null && translationCharTolerance != null) {
            return truncate(cleanValues, translationCharLimit, translationCharTolerance);
        }
        return cleanValues;
    }


    private static List<String> getValueFromMultiLingualMap(Map<String, List<String>> map, String lang) {
        List<String> valuesToTranslate = map.get(lang);
        // check if value is present as region code en-GB or nl-DE etc
        if (valuesToTranslate == null || valuesToTranslate.isEmpty()) {
            valuesToTranslate = map.entrySet().stream()
                    .filter(entry -> !StringUtils.equals(entry.getKey(), Language.DEF) && entry.getKey().startsWith(lang))
                    .map(Map.Entry :: getValue)
                    .findFirst().orElse(null);
        }
        return valuesToTranslate;
    }

    public static List<String> truncate(List<String> valuesToTranslate , Integer translationCharLimit, Integer translationCharTolerance) {
        List<String> truncatedValues = new ArrayList<>();
        boolean noFurtherLooking = false;
        Integer charAccumulated = 0;
        for (String value : valuesToTranslate) {
            // check if the value exceeded the limit.
            if ((charAccumulated + value.length()) >= translationCharLimit) {
                // get exceeded String value
                Integer charLimitIndex = translationCharLimit - charAccumulated;
                String valueAfterLimit = StringUtils.substring(value, charLimitIndex, value.length());

                //  check if the string has a phrase or new line
                Matcher m = getValuesBeforePhrasePattern.matcher(valueAfterLimit);
                if (m.find()) {
                    truncatedValues.add(StringUtils.substring(value, 0,  charLimitIndex) + m.group(0) + TRUNCATED_INDICATOR) ;
                } else {
                    // abbreviate the value till the tolerance or if the end of the value is reached
                    truncatedValues.add(WordUtils.abbreviate(
                            value, charLimitIndex,translationCharLimit+ translationCharTolerance, TRUNCATED_INDICATOR));
                }
                noFurtherLooking = true;
            } else {
                truncatedValues.add(value);
            }
            charAccumulated +=value.length();
            // ignore any other value after limit is reached
            if(noFurtherLooking) break;
        }
        return truncatedValues;
    }
}
