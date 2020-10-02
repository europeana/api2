package eu.europeana.api2.config;

import org.junit.Test;

import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class RouteConfigLoaderTest {

    @Test
    public void shouldLoadConfigCorrectly() {
        Properties props = new Properties();

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
}