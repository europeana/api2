package eu.europeana.api2.v2.service.parser.jjt.datamodel;

public abstract class BinaryArgumentExpression implements SetExpression{
  private   TopLevelExpression left;
  private TopLevelExpression right ;
  public  String operator="NA";

  public TopLevelExpression getLeft() {
    return left;
  }

  public TopLevelExpression getRight() {
    return right;
  }

  public void setLeft(TopLevelExpression left) {
    this.left = left;
  }

  public void setRight(TopLevelExpression right) {
    this.right = right;
  }

  public String getOperator() {
    return operator;
  }
}
