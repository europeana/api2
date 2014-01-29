package eu.europeana.api2.v2.utils;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;

import eu.europeana.api2.v2.model.NumericFacetParameter;
import eu.europeana.corelib.definitions.solr.Facet;

public class FacetParameterUtils {

	final static String DEFAULT_LIMIT_KEY = "f.DEFAULT.facet.limit";
	final static String DEFAULT_OFFSET_KEY = "f.DEFAULT.facet.offset";


	public static Map<String, Integer> getFacetParams(String type, String[] facets, 
			Map<Object, Object> parameters,
			boolean isDefaultFacetsRequested) {
		Map<String, Integer> facetParams = new HashMap<String, Integer>();
		if (isDefaultFacetsRequested) {
			for (Facet facet : Facet.values()) {
				getFacetParam(type, facet.name(), parameters, true, facetParams);
			}
		}

		if (ArrayUtils.isNotEmpty(facets)) {
			for (String facet : facets) {
				getFacetParam(type, facet, parameters, false, facetParams);
			}
		}
		return facetParams;
	}

	public static void getFacetParam(String type, String name, Map<Object, Object> parameters, 
			boolean isDefault, Map<String, Integer> facetParams) {
		NumericFacetParameter parameter = null;
		if (type.equals("limit")) {
			parameter = getFacetLimit(name, parameters, true);
		} else if (type.equals("offset")) {
			parameter = getFacetOffset(name, parameters, true);
		}
		if (parameter != null) {
			facetParams.put(parameter.getName(), parameter.getValue());
		}
	}

	public static NumericFacetParameter getFacetLimit(String facet, Map<Object, Object> parameters, boolean isDefault) {
		String key = "f." + facet + ".facet.limit";
		return extractParameter(key, DEFAULT_LIMIT_KEY, parameters, isDefault);
	}

	public static NumericFacetParameter getFacetOffset(String facet, Map<Object, Object> parameters, boolean isDefault) {
		String key = "f." + facet + ".facet.offset";
		return extractParameter(key, DEFAULT_OFFSET_KEY, parameters, isDefault);
	}

	public static NumericFacetParameter extractParameter(String key, String defaultKey, Map<Object, Object> parameters, boolean isDefault) {
		if (parameters.containsKey(key)) {
			String[] value = (String[]) parameters.get(key);
			return new NumericFacetParameter(key, value[0]);
		}
		if (isDefault && parameters.containsKey(defaultKey)) {
			String[] value = (String[]) parameters.get(defaultKey);
			return new NumericFacetParameter(key, value[0]);
		}
		return null;
	}
}
