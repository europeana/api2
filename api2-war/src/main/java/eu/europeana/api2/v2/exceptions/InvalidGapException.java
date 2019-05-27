package eu.europeana.api2.v2.exceptions;

/**
 * Created by luthien on 07/02/2019.
 */
public class InvalidGapException extends Exception{

    private static final long serialVersionUID = -8115132548321277597L;
    private final String message;

    public InvalidGapException(String message){
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
