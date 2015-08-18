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

package eu.europeana.api2demo.web.model;

import eu.europeana.api2.v2.model.json.UserResults;
import eu.europeana.api2.v2.model.json.user.Search;

public class UserSearches extends UserResults<Search> {
	
	public UserSearches() {
		super();
	}
	
	public UserSearches(String apikey, String action) {
		super(apikey, action);
	}

}
