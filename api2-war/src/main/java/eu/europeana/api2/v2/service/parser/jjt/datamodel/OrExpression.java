package eu.europeana.api2.v2.service.parser.jjt.datamodel;

public class OrExpression extends BinaryArgumentExpression {
@Override
  public String getOperator() {
    return " OR ";
  }

}
