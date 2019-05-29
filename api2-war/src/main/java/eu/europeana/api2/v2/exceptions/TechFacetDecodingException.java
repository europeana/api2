package eu.europeana.api2.v2.exceptions;

/**
 * Created by luthien on 07/02/2019.
 */
public class TechFacetDecodingException extends Exception{

    private static final long serialVersionUID = -5618924637715764723L;
    private final String message;

    public TechFacetDecodingException(String message){
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
