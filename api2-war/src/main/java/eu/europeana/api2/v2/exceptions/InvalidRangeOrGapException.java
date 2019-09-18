package eu.europeana.api2.v2.exceptions;

/**
 * Exception thrown when there is a problem with facet range fields
 * Created by luthien on 07/02/2019.
 */
public class InvalidRangeOrGapException extends Exception{

    private static final long serialVersionUID = -8115132548321277597L;

    private final String message;

    public InvalidRangeOrGapException(String message){
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
