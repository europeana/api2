package eu.europeana.api2.v2.utils;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class FacetParameterUtilsTest {

	private String[] facets;
	private Map<String, String[]> parameters;

	@Before
	public void setUp() throws Exception {
		facets = new String[]{"proxy_dc_contributor"};

		parameters = new HashMap<String, String[]>();
		parameters.put("facet", new String[]{"proxy_dc_contributor"});
		parameters.put("profile", new String[]{"facets"});
		parameters.put("f.proxy_dc_contributor.facet.limit", new String[]{"30"});
		parameters.put("f.proxy_dc_contributor.facet.offset", new String[]{"0"});
	}

	@Test
	public void testLimitWithDefaults() {
		Map<String, Integer> resultMap = FacetParameterUtils.getFacetParams("limit", facets, parameters, true);
		assertNotNull(resultMap);
		assertEquals(9, resultMap.size());
		assertTrue(resultMap.containsKey("f.proxy_dc_contributor.facet.limit"));
		assertTrue(resultMap.containsKey("f.DATA_PROVIDER.facet.limit"));
		assertEquals(new Integer(30), resultMap.get("f.proxy_dc_contributor.facet.limit"));
		assertEquals(new Integer(3000), resultMap.get("f.DATA_PROVIDER.facet.limit"));
	}

	@Test
	public void testLimitWithoutDefaults() {
		Map<String, Integer> resultMap = FacetParameterUtils.getFacetParams("limit", facets, parameters, true);
		assertNotNull(resultMap);
		assertEquals(9, resultMap.size());
		assertTrue(resultMap.containsKey("f.proxy_dc_contributor.facet.limit"));
		assertEquals(new Integer(30), resultMap.get("f.proxy_dc_contributor.facet.limit"));
		assertEquals(new Integer(3000), resultMap.get("f.DATA_PROVIDER.facet.limit"));
	}

	@Test
	public void testOffsetWithDefaults() {
		Map<String, Integer> resultMap = FacetParameterUtils.getFacetParams("offset", facets, parameters, true);
		assertNotNull(resultMap);
		assertEquals(1, resultMap.size());
		assertTrue(resultMap.containsKey("f.proxy_dc_contributor.facet.offset"));
		assertEquals(new Integer(0), resultMap.get("f.proxy_dc_contributor.facet.offset"));
	}

	@Test
	public void testOffsetWithoutDefaults() {
		Map<String, Integer> resultMap = FacetParameterUtils.getFacetParams("offset", facets, parameters, false);
		assertNotNull(resultMap);
		assertEquals(1, resultMap.size());
		assertTrue(resultMap.containsKey("f.proxy_dc_contributor.facet.offset"));
		assertEquals(new Integer(0), resultMap.get("f.proxy_dc_contributor.facet.offset"));
	}

	@Test
	public void testAdditionalOffsetWithDefaults() {
		parameters.put("f.PROVIDER.facet.offset", new String[]{"0"});

		Map<String, Integer> resultMap = FacetParameterUtils.getFacetParams("offset", facets, parameters, true);
		assertNotNull(resultMap);
		assertEquals(2, resultMap.size());
		assertTrue(resultMap.containsKey("f.proxy_dc_contributor.facet.offset"));
		assertEquals(new Integer(0), resultMap.get("f.proxy_dc_contributor.facet.offset"));
		assertTrue(resultMap.containsKey("f.proxy_dc_contributor.facet.offset"));
		assertEquals(new Integer(0), resultMap.get("f.PROVIDER.facet.offset"));
	}

	@Test
	public void testAdditionalOffsetWithoutDefaults() {
		parameters.put("f.PROVIDER.facet.offset", new String[]{"0"});

		Map<String, Integer> resultMap = FacetParameterUtils.getFacetParams("offset", facets, parameters, false);
		assertNotNull(resultMap);
		assertEquals(1, resultMap.size());
		assertTrue(resultMap.containsKey("f.proxy_dc_contributor.facet.offset"));
		assertEquals(new Integer(0), resultMap.get("f.proxy_dc_contributor.facet.offset"));
	}
}
