/**
 * 
 */
package eu.europeana.api.search.syntax.function;

/**
 * @author Hugo
 * @since 7 Feb 2024
 */
public class DateContainsFunction extends DateFunction {

    public static final String NAME = "dateContains";

    @Override
    public String getName() { return NAME; }

    @Override
    public String toString() { return "DateContainsFunction{} " + getName(); }

    @Override
    protected Operation getOperation() { return Operation.Contains; }
}