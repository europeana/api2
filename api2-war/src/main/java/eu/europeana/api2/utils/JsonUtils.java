package eu.europeana.api2.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;

import java.util.LinkedHashMap;
import java.util.Map;

public class JsonUtils {

    private static final Logger log = Logger.getLogger(JsonUtils.class);

    public static ModelAndView toJson(Object object) {
        return toJson(object, null);
    }

    private static ModelAndView toJsonOrLd(String json, boolean isJsonLd, String callback) {
        String resultPage = isJsonLd ? "jsonld" : "json";
        Map<String, Object> model = new LinkedHashMap<>();
        model.put(resultPage, json);
        if (StringUtils.isNotBlank(callback)) {
            resultPage = "jsonp";
            model.put("callback", callback);
        }
        return new ModelAndView(resultPage, model);
    }

    public static ModelAndView toJson(String json, String callback) {
        return toJsonOrLd(json, false, callback);
    }

    public static ModelAndView toJsonLd(String json, String callback) {
        return toJsonOrLd(json, true, callback);
    }

    public static ModelAndView toJson(Object object, String callback) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        try {
            return toJson(objectMapper.writeValueAsString(object), callback);
        } catch (JsonProcessingException e) {
            log.error("Json Generation Exception: " + e.getMessage(), e);
        }
        // TODO report error...
        String resultPage = "json";
        Map<String, Object> model = new LinkedHashMap<>();
        if (StringUtils.isNotBlank(callback)) {
            resultPage = "jsonp";
            model.put("callback", callback);
        }
        return new ModelAndView(resultPage, model);
    }
}
