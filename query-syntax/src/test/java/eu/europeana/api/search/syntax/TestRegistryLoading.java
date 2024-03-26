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


        ParserUtils.loadFieldRegistryFromResource(Constants.FIELD_REGISTRY_XML);

        FieldDeclaration expected= new FieldDeclaration("issued", FieldType.DATE,  "issued_date", "issued", "issued_date_end", "issued_date_begin");
        FieldDeclaration actual =FieldRegistry.INSTANCE.getField("issued");
        Assert.assertEquals(expected.getName(),actual.getName());
        Assert.assertEquals(expected.getType(),actual.getType());
        Assert.assertEquals(expected.getField(FieldMode.SEARCH), actual.getField(FieldMode.SEARCH));
        Assert.assertEquals(expected.getField(FieldMode.FACET), actual.getField(FieldMode.FACET));
        Assert.assertEquals(expected.getField(FieldMode.SORT_ASC), actual.getField(FieldMode.SORT_ASC));
        Assert.assertEquals(expected.getField(FieldMode.SORT_DESC), actual.getField(FieldMode.SORT_DESC));
    }

}
