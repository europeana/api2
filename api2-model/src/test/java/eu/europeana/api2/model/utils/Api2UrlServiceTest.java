package eu.europeana.api2.model.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import eu.europeana.corelib.definitions.EuropeanaStaticUrl;
import eu.europeana.corelib.definitions.solr.DocType;
import org.junit.jupiter.api.Test;

/**
 * Test Api2UrlService class
 */
public class Api2UrlServiceTest {

    @Test
    public void testGetPortalBaseUrl() {
        String baseUrl = null;
        Api2UrlService s1 = new Api2UrlService(baseUrl,null);
        assertEquals(EuropeanaStaticUrl.EUROPEANA_PORTAL_URL, s1.getPortalBaseUrl());

        baseUrl = "www.europeana.eu";
        Api2UrlService s2 = new Api2UrlService(baseUrl,null);
        assertEquals(baseUrl, s2.getPortalBaseUrl());

        baseUrl = "http://www.europeana.eu";
        Api2UrlService s3 = new Api2UrlService(baseUrl,null);
        assertEquals(baseUrl, s3.getPortalBaseUrl());
    }

    @Test
    public void testGetApi2BaseUrl() {
        String baseUrl = null;
        Api2UrlService s1 = new Api2UrlService(null,baseUrl);
        assertEquals(Api2UrlService.API_BASEURL, s1.getApi2BaseUrl());

        baseUrl = "api.europeana.eu";
        Api2UrlService s2 = new Api2UrlService(null, baseUrl);
        assertEquals(baseUrl, s2.getApi2BaseUrl());

        baseUrl = "http://localhost";
        Api2UrlService s3 = new Api2UrlService(null, baseUrl);
        assertEquals(baseUrl, s3.getApi2BaseUrl());
    }

    @Test
    public void testGetRecordPortalUrl() {
        String baseUrl = null;
        Api2UrlService s1 = new Api2UrlService(baseUrl,null);
        assertEquals("https://www.europeana.eu/portal/record/1/2.html",
                s1.getRecordPortalUrl("/1/2"));

        baseUrl = "pro.europeana.eu";
        Api2UrlService s2 = new Api2UrlService(baseUrl,null);
        // In this case https:// is added by UrlBuilder class!
        assertEquals("https://pro.europeana.eu/portal/record/x/y.html",
                s2.getRecordPortalUrl("/x/y"));

        baseUrl = "http://localhost:8080";
        Api2UrlService s3 = new Api2UrlService(baseUrl,null);
        assertEquals("http://localhost:8080/portal/record/x/y.html",
                s3.getRecordPortalUrl("/x/y"));
    }

    @Test
    public void testGetThumbnailUrl() {
        String baseUrl = null;
        Api2UrlService s1 = new Api2UrlService(null,baseUrl);
        assertEquals("https://api.europeana.eu/api/v2/thumbnail-by-url.json?uri=https%3A%2F%2Ftest1.eu&type=IMAGE",
                s1.getThumbnailUrl("https://test1.eu", DocType.IMAGE));

        baseUrl = "api.europeana.eu";
        Api2UrlService s2 = new Api2UrlService(null,baseUrl);
        // In this case https:// is added by UrlBuilder class!
        assertEquals("https://api.europeana.eu/api/v2/thumbnail-by-url.json?uri=https%3A%2F%2Ftest2.eu&type=IMAGE",
                s2.getThumbnailUrl("https://test2.eu", DocType.IMAGE));

        baseUrl = "https://localhost";
        Api2UrlService s3 = new Api2UrlService(null,baseUrl);
        assertEquals("https://localhost/api/v2/thumbnail-by-url.json?uri=http%3A%2F%2Ftest3.eu&type=IMAGE",
                s3.getThumbnailUrl("http://test3.eu", DocType.IMAGE));
    }
}
