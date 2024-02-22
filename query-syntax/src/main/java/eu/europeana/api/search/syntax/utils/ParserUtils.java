package eu.europeana.api.search.syntax.utils;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import eu.europeana.api.search.syntax.converter.ConverterContext;
import eu.europeana.api.search.syntax.field.FieldRegistry;
import eu.europeana.api.search.syntax.function.DateContainsFunction;
import eu.europeana.api.search.syntax.function.DateFunction;
import eu.europeana.api.search.syntax.function.DateIntersectsFunction;
import eu.europeana.api.search.syntax.function.DateWithinFunction;
import eu.europeana.api.search.syntax.function.FunctionRegistry;
import eu.europeana.api.search.syntax.function.IntervalFunction;
import eu.europeana.api.search.syntax.model.SyntaxExpression;
import eu.europeana.api.search.syntax.parser.ParseException;
import eu.europeana.api.search.syntax.parser.SearchExpressionParser;
import java.io.IOException;
import java.io.InputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ParserUtils {

  static Logger LOG = LogManager.getLogger(ParserUtils.class);
  private ParserUtils(){
  }
  public static String parseQueryFilter(String queryString) throws ParseException {
    SyntaxExpression expr = getParsedModel(queryString);
    String solrFormat = expr.toSolr(new ConverterContext());
    LOG.info(queryString + " => " +solrFormat);
    return solrFormat;
  }

  private static SyntaxExpression getParsedModel(String queryString) throws ParseException {
    SearchExpressionParser parser = new SearchExpressionParser(new java.io.StringReader(queryString));
    return parser.parse();
  }


  /**
   * To load the mapping of valid field names to accept in search query and then to query solr.
   */
  public static void loadFieldRegistry()  {
    try {
      InputStream inputStream = getInputStreamFromFile(Constants.FIELD_REGISTRY_XML);
      XmlMapper xmlMapper = getXmlMapper(FieldRegistry.class,new FieldInfoDeserializer());
      FieldRegistry fieldRegistry = xmlMapper.readValue(inputStream, FieldRegistry.class);
    }
    catch (IOException ex){
      LOG.error("query-parser -> Error while loading fieldRegistry. "+ ex.getMessage());
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

  private static InputStream getInputStreamFromFile(String fileName) {
    ClassLoader loader = ParserUtils.class.getClassLoader();
    InputStream resource = loader.getResourceAsStream(fileName);
    return resource;
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
  }

}
