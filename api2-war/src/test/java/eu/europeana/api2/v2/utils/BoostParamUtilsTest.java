package eu.europeana.api2.v2.utils;

import eu.europeana.api2.v2.utils.BoostParamUtils;
import eu.europeana.corelib.edm.exceptions.SolrQueryException;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class BoostParamUtilsTest {

    private static String VALID_PARAM_1 = "{!edismax qf=\"title^4 proxy_dc_creator^5 proxy_dc_description^2 subject^2 qftext\" pf=\"title^2 proxy_dc_creator^1 proxy_dc_description^5 subject^6 pftext\" ps=2 tie=0.1}";
    private static String VALID_PARAM_2 = "{!edismax ps=2 tie=0.1}";
    private static String VALID_PARAM_3 = "{!edismax qf=\"title^4 proxy_dc_creator^5 proxy_dc_description^2 subject^2 qftext\" tie=0.1}";

    private static String INVALID_PARAM_PATTERN_1 = "{edismax qf=\"title^4 proxy_dc_creator^5 proxy_dc_description^2 subject^2 qftext\" pf=\"title^2 proxy_dc_creator^1 proxy_dc_description^5 subject^6 pftext\" ps=2 tie=0.1}";
    private static String INVALID_PARAM_PATTERN_2 = "{!edismax pf=\"title^2 proxy_dc_creator^1 proxy_dc_description^5 subject^6 pftext\" ps=2 tie=0.1";

    private static String INVALID_PARAM_WITH_BRACES = "{!edismax qf=\"title^4 {subject^2 qftext\" }{pf=\"title^2 pftext\" ps=2 tie=0.1}";


    @Test
    public void testValidateBoostParam_Success() throws SolrQueryException {
        assertTrue(BoostParamUtils.validateBoostParam(VALID_PARAM_1));
        assertTrue(BoostParamUtils.validateBoostParam(VALID_PARAM_2));
        assertTrue(BoostParamUtils.validateBoostParam(VALID_PARAM_3));
    }

    @Test(expected = SolrQueryException.class)
    public void testValidateBoostParam_InvalidFormatStart() throws SolrQueryException {
        BoostParamUtils.validateBoostParam(INVALID_PARAM_PATTERN_1);
    }

    @Test(expected = SolrQueryException.class)
    public void testValidateBoostParam_InvalidFormatEnd() throws SolrQueryException {
        BoostParamUtils.validateBoostParam(INVALID_PARAM_PATTERN_2);
    }

    @Test(expected = SolrQueryException.class)
    public void testValidateBoostParam_ExtraCurlyBraces() throws SolrQueryException {
        BoostParamUtils.validateBoostParam(INVALID_PARAM_WITH_BRACES);
    }

    @Test
    public void testGetFieldsFromString() throws SolrQueryException {
        List<String> fields = BoostParamUtils.getFieldsFromString(VALID_PARAM_1);
        checkFieldsExtracted(fields, 4, Arrays.asList("qf=", "pf=", "ps=", "tie="));

        fields.clear();
        fields = BoostParamUtils.getFieldsFromString(VALID_PARAM_2);
        checkFieldsExtracted(fields, 2, Arrays.asList("ps=", "tie="));

        fields.clear();
        fields = BoostParamUtils.getFieldsFromString(VALID_PARAM_3);
        checkFieldsExtracted(fields, 2, Arrays.asList("qf=", "tie="));
    }

    @Test
    public void testGetDismaxQueryMap() throws SolrQueryException {
        Map<String, String> map = BoostParamUtils.getDismaxQueryMap(VALID_PARAM_1);
        Map<String, String> expectedMap = new HashMap<>();
        expectedMap.put("qf", "title^4 proxy_dc_creator^5 proxy_dc_description^2 subject^2 qftext");
        expectedMap.put("pf", "title^2 proxy_dc_creator^1 proxy_dc_description^5 subject^6 pftext");
        expectedMap.put("ps", "2");
        expectedMap.put("tie", "0.1");

        checkMapExtracted(map, 4, expectedMap);

        map.clear();
        expectedMap.clear();
        map = BoostParamUtils.getDismaxQueryMap(VALID_PARAM_2);
        expectedMap = new HashMap<>();
        expectedMap.put("ps", "2");
        expectedMap.put("tie", "0.1");

        checkMapExtracted(map, 2, expectedMap);

        map.clear();
        expectedMap.clear();
        map = BoostParamUtils.getDismaxQueryMap(VALID_PARAM_3);
        expectedMap = new HashMap<>();
        expectedMap.put("qf", "title^4 proxy_dc_creator^5 proxy_dc_description^2 subject^2 qftext");
        expectedMap.put("tie", "0.1");

        checkMapExtracted(map, 2, expectedMap);
    }

    private void checkFieldsExtracted(List<String> fields, int expectedSize, List<String> expectedFields) {
        assertNotNull(fields);
        assertEquals(expectedSize, fields.size());
        expectedFields.stream().forEach(field -> assertTrue(fields.contains(field)));
    }

    private void checkMapExtracted(Map<String, String> map, int expectedSize, Map<String, String> expectedMap) {
        assertNotNull(map);
        assertEquals(expectedSize, map.size());
        expectedMap.entrySet().stream().forEach(value -> {
            assertTrue(map.containsKey(value.getKey()));
            assertEquals(value.getValue(), map.get(value.getKey()));
        });
    }

}
