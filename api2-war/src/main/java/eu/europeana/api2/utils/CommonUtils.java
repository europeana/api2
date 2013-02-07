package eu.europeana.api2.utils;

import java.io.IOException;
import java.util.logging.Logger;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

public class CommonUtils {

	private static final Logger log = Logger.getLogger(CommonUtils.class.getCanonicalName());

	/**
	 * Test JSON creating
	 * @param response
	 */
	public static void createJson(Object response) {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setSerializationInclusion(Inclusion.NON_EMPTY);
		try {
			String json = objectMapper.writeValueAsString(response);
			log.info("JSON: " + json);
		} catch (JsonGenerationException e) {
			log.severe(ExceptionUtils.getStackTrace(e));
		} catch (JsonMappingException e) {
			log.severe(ExceptionUtils.getStackTrace(e));
		} catch (IOException e) {
			log.severe(ExceptionUtils.getStackTrace(e));
		}
	}
}
