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

package eu.europeana.api2.v2.model.json;

import java.util.List;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

import eu.europeana.api2.model.json.abstracts.ApiResponse;
import eu.europeana.corelib.neo4j.entity.Neo4jBean;

/**
 * @author Peter Kiraly <peter.kiraly@europeana.eu>
 */
@JsonSerialize(include = Inclusion.NON_EMPTY)
public class HierarchicalResult extends ApiResponse {

	public Neo4jBean object;

	public Neo4jBean parent;

	public boolean hasParent;

	public List<Neo4jBean> children;

	public Long childrenCount;

	@JsonProperty("preceeding-siblings")
	public List<Neo4jBean> preceedingSiblings;

	@JsonProperty("following-siblings")
	public List<Neo4jBean> followingSiblings;

	public String message;

	public HierarchicalResult(String apikey, String action) {
		super(apikey, action);
	}

	public HierarchicalResult(String apikey, String action, long requestNumber) {
		this(apikey, action);
		this.requestNumber = requestNumber;
	}
}
