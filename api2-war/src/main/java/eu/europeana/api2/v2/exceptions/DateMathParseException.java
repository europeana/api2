package eu.europeana.api2.v2.exceptions;


/**
 * Exception that is thrown when there's a problem calculating facet.range.gap value
 */
public class DateMathParseException extends Exception{

    private static final long serialVersionUID = 3900055035686291976L;

    private final String parsing;
    private final String whatsParsed;

    public DateMathParseException(Throwable causedBy, String parsing, String whatsParsed) {
        super(causedBy);
        this.parsing        = parsing;
        this.whatsParsed    = whatsParsed;
    }

    public String getParsing() {
        return parsing;
    }
    public String getWhatsParsed() {
        return whatsParsed;
    }
}
