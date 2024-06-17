/**
 * 
 */
package eu.europeana.api.search.syntax.validation;

import static eu.europeana.api.search.syntax.validation.SyntaxErrorUtils.newIllegalFunctionArg;
import static eu.europeana.api.search.syntax.validation.SyntaxErrorUtils.newWrongFieldType;

import eu.europeana.api.search.syntax.field.FieldDeclaration;
import eu.europeana.api.search.syntax.field.FieldType;
import eu.europeana.api.search.syntax.model.ArgumentExpression;
import eu.europeana.api.search.syntax.model.FunctionExpression;

/**
 * @author Hugo
 * @since 13 Feb 2024
 */
public class SyntaxValidation {

    public static void checkFieldType(FieldDeclaration field
                                    , FieldType expected) {
        if ( expected != field.getType() ) {
            newWrongFieldType(field.getName(), expected, field.getType());
        }
    }

    public static void checkArgumentNotFunction(String argNr
            , FunctionExpression funcExpr, ArgumentExpression arg) {
        if ( arg instanceof FunctionExpression argExpr) {
            //FunctionExpression object will always have function associated to it
            newIllegalFunctionArg(funcExpr.getFunction().getName(), argNr, "value"
                                , argExpr.getFunction().getName());
        }
    }

}
