package eu.europeana.api2.v2.utils;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;

import eu.europeana.api2.v2.model.NumericFacetParameter;
import eu.europeana.corelib.definitions.solr.Facet;

/**
 * Utility class for extracting numeric parameters specific to a given facet.
 * 
 * @author Peter.Kiraly@kb.nl
 */
public class FacetParameterUtils {

	final static String DEFAULT_LIMIT_KEY = "f.DEFAULT.facet.limit";
	final static String DEFAULT_OFFSET_KEY = "f.DEFAULT.facet.offset";
	final static int LIMIT_FOR_DEFAULT = 750;
	final static int LIMIT_FOR_CUSTOM = 50;

	/**
	 * Returns all relevant parameters of a given type (right now: limit and offset)
	 * 
	 * @param type
	 *   Type of parameter. Currently we support "limit" and "offset"
	 * @param facets
	 *   List of facets
	 * @param parameters
	 *   The request parameters
	 * @param isDefaultFacetsRequested
	 *   Whether or not the default facets should be checked
	 * @return
	 */
	public static Map<String, Integer> getFacetParams(String type, String[] facets, 
			Map<Object, Object> parameters,
			boolean isDefaultFacetsRequested) {
		Map<String, Integer> facetParams = new HashMap<String, Integer>();
		if (isDefaultFacetsRequested) {
			for (Facet facet : Facet.values()) {
				saveFacetParam(type, facet.name(), parameters, true, facetParams);
			}
		}

		if (ArrayUtils.isNotEmpty(facets)) {
			for (String facet : facets) {
				saveFacetParam(type, facet, parameters, false, facetParams);
			}
		}
		return facetParams;
	}

	/**
	 * Extracts and saves parameter of a given type (right now: limit and offset) belongs to a facet
	 * 
	 * @param type
	 *   The type of parameter (limit or offset)
	 * @param name
	 *   The name of the facet
	 * @param parameters
	 *   Request parameters
	 * @param isDefault
	 *   The facet is a default facet
	 * @param facetParams
	 *   The container to save into
	 */
	public static void saveFacetParam(String type, String name, Map<Object, Object> parameters, 
			boolean isDefault, Map<String, Integer> facetParams) {
		NumericFacetParameter parameter = null;
		if (type.equals("limit")) {
			parameter = getFacetLimit(name, parameters, isDefault);
		} else if (type.equals("offset")) {
			parameter = getFacetOffset(name, parameters, isDefault);
		}
		if (parameter != null) {
			facetParams.put(parameter.getName(), parameter.getValue());
		}
	}

	public static NumericFacetParameter getFacetLimit(String facet, Map<Object, Object> parameters, boolean isDefault) {
		String key = "f." + facet + ".facet.limit";
		return extractParameter(key, DEFAULT_LIMIT_KEY, parameters, isDefault, (isDefault ? LIMIT_FOR_DEFAULT : LIMIT_FOR_CUSTOM));
	}

	public static NumericFacetParameter getFacetOffset(String facet, Map<Object, Object> parameters, boolean isDefault) {
		String key = "f." + facet + ".facet.offset";
		return extractParameter(key, DEFAULT_OFFSET_KEY, parameters, isDefault, null);
	}

	public static NumericFacetParameter extractParameter(String key, String defaultKey, 
			Map<Object, Object> parameters, boolean isDefault, Integer defaultValue) {
		if (parameters.containsKey(key)) {
			String[] value = (String[]) parameters.get(key);
			return new NumericFacetParameter(key, value[0]);
		}
		if (isDefault && parameters.containsKey(defaultKey)) {
			String[] value = (String[]) parameters.get(defaultKey);
			return new NumericFacetParameter(key, value[0]);
		}
		if (defaultValue != null) {
			return new NumericFacetParameter(key, defaultValue);
		}
		return null;
	}
}
