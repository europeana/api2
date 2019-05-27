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
