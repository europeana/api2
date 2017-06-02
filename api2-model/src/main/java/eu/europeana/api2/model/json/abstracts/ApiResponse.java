/*
 * Copyright 2007-2015 The Europeana Foundation
 *
 * Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 * by the European Commission;
 * You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 * http://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 * any kind, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under
 * the Licence.
 */

package eu.europeana.api2.model.json.abstracts;

import com.fasterxml.jackson.annotation.JsonInclude;
import eu.europeana.corelib.utils.StringArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

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

	public Date statsStartTime;

	public Long statsDuration;

	public String debug;

	public Long requestNumber;

	public Map<String, Object> params;

	public ApiResponse(String apikey) {
		this.apikey = apikey;
	}

	public ApiResponse() {
		// used by Jackson
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
