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

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

@JsonSerialize(include = Inclusion.NON_EMPTY)
public class TagCloudItem {
	
	private String label;
	
	private long count;
	
	public TagCloudItem() {
	}
	
	public TagCloudItem(String label, Long count) {
		this.label = label;
		this.count = count.longValue();
	}
	
	public String getLabel() {
		return label;
	}
	
	public long getCount() {
		return count;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	public void setCount(long count) {
		this.count = count;
	}

}
