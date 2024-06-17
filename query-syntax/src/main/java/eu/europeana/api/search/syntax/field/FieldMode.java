/**
 * 
 */
package eu.europeana.api.search.syntax.field;

/**
 * @author Hugo
 * @since 7 Feb 2024
 */
public enum FieldMode
{
    SEARCH("search"),
    FACET("facet"),
    SORT_ASC("sort_asc"),
    SORT_DESC("sort_desc");
    FieldMode(String val) {
        this.value = val;
    }
    private String value;
    public String getValue() {
        return this.value;
    }
}
