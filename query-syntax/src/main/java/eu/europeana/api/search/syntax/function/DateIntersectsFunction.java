/**
 * 
 */
package eu.europeana.api.search.syntax.function;

/**
 * @author Hugo
 * @since 7 Feb 2024
 */
public class DateIntersectsFunction extends DateFunction {

    public static final String NAME = "dateIntersects";

    @Override
    public String getName() { return NAME; }

    @Override
    public String toString() { return "DateIntersectsFunction{} " + getName() ; }

    @Override
    protected Operation getOperation() { return Operation.INTERSECTS; }
}
