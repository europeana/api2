package eu.europeana.api2.v2.service;

import eu.europeana.api2.config.RouteConfigLoader;
import eu.europeana.corelib.record.DataSourceWrapper;
import eu.europeana.corelib.record.config.RecordServerBeanConfig;
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
import static org.mockito.Mockito.when;

// Mock out relevant dependencies instead of loading Spring context
@RunWith(MockitoJUnitRunner.class)
public class RouteDataServiceTest {

    private RouteDataService routeService;

    @Mock
    private RouteConfigLoader routeConfig;


    @Mock
    private RecordServerBeanConfig recordServerConfig;

    private final DataSourceWrapper ds1 = new DataSourceWrapper();
    private final DataSourceWrapper ds2 = new DataSourceWrapper();

    @Before
    public void setUp() throws Exception {
        Map<String, String> routeDataSourceMapping = new HashMap<>();
        routeDataSourceMapping.put("localhost", "ds-1");
        routeDataSourceMapping.put("api-acceptance", "ds-2");


        when(routeConfig.getRouteDataSourceMap()).thenReturn(routeDataSourceMapping);
        when(recordServerConfig.getDataSourceById(eq("ds-1"))).thenReturn(Optional.of(ds1));
        when(recordServerConfig.getDataSourceById(eq("ds-2"))).thenReturn(Optional.of(ds2));

        routeService = new RouteDataService(routeConfig, recordServerConfig);
    }

    @Test
    public void shouldMatchFullPath() {
        Optional<DataSourceWrapper> result = routeService.getRecordServerForRequest("api-acceptance.eanadev.org");
        assertTrue(result.isPresent());
        assertEquals(ds2, result.get());
    }



    @Test
    public void shouldMatchPartialPath() {
        Optional<DataSourceWrapper> result = routeService.getRecordServerForRequest("search-api-acceptance.eanadev.org");
        assertTrue(result.isPresent());
        assertEquals(ds2, result.get());
    }
}