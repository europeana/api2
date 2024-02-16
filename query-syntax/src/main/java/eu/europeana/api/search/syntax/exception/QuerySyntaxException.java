/**
 * 
 */
package eu.europeana.api.search.syntax.exception;

/**
 * @author Hugo
 * @since 13 Feb 2024
 */
public class QuerySyntaxException extends RuntimeException {

    public QuerySyntaxException() {
        super();
    }

    public QuerySyntaxException(String message) {
        super(message);
    }

    public QuerySyntaxException(String message, Throwable cause) {
        super(message, cause);
    }

    public QuerySyntaxException(Throwable cause) {
        super(cause);
    }
}
