package eu.europeana.api2.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.springframework.web.servlet.ModelAndView;

public class JsonUtils {

	private static final Logger log = Logger.getLogger(JsonUtils.class.getName());

	public static ModelAndView toJson(Object object) {
		return toJson(object, null);
	}

	public static ModelAndView toJson(Object object, String callback) {
		String resultPage = "json";
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setSerializationInclusion(Inclusion.NON_NULL);
		Map<String, Object> model = new HashMap<String, Object>();
		try {
			model.put("json", objectMapper.writeValueAsString(object));
		} catch (JsonGenerationException e) {
			log.severe("Json Generation Exception: " + e.getMessage());
			e.printStackTrace();
		} catch (JsonMappingException e) {
			log.severe("Json Mapping Exception: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			log.severe("I/O Exception: " + e.getMessage());
			e.printStackTrace();
		}
		if (StringUtils.isNotBlank(callback)) {
			resultPage = "jsonp";
			model.put("callback", callback);
		}
		return new ModelAndView(resultPage, model);
	}
	
}
