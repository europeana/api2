package eu.europeana.api2.model.utils;

import eu.europeana.corelib.definitions.EuropeanaStaticUrl;
import eu.europeana.corelib.definitions.solr.DocType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Api2UrlService class
 */
public class Api2UrlServiceTest {

    @Test
    public void testGetPortalBaseUrl() {
        String baseUrl = null;

        Api2UrlService s1 = new Api2UrlService(baseUrl,null, null, null);
        assertEquals(EuropeanaStaticUrl.EUROPEANA_PORTAL_URL, s1.getPortalBaseUrl());

        baseUrl = "www.europeana.eu";
        Api2UrlService s2 = new Api2UrlService(baseUrl, null, null, null);
        assertEquals(baseUrl, s2.getPortalBaseUrl());

        baseUrl = "http://www.europeana.eu";
        Api2UrlService s3 = new Api2UrlService(baseUrl,null, null, null);
        assertEquals(baseUrl, s3.getPortalBaseUrl());
    }

    @Test
    public void testGetApi2BaseUrl() {
        String baseUrl = null;

        Api2UrlService s1 = new Api2UrlService(null, baseUrl, null, null);
        assertEquals(Api2UrlService.API_BASEURL, s1.getApi2BaseUrl());

        baseUrl = "api.europeana.eu";
        Api2UrlService s2 = new Api2UrlService(null, baseUrl, null, null);
        assertEquals(baseUrl, s2.getApi2BaseUrl());

        baseUrl = "http://localhost";
        Api2UrlService s3 = new Api2UrlService(null, baseUrl, null, null);
        assertEquals(baseUrl, s3.getApi2BaseUrl());
    }

    @Test
    public void testGetApiKeyValidateUrl() {
        String validateUrl = null;

        Api2UrlService s1 = new Api2UrlService(null, null, validateUrl, null);
        assertNull(s1.getApikeyValidateUrl());

        validateUrl = "https://apikey.test.org/apikey/validate";
        Api2UrlService s2 = new Api2UrlService(null, null, validateUrl, null);
        assertEquals(validateUrl, s2.getApikeyValidateUrl());
    }

    @Test
    public void testGetApiGatewayBaseUrl() {
        String apiGatewayBaseUrl = null;

        Api2UrlService s1 = new Api2UrlService(null, null, null, apiGatewayBaseUrl);
        assertNotNull(s1.getApiGatewayBaseUrl());
        assertEquals(EuropeanaStaticUrl.API_GATEWAY_URL, s1.getApiGatewayBaseUrl());

        apiGatewayBaseUrl = "api.europeana.eu";
        Api2UrlService s2 = new Api2UrlService(null, null, null, apiGatewayBaseUrl);
        assertEquals(apiGatewayBaseUrl, s2.getApiGatewayBaseUrl());

        apiGatewayBaseUrl = "https://api.europeana.eu";
        Api2UrlService s3 = new Api2UrlService(null, null, null, apiGatewayBaseUrl);
        assertEquals(apiGatewayBaseUrl, s3.getApiGatewayBaseUrl());
    }

    @Test
    public void testGetRecordPortalUrl() {
        String baseUrl = null;

        Api2UrlService s1 = new Api2UrlService(baseUrl, null, null, null);
        assertEquals("https://www.europeana.eu/item/1/2", s1.getRecordPortalUrl("/1/2"));

        baseUrl = "pro.europeana.eu";
        Api2UrlService s2 = new Api2UrlService(baseUrl, null, null, null);
        // In this case https:// is added by UrlBuilder class!
        assertEquals("https://pro.europeana.eu/item/x/y", s2.getRecordPortalUrl("/x/y"));

        baseUrl = "http://localhost:8080";
        Api2UrlService s3 = new Api2UrlService(baseUrl, null, null, null);
        assertEquals("http://localhost:8080/item/x/y", s3.getRecordPortalUrl("/x/y"));
    }

    @Test
    public void testGetThumbnailUrl() {
        String apiGatewayBaseUrl = null;

        Api2UrlService s1 = new Api2UrlService(null, null, null, apiGatewayBaseUrl);
        assertEquals("https://api.europeana.eu/thumbnail/v2/url.json?uri=https%3A%2F%2Ftest1.eu&type=IMAGE",
                s1.getThumbnailUrl("https://test1.eu", DocType.IMAGE));

        apiGatewayBaseUrl = "api.europeana.eu";
        Api2UrlService s2 = new Api2UrlService(null, null, null, apiGatewayBaseUrl);
        // In this case https:// is added by UrlBuilder class!
        assertEquals("https://api.europeana.eu/thumbnail/v2/url.json?uri=https%3A%2F%2Ftest2.eu&type=IMAGE",
                s2.getThumbnailUrl("https://test2.eu", DocType.IMAGE));

        apiGatewayBaseUrl = "https://localhost";
        Api2UrlService s3 = new Api2UrlService(null, null, null, apiGatewayBaseUrl);
        assertEquals("https://localhost/thumbnail/v2/url.json?uri=http%3A%2F%2Ftest3.eu&type=IMAGE",
                s3.getThumbnailUrl("http://test3.eu", DocType.IMAGE));

        Api2UrlService s4 = new Api2UrlService(null, "https://testing", null, null);
        assertEquals("https://api.europeana.eu/thumbnail/v2/url.json?uri=https%3A%2F%2Ftest1.eu&type=IMAGE",
                s4.getThumbnailUrl("https://test1.eu", DocType.IMAGE));
    }

    @Test
    public void testGetRecordApi2Url() {
        String baseUrl = null;

        Api2UrlService s1 = new Api2UrlService(null, baseUrl, null, null);
        assertEquals("https://api.europeana.eu/api/v2/record/1/2.json?wskey=test", s1.getRecordApi2Url("/1/2", "test"));

        baseUrl = "api.europeana.eu";
        Api2UrlService s2 = new Api2UrlService(null, baseUrl, null, null);
        // In this case https:// is added by UrlBuilder class!
        assertEquals("https://api.europeana.eu/api/v2/record/x/y.json?wskey=test", s2.getRecordApi2Url("/x/y", "test"));

        baseUrl = "http://localhost:8080";
        Api2UrlService s3 = new Api2UrlService(null, baseUrl, null, null);
        assertEquals("http://localhost:8080/api/v2/record/x/y.json?wskey=test", s3.getRecordApi2Url("/x/y", "test"));
    }
}
