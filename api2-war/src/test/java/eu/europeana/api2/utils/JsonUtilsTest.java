package eu.europeana.api2.utils;

import eu.europeana.api2.v2.model.json.SearchResults;
import eu.europeana.api2.v2.model.json.common.LabelFrequency;
import eu.europeana.api2.v2.model.json.view.submodel.Facet;
import org.junit.Test;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Created by Jeroen Jeurissen on 15-9-16.
 */
public class JsonUtilsTest {

    @Test
    public void testToJson() {
        SearchResults response = new SearchResults<>("api2demo");
        response.items = new ArrayList<>();
        response.itemsCount = 1;

        LabelFrequency lf = new LabelFrequency();
        lf.label = "someLabel";
        lf.count = 3;
        Facet facet = new Facet();
        facet.name = "test";
        facet.fields = new ArrayList();
        facet.fields.add(lf);
        response.facets = new ArrayList();
        response.facets.add(facet);

        try {
            ModelAndView modelAndView = JsonUtils.toJson(response, null);
            assertNotNull(modelAndView);
        } catch (Exception e) {
            fail(e.getMessage());
        }

    }
}