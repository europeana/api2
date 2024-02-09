/**
 * 
 */
package eu.europeana.api2.v2.service.search.syntax.function;

/**
 * @author Hugo
 * @since 7 Feb 2024
 */
public class DateIntersectsFunction extends DateFunction {

    @Override
    public String getName() {
        return "dateIntersects";
    }

    @Override
    protected Operation getOperation() { return Operation.Intersects; }

    @Override
    public String toString() {
        return "DateIntersectsFunction{} " + getName() ;
    }
}
