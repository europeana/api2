/**
 * 
 */
package eu.europeana.api.search.syntax.function;

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
