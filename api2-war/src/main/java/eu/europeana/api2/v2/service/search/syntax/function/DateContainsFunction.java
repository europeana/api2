/**
 * 
 */
package eu.europeana.api2.v2.service.search.syntax.function;

/**
 * @author Hugo
 * @since 7 Feb 2024
 */
public class DateContainsFunction extends DateFunction {

    @Override
    public String getName() {
        return "dateContains";
    }

    @Override
    protected Operation getOperation() { return Operation.Contains; }

    @Override
    public String toString() {
        return "DateContainsFunction{} " +getName();
    }
}
