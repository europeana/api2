package eu.europeana.api2.v2.model.json.view.submodel;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;


/**
 * Created by luthien on 03/01/2019.
 */

@JsonInclude(NON_EMPTY)
@JsonPropertyOrder({"type", "field", "prefix", "exact", "suffix"})
public class HitSelector {

    @JsonIgnore
    private String type = "TextQuoteSelector";
    private String field;
    private String exact;
    private String prefix;
    private String suffix;
    @JsonIgnore
    private String remainder;

    public HitSelector(String field){
        this.field = field;
    }


    public HitSelector(String prefix, String exact, String suffix, String remainder){
        this.prefix = prefix;
        this.exact = exact;
        this.suffix = suffix;
        this.remainder = remainder;
    }

    public String getSuffix() {
        return suffix;
    }

    public String getRemainder() {
        return remainder;
    }

    public void setRemainder(String remainder) {
        this.remainder = remainder;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getExact() {
        return exact;
    }

    public void setExact(String exact) {
        this.exact = exact;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }
}
