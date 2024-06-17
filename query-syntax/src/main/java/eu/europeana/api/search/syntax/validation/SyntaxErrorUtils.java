/**
 * 
 */
package eu.europeana.api.search.syntax.validation;

import eu.europeana.api.search.syntax.exception.QuerySyntaxException;
import eu.europeana.api.search.syntax.field.FieldType;

/**
 * @author Hugo
 * @since 13 Feb 2024
 */
public class SyntaxErrorUtils {


    private static final String ERR_WRONG_QUERY_EXPRESSION =
        "function %s cannot be used as a field query expression";

    private static final String ERR_WRONG_FUNCTION_ARGUMENT =
        "Invalid %s argument for function %s, expected %s";

    private static final String ERR_ILLEGAL_FUNCTION_ARGUMENT =
        "Invalid %s argument type for function %s, expected %s got %s";

    private static final String ERR_MISSING_ARGUMENT =
        "Missing arguments for function %s, expected %s got %s";

    private static final String ERR_UNKNOWN_FIELD =
        "Unknown field %s";

    private static final String ERR_UNKNOWN_FIELD_MODE =
        "Missing field name to be used for mode %s and input field %s";

    private static final String ERR_WRONG_FIELD =
        "Unexpected field type for %s, expected %s got %s";


    public static void newWrongQueryExpression(String funcName) {
        newException(String.format(ERR_WRONG_QUERY_EXPRESSION, funcName));
    }

    public static void newUnknownFunction(String funcname) {
      String ERR_UNKNOWN_FUNCTION = "Unknown function %s";
      newException(String.format(ERR_UNKNOWN_FUNCTION, funcname));
    }

    public static void newWrongFunctionArg(String funcName
                                         , String argNr, String expectation) {
        newException(String.format(ERR_WRONG_FUNCTION_ARGUMENT, argNr
                                 , funcName, expectation));
    }

    public static void newIllegalFunctionArg(String funcName
                                           , String argNr, String expected
                                           , String got) {
        newException(String.format(ERR_ILLEGAL_FUNCTION_ARGUMENT,
                                   argNr, funcName, expected, got));
    }

    public static void newMissingFunctionArg(String funcName
                                           , int expected, int got) {
        newException(String.format(ERR_MISSING_ARGUMENT, funcName
                                 , expected, got));
    }

    public static void newUnknownField(String fieldname) {
        newException(String.format(ERR_UNKNOWN_FIELD, fieldname));
    }

    public static void newWrongFieldType(String fieldname
                                       , FieldType expected, FieldType got) {
        newException(String.format(ERR_WRONG_FIELD, fieldname
                                 , expected.name(), got.name()));
    }

    public static void newException(String message) {
        throw new QuerySyntaxException(message);
    }

    public static void newUnknownFieldNameForMode(String mode,String fieldName) {
        newException(String.format(ERR_UNKNOWN_FIELD_MODE, mode,fieldName));
    }
}
