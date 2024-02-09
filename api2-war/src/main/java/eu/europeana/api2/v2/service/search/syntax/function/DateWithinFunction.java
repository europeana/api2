/**
 * 
 */
package eu.europeana.api2.v2.service.search.syntax.function;

/**
 * @author Hugo
 * @since 7 Feb 2024
 */
public class DateWithinFunction extends DateFunction {

    @Override
    public String getName() {
        return "dateWithin";
    }

    @Override
    protected Operation getOperation() { return Operation.Within; }

    @Override
    public String toString() {
        return "DateWithinFunction{} "+getName();
    }
}
