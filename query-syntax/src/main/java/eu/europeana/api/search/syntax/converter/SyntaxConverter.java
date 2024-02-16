/**
 * 
 */
package eu.europeana.api.search.syntax.converter;

/**
 * @author Hugo
 * @since 7 Feb 2024
 */
public interface SyntaxConverter
{
    public String toSolr(ConverterContext context);
}
