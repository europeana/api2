package eu.europeana.api.search.syntax.field;

/**
 * @author Hugo
 * @since 7 Feb 2024
 */
public enum FieldType
{

    TEXT("text"), GEO("geo"), DATE("date");
    FieldType(String val) {
        this.value = val;
    }
    private String value;
    public String getValue() {
        return this.value;
    }
    public static FieldType getEnumByValue(String code){
        for(FieldType e : FieldType.values()){
            if(code.equals(e.getValue()))  return e;
        }
        return null;
    }







}
