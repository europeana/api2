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

package eu.europeana.api2.v2.model;

/**
 * Data object for a name - integer value pair. It is used for storing Solr parameters
 * @author Peter.Kiraly@kb.nl
 */

// TODO THIS IS A DUPLICATE CLASS (also in api-model)

public class NumericFacetParameter {


	private String name;

	private Integer value;

	public NumericFacetParameter(String name, Integer value) {
		this.name = name;
		this.value = value;
	}

	public NumericFacetParameter(String name, String value) {
		Integer intValue = 0;
		try {
			intValue = Integer.parseInt(value);
		} catch (NumberFormatException e) {
			// 
		}
		this.name = name;
		this.value = intValue;
	}

	public String getName() {
		return name;
	}

	public Integer getValue() {
		return value;
	}

}
