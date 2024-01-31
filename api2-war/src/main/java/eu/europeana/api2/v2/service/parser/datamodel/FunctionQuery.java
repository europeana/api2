package eu.europeana.api2.v2.service.parser.datamodel;

import java.util.List;

public class FunctionQuery {
  private String functionName ;

  private String fieldName;
  private List<FunctionParam> paramlist;

  public String getFunctionName() {
    return functionName;
  }

  public void setFunctionName(String functionName) {
    this.functionName = functionName;
  }

  public String getFieldName() {
    return fieldName;
  }

  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }

  public List<FunctionParam> getParamlist() {
    return paramlist;
  }

  public void setParamlist(
      List<FunctionParam> paramlist) {
    this.paramlist = paramlist;
  }

  @Override
  public String toString() {
    return "FunctionQuery{" +
        "functionName='" + functionName + '\'' +
        ", fieldName='" + fieldName + '\'' +
        ", paramlist=" + paramlist +
        '}';
  }
}
