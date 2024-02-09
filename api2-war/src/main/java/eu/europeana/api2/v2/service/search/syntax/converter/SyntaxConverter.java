/**
 * 
 */
package eu.europeana.api2.v2.service.search.syntax.converter;

/**
 * @author Hugo
 * @since 7 Feb 2024
 */
public interface SyntaxConverter
{
    public String toSolr(ConverterContext context);
}
