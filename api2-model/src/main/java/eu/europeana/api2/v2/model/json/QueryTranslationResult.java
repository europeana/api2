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

package eu.europeana.api2.v2.model.json;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize.Inclusion;

import eu.europeana.api2.model.json.abstracts.ApiResponse;
import eu.europeana.corelib.utils.model.LanguageVersion;

@JsonSerialize(include = Inclusion.NON_EMPTY)
public class QueryTranslationResult extends ApiResponse {

	public List<LanguageVersion> translations;

	public String translatedQuery;

	public QueryTranslationResult(String apikey) {
		super(apikey);
	}

	public QueryTranslationResult(String apikey, long requestNumber) {
		this(apikey);
		this.requestNumber = requestNumber;
	}
}
