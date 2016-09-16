package eu.europeana.api2.utils;

import eu.europeana.api2.v2.model.json.UserResults;
import eu.europeana.api2.v2.model.json.user.Search;
import org.junit.Test;
import org.springframework.web.servlet.ModelAndView;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Created by Jeroen Jeurissen on 15-9-16.
 */
public class JsonUtilsTest {

    @Test
    public void testToJson() throws Exception {
        UserResults<Search> response = new UserResults<>("api2demo");
        response.items = new ArrayList<>();
        response.username = "test";
        response.itemsCount = (long) 1;
        Search search = new Search();
        search.id = 1l;
        search.query = "*:*";
        search.queryString = "http://localhost:8080*:0&start=1";
        search.dateSaved = Date.from(Instant.now());
        response.items.add(search);
        try {
            ModelAndView modelAndView = JsonUtils.toJson(response, null);
            assertNotNull(modelAndView);
        } catch (Exception e) {
            fail(e.getMessage());
        }

    }
}