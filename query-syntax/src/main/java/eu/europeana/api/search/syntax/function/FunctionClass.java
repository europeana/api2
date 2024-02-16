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

    public String getName();

    public int getArgumentNr();

    public void isValid(FunctionExpression expr);

    public String toSolr(FunctionExpression expr, ConverterContext context);
}
