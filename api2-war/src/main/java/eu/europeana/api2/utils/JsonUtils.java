/*
 * Copyright 2007-2015 The Europeana Foundation
 *
 * Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 * by the European Commission;
 * You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 * http://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 * any kind, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under
 * the Licence.
 */

package eu.europeana.api2.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class JsonUtils {

    private static final Logger log = Logger.getLogger(JsonUtils.class);

    public static ModelAndView toJson(Object object) {
        return toJson(object, null);
    }

    public static ModelAndView toJson(String json, String callback) {
        String resultPage = "json";
        Map<String, Object> model = new LinkedHashMap<>();
        model.put("json", json);
        if (StringUtils.isNotBlank(callback)) {
            resultPage = "jsonp";
            model.put("callback", callback);
        }
        return new ModelAndView(resultPage, model);
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
