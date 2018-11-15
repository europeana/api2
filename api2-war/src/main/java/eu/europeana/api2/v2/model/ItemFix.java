package eu.europeana.api2.v2.model;

import eu.europeana.corelib.definitions.edm.beans.FullBean;
import eu.europeana.corelib.definitions.edm.entity.Aggregation;
import eu.europeana.corelib.definitions.edm.entity.EuropeanaAggregation;
import eu.europeana.corelib.definitions.edm.entity.ProvidedCHO;
import eu.europeana.corelib.definitions.edm.entity.Proxy;

import java.util.List;
import java.util.Locale;

/**
 * For Metis the '/item' before ProvidedCHO, AggregatedCHO and ProxyFor values in Mongo was removed
 * so we need to add it in various places for consistency reasons. We can't add it in the FullBean class itself because
 * that is used by Metis.
 *
 * @author Patrick Ehlert
 * Created on 27-07-2018
 */
public class ItemFix {

    private ItemFix() {
        // empty constructor to prevent initialization
    }

    /**
     * Make sure the ProvideCHO, AggregatedCHO and ProxyFor values start with '/item'
     * @param bean
     */
    public static void apply(FullBean bean) {
        // ProvidedCHO
        List<ProvidedCHO> items = (List<ProvidedCHO>) bean.getProvidedCHOs();
        for (ProvidedCHO item : items) {
            if (!item.getAbout().toLowerCase(Locale.getDefault()).startsWith("/item")) {
                item.setAbout("/item" + item.getAbout());
            }
        }
        // AggregatedCHO
        for (Aggregation aggr : bean.getAggregations()) {
            if (!aggr.getAggregatedCHO().toLowerCase(Locale.getDefault()).startsWith("/item")) {
                aggr.setAggregatedCHO("/item" + aggr.getAggregatedCHO());
            }
        }
        EuropeanaAggregation euAggr = bean.getEuropeanaAggregation();
        if (!euAggr.getAggregatedCHO().toLowerCase(Locale.getDefault()).startsWith("/item")) {
            euAggr.setAggregatedCHO("/item" + euAggr.getAggregatedCHO());
        }
        // ProxyFor
        for (Proxy proxy: bean.getProxies()) {
            if (!proxy.getProxyFor().toLowerCase(Locale.getDefault()).startsWith("/item")) {
                proxy.setProxyFor("/item" + proxy.getProxyFor());
            }
        }
    }
}
