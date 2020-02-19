package eu.europeana.api2.v2.web.controller;

import eu.europeana.api2.v2.utils.ApiKeyUtils;
import eu.europeana.api2.v2.utils.HttpCacheUtils;
import eu.europeana.corelib.search.SearchService;
import org.junit.Before;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class ObjectControllerTest {

    private static final String MEDIA_TYPE_JSONLD_UTF8  = "application/ld+json; charset=UTF-8";

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

        objectController = spy(new ObjectController(searchService, apiKeyUtils, httpCacheUtils));

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

        //Invalid accept Header
        objectControllerMock.perform(get("/v2/record/context.jsonld")
                .accept(MediaType.APPLICATION_XML))
                .andExpect(status().is(406));
    }
}
