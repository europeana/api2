package eu.europeana.api2.v2.service.parser.datamodel;

public class FunctionParam {

  public String getParamValue() {
    return paramValue;
  }

  public void setParamValue(String paramValue) {
    this.paramValue = paramValue;
  }

  public FunctionQuery getFunction() {
    return function;
  }

  public void setFunction(FunctionQuery function) {
    this.function = function;
  }

  private String paramValue;
  private FunctionQuery function;

  @Override
  public String toString() {
    return "FunctionParam{" +
        "paramValue='" + paramValue + '\'' +
        ", function=" + function +
        '}';
  }
}
