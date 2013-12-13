package eu.europeana.api2.v2.web.controller;

import static org.junit.Assert.*;

import org.junit.Test;

public class SearchControllerTest {

	/**
	 * Testing clearReusability();
	 */
	@Test
	public void testClearReusability() {
		SearchController searchController = new SearchController();

		assertArrayEquals(new String[] {"open"}, searchController.clearReusability(new String[]{"open"}));
		assertArrayEquals(new String[] {"open", "permission"}, searchController.clearReusability(new String[]{"open", "permission"}));
		assertArrayEquals(new String[] {"open", "permission"}, searchController.clearReusability(new String[]{"open permission"}));
		assertArrayEquals(new String[] {"open", "permission"}, searchController.clearReusability(new String[]{"open+permission"}));
		assertArrayEquals(new String[] {"open", "permission"}, searchController.clearReusability(new String[]{"open,permission"}));

		// this is not cleared
		assertArrayEquals(new String[] {"open/permission"}, searchController.clearReusability(new String[]{"open/permission"}));
	}

}
