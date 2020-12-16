package eu.europeana.api2.model.utils;

import java.util.Map;
import java.util.Optional;

public class RouteMatcher {

    private RouteMatcher() {
        // hide implicit constructor
    }

    /**
     * Finds matching map entry for route.
     * Code reproduced from Thumbnail API
     */
    public static <T> Optional<T> getEntryForRoute(String route, Map<String, T> sourceMap) {
        // make sure we use only the highest level part for matching and not the FQDN
        String topLevelName = getTopLevelName(route);

        // exact matching
        T result = sourceMap.get(topLevelName);
        if (result != null) {
            return Optional.of(result);
        }

        // fallback 1: try to match with "contains"
        for (Map.Entry<String, T> entry : sourceMap.entrySet()) {
            if (topLevelName.contains(entry.getKey())) {
                return Optional.ofNullable(entry.getValue());
            }
        }
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
