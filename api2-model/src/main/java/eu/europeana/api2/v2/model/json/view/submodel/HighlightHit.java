package eu.europeana.api2.v2.model.json.view.submodel;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import eu.europeana.api2.v2.model.json.common.LabelFrequency;

import java.util.ArrayList;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;


/**
 * Created by luthien on 03/01/2019.
 */

@JsonInclude(NON_EMPTY)
public class HighlightHit {

    @JsonIgnore
    private String type = "Hit";
    private String scope;
    private List<HitSelector> selectors = new ArrayList<>();


    public HighlightHit(String scope){
        this.scope = scope;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public List<HitSelector> getSelectors() {
        return selectors;
    }

    public void setSelectors(List<HitSelector> selectors) {
        this.selectors = selectors;
    }


}
