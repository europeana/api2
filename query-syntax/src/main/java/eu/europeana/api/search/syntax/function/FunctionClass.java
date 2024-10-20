/**
 * 
 */
package eu.europeana.api.search.syntax.function;

import eu.europeana.api.search.syntax.converter.ConverterContext;
import eu.europeana.api.search.syntax.model.FunctionExpression;

/**
 * @author Hugo
 * @since 7 Feb 2024
 */
public interface FunctionClass {

     String getName();

     int getArgumentNr();

     boolean isValid(FunctionExpression expr);

     String toSolr(FunctionExpression expr, ConverterContext context);
}
