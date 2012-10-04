package eu.europeana.api2.web.model.json.api1;
import static org.junit.Assert.*;

import org.apache.commons.lang.ArrayUtils;
import org.junit.Test;


public class ArrayCopyTest {

	String[] a;
	private String[] b = new String[]{"Bernstein project: http://www.memoryofpaper.eu"};
	private String[] expected = new String[]{"Bernstein project: http://www.memoryofpaper.eu"};

	@Test
	public void testUnitialized() {
		add(a, b);
		assertEquals(expected, a);
	}

	@Test
	public void testNull() {
		String[] a = null;
		add(a, b);
		assertEquals(expected, a);
	}

	@Test
	public void testEmpty() {
		String[] a = new String[]{};
		add(a, b);
		assertEquals(expected, a);
	}

	private void add(String[] field, String[] value) {
		if (value != null) {
			if (field == null) {
				field = value;
			} else {
				field = (String[]) ArrayUtils.addAll(field, value);
			}
		}
		// System.out.println(field);
		// return field;
	}
}
