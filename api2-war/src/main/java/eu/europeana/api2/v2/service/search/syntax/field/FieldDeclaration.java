/**
 * 
 */
package eu.europeana.api2.v2.service.search.syntax.field;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Hugo
 * @since 7 Feb 2024
 */
public class FieldDeclaration
{
    private String name;
    private Map<FieldMode,String> field_map;

    public FieldDeclaration(String name, String f_search, String f_facet
                          , String f_sort_asc, String f_sort_desc) {
        this.name        = name;

        field_map = new LinkedHashMap();
        field_map.put(FieldMode.search   , f_search);
        field_map.put(FieldMode.facet    , f_facet);
        field_map.put(FieldMode.sort_asc , f_sort_asc);
        field_map.put(FieldMode.sort_desc, f_sort_desc);
    }

    public String getName() {
        return name;
    }

    public String getField(FieldMode mode) {
        return field_map.get(mode);
    }
}
