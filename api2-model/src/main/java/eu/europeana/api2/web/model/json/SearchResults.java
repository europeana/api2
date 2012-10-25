/*
 * Copyright 2007-2012 The Europeana Foundation
 *
 *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved 
 *  by the European Commission;
 *  You may not use this work except in compliance with the Licence.
 *  
 *  You may obtain a copy of the Licence at:
 *  http://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under 
 *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of 
 *  any kind, either express or implied.
 *  See the Licence for the specific language governing permissions and limitations under 
 *  the Licence.
 */

package eu.europeana.api2.web.model.json;

import java.util.List;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

import eu.europeana.api2.web.model.json.abstracts.AbstractSearchResults;
import eu.europeana.api2.web.model.json.facets.Facet;
import eu.europeana.api2.web.model.json.spellcheck.SpellCheck;
import eu.europeana.corelib.definitions.model.web.BreadCrumb;

/**
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 */
@JsonSerialize(include = Inclusion.NON_EMPTY)
public class SearchResults<T> extends AbstractSearchResults<T> {

	public List<BreadCrumb> breadCrumbs;

	public List<Facet> facets;

	public SpellCheck spellcheck;

	public SearchResults(String apikey, String action) {
		super(apikey, action);
	}
}
