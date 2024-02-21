package eu.europeana.api2.v2.service.translate;

import eu.europeana.api.commons.definitions.utils.ComparatorUtils;
import eu.europeana.api.translation.definitions.language.Language;
import eu.europeana.api2.v2.model.translate.LanguageValueFieldMap;
import eu.europeana.corelib.definitions.edm.beans.BriefBean;
import eu.europeana.corelib.definitions.edm.beans.FullBean;
import eu.europeana.corelib.definitions.edm.beans.IdBean;
import eu.europeana.corelib.definitions.edm.entity.ContextualClass;
import eu.europeana.corelib.utils.EuropeanaUriUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class LanguageDetectionUtils {

    private static final Logger LOG = LogManager.getLogger(LanguageDetectionUtils.class);

    private static final String PATTERN = "\\p{L}|[0-9]";
    private static final Pattern unicodeNumberPattern = Pattern.compile(PATTERN);

    private LanguageDetectionUtils() {

    }

    /**
     * Returns the default language list of the edm:languages
     * NOTE : For region locales values, if present in edm:languages
     * the first two ISO letters will be picked up.
     * <p>
     * Only returns the supported official languages,See: {@link Language}
     * Default translation and filtering for non-official language
     * is not supported
     *
     * @param bean bean that extends IdBean See: {@link IdBean}
     *
     * @return the default language as specified in Europeana Aggregation edmLanguage field OR in the Language field of the serach Results Brief beans
     * (if the language found there is one of the EU languages we support in this application for translation)
     */
    public static <T extends IdBean> List<Language> getEdmLanguage(T bean, boolean searchResults) {
        List<Language> lang = new ArrayList<>();
        Map<String, List<String>> edmLanguage = getLanguageFieldValue(bean, searchResults);

        for (Map.Entry<String, List<String>> entry : edmLanguage.entrySet()) {
            for (String languageAbbreviation : entry.getValue()) {
                if (Language.isSupported(languageAbbreviation)) {
                    lang.add(Language.getLanguage(languageAbbreviation));
                } else {
                    LOG.warn("edm:language '{}' is not supported", languageAbbreviation);
                }
            }
        }
        if (!lang.isEmpty()) {
            LOG.debug("EDM language - {} fetched for record - {} ", lang, searchResults ? bean.getId() : ((FullBean) bean).getAbout());
        }
        return lang;
    }

    /**
     * Get the language from Bean.
     * @param bean
     * @param searchResults if true fetches the language value field from BriefBean.
     *                      If false fetches the edmlanguage field from Fullbean
     * @param <T> bean
     * @return
     */
    private static <T extends IdBean>  Map<String, List<String>> getLanguageFieldValue(T bean, boolean searchResults) {
        Map<String, List<String>> edmLanguage = new HashMap<>();
        if (searchResults) {
            if (((BriefBean) bean).getLanguage() != null) {
                edmLanguage.put("", Arrays.asList(((BriefBean) bean).getLanguage()));
            }
        } else {
            edmLanguage = ((FullBean)bean).getEuropeanaAggregation().getEdmLanguage();
        }
        return edmLanguage;
    }

    /**
     * Method to get values of non-language tagged prefLabel (only if no other language tagged value doesn't exists)
     * @param entity entity object
     * @return
     */
    public static List<String> getPrefLabelofEntity(ContextualClass entity, String recordId) {
        List<String> prefLabels = new ArrayList<>();
        if (entity != null) {
            if (entity.getPrefLabel() != null) {
                Map<String, List<String>> map = entity.getPrefLabel();
                if (!map.isEmpty() && !map.keySet().isEmpty()) {
                    // if preflabel is present in other languages than "def" then do nothing
                    if (!map.isEmpty() && !map.keySet().isEmpty() && mapHasOtherLanguagesThanDef(map.keySet())) {
                        LOG.debug("Entity {} already has language tagged values. PrefLabels NOT added...", entity.getAbout());
                    } else { // pick the def value
                        LOG.debug("Entity {} has only non-language tagged values. Adding the prefLabels...", entity.getAbout());
                        prefLabels.addAll(map.get(Language.DEF));
                    }
                }
            } else {
                LOG.error("prefLabels NOT available for entity {} in record {} .", entity.getAbout(), recordId);
            }
        }
        return prefLabels;
    }

    /**
     * This methods adds the texts to be sent for detection in a list.
     * Additionally also saves the texts sent per field for detection
     *
     * @param textsForDetection List to store texts to be sent for language detection
     * @param textsPerField to add the text size sent for detection per field
     * @param langValueFieldMapForDetection lang-value "def" map for the whitelisted field
     */
    public static void getTextsForDetectionRequest(List<String> textsForDetection,
                                                   Map<String, Integer> textsPerField, List<LanguageValueFieldMap> langValueFieldMapForDetection ) {
        for (LanguageValueFieldMap languageValueFieldMap : langValueFieldMapForDetection) {
            for (Map.Entry<String, List<String>> def : languageValueFieldMap.entrySet()) {
                textsForDetection.addAll(def.getValue());
                textsPerField.put(languageValueFieldMap.getFieldName(), def.getValue().size());
            }
        }
    }

    /**
     * Assigns the correct language to the values for the fields
     * @param textsPerField number of texts present per field list
     * @param detectedLanguages languages detected by the Engine
     * @param textsForDetection texts sent for lang-detect requests
     * @return
     */
    public static List<LanguageValueFieldMap> getLangDetectedFieldValueMap(Map<String, Integer> textsPerField, List<String> detectedLanguages, List<String> textsForDetection) {
        int counter =0;
        List<LanguageValueFieldMap> correctLangMap = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : textsPerField.entrySet()) { // field loop
            // new value map for field
            Map<String, List<String>> newValueMap = new HashMap<>();

            for(int i=0; i< entry.getValue(); i++) {
                String newLang = detectedLanguages.get(counter);
                // if the service did not return any language for the text then source language should be kept intact
                // which is "def" in these cases
                newLang = newLang == null ? Language.DEF : newLang;
                if (newValueMap.containsKey(newLang)) {
                    newValueMap.get(newLang).add(textsForDetection.get(counter));
                } else {
                    List<String> values = new ArrayList<>();
                    values.add(textsForDetection.get(counter));
                    newValueMap.put(newLang, values);
                }
                counter++;
            }
            // add the new map for the field
            correctLangMap.add(new LanguageValueFieldMap(entry.getKey(), newValueMap));
        }
        return correctLangMap;
    }

    /**
     * Returns the def values of the field (removing the values which are already present in the lang-tagged)
     *
     * @param map map of the field
     * @param fieldName field name
     * @return
     */
    public static <T extends IdBean> LanguageValueFieldMap getValueFromLanguageMap(Map<String, List<String>> map, String fieldName, T bean, boolean onlyLiterals) {
        // get non-language tagged values only
        List<String> defValues = new ArrayList<>();
        if (!map.isEmpty() && map.containsKey(Language.DEF)) {
            List<String> values = map.get(Language.DEF);
            // check if there is any other language present in the map and
            // if yes, then check if lang-tagged values already have the def tagged values present
            if (LanguageDetectionUtils.mapHasOtherLanguagesThanDef(map.keySet())) {
                defValues.addAll(LanguageDetectionUtils.removeLangTaggedValuesFromDef(map));
            } else {
                defValues.addAll(values);
            }
        }

        // For search results - only gather literals
        // For record - resolve the uri's and if contextual entity present get the preflabel
        List<String> resolvedNonLangTaggedValues = onlyLiterals ? filterOutUris(defValues) : checkForUrisAndGetPrefLabel((FullBean) bean, defValues);

        if (!resolvedNonLangTaggedValues.isEmpty()) {
            return new LanguageValueFieldMap(fieldName, Language.DEF, resolvedNonLangTaggedValues);
        }
        return null;
    }

    public static List<String> filterOutUris(List<String> values) {
        if (values != null && !values.isEmpty())  {
            return values.stream().filter(v -> !EuropeanaUriUtils.isUri(v)).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private static List<String> checkForUrisAndGetPrefLabel(FullBean bean, List<String> nonLanguageTaggedValues) {
        List<String> resolvedNonLangTaggedValues = new ArrayList<>();
        if( !nonLanguageTaggedValues.isEmpty()) {
            for (String value : nonLanguageTaggedValues) {
                if (EuropeanaUriUtils.isUri(value)) {
                    ContextualClass entity = BaseService.entityExistsWithUrl(bean, value);
                    // For uri who have contextual entity we add the prefLabels only if non-language tagged values are present.
                    // We ignore the prefLabels if language tagged values are present.
                    // Also, ignore the other uri values whose entity doesn't exist
                    if (entity != null) {
                        // preflabels here will either have "def" values (only if there was no other language value present) OR will be empty
                        List<String> preflabels = getPrefLabelofEntity(entity, bean.getAbout());
                        resolvedNonLangTaggedValues.addAll(preflabels);
                    }
                } else {
                    resolvedNonLangTaggedValues.add(value); // add other texts as it is
                }
            }
        }
        // remove duplicates and return values
        ComparatorUtils.removeDuplicates(resolvedNonLangTaggedValues);
        return resolvedNonLangTaggedValues;
    }

    /**
     * Checks if map contains keys other than "def"
     * @param keyset
     * @return
     */
    public static boolean mapHasOtherLanguagesThanDef(Set<String> keyset) {
        Set<String> copy = new HashSet<>(keyset); // deep copy
        copy.remove(Language.DEF);
        return !copy.isEmpty();
    }

    /**
     * Remove the lang-tagged values from "def"
     * NOTE : make sure to ot remove the uri's from the def values
     *
     * ex if map has values : {def=["paris", "budapest" , "venice"], en=["budapest"]}
     * then returns : ["paris", "venice"]
     * @param map
     * @return
     */
    public static List<String> removeLangTaggedValuesFromDef(Map<String, List<String>> map) {
        List<String> nonLangTaggedDefvalues = new ArrayList<>(map.get(Language.DEF));
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            if (!entry.getKey().equals(Language.DEF)) {
                nonLangTaggedDefvalues.removeAll(filterOutUris(entry.getValue()));
            }
        }
        return nonLangTaggedDefvalues;
    }

    public static boolean onlyNulls(List<String> values) {
        return values.stream().noneMatch(Objects::nonNull);
    }
}
