package eu.europeana.api2.v2.service.parser.jjt.datamodel;

public class AndExpression extends BinaryArgumentExpression {
  @Override
  public String getOperator() {
    return " AND ";
  }

}
