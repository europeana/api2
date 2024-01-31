package eu.europeana.api2.v2.service.parser.datamodel;

public class FunctionExpression extends TopLevelExpression{
 private FunctionQuery Left;
 private enum QueryOperator  { AND,OR,NOT,NA}
 //In case of NA and NOT operators the right functionQuery Should be null
 private FunctionQuery right;
}
