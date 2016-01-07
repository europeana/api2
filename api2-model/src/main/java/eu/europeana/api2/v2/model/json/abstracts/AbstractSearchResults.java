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

package eu.europeana.api2.v2.model.json.abstracts;

import com.fasterxml.jackson.annotation.JsonInclude;
import eu.europeana.api2.model.json.abstracts.ApiResponse;

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

/**
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 */
@JsonInclude(NON_EMPTY)
public class AbstractSearchResults<T> extends ApiResponse {

	public long itemsCount;

	@JsonInclude(NON_NULL)
	public Long totalResults;

	public String nextCursor;

	@JsonInclude(NON_NULL)
	public List<T> items;

	public AbstractSearchResults(String apikey) {
		super(apikey);
	}

	public AbstractSearchResults() {
		// used by Jackson
		super();
	}

	public Long getTotalResults() {
		if (totalResults < itemsCount) {
			return itemsCount;
		}
		return totalResults;
	}

}
