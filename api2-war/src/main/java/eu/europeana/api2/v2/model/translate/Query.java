package eu.europeana.api2.v2.model.translate;

import java.util.ArrayList;
import java.util.List;

// TODO if we move to production with this, we probably want to merge this Query class with the
//  eu.europeana.corelib.definitions.solr.model.Query class
public class Query {

    private String text;
    private List<QueryPart> queryPartList;

    public Query(String text){
        this.text = text;
        this.queryPartList = new ArrayList<>();
        queryPartList.add(new QueryPart(0,text.length(), this));
    }

    public Query(String text, List<QueryPart> queryPartList){
        this.text = text;
        this.queryPartList = new ArrayList<>();
        this.queryPartList = queryPartList;
    }

    public String getText() {
        return text;
    }

    public List<QueryPart> getQueryPartList() {
        return queryPartList;
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        for (QueryPart part: this.getQueryPartList()){
            sb.append("[").append(part.toString()).append("]");
        }
        return sb.toString();
    }

}
