package eu.europeana.api2.v2.web.controller;

import eu.europeana.api2.v2.utils.ApiKeyUtils;
import eu.europeana.api2.v2.utils.HttpCacheUtils;
import eu.europeana.corelib.definitions.edm.beans.FullBean;
import eu.europeana.corelib.search.SearchService;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import org.junit.Before;

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


import org.junit.Test;
import org.mockito.Matchers;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.util.NestedServletException;

public class ObjectControllerTest {

    private static final String MEDIA_TYPE_JSONLD_UTF8  = "application/ld+json; charset=UTF-8";
    private static final String COLLECTIONID_TEST = "2024904";
    private static final String RECORDID_TEST  = "photography_ProvidedCHO_TopFoto_co_uk_EU017407";


    private static FullBean bean = new FullBeanImpl();

    private static ObjectController objectController;
    private static SearchService searchService;
    private static ApiKeyUtils apiKeyUtils;
    private static HttpCacheUtils httpCacheUtils;

    private static MockMvc objectControllerMock;

    @Before
    public void setup() throws Exception {

        searchService = mock(SearchService.class);
        apiKeyUtils = mock(ApiKeyUtils.class);
        httpCacheUtils = mock(HttpCacheUtils.class);
        objectController = new ObjectController(searchService, apiKeyUtils, httpCacheUtils);

        objectControllerMock = MockMvcBuilders
                .standaloneSetup(objectController)
                .build();
    }

    @Test
    public void contextJSONLDAcceptHeaderTest () throws Exception {
        objectControllerMock.perform(get("/v2/record/context.jsonld")
                .accept(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(status().is(200));

        objectControllerMock.perform(get("/v2/record/context.jsonld")
                .accept(MEDIA_TYPE_JSONLD_UTF8))
                .andExpect(status().is(200));

        objectControllerMock.perform(get("/v2/record/context.jsonld")
                .accept("*"))
                .andExpect(status().is(200));
    }

    @Test(expected= NestedServletException.class)
    public void recordJSON_LDAcceptHeaderTest () throws Exception {
        FullBean bean = new FullBeanImpl();

        when(searchService.fetchFullBean(anyString())).thenReturn(bean);
        when(searchService.processFullBean(Matchers.any(), Matchers.anyString(), anyBoolean())).thenReturn(bean);


        objectControllerMock.perform(get("/v2/record/{collectionId}/{recordId}.jsonld" , COLLECTIONID_TEST, RECORDID_TEST)
                .param("wskey", "api2demo")
                .accept(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(status().is(200))
                .andExpect(content().contentType(MEDIA_TYPE_JSONLD_UTF8));

        objectControllerMock.perform(get("/v2/record/{collectionId}/{recordId}.jsonld" , COLLECTIONID_TEST, RECORDID_TEST)
                .param("wskey", "api2demo")
                .accept(MEDIA_TYPE_JSONLD_UTF8))
                .andExpect(status().is(200))
                .andExpect(content().contentType(MEDIA_TYPE_JSONLD_UTF8));

        objectControllerMock.perform(get("/v2/record/{collectionId}/{recordId}.jsonld" , COLLECTIONID_TEST, RECORDID_TEST)
                .param("wskey", "api2demo")
                .accept("*"))
                .andExpect(status().is(200))
                .andExpect(content().contentType(MEDIA_TYPE_JSONLD_UTF8));

        objectControllerMock.perform(get("/v2/record/{collectionId}/{recordId}.json-ld" , COLLECTIONID_TEST, RECORDID_TEST)
                .param("wskey", "api2demo")
                .accept(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(status().is(200))
                .andExpect(content().contentType(MEDIA_TYPE_JSONLD_UTF8));

        objectControllerMock.perform(get("/v2/record/{collectionId}/{recordId}.json-ld" , COLLECTIONID_TEST, RECORDID_TEST)
                .param("wskey", "api2demo")
                .accept(MEDIA_TYPE_JSONLD_UTF8))
                .andExpect(status().is(200))
                .andExpect(content().contentType(MEDIA_TYPE_JSONLD_UTF8));

        objectControllerMock.perform(get("/v2/record/{collectionId}/{recordId}.json-ld" , COLLECTIONID_TEST, RECORDID_TEST)
                .param("wskey", "api2demo")
                .accept("*"))
                .andExpect(status().is(200))
                .andExpect(content().contentType(MEDIA_TYPE_JSONLD_UTF8));

    }

}
