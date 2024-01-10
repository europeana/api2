package eu.europeana.api2.v2.model.translate;

import javax.validation.constraints.NotNull;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LanguageValueFieldMap extends LinkedHashMap<String, List<String>> {

  /**
  * 
  */
  private static final long serialVersionUID = -7661674399244406380L;
  @NotNull
  private final String fieldName;

  /**
   * Create a new map for a particular field
   * 
   * @param fieldName the fieldName
   */
  public LanguageValueFieldMap(@NotNull String fieldName) {
    this.fieldName = fieldName;
  }


  /**
   * Create a new map for a particular language and provided map
   * 
   * @param fieldName the fieldName
   * @param map the map to be added
   */
  public LanguageValueFieldMap(String fieldName, Map<String, List<String>> map) {
    super.putAll(map);
    this.fieldName = fieldName;
  }

  /**
   * Create a new map and populate with a field and values to translate
   * 
   * @param sourceLanguage the source language of all all added objects
   * @param fieldName String containing the field name of the texts that should be translated
   * @param valuesOfSourceLanguage List of String containing the value of the source language
   */
  public LanguageValueFieldMap(String fieldName, String sourceLanguage,
      List<String> valuesOfSourceLanguage) {
    super.put(sourceLanguage, valuesOfSourceLanguage);
    this.fieldName = fieldName;
  }

  public String getFieldName() {
    return fieldName;
  }
}
