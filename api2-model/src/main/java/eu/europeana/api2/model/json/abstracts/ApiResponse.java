package eu.europeana.api2.model.json.abstracts;

import com.fasterxml.jackson.annotation.JsonInclude;
import eu.europeana.corelib.utils.StringArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

/**
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 */
@JsonInclude(NON_EMPTY)
public abstract class ApiResponse {

    public String apikey;

    public boolean success = true;

    public String error;

    /**
     * @deprecated September 2019, not used anywhere
     */
    @Deprecated
    public Date statsStartTime;

    public Long statsDuration;

    public String debug;

    /**
     * @deprecated September 2019, we do not use requestNumber anymore, but we do output it with a fixed value because
     * we don't know if it will break clients if we remove it.
     */
    @Deprecated
    public Long requestNumber = 999L;

    public Map<String, Object> params;

    public ApiResponse() {
        // used by Jackson
    }

    public ApiResponse(String apikey) {
        this.apikey = apikey;
    }

    public void addParam(String name, Object value) {
        if (StringUtils.isNotBlank(name) && value != null) {
            if (params == null) {
                params = new LinkedHashMap<>();
            }
            params.put(name, value);
        }
    }

    public void addParams(Map<String, String[]> map, String... excl) {
        List<String> excluded = StringArrayUtils.toList(excl);
        if (map != null) {
            for (Entry<String, String[]> item : map.entrySet()) {
                if (excluded.contains(item.getKey())) {
                    continue;
                }

                if (item.getValue().length == 1) {
                    String value = item.getValue()[0];
                    if (NumberUtils.isNumber(value)) {
                        addParam(item.getKey(), NumberUtils.toLong(item.getValue()[0]));
                    } else {
                        addParam(item.getKey(), item.getValue()[0]);
                    }
                } else {
                    addParam(item.getKey(), item.getValue());
                }
            }
        }
    }
}
