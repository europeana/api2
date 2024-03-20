package eu.europeana.api.search.syntax.utils;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import eu.europeana.api.search.syntax.converter.ConverterContext;
import eu.europeana.api.search.syntax.exception.QuerySyntaxException;
import eu.europeana.api.search.syntax.field.FieldRegistry;
import eu.europeana.api.search.syntax.function.DateContainsFunction;
import eu.europeana.api.search.syntax.function.DateFunction;
import eu.europeana.api.search.syntax.function.DateIntersectsFunction;
import eu.europeana.api.search.syntax.function.DateWithinFunction;
import eu.europeana.api.search.syntax.function.DistanceFunction;
import eu.europeana.api.search.syntax.function.FunctionRegistry;
import eu.europeana.api.search.syntax.function.IntervalFunction;
import eu.europeana.api.search.syntax.model.SyntaxExpression;
import eu.europeana.api.search.syntax.parser.SearchExpressionParser;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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
      set.stream().forEach( entry -> paramTovalueMap.put(entry.getKey() ,entry.getValue()));

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
  public static void loadFieldRegistryFromResource(Class loaderClass ,String fileToLoad)  {
    try {

      if(!FieldRegistry.INSTANCE.isLoaded) {
        log.info("Loading field Registry !");
        InputStream inputStream =  loaderClass.getClassLoader().getResourceAsStream(fileToLoad);
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
  private static XmlMapper getXmlMapper(Class type, JsonDeserializer deserializer) {
    JacksonXmlModule module = new JacksonXmlModule();
    module.addDeserializer(type, deserializer);
    XmlMapper xmlMapper = new XmlMapper(module);
    xmlMapper.registerModule(module);
    return xmlMapper;
  }

  /**
   * To load the functions used during the parsing of Search queries.
   */
  public static void loadFunctionRegistry() {
    FunctionRegistry registry = FunctionRegistry.INSTANCE;
    registry.addFunction(new DateFunction());
    registry.addFunction(new DateContainsFunction());
    registry.addFunction(new DateIntersectsFunction());
    registry.addFunction(new DateWithinFunction());
    registry.addFunction(new IntervalFunction());
    registry.addFunction(new DistanceFunction());
  }
}
