package eu.europeana.api2.config;

import eu.europeana.corelib.record.BaseUrlWrapper;
import org.junit.Test;

import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RouteConfigLoaderTest {


    private final String PORTAL_TEST_URL = "http://portal-url";
    private final String GATEWAY_TEST_URL = "http://gateway-url";
    private final String API_TEST_URL = "http://api-url";

    @Test
    public void shouldLoadConfigCorrectly() {
        Properties props = setBaseUrls();

        props.setProperty("route1.path", "test.1.com");
        props.setProperty("route1.data-source", "dataSource1");
        props.setProperty("route1.solr", "solr-id-1");

        props.setProperty("route2.path", "test.2.com");
        props.setProperty("route2.data-source", "dataSource2");
        props.setProperty("route2.solr", "solr-id-2");

        RouteConfigLoader configLoader = new RouteConfigLoader(props);
        configLoader.loadRouteConfig();

        Map<String, String> map = configLoader.getRouteDataSourceMap();

        assertEquals(2, map.size());
        assertEquals("dataSource1", map.get("test.1.com"));
        assertEquals("dataSource2", map.get("test.2.com"));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowOnMissingRoutePath() {
        Properties props = setBaseUrls();

        props.setProperty("route1.data-source", "ds-1");
        props.setProperty("route1.solr", "solr-id-1");

        new RouteConfigLoader(props).loadRouteConfig();
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowOnMissingRouteDataSource() {
        Properties props = setBaseUrls();

        props.setProperty("route1.path", "route.1.com");
        props.setProperty("route1.solr", "solr-id-1");

        new RouteConfigLoader(props).loadRouteConfig();
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowOnDuplicateRoutePath() {
        Properties props = setBaseUrls();
        String path = "test.1.com";

        props.setProperty("route1.path", path);
        props.setProperty("route1.data-source", "dataSource1");
        props.setProperty("route1.solr", "solr-id-1");

        // path matches route1.path
        props.setProperty("route2.path", path);
        props.setProperty("route2.data-source", "dataSource2");
        props.setProperty("route2.solr", "solr-id-2");

        new RouteConfigLoader(props).loadRouteConfig();
    }

    @Test
    public void shouldLoadDefaultBaseUrls() {
        Properties props = setBaseUrls();
        String routePath = configureRoute(props);

        RouteConfigLoader configLoader = new RouteConfigLoader(props);
        configLoader.loadRouteConfig();

        BaseUrlWrapper urls = configLoader.getRouteBaseUrlMap().get(routePath);
        assertEquals(PORTAL_TEST_URL, urls.getPortalBaseUrl());
        assertEquals(GATEWAY_TEST_URL, urls.getApiGatewayBaseUrl());
        assertEquals(API_TEST_URL, urls.getApi2BaseUrl());
    }


    @Test
    public void shouldLoadBaseUrlOverrides() {
        Properties props = setBaseUrls();
        String routePath = configureRoute(props);

        String portalBaseUrl = "http://portal-route-override";
        String gatewayBaseUrl = "http://gateway-route-override";
        String apiBaseUrl = "http://api-route-override";

        props.setProperty("route1.portal.baseUrl", portalBaseUrl);
        props.setProperty("route1.apiGateway.baseUrl", gatewayBaseUrl);
        props.setProperty("route1.api2.baseUrl", apiBaseUrl);

        RouteConfigLoader configLoader = new RouteConfigLoader(props);
        configLoader.loadRouteConfig();

        BaseUrlWrapper urls = configLoader.getRouteBaseUrlMap().get(routePath);
        assertEquals(portalBaseUrl, urls.getPortalBaseUrl());
        assertEquals(gatewayBaseUrl, urls.getApiGatewayBaseUrl());
        assertEquals(apiBaseUrl, urls.getApi2BaseUrl());
    }

    @Test
    public void shouldReturnEmptyBaseUrlsIfNoneConfigured() {
        // no baseURl props set
        Properties props = new Properties();
        String routePath = configureRoute(props);

        RouteConfigLoader configLoader = new RouteConfigLoader(props);
        configLoader.loadRouteConfig();

        BaseUrlWrapper urls = configLoader.getRouteBaseUrlMap().get(routePath);
        assertTrue(urls.getPortalBaseUrl().isEmpty());
        assertTrue(urls.getApiGatewayBaseUrl().isEmpty());
        assertTrue(urls.getApi2BaseUrl().isEmpty());
    }


    private Properties setBaseUrls() {
        Properties props = new Properties();
        props.setProperty("portal.baseUrl", PORTAL_TEST_URL);
        props.setProperty("apiGateway.baseUrl", GATEWAY_TEST_URL);
        props.setProperty("api2.baseUrl", API_TEST_URL);
        return props;
    }


    private String configureRoute(Properties props) {
        String routePath = "test.1.com";
        props.setProperty("route1.path", routePath);
        props.setProperty("route1.data-source", "dataSource1");
        props.setProperty("route1.solr", "solr-id-1");
        return routePath;
    }
}