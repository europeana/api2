package eu.europeana.api2.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;

import java.util.LinkedHashMap;
import java.util.Map;

public final class JsonUtils {

    private JsonUtils() {
        // private constructor to prevent initialization
    }

    public static ModelAndView toJson(Object object)  {
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

    public static ModelAndView toJson(Object object, String callback)  {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        try {
            return toJson(objectMapper.writeValueAsString(object), callback);
        } catch (JsonProcessingException e) {
            String msg = "Error serializing to json";
            LogManager.getLogger(JsonUtils.class).error(msg, e);
            ModelAndView mv = toJson(msg + "\nCause: " + e.getMessage());
            mv.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            return mv;
        }
    }
}
