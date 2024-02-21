package eu.europeana.api.search.syntax;

import eu.europeana.api.search.syntax.field.FieldDeclaration;
import eu.europeana.api.search.syntax.field.FieldRegistry;
import eu.europeana.api.search.syntax.field.FieldType;
import eu.europeana.api.search.syntax.utils.ParserUtils;
import org.junit.Assert;
import org.junit.Test;

public class TestRegistryLoading {

    @Test
    public void testFieldRegistryloading(){

        ParserUtils.loadFieldRegistry();

        FieldDeclaration expected= new FieldDeclaration("issued", FieldType.date,  "issued_date", "issued_date", "issued_date_begin", "issued_date_end");//    FieldRegistry registry = FieldRegistry.INSTANCE;
        FieldDeclaration actual =FieldRegistry.INSTANCE.getField("issued");
        Assert.assertEquals(expected.getName(),actual.getName());
        Assert.assertEquals(expected.getType(),actual.getType());
    }

}
