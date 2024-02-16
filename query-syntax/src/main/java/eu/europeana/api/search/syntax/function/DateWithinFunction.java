/**
 * 
 */
package eu.europeana.api.search.syntax.function;

/**
 * @author Hugo
 * @since 7 Feb 2024
 */
public class DateWithinFunction extends DateFunction {

    public static final String NAME = "dateWithin";

    @Override
    public String getName() { return NAME; }

    @Override
    public String toString() { return "DateWithinFunction{} " + getName(); }

    @Override
    protected Operation getOperation() { return Operation.Within; }
}
