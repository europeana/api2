package eu.europeana.api2.v2.model.translate;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QueryParser {

    public static final String GROUP_TO_CAPTURE = "annotate";

    private static final String[][] ANNOTATIONS = {
            {"\"(?<"+GROUP_TO_CAPTURE+">[^\"]*?[^\\\\])\"", QueryPartType.QUOTED.toString()}, //Quoted text
            {"\\[(?<"+GROUP_TO_CAPTURE+">[^\\[]*?[^\\\\])\\]", QueryPartType.NOTEXT.toString()}, //No text, brackets []
            {"\\{(?<"+GROUP_TO_CAPTURE+">[^\\{]*?[^\\\\])\\}", QueryPartType.NOTEXT.toString()}, //No text, brackets {}
            {"[^\\-\\w](?<"+GROUP_TO_CAPTURE+">\\-)\\w+", QueryPartType.UNARY_OPERATOR.toString()}, //NOT(-) except compound words
            {"(?<"+GROUP_TO_CAPTURE+">\\+\\b)", QueryPartType.UNARY_OPERATOR.toString()}, //+ clause
            {"[^\\-\\w](?<"+GROUP_TO_CAPTURE+">NOT)[^\\-\\w]", QueryPartType.UNARY_OPERATOR.toString()}, //NOT
            {"(?<"+GROUP_TO_CAPTURE+">\\w+?\\s*\\:)", QueryPartType.UNARY_OPERATOR.toString()}, //field clause
            {"(?<"+GROUP_TO_CAPTURE+">(\\S*[\\~\\^\\*\\/\\?\\\\]\\S*)+)", QueryPartType.NOTEXT.toString()}, //regex, boosting or escaping, do not translate
            {"(?<"+GROUP_TO_CAPTURE+">\\&\\&|\\|\\|)", QueryPartType.BINARY_OPERATOR.toString()},
            {"[^\\-\\w](?<"+GROUP_TO_CAPTURE+">AND)[^\\-\\w]", QueryPartType.BINARY_OPERATOR.toString()}, //AND
            {"[^\\-\\w](?<"+GROUP_TO_CAPTURE+">OR)[^\\-\\w]", QueryPartType.BINARY_OPERATOR.toString()}, //OR
            {"(?<"+GROUP_TO_CAPTURE+">\"|\\(|\\)|\\[|\\]|\\{|\\})", QueryPartType.DELIMITER_OPERATOR.toString()}
    }; //TODO: boosted expressions could be translated as well

    public Query parse(Query query){
        List<QueryPart> annotatedParts = annotate(query);
        return new Query(query.getText(), annotatedParts);
    }

    private List<QueryPart> annotate(Query query){
        List<QueryPart> annotatedParts = query.getQueryPartList();
        for (String[] ann: ANNOTATIONS) {
            String regex = ann[0];
            Pattern pattern = Pattern.compile(regex, Pattern.UNICODE_CHARACTER_CLASS); //TODO: compile before to increase performance
            QueryPartType label = QueryPartType.valueOf(ann[1]);
            annotatedParts = annotateQueryParts(pattern, label, annotatedParts);
        }
        return annotatedParts;
    }

    private List<QueryPart> annotateQueryParts(Pattern pattern, QueryPartType label, List<QueryPart> queryPartList){
        List<QueryPart> newList = new ArrayList<>();
        for (QueryPart part: queryPartList){
            if (part.getPartType() == QueryPartType.TEXT){
                newList.addAll(annotate(part,pattern, label));
            } else {
                newList.add(part);
            }
        }
        return  newList;
    }

    public List<QueryPart> annotate(QueryPart queryPart, Pattern pattern, QueryPartType partType){
        int bIndex = queryPart.getBeginIndex();
        List<QueryPart> queryParts = new ArrayList<>();
        String text = queryPart.getText();
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()){
            int startGroup = matcher.start(GROUP_TO_CAPTURE);
            int endGroup = matcher.end(GROUP_TO_CAPTURE);
            queryParts.add(new QueryPart(bIndex,startGroup+queryPart.getBeginIndex(), queryPart.getPartType(), queryPart.getQuery()));
            queryParts.add(new QueryPart(startGroup+queryPart.getBeginIndex(),endGroup+queryPart.getBeginIndex(), partType, queryPart.getQuery()));
            bIndex = endGroup+queryPart.getBeginIndex();
        }
        if (bIndex < queryPart.getEndIndex()){
            queryParts.add(new QueryPart(bIndex,queryPart.getEndIndex(), queryPart.getPartType(), queryPart.getQuery()));
        }
        return queryParts;
    }
}
