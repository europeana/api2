package eu.europeana.api2.model.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.Optional;

public class RouteMatcher {

    private static final Logger LOG = LogManager.getLogger(RouteMatcher.class);

    private RouteMatcher() {
        // hide implicit constructor
    }

    /**
     * Finds matching map entry for route.
     * Code reproduced from Thumbnail API
     */
    public static <T> Optional<T> getEntryForRoute(String route, Map<String, T> sourceMap, String type) {
        // make sure we use only the highest level part for matching and not the FQDN
        String topLevelName = getTopLevelName(route);

        // exact matching
        T result = sourceMap.get(topLevelName);
        if (result != null) {
            LOG.debug("Route {} - found exact match for {}", topLevelName, type);
            return Optional.of(result);
        }

        // fallback 1: try to match with "contains"
        for (Map.Entry<String, T> entry : sourceMap.entrySet()) {
            if (topLevelName.contains(entry.getKey())) {
                LOG.debug("Route {} - matched with {} for {}", topLevelName, entry.getKey(), type);
                return Optional.ofNullable(entry.getValue());
            }
        }

        LOG.warn("Route {} - no configured {} found", topLevelName, type);
        return Optional.empty();
    }

    private static String getTopLevelName(String route) {
        int i = route.indexOf('.');
        if (i >= 0) {
            return route.substring(0, i);
        }
        return route;
    }
}
