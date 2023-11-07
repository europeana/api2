package eu.europeana.api2.model.utils;

import eu.europeana.corelib.definitions.EuropeanaStaticUrl;
import eu.europeana.corelib.definitions.solr.DocType;
import eu.europeana.corelib.record.BaseUrlWrapper;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Api2UrlService class
 */
public class Api2UrlServiceTest {


    public static final String TEST_ROUTE = "test-route-1";

    // default baseUrl config settings.
    // TODO: write tests to cover then these are set
    private String configApi2Url = "";
    private String configGatewayUrl = "";
    private String configPortalUrl = "";

    private String apikeyServiceUrl = "";


    @Test
    void shouldReturnDefaultPortalBaseUrl() {
        Api2UrlService s1 = new Api2UrlService(new HashMap<>(), configPortalUrl, configApi2Url,
            apikeyServiceUrl, configGatewayUrl);
        assertEquals(EuropeanaStaticUrl.EUROPEANA_PORTAL_URL, s1.getPortalBaseUrl(""));
    }

    @Test
    void shouldReturnConfiguredPortalBaseUrlForRoute() {
        String baseUrl = "http://portal-url";
        Map<String, BaseUrlWrapper> urlMap = new HashMap<>();
        urlMap.put(TEST_ROUTE, new BaseUrlWrapper("", "", baseUrl));

        Api2UrlService service = new Api2UrlService(urlMap, "", configApi2Url, apikeyServiceUrl, configGatewayUrl);
        assertEquals(baseUrl, service.getPortalBaseUrl(TEST_ROUTE));
    }

    @Test
    void shouldReturnDefaultApi2BaseUrl() {
        Api2UrlService s1 = new Api2UrlService(new HashMap<>(), "", configApi2Url, apikeyServiceUrl, configGatewayUrl);
        assertEquals(Api2UrlService.API_BASEURL, s1.getApi2BaseUrl(""));
    }

    @Test
    void shouldReturnConfiguredApi2BaseUrlForRoute() {
        String baseUrl = "http://api2-base-url";
        Map<String, BaseUrlWrapper> urlMap = new HashMap<>();
        urlMap.put("test-route-1", new BaseUrlWrapper(baseUrl, "", ""));

        Api2UrlService service = new Api2UrlService(urlMap, "", configApi2Url, apikeyServiceUrl, configGatewayUrl);
        assertEquals(baseUrl, service.getApi2BaseUrl("test-route-1"));
    }

    @Test
    void shouldReturnDefaultApiGatewayBaseUrl() {
        Api2UrlService s1 = new Api2UrlService(new HashMap<>(), "", configApi2Url, apikeyServiceUrl, configGatewayUrl);
        assertEquals(EuropeanaStaticUrl.API_GATEWAY_URL, s1.getApiGatewayBaseUrl(""));
    }

    @Test
    void shouldReturnConfiguredApiGatewayBaseUrlForRoute() {
        String baseUrl = "http://api-gateway-base-url";
        Map<String, BaseUrlWrapper> urlMap = new HashMap<>();
        urlMap.put("test-route-1", new BaseUrlWrapper("", baseUrl, ""));

        Api2UrlService service = new Api2UrlService(urlMap, "", configApi2Url, apikeyServiceUrl, configGatewayUrl);
        assertEquals(baseUrl, service.getApiGatewayBaseUrl("test-route-1"));
    }

    @Test
    void testGetApiKeyServiceUrl() {
        String validateUrl = "";
        Api2UrlService s1 = new Api2UrlService(null, null, configApi2Url, validateUrl, configGatewayUrl);
        assertTrue(s1.getApikeyServiceUrl().isEmpty());

        validateUrl = "https://apikey.test.org/apikey";
        Api2UrlService s2 = new Api2UrlService(null, null, configApi2Url, validateUrl, configGatewayUrl);
        assertEquals(validateUrl, s2.getApikeyServiceUrl());
    }


    @Test
    void testGetRecordPortalUrl() {
        String baseUrl = null;
        String testRoute = "test-route-1";

        Map<String, BaseUrlWrapper> urlMap = new HashMap<>();
        urlMap.put(testRoute, new BaseUrlWrapper("", "", ""));

        Api2UrlService s1 = new Api2UrlService(urlMap, "", configApi2Url, apikeyServiceUrl, configGatewayUrl);
        assertEquals("https://www.europeana.eu/item/1/2", s1.getRecordPortalUrl(testRoute, "/1/2"));

        baseUrl = "pro.europeana.eu";
        urlMap.put(testRoute, new BaseUrlWrapper("", "", baseUrl));
        Api2UrlService s2 = new Api2UrlService(urlMap, "", configApi2Url, apikeyServiceUrl, configGatewayUrl);
        // In this case https:// is added by UrlBuilder class!
        assertEquals("https://pro.europeana.eu/item/x/y", s2.getRecordPortalUrl(testRoute, "/x/y"));

        baseUrl = "http://localhost:8080";
        urlMap.put(testRoute, new BaseUrlWrapper("", "", baseUrl));
        Api2UrlService s3 = new Api2UrlService(urlMap, "", configApi2Url, apikeyServiceUrl, configGatewayUrl);
        assertEquals("http://localhost:8080/item/x/y", s3.getRecordPortalUrl(testRoute,"/x/y"));
    }

