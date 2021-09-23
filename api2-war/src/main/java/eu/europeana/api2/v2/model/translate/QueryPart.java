package eu.europeana.api2.v2.model.translate;

public class QueryPart {

    private int beginIndex; //inclusive; begins in 0
    private int endIndex;   //exclusive; string starts in initOffset and ends in endOffset -1
    private QueryPartType partType = QueryPartType.TEXT;
    private Query query;

    public QueryPart(int beginIndex, int endIndex, Query query){
        this.beginIndex = beginIndex;
        this.endIndex = endIndex;
        this.query = query;
    }

    public QueryPart(int beginIndex, int endIndex, QueryPartType partType, Query query){
        this.beginIndex = beginIndex;
        this.endIndex = endIndex;
        this.query = query;
        this.partType = partType;
    }

    public int getBeginIndex() {
        return beginIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public void setPartType(QueryPartType partType) {
        this.partType = partType;
    }

    public QueryPartType getPartType() {
        return partType;
    }

    public Query getQuery() {
        return query;
    }

    public String getText() throws IndexOutOfBoundsException {
        return this.query.getText().substring(beginIndex, endIndex);
    }

    @Override
    public String toString() {
        return this.getText();
    }

}
