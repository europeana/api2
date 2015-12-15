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

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Testing the NumericFacetParameter class against different initializations
 * @author Peter.Kiraly@kb.nl
 */
public class NumericFacetParameterTest {

	/**
	 * Testing with int
	 */
	@Test
	public void initializeWithInt() {
		NumericFacetParameter parameter = new NumericFacetParameter("real", 3);
		assertNotNull(parameter);
		assertEquals("real", parameter.getName());
		assertEquals(new Integer(3), parameter.getValue());
	}

	/**
	 * Testing with numeric string
	 */
	@Test
	public void initializeWithString() {
		NumericFacetParameter parameter = new NumericFacetParameter("real", "3");
		assertNotNull(parameter);
		assertEquals("real", parameter.getName());
		assertEquals(new Integer(3), parameter.getValue());
	}

	/**
	 * Testing with empty string
	 */
	@Test
	public void initializeWithEmptyValue() {
		NumericFacetParameter parameter;
		parameter = new NumericFacetParameter("real", "");
		assertNotNull(parameter);
		assertEquals(new Integer(0), parameter.getValue());
	}

	/**
	 * Testing with alphabetic string
	 */
	@Test
	public void initializeWithNonValidValue() {
		NumericFacetParameter parameter;
		parameter = new NumericFacetParameter("real", "real");
		assertNotNull(parameter);
		assertEquals(new Integer(0), parameter.getValue());
	}

	/**
	 * Testing with negative number.
	 * Since Solr allows it, we also allow it.
	 */
	@Test
	public void initializeWithNegativeValue() {
		NumericFacetParameter parameter;
		parameter = new NumericFacetParameter("real", "-3");
		assertNotNull(parameter);
		assertEquals(new Integer(-3), parameter.getValue());
	}

}
