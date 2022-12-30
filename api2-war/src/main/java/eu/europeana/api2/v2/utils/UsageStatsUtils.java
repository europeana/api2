package eu.europeana.api2.v2.utils;

import eu.europeana.api.commons.definitions.statistics.UsageStatsFields;
import eu.europeana.api.commons.definitions.statistics.search.HighQualityMetric;
import eu.europeana.corelib.definitions.solr.model.Query;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

public class UsageStatsUtils extends UsageStatsFields {

    private static final String URL_PREFIX = "\"http://data.europeana.eu/";
    private static final String ORGANIZATION_QUERY = "foaf_organization:*";
    public static final String QUERY_ALL = "*";
    public static final String QUERY_ALL_MINUS_0 = "NOT contentTier:0";
    public static final String QUERY_T0 = "contentTier:0";
    public static final String QUERY_T2 = "contentTier:(2 OR 3 OR 4)";
    public static final String QUERY_T2_TA = "contentTier:(2 OR 3 OR 4) AND NOT metadataTier:0";
    public static final String QUERY_T3 = "contentTier:(3 OR 4)";
    public static final String QUERY_TA = "NOT metadataTier:0";

    private static final String SOLR_SOUND = "SOUND";

    /**
     * creates query for solr
     * @param entityType
     * @return
     */
    public static Query createQueryForLinkedItems(String entityType) {
        /** The Organizations URIs are not being indexed as expected.Hence we are making and exception
         * for Organizations for now to get the counts this way
         * https://api.europeana.eu/record/search.json?wskey=api2demo&query=foaf_organization:*&rows=0
         */
        if(StringUtils.equals(entityType, ORGANISATION)) {
            Query query = new Query(ORGANIZATION_QUERY);
            query.setPageSize(0);
            return query;
        } else {
            StringBuilder q = new StringBuilder(URL_PREFIX);
            q.append(entityType).append("/\"");
            Query query = new Query(q.toString());
            query.setPageSize(0);
            return query;
        }
    }

    public static HighQualityMetric processFacetMap(Map<String, Long> valueCountmap) {
        HighQualityMetric metric = new HighQualityMetric();

        for (Map.Entry<String, Long> entry : valueCountmap.entrySet()) {
           if (StringUtils.equals(entry.getKey(), IMAGE)) {
               metric.setImage(entry.getValue());
           }
            if (StringUtils.equals(entry.getKey(), TEXT)) {
                metric.setText(entry.getValue());
            }
            if (StringUtils.equals(entry.getKey(), SOLR_SOUND)) {
                metric.setAudio(entry.getValue());
            }
            if (StringUtils.equals(entry.getKey(), VIDEO)) {
                metric.setVideo(entry.getValue());
            }
            if (StringUtils.equals(entry.getKey(), THREE_D)) {
                metric.setThreeD(entry.getValue());
            }
        }
        metric.setTotal(metric.getOverall());
        return metric;
    }
}
