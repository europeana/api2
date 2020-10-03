package eu.europeana.api2.v2.service;

import eu.europeana.api2.config.RouteConfigLoader;
import eu.europeana.corelib.record.DataSourceWrapper;
import eu.europeana.corelib.record.config.RecordServerConfig;
import eu.europeana.corelib.search.config.SearchServerConfig;
import org.apache.solr.client.solrj.SolrClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

// Mock out relevant dependencies instead of dealing with Spring context
@RunWith(MockitoJUnitRunner.class)
public class RouteDataServiceTest {

    private RouteDataService routeService;
    @Mock
    private RouteConfigLoader routeConfig;
    @Mock
    private RecordServerConfig recordServerConfig;
    @Mock
    private SearchServerConfig searchServerConfig;


    private final DataSourceWrapper ds1 = new DataSourceWrapper();
    private final DataSourceWrapper ds2 = new DataSourceWrapper();

    private final SolrClient solrClient1 = mock(SolrClient.class);
    private final SolrClient solrClient2 = mock(SolrClient.class);

    private Map<String, String> mongoRouteMapping;
    private Map<String, String> solrRouteMapping;


    @Before
    public void setUp() throws Exception {
        mongoRouteMapping = new HashMap<>();
        mongoRouteMapping.put("localhost", "ds-1");
        mongoRouteMapping.put("api-acceptance", "ds-2");

        solrRouteMapping = new HashMap<>();
        solrRouteMapping.put("localhost", "solr-client-1");
        solrRouteMapping.put("api-acceptance", "solr-client-2");

        when(routeConfig.getRouteDataSourceMap()).thenReturn(mongoRouteMapping);
        when(recordServerConfig.getDataSourceById(eq("ds-1"))).thenReturn(Optional.of(ds1));
        when(recordServerConfig.getDataSourceById(eq("ds-2"))).thenReturn(Optional.of(ds2));

        when(routeConfig.getRouteSolrMap()).thenReturn(solrRouteMapping);
        when(searchServerConfig.getSolrClientById(eq("solr-client-1"))).thenReturn(Optional.of(solrClient1));
        when(searchServerConfig.getSolrClientById(eq("solr-client-2"))).thenReturn(Optional.of(solrClient2));

        routeService = new RouteDataService(routeConfig, recordServerConfig, searchServerConfig);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowOnInvalidRouteConfig() {
        // un-configured datasource in mapping
        mongoRouteMapping.put("test-route", "invalid-data-source");
        when(recordServerConfig.getDataSourceById(eq("invalid-data-source"))).thenReturn(Optional.empty());

        routeService.validateRouteConfig();
    }

    @Test
    public void shouldMatchFullPathForRecordServer() {
        Optional<DataSourceWrapper> result = routeService.getRecordServerForRequest("localhost");
        assertTrue(result.isPresent());
        assertEquals(ds1, result.get());
    }

    @Test
    public void shouldMatchFullPathForSearchServer() {
        Optional<SolrClient> result = routeService.getSolrClientForRequest("localhost");
        assertTrue(result.isPresent());
        assertEquals(solrClient1, result.get());
    }


    @Test
    public void shouldMatchPartialPathForRecordServer() {
        Optional<DataSourceWrapper> result = routeService.getRecordServerForRequest("search-api-acceptance.eanadev.org");
        assertTrue(result.isPresent());
        assertEquals(ds2, result.get());
    }

    @Test
    public void shouldMatchPartialPathForSearchServer() {
        Optional<SolrClient> result = routeService.getSolrClientForRequest("search-api-acceptance.eanadev.org");
        assertTrue(result.isPresent());
        assertEquals(solrClient2, result.get());
    }
}