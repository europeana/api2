/**
 * 
 */
package eu.europeana.api2.v2.service.search.syntax.function;

import eu.europeana.api2.v2.service.search.syntax.converter.ConverterContext;
import eu.europeana.api2.v2.service.search.syntax.model.FunctionExpression;

/**
 * @author Hugo
 * @since 7 Feb 2024
 */
public interface FunctionClass {

    public String getName();

    public void isValid(FunctionExpression expr);

    public String toSolr(FunctionExpression expr, ConverterContext context);


}
