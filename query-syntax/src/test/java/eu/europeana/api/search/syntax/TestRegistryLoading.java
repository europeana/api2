package eu.europeana.api.search.syntax;

import eu.europeana.api.search.syntax.field.FieldDeclaration;
import eu.europeana.api.search.syntax.field.FieldMode;
import eu.europeana.api.search.syntax.field.FieldRegistry;
import eu.europeana.api.search.syntax.field.FieldType;
import eu.europeana.api.search.syntax.utils.Constants;
import eu.europeana.api.search.syntax.utils.ParserUtils;
import org.junit.Assert;
import org.junit.Test;

public class TestRegistryLoading {

    @Test
    public void testFieldRegistryloading(){


        ParserUtils.loadFieldRegistryFromResource(this.getClass(), Constants.FIELD_REGISTRY_XML);

        FieldDeclaration expected= new FieldDeclaration("issued", FieldType.date,  "issued_date", "issued", "issued_date_end", "issued_date_begin");//    FieldRegistry registry = FieldRegistry.INSTANCE;
        FieldDeclaration actual =FieldRegistry.INSTANCE.getField("issued");
        Assert.assertEquals(expected.getName(),actual.getName());
        Assert.assertEquals(expected.getType(),actual.getType());
        Assert.assertEquals(expected.getField(FieldMode.search), actual.getField(FieldMode.search));
        Assert.assertEquals(expected.getField(FieldMode.facet), actual.getField(FieldMode.facet));
        Assert.assertEquals(expected.getField(FieldMode.sort_asc), actual.getField(FieldMode.sort_asc));
        Assert.assertEquals(expected.getField(FieldMode.sort_desc), actual.getField(FieldMode.sort_desc));
    }

}
