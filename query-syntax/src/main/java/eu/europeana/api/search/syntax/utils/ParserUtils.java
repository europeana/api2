package eu.europeana.api.search.syntax.utils;

import static eu.europeana.api.search.syntax.validation.SyntaxValidation.checkFieldType;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import eu.europeana.api.search.syntax.converter.ConverterContext;
import eu.europeana.api.search.syntax.exception.QuerySyntaxException;
import eu.europeana.api.search.syntax.field.FieldDeclaration;
import eu.europeana.api.search.syntax.field.FieldMode;
import eu.europeana.api.search.syntax.field.FieldRegistry;
import eu.europeana.api.search.syntax.field.FieldType;
import eu.europeana.api.search.syntax.function.FunctionRegistry;
import eu.europeana.api.search.syntax.model.SyntaxExpression;
import eu.europeana.api.search.syntax.model.ValueExpression;
import eu.europeana.api.search.syntax.parser.SearchExpressionParser;
import eu.europeana.api.search.syntax.validation.SyntaxErrorUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ParserUtils {
  static Logger log = LogManager.getLogger(ParserUtils.class);
  private ParserUtils(){
  }

  public static Map<String,String> getParsedParametersMap(String queryString){
    Map<String,String> paramTovalueMap = new HashMap<>();
    if(queryString!=null){
      Set<Entry<String, String>>  set =  parseQueryFilter( queryString);
      set.forEach( entry -> paramTovalueMap.put(entry.getKey() ,entry.getValue()));

    }
    return paramTovalueMap;
  }

  public static Set<Entry<String, String>> parseQueryFilter(String queryString) throws QuerySyntaxException {
    SyntaxExpression expr = getParsedModel(queryString);
    ConverterContext context = new ConverterContext();
    String solrFormat = expr.toSolr(context);
    context.setParameter(Constants.FQ_PARAM, solrFormat);
    return context.getParameters();
  }
  private static SyntaxExpression getParsedModel(String queryString) throws QuerySyntaxException {
    try {
      SearchExpressionParser parser = new SearchExpressionParser(
          new java.io.StringReader(queryString));
      return parser.parse();
    }
    catch(Exception ex){
      throw new QuerySyntaxException("Exception : Unable to Parse "+queryString +" "+ex.getMessage());
    }
    catch(Error ex){
      throw new QuerySyntaxException("Error : Unable to Parse "+queryString +" "+ex.getMessage());
    }
  }

  /**
   * To load the mapping of valid field names to accept in search query and then to query solr.
   */
  public static void loadFieldRegistryFromResource(String fileToLoad)  {
    try {

      if(!FieldRegistry.INSTANCE.isLoaded) {
        log.info("Loading field Registry !");
        InputStream inputStream =  ParserUtils.class.getClassLoader().getResourceAsStream(fileToLoad);
        XmlMapper xmlMapper = getXmlMapper(FieldRegistry.class, new FieldInfoDeserializer());
        xmlMapper.readValue(inputStream, FieldRegistry.class);
        FieldRegistry.INSTANCE.isLoaded =true;
      }
    }
    catch (IOException ex){
      log.error(String.format("query-parser -> Error while loading fieldRegistry. %s", ex.getMessage()));
    }
  }

  /**
   * Load the mapper with appropriate deserializer.
   * @param type Class for deserialization
   * @param deserializer Custom deserializer
   * @return XmlMapper Object
   */
  private static XmlMapper getXmlMapper(Class<?> type, JsonDeserializer deserializer) {
    JacksonXmlModule module = new JacksonXmlModule();
    module.addDeserializer(type, deserializer);
    XmlMapper xmlMapper = new XmlMapper(module);
    xmlMapper.registerModule(module);
    return xmlMapper;
  }

  /**
   * To load the functions used during the parsing of Search queries.
   */
  public static void loadFunctionRegistry(String fileToLoad){
    try {
      if (!FunctionRegistry.INSTANCE.isLoaded) {
        log.info("Loading function Registry !");
        InputStream inputStream = ParserUtils.class.getClassLoader()
            .getResourceAsStream(fileToLoad);
        XmlMapper xmlMapper = getXmlMapper(FunctionRegistry.class, new FunctionInfoDeserializer());
        xmlMapper.readValue(inputStream, FunctionRegistry.class);
        FunctionRegistry.INSTANCE.isLoaded = true;
      }
    }
    catch (IOException ex){
      log.error(String.format("query-parser -> Error while loading functionRegistry. %s", ex.getMessage()));
    }
  }



  public static String getValidFieldFromRegistry(ValueExpression valExpr, ConverterContext context,
      FieldType type , FieldMode mode) {

    String value = valExpr.getValue();
    FieldDeclaration field = context.getField(value);
    checkFieldType(field, type);
    return getFiledNameForSpecificMode(mode, field);
  }

  /**
   * FieldNames (refer field registry xml from module resources ) coming in query parameters  can have different
   * field name associated to it which are used for solr search ,
   * below method gets the appropriate filedName based on the mode
   * @param mode FieldMode  e.g. search ,sort
   * @param field FieldDeclaration
   * @return  associated field name based on mode
   */
  public static String getFiledNameForSpecificMode(FieldMode mode, FieldDeclaration field) {
    String filedNameBasedOnMode  = field.getField(mode);
    if(!StringUtils.isNotBlank(filedNameBasedOnMode))
      SyntaxErrorUtils.newUnknownFieldNameForMode(mode.toString(), field.getName());
    return field.getField(mode);
  }

  /**
   * Method accepts the search field and field mode for searching.
   * It looks into registry for finding the associated field name for that mode and returns it.
   * @param term
   * @param mode
   * @return
   */
  public String getFieldNameBasedOnModeForSearchField(String term,FieldMode mode){
    Map<String, FieldDeclaration> registry = FieldRegistry.INSTANCE.registry;
    for(Entry<String, FieldDeclaration> e : registry.entrySet()){
      FieldDeclaration decl = e.getValue();
      if(term.equals(decl.getName()))
         return decl.getField(mode);
    }
    return null;
  }




}
