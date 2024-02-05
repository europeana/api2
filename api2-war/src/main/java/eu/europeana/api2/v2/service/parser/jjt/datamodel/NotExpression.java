package eu.europeana.api2.v2.service.parser.jjt.datamodel;

public class NotExpression implements SetExpression{

  TopLevelExpression right;
  String operator=" NOT ";

  public TopLevelExpression getRight() {
    return right;
  }

  public void setRight(TopLevelExpression right) {
    this.right = right;
  }

  public String getOperator() {
    return operator;
  }
}
