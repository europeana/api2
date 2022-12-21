package eu.europeana.api2.v2.utils;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.util.*;

public class PangeanicUtils {

    public static final int MAX_CONNECTIONS = 100;
    public static final int MAX_CONNECTIONS_PER_ROUTE = 100;
    public static final int TOKEN_MIN_AGE = 30_000; //ms

    public static final String LANG_ZXX = "zxx";
    public static final String LANG_NA = "na";

    // request body fields
    public static final String MODE = "mode";
    public static final String MODE_EUROPEANA = "EUROPEANA";
    public static final String SOURCE_LANG = "src_lang";
    public static final String TARGET_LANG = "tgt_lang";
    public static final String INCLUDE_SRC = "include_src";
    public static final String TEXT = "text";

    // translate endpoint response values
    public static final String TRANSLATIONS = "translations";
    public static final String TRANSLATE_SOURCE = "src";
    public static final String TRANSLATE_TARGET = "tgt";

    //detect endpoint response fields
    public static final String SOURCE_DETECTED = "src_detected";
    public static final String DETECTED_LANGUAGE = "detected_langs";

    private PangeanicUtils() {
        // to hide implicit public one
    }

    /**
     * Create the post body for translate requests
     * @param texts
     * @param targetLanguage
     * @param sourceLanguage
     * @param apikey
     * @param v2
     * @return
     * @throws JSONException
     */
    public static JSONObject createTranslateRequestBody(List<String> texts, String targetLanguage, String sourceLanguage,
                                                        String apikey, boolean v2) throws JSONException {
        JSONObject body = new JSONObject();
        JSONArray textArray = new JSONArray();
        for (String text : texts) {
            textArray.put(text);
        }
        // create post body
        if (v2) {
            body.put("apikey" , apikey);
            body.put(MODE, MODE_EUROPEANA);
            body.put(TRANSLATE_SOURCE, textArray);
            body.put(SOURCE_LANG, sourceLanguage);
            body.put(INCLUDE_SRC, "true");
            body.put(TARGET_LANG, targetLanguage);
        } else {
            body.put(TEXT, textArray);
            if (StringUtils.isNotEmpty(sourceLanguage)) {
                body.put(TRANSLATE_SOURCE, sourceLanguage);
            }
            body.put(TRANSLATE_TARGET, targetLanguage);
        }
        return body;
    }

    /**
     * Creates Detect language request body
     * @param texts
     * @param hint
     * @param apikey
     * @return
     * @throws JSONException
     */
    public static JSONObject createDetectRequestBody(List<String> texts, String hint, String apikey) throws JSONException {
        JSONObject body = new JSONObject();
        JSONArray textArray = new JSONArray();
        for (String text : texts) {
            textArray.put(text);
        }
        // create post body
        body.put("apikey" , apikey);
        body.put(MODE, MODE_EUROPEANA);
        body.put(TRANSLATE_SOURCE, textArray);
        if (StringUtils.isNotEmpty(hint)) {
            body.put(SOURCE_LANG, hint);
        }
        return body;
    }

    /**
     * Creates a map with detected language and text to be translated
     * @param texts text to be translated
     * @param detectedLanguage language detected in the order of the texts
     * @return
     */
    public static Map<String, List<String>> getDetectedLangValueMap(List<String> texts, List<String> detectedLanguage) {
        Map<String, List<String>> map = new LinkedHashMap<>();
        int i = 0;
        for (String langDetected : detectedLanguage) {
            if (map.containsKey(langDetected)) {
                map.get(langDetected).add(texts.get(i));
            } else {
                // create a mutable list
                map.put(langDetected, new ArrayList<>(Arrays.asList(texts.get(i))));
            }
            i++;
        }
        return map;
    }

    /**
     * Returns the translations.
     * If nonTranslatedDataExists is true then add non-translated values as it is
     *
     * LOGIC :
     * if there is a size mismatch then the order the translation according to text sequence is performed.
     * As if multiple language were detected the order of translation result will vary.
     * Also, if duplicate texts are sent for translation there will be a size mismatch
     * ex: [Chine, Notice du catalogue, Chine] which results in
     * {Chine=China, Notice du catalogue=Catalogue notice} as translateResult is a map and will contain unique values
     *
     * Or else the translations are returned in the same order
     *
     * @param texts original values sent for translations
     * @param translateResult
     * @param nonTranslatedDataExists if language zxx or na was detected
     * @return
     */
    public static List<String> getResults(List<String> texts, Map<String, String> translateResult, boolean nonTranslatedDataExists) {
        List<String> translations = new ArrayList<>();
        if (texts.size() != translateResult.size()) {
            for (String text : texts) {
                if (translateResult.containsKey(text)) {
                    translations.add(translateResult.get(text));
                } else if (nonTranslatedDataExists) {
                    // add non-translated values as it is. Only if "zxx" or no-lang detected responses were present.
                    translations.add(text);
                }
            }
        } else {
            for (Map.Entry<String, String> entry : translateResult.entrySet()) {
                translations.add(entry.getValue());
            }
        }
        return translations;
    }

    public static boolean noTranslationRequired(String lang) {
        return (StringUtils.equals(lang, PangeanicUtils.LANG_ZXX) || StringUtils.equals(lang, PangeanicUtils.LANG_NA));
    }
}
