package eu.europeana.api2.v2.service.parser.jjt.datamodel;

public class FieldQueryExpression implements TopLevelExpression{
private String fieldName;
private FieldArgumentExpression value;

  public String getFieldName() {
    return fieldName;
  }

  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }

  public FieldArgumentExpression getValue() {
    return value;
  }

  public void setValue(FieldArgumentExpression value) {
    this.value = value;
  }
}
