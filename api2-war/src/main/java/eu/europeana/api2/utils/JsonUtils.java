package eu.europeana.api2.utils;

import java.io.IOException;
import java.util.logging.Logger;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

public class JsonUtils {

	private final Logger log = Logger.getLogger(getClass().getName());

	private ObjectMapper objectMapper;

	public JsonUtils() {
		objectMapper = new ObjectMapper();
		objectMapper.setSerializationInclusion(Inclusion.NON_NULL);
	}

	public String toJson(Object object) {
		String json = null;
		try {
			json = objectMapper.writeValueAsString(object);
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
		return json;
	}
}
