package eu.europeana.api2.web.controller.v1;

import java.io.IOException;
import java.util.logging.Logger;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

public class Api1Utils {

	private final Logger log = Logger.getLogger(getClass().getName());

	private ObjectMapper objectMapper;

	public Api1Utils() {
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
