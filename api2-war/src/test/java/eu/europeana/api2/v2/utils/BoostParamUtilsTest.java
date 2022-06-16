package eu.europeana.api2.v2.utils;

import eu.europeana.corelib.edm.exceptions.SolrQueryException;
import org.junit.Test;

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
}