    @Test
    void testGetThumbnailUrl() {
        String apiGatewayBaseUrl = null;
        String testRoute = "test-route-1";

        Map<String, BaseUrlWrapper> urlMap = new HashMap<>();
        urlMap.put(testRoute, new BaseUrlWrapper("", "", ""));

        Api2UrlService s1 = new Api2UrlService(urlMap, "", configApi2Url, apikeyServiceUrl, apiGatewayBaseUrl);
        assertEquals("https://api.europeana.eu/thumbnail/v2/url.json?uri=https%3A%2F%2Ftest1.eu&type=IMAGE",
                s1.getThumbnailUrl(testRoute, "https://test1.eu", DocType.IMAGE.getEnumNameValue()));

        apiGatewayBaseUrl = "api.europeana.eu";
        urlMap.put(testRoute, new BaseUrlWrapper("", apiGatewayBaseUrl, ""));
        Api2UrlService s2 = new Api2UrlService(urlMap, "", configApi2Url, apikeyServiceUrl, apiGatewayBaseUrl);
        // In this case https:// is added by UrlBuilder class!
        assertEquals("https://api.europeana.eu/thumbnail/v2/url.json?uri=https%3A%2F%2Ftest2.eu&type=IMAGE",
                s2.getThumbnailUrl(testRoute, "https://test2.eu", DocType.IMAGE.getEnumNameValue()));

        apiGatewayBaseUrl = "https://localhost";
        urlMap.put(testRoute, new BaseUrlWrapper("", apiGatewayBaseUrl, ""));
        Api2UrlService s3 = new Api2UrlService(urlMap, "", configApi2Url, apikeyServiceUrl, apiGatewayBaseUrl);
        assertEquals("https://localhost/thumbnail/v2/url.json?uri=http%3A%2F%2Ftest3.eu&type=IMAGE",
                s3.getThumbnailUrl(testRoute, "http://test3.eu", DocType.IMAGE.getEnumNameValue()));
    
        apiGatewayBaseUrl = "api.europeana.eu";
        urlMap.put(testRoute, new BaseUrlWrapper("https://testing", "", ""));
        Api2UrlService s4 = new Api2UrlService(urlMap, "", configApi2Url, apikeyServiceUrl, apiGatewayBaseUrl);
        assertEquals("https://api.europeana.eu/thumbnail/v2/url.json?uri=https%3A%2F%2Ftest1.eu&type=IMAGE",
                s4.getThumbnailUrl(testRoute, "https://test1.eu", DocType.IMAGE.getEnumNameValue()));
    }

    @Test
    void testGetRecordApi2Url() {
        /**
         * EA-2151: /api/v2/ only included in URLs when:
         *          - baseUrl is specified; and
         *          - its value is not https://api.europeana.eu
         */
        String baseUrl = null;
        Map<String, BaseUrlWrapper> urlMap = new HashMap<>();
        urlMap.put(TEST_ROUTE, new BaseUrlWrapper("", "", ""));

        Api2UrlService s1 = new Api2UrlService(urlMap, "", configApi2Url, apikeyServiceUrl, configGatewayUrl);
        assertEquals("https://api.europeana.eu/record/1/2.json?wskey=test", s1.getRecordApi2Url(TEST_ROUTE, "/1/2", "test"));

        baseUrl = "https://api.europeana.eu";
        urlMap.put(TEST_ROUTE, new BaseUrlWrapper(baseUrl, "", ""));
        Api2UrlService s2 = new Api2UrlService(urlMap, "", configApi2Url, apikeyServiceUrl, configGatewayUrl);
        // In this case https:// is added by UrlBuilder class!
        assertEquals("https://api.europeana.eu/record/x/y.json?wskey=test", s2.getRecordApi2Url(TEST_ROUTE, "/x/y", "test"));

        baseUrl = "http://localhost:8080";
        urlMap.put(TEST_ROUTE, new BaseUrlWrapper(baseUrl, "", ""));
        Api2UrlService s3 = new Api2UrlService(urlMap, "", configApi2Url, apikeyServiceUrl, configGatewayUrl);
        assertEquals("http://localhost:8080/record/x/y.json?wskey=test", s3.getRecordApi2Url(TEST_ROUTE, "/x/y", "test"));
    }
}
